def doc_workspace(name, workspace, visibility):
    native.genrule(
        name = name,
        srcs = [workspace],
        outs = ["WORKSPACE.doc"],
        # Transforms a workspace snippet of the form
        # workspace(.*
        #
        # local_repository(
        #   name = .*,
        #   path = .*
        # )
        #
        # To the following:
        # load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
        #
        # http_archive(
        #   name = .*,
        #   sha256 = "<checksum>",
        #   strip_prefix = "tecl-<version>",
        #   url = "https://github.com/tulipsolutions/tecl/archive/<version>.tar.gz",
        #)
        cmd = "sed " +
              "-e 's/local_repository/http_archive/' " +
              "-e 's#workspace.*#load(\"@bazel_tools//tools/build_defs/repo:http.bzl\", \"http_archive\")#' " +
              "-e 's#path = .*#sha256 = \"<checksum>\",\\n    strip_prefix = \"tecl-<version>\",\\n    url = \"https://github.com/tulipsolutions/tecl/archive/<version>.tar.gz\"#' " +
              "$(location {workspace}) > \"$@\"".format(
                  workspace = workspace,
              ),
        visibility = visibility,
    )
