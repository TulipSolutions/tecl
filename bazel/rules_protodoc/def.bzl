# Borrowed from Rules Go, licensed under Apache 2.
# https://github.com/bazelbuild/rules_go/blob/67f44035d84a352cffb9465159e199066ecb814c/proto/compiler.bzl#L72
def _proto_path(proto):
    path = proto.path
    root = proto.root.path
    ws = proto.owner.workspace_root
    if path.startswith(root):
        path = path[len(root):]
    if path.startswith("/"):
        path = path[1:]
    if path.startswith(ws):
        path = path[len(ws):]
    if path.startswith("/"):
        path = path[1:]
    return path

def _protoc_output_path(proto_src):
    return _proto_path(proto_src).replace("-", "_").replace(".proto", "")

def _rst_proto_gen_impl(ctx):
    srcs = [f for dep in ctx.attr.deps for f in dep.proto.direct_sources]
    includes = [f for dep in ctx.attr.deps for f in dep.proto.transitive_imports.to_list()]

    proto_include_args = ["--proto_path={0}={1}".format(_proto_path(include), include.path) for include in includes]
    options = ",".join([])  # Empty, for now.
    plugin_arg = "--plugin=protoc-gen-protodoc=" + ctx.executable._plugin.path

    outs = []
    for src in srcs:
        output_rst_path = _protoc_output_path(src)

        rst_out_file = ctx.actions.declare_directory("/".join([ctx.attr.name, output_rst_path]) + "/")
        protoc_rst_output_dir = "".join(rst_out_file.path.rsplit(output_rst_path, 1)[:-1])
        protoc_rst_out_arg = "--protodoc_out={options}:{path}".format(options = options, path = protoc_rst_output_dir)
        outs.append(rst_out_file)

        ctx.actions.run(
            inputs = includes,
            tools = [ctx.executable._plugin],
            outputs = [rst_out_file],
            executable = ctx.executable._protoc,
            arguments = proto_include_args + [plugin_arg, protoc_rst_out_arg, src.path],
            progress_message = "Generating %s" % output_rst_path,
        )

    return [DefaultInfo(files = depset(outs))]

_rst_proto_gen = rule(
    attrs = {
        "deps": attr.label_list(
            mandatory = True,
        ),
        "_protoc": attr.label(
            default = Label("@com_google_protobuf//:protoc"),
            executable = True,
            allow_single_file = True,
            cfg = "host",
        ),
        "_plugin": attr.label(
            default = Label("//bazel/rules_protodoc:protodoc"),
            executable = True,
            cfg = "host",
        ),
    },
    output_to_genfiles = True,
    implementation = _rst_proto_gen_impl,
)

def rst_proto(
        name,
        deps = [],
        visibility = None,
        **kwargs):
    if not name:
        fail("name is required", "name")
    if len(deps) != 1:
        fail("'deps' attribute must contain exactly one label", "deps")

    _rst_proto_gen(
        name = name,
        deps = deps,
        visibility = visibility,
        **kwargs
    )
