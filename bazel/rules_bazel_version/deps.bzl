load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

def bazel_version_dependencies():
    excludes = native.existing_rules().keys()

    if "bazel_skylib" not in excludes:
        bazel_skylib_version = "0.6.0"
        http_archive(
            name = "bazel_skylib",
            strip_prefix = "bazel-skylib-%s" % bazel_skylib_version,
            sha256 = "eb5c57e4c12e68c0c20bc774bfbc60a568e800d025557bc4ea022c6479acc867",
            urls = ["https://github.com/bazelbuild/bazel-skylib/archive/%s.tar.gz" % bazel_skylib_version],
        )
