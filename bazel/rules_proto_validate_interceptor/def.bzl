load("@bazel_tools//tools/jdk:toolchain_utils.bzl", "find_java_runtime_toolchain", "find_java_toolchain")
load("//bazel/proto:path.bzl", "proto_path", "src_path")

def _java_validate_interceptor_library_impl(ctx):
    if len(ctx.attr.srcs) > 1:
        fail("Only one src value supported", "srcs")
    proto = ctx.attr.srcs[0][ProtoInfo]
    srcs = proto.check_deps_sources
    descriptors = proto.transitive_descriptor_sets
    gensrcjar = ctx.actions.declare_file("%s-validate-interceptor-gensrc.jar" % ctx.label.name)

    args = ctx.actions.args()
    args.add(ctx.executable._plugin.path, format = "--plugin=protoc-gen-java-validate-interceptor=%s")
    args.add("--java-validate-interceptor_out=:{0}".format(gensrcjar.path))
    args.add_joined("--descriptor_set_in", descriptors, join_with = ":")
    for src in srcs.to_list():
        args.add("-I{0}={1}".format(proto_path(src, proto), src.path))
    args.add_all(srcs, map_each = src_path)

    ctx.actions.run(
        inputs = depset(transitive = [srcs, descriptors]),
        tools = [ctx.executable._plugin],
        outputs = [gensrcjar],
        executable = ctx.executable._protoc,
        arguments = [args],
        progress_message = "Generating %s" % gensrcjar.path,
        mnemonic = "GenJavaValidateInterceptor",
    )

    deps = [java_common.make_non_strict(dep[JavaInfo]) for dep in ctx.attr.deps]
    deps += [dep[JavaInfo] for dep in ctx.attr._validate_interceptor_deps]

    java_info = java_common.compile(
        ctx,
        source_jars = [gensrcjar],
        deps = deps,
        output_source_jar = ctx.outputs.srcjar,
        output = ctx.outputs.jar,
        java_toolchain = find_java_toolchain(ctx, ctx.attr._java_toolchain),
        host_javabase = find_java_runtime_toolchain(ctx, ctx.attr._host_javabase),
    )

    return [java_info]

java_validate_interceptor_library = rule(
    attrs = {
        "srcs": attr.label_list(
            mandatory = True,
            allow_empty = False,
            providers = [ProtoInfo],
        ),
        "deps": attr.label_list(
            mandatory = True,
            allow_empty = False,
            providers = [JavaInfo],
        ),
        "_protoc": attr.label(
            default = Label("@com_google_protobuf//:protoc"),
            executable = True,
            cfg = "host",
        ),
        "_validate_interceptor_deps": attr.label_list(
            providers = [JavaInfo],
            default = [
                Label("@com_google_api_grpc_proto_google_common_protos//jar"),
                Label("@com_google_protobuf//:protobuf_java"),
                Label("@io_grpc_grpc_java//core"),
                Label("@io_grpc_grpc_java//protobuf"),
                Label("@com_envoyproxy_protoc_gen_validate//java/pgv-java-stub/src/main/java/io/envoyproxy/pgv"),
                Label("@com_envoyproxy_protoc_gen_validate//java/pgv-java-validation/src/main/java/io/envoyproxy/pgv"),
                Label("@nl_tulipsolutions_tecl//tulipsolutions/api/common:errors_jvm_proto"),
            ],
        ),
        "_plugin": attr.label(
            default = Label("@nl_tulipsolutions_tecl//bazel/rules_proto_validate_interceptor"),
            executable = True,
            cfg = "host",
        ),
        "_java_toolchain": attr.label(
            default = Label("@bazel_tools//tools/jdk:current_java_toolchain"),
        ),
        "_host_javabase": attr.label(
            cfg = "host",
            default = Label("@bazel_tools//tools/jdk:current_host_java_runtime"),
        ),
    },
    fragments = ["java"],
    outputs = {
        "jar": "lib%{name}.jar",
        "srcjar": "lib%{name}-src.jar",
    },
    provides = [JavaInfo],
    implementation = _java_validate_interceptor_library_impl,
)
