# Taken from bazelbuild/rules_go license: Apache 2
# https://github.com/bazelbuild/rules_go/blob/528f6faf83f85c23da367d61f784893d1b3bd72b/proto/compiler.bzl#L94
# replaced `prefix = paths.join(..` with `prefix = "/".join(..`
def proto_path(src, proto):
    """proto_path returns the string used to import the proto. This is the proto
    source path within its repository, adjusted by import_prefix and
    strip_import_prefix.

    Args:
        src: the proto source File.
        proto: the ProtoInfo provider.

    Returns:
        An import path string.
    """
    if proto.proto_source_root == ".":
        # true if proto sources were generated
        prefix = src.root.path + "/"
    elif proto.proto_source_root.startswith(src.root.path):
        # sometimes true when import paths are adjusted with import_prefix
        prefix = proto.proto_source_root + "/"
    else:
        # usually true when paths are not adjusted
        prefix = "/".join([src.root.path, proto.proto_source_root]) + "/"
    if not src.path.startswith(prefix):
        # sometimes true when importing multiple adjusted protos
        return src.path

    return src.path[len(prefix):]

def src_path(src):
    return src.path
