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

PROTOC_PY_SUFFIX = "_pb2.py"
PROTOC_PY_GRPC_SUFFIX = "_pb2_grpc.py"

def _protoc_output_path(proto_src, suffix):
    return _proto_path(proto_src).replace("-", "_").replace(".proto", suffix)

def _py_proto_library_gen_impl(ctx):
    srcs = [f for dep in ctx.attr.deps for f in dep[ProtoInfo].transitive_sources.to_list()]
    includes = [f for dep in ctx.attr.deps for f in dep[ProtoInfo].transitive_imports.to_list()]

    proto_include_args = ["--proto_path={0}={1}".format(_proto_path(include), include.path) for include in includes]
    options = ",".join([])  # Empty, for now.

    outs = []
    for src in srcs:
        output_py_pb_path = _protoc_output_path(src, PROTOC_PY_SUFFIX)
        py_pb_out_file = ctx.actions.declare_file("/".join([ctx.attr.name, output_py_pb_path]))
        protoc_py_pb_output_dir = py_pb_out_file.path.replace(output_py_pb_path, "")
        protoc_py_pb_out_arg = "--python_out={options}:{path}".format(options = options, path = protoc_py_pb_output_dir)
        outs.append(py_pb_out_file)

        ctx.actions.run(
            inputs = includes,
            outputs = [py_pb_out_file],
            executable = ctx.executable._protoc,
            arguments = proto_include_args + [protoc_py_pb_out_arg, src.path],
            progress_message = "Generating %s" % output_py_pb_path,
        )

    return [DefaultInfo(files = depset(outs))]

_py_proto_library_gen = rule(
    attrs = {
        "deps": attr.label_list(
            mandatory = True,
            providers = [ProtoInfo],
        ),
        "_protoc": attr.label(
            default = Label("@com_google_protobuf//:protoc"),
            executable = True,
            allow_single_file = True,
            cfg = "host",
        ),
    },
    output_to_genfiles = True,
    implementation = _py_proto_library_gen_impl,
)

def py_proto_library(name, deps, **kwargs):
    if not name:
        fail("name is required", "name")

    srcs_name = name + "_srcs"
    _py_proto_library_gen(
        name = srcs_name,
        deps = deps,
    )

    native.py_library(
        name = name,
        srcs = [srcs_name],
        imports = [srcs_name],
        **kwargs
    )

def _py_grpc_library_gen_impl(ctx):
    srcs = [f for dep in ctx.attr.deps for f in dep[ProtoInfo].transitive_sources.to_list()]
    includes = [f for dep in ctx.attr.deps for f in dep[ProtoInfo].transitive_imports.to_list()]

    proto_include_args = ["--proto_path={0}={1}".format(_proto_path(include), include.path) for include in includes]
    options = ",".join([])  # Empty, for now.
    plugin_arg = "--plugin=protoc-gen-python-grpc=" + ctx.executable._plugin.path

    outs = []
    for src in srcs:
        output_py_pb_path = _protoc_output_path(src, PROTOC_PY_SUFFIX)
        py_pb_out_file = ctx.actions.declare_file("/".join([ctx.attr.name, output_py_pb_path]))
        protoc_py_pb_output_dir = py_pb_out_file.path.replace(output_py_pb_path, "")
        protoc_py_pb_out_arg = "--python_out={options}:{path}".format(options = options, path = protoc_py_pb_output_dir)
        outs.append(py_pb_out_file)

        output_py_grpc_path = _protoc_output_path(src, PROTOC_PY_GRPC_SUFFIX)
        py_grpc_out_file = ctx.actions.declare_file("/".join([ctx.attr.name, output_py_grpc_path]))
        protoc_py_grpc_output_dir = py_grpc_out_file.path.replace(output_py_grpc_path, "")
        protoc_py_grpc_out_arg = "--python-grpc_out={options}:{path}".format(options = options, path = protoc_py_grpc_output_dir)
        outs.append(py_grpc_out_file)

        ctx.actions.run(
            inputs = includes,
            tools = [ctx.executable._plugin],
            outputs = [py_pb_out_file, py_grpc_out_file],
            executable = ctx.executable._protoc,
            arguments = proto_include_args + [plugin_arg, protoc_py_pb_out_arg, protoc_py_grpc_out_arg, src.path],
            progress_message = "Generating %s" % output_py_pb_path,
        )

    # Include the files in include attrs in the generated files
    # The output path is the path from the BUILD rule to the file itself
    # This is done to enable placing generated files in the same namespace as non-generated files
    # e.g., tulipsolutions.api.priv.order_pb2 and tulipsolutions.api.auth.jwt_interceptor
    for include in ctx.attr.includes:
        for file in include.files.to_list():
            path_without_ws = _proto_path(file)
            py_package_path = path_without_ws.replace(include.label.package, "").lstrip("/")

            copied_file = ctx.actions.declare_file("/".join([ctx.attr.name, py_package_path]))
            outs.append(copied_file)

            ctx.actions.run(
                executable = "cp",
                inputs = [file],
                outputs = [copied_file],
                arguments = [file.path, copied_file.path],
            )

    return [DefaultInfo(files = depset(outs))]

_py_grpc_library_gen = rule(
    attrs = {
        "deps": attr.label_list(
            mandatory = True,
            providers = [ProtoInfo],
        ),
        "includes": attr.label_list(),
        "_protoc": attr.label(
            default = Label("@com_google_protobuf//:protoc"),
            executable = True,
            allow_single_file = True,
            cfg = "host",
        ),
        "_plugin": attr.label(
            default = Label("@com_github_grpc_grpc//:grpc_python_plugin"),
            executable = True,
            allow_single_file = True,
            cfg = "host",
        ),
    },
    output_to_genfiles = True,
    implementation = _py_grpc_library_gen_impl,
)

def py_grpc_library(name, srcs, includes = [], **kwargs):
    if not name:
        fail("name is required", "name")

    srcs_name = name + "_srcs"
    _py_grpc_library_gen(
        name = srcs_name,
        deps = srcs,
        includes = includes,
    )

    native.py_library(
        name = name,
        srcs = [srcs_name],
        imports = [srcs_name],
        **kwargs
    )
