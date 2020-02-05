load(
    "@com_github_grpc_grpc//bazel:protobuf.bzl",
    "get_include_directory",
    "get_plugin_args",
    "get_proto_arguments",
    "includes_from_deps",
    "is_in_virtual_imports",
    "protos_from_context",
    _grpc_get_out_dir = "get_out_dir",
)

def declare_rst_out_dirs(protos, context):
    declared = []
    for proto in protos:
        if is_in_virtual_imports(proto):
            fail("unsupported")

        proto_file_name = proto.basename[:proto.basename.find(".proto")]
        rule_name = context.attr.name
        declared.append(context.actions.declare_directory("/".join([proto_file_name, rule_name])))

    return declared

def proto_pkg_dir(source_file):
    directory = source_file.dirname
    prefix_len = 0

    if not source_file.is_source and directory.startswith(source_file.root.path):
        prefix_len = len(source_file.root.path) + 1

    if directory.startswith("external", prefix_len):
        external_separator = directory.find("/", prefix_len)
        repository_separator = directory.find("/", external_separator + 1)
        return directory[repository_separator + 1:]
    else:
        return source_file.root.path + directory

def _generate_rst_impl(context):
    protos = protos_from_context(context)
    includes = includes_from_deps(context.attr.deps)
    out_files = declare_rst_out_dirs(protos, context)
    tools = [context.executable._protoc]

    rule_name = context.attr.name
    proto_file_name = protos[0].basename[:protos[0].basename.find(".proto")]
    out_dir_path = "/".join([
        _grpc_get_out_dir(protos, context).path,
        proto_pkg_dir(protos[0]),
        proto_file_name,
        rule_name,
    ])

    arguments = ([
        "--proto_path={}".format(get_include_directory(i))
        for i in includes
    ] + [
        "--proto_path={}".format(context.genfiles_dir.path),
    ])
    if context.attr.plugin:
        plugin_args = get_plugin_args(
            context.executable.plugin,
            [],
            out_dir_path,
            False,
            context.attr.plugin.label.name,
        )
        arguments += plugin_args
        tools.append(context.executable.plugin)

    arguments += get_proto_arguments(protos, context.genfiles_dir.path)

    context.actions.run(
        inputs = protos + includes,
        tools = tools,
        outputs = out_files,
        executable = context.executable._protoc,
        arguments = arguments,
        mnemonic = "ProtocInvocation",
    )

    return [DefaultInfo(files = depset(direct = out_files))]

_generate_rst = rule(
    attrs = {
        "deps": attr.label_list(
            mandatory = True,
            allow_empty = False,
            providers = [ProtoInfo],
        ),
        "plugin": attr.label(
            default = Label("//bazel/rules_protodoc:protodoc"),
            mandatory = False,
            executable = True,
            providers = ["files_to_run"],
            cfg = "host",
        ),
        "_protoc": attr.label(
            default = Label("@com_google_protobuf//:protoc"),
            providers = ["files_to_run"],
            executable = True,
            cfg = "host",
        ),
    },
    implementation = _generate_rst_impl,
)

def rst_proto(
        name,
        deps,
        plugin = None,
        **kwargs):
    if len(deps) != 1:
        fail("Can only compile a single proto at a time.")

    _generate_rst(
        name = name,
        deps = deps,
        plugin = plugin,
        **kwargs
    )
