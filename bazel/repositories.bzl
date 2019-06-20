load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
load("@bazel_tools//tools/build_defs/repo:java.bzl", "java_import_external")
load(
    "@nl_tulipsolutions_tecl//third_party/protoc-gen-validate:version.bzl",
    com_envoyproxy_protoc_gen_validate_version = "version",
)

def repositories(
        omit_bazel_skylib = False,
        omit_com_google_protobuf = False,
        omit_com_github_grpc_grpc = False,
        omit_cython = False,
        omit_io_grpc_grpc_java = False,
        omit_com_github_grpc_grpc_web = False,
        omit_io_bazel_rules_closure = False,
        omit_six_archive = False,
        omit_io_bazel_rules_python = False,
        omit_six = False,
        omit_python_headers = False,
        omit_org_llvm_clang = False,
        omit_bazel_gazelle = False,
        omit_build_stack_rules_proto = False,
        omit_io_bazel_rules_go_version = False,
        omit_com_github_bazelbuild_buildtools = False,
        omit_build_bazel_rules_nodejs = False,
        omit_net_zlib = False,
        omit_com_google_errorprone_error_prone_annotations = False,
        omit_com_envoyproxy_protoc_gen_validate = False):
    if not omit_bazel_skylib:
        bazel_skylib_version = "0.6.0"
        http_archive(
            name = "bazel_skylib",
            strip_prefix = "bazel-skylib-%s" % bazel_skylib_version,
            sha256 = "eb5c57e4c12e68c0c20bc774bfbc60a568e800d025557bc4ea022c6479acc867",
            urls = ["https://github.com/bazelbuild/bazel-skylib/archive/%s.tar.gz" % bazel_skylib_version],
        )

    if not omit_com_google_protobuf:
        # Latest on branch 3.7.x on 2019-03-13 because we need 4b9a5df4e8ba2066794da56598ad2905dc42051e
        # See also https://github.com/protocolbuffers/protobuf/issues/5795
        protobuf_version = "207d01daa170306370308eb27a5da38d5822a21a"

        http_archive(
            name = "com_google_protobuf",
            sha256 = "8fc25ea0061df747c190468269e25a50931cc2ac92983ab83f32135b5770c252",
            strip_prefix = "protobuf-%s" % protobuf_version,
            urls = ["https://github.com/google/protobuf/archive/%s.zip" % protobuf_version],
        )

    if not omit_com_github_grpc_grpc:
        grpc_version = "1.20.0"

        http_archive(
            name = "com_github_grpc_grpc",
            sha256 = "5c00f09f7b0517a9ccbd6f0de356b1be915bc7baad2d2189adf8ce803e00af12",
            strip_prefix = "grpc-%s" % grpc_version,
            urls = ["https://github.com/grpc/grpc/archive/v%s.zip" % grpc_version],
        )

    if not omit_cython:
        # Mirrors https://github.com/grpc/grpc/blob/9aee1731c92c8e7c880703fc948557c70bb4fc47/WORKSPACE#L20
        cython_version = "c2b80d87658a8525ce091cbe146cb7eaa29fed5c"

        http_archive(
            name = "cython",
            build_file = "@com_github_grpc_grpc//third_party:cython.BUILD",
            sha256 = "d68138a2381afbdd0876c3cb2a22389043fa01c4badede1228ee073032b07a27",
            strip_prefix = "cython-%s" % cython_version,
            urls = ["https://github.com/cython/cython/archive/%s.tar.gz" % cython_version],
        )

    if not omit_io_grpc_grpc_java:
        io_grpc_grpc_java_version = "v1.20.0"

        http_archive(
            name = "io_grpc_grpc_java",
            patches = [
                "@nl_tulipsolutions_tecl//third_party/patches/io_grpc_grpc_java:0001-Set-core-internal-visibility-to-public.patch",
                "@nl_tulipsolutions_tecl//third_party/patches/io_grpc_grpc_java:0002-Set-gRPC-method-name-in-OpenCensus-statistics.patch",
            ],
            sha256 = "9d23d9fec84e24bd3962f5ef9d1fd61ce939d3f649a22bcab0f19e8167fae8ef",
            strip_prefix = "grpc-java-%s" % io_grpc_grpc_java_version[1:],
            urls = ["https://github.com/grpc/grpc-java/archive/%s.zip" % io_grpc_grpc_java_version],
        )

    if not omit_com_github_grpc_grpc_web:
        grpc_web_version = "1.0.3"

        http_archive(
            name = "com_github_grpc_grpc_web",
            sha256 = "0ede99da93e56277bb4e90a52dd34e4fada3ddba39c9f2022981ff2f8f97a5a8",
            strip_prefix = "grpc-web-%s" % grpc_web_version,
            urls = ["https://github.com/grpc/grpc-web/archive/%s.zip" % grpc_web_version],
        )

    if not omit_io_bazel_rules_closure:
        io_bazel_rules_closure_version = "b6cf8c57f0302a3aaafaf7c14faaadf0d45ddb3c"
        http_archive(
            name = "io_bazel_rules_closure",
            sha256 = "0c1db33c543243a24dc791b9b29ff3c450fbc671f99fd27c6d4fb2e461da73ad",
            strip_prefix = "rules_closure-%s" % io_bazel_rules_closure_version,
            urls = ["https://github.com/bazelbuild/rules_closure/archive/%s.zip" % io_bazel_rules_closure_version],
        )

    # API documentation using Sphinx uses Python/PIP
    if not omit_io_bazel_rules_python:
        rules_python_version = "e6399b601e2f72f74e5aa635993d69166784dde1"

        http_archive(
            name = "io_bazel_rules_python",
            sha256 = "3f702d40976264b1723605aacb776ac41fb801854d8b26ee2312e60936b69dd6",
            strip_prefix = "rules_python-%s" % rules_python_version,
            url = "https://github.com/bazelbuild/rules_python/archive/%s.zip" % rules_python_version,
        )

    # Six is a dependency for the Python Protobuf rules
    if not omit_six_archive:
        six_archive_build_file_content = """
genrule(
  name = "copy_six",
  srcs = ["six-1.11.0/six.py"],
  outs = ["six.py"],
  cmd = "cp $< $(@)",
)

py_library(
  name = "six",
  srcs = ["six.py"],
  srcs_version = "PY2AND3",
  visibility = ["//visibility:public"],
)
        """
        http_archive(
            name = "six_archive",
            build_file_content = six_archive_build_file_content,
            sha256 = "70e8a77beed4562e7f14fe23a786b54f6296e34344c23bc42f07b15018ff98e9",
            urls = ["https://pypi.python.org/packages/source/s/six/six-1.11.0.tar.gz"],
        )

    if not omit_six:
        native.bind(
            name = "six",
            actual = "@six_archive//:six",
        )

    if not omit_python_headers:
        native.bind(
            name = "python_headers",
            actual = "@com_google_protobuf//util/python:python_headers",
        )

    if not omit_org_llvm_clang:
        org_llvm_clang_version = "7.0.0"

        http_archive(
            name = "org_llvm_clang",
            sha256 = "69b85c833cd28ea04ce34002464f10a6ad9656dd2bba0f7133536a9927c660d2",
            strip_prefix = "clang+llvm-%s-x86_64-linux-gnu-ubuntu-16.04" % org_llvm_clang_version,
            url = "http://releases.llvm.org/{0}/clang+llvm-{0}-x86_64-linux-gnu-ubuntu-16.04.tar.xz".format(org_llvm_clang_version),
            build_file_content = """
sh_binary(
    name = "clang_format",
    srcs = ["bin/clang-format"],
    visibility = ['//visibility:public'],
)
""",
        )

    if not omit_bazel_gazelle:
        bazel_gazelle_version = "0.17.0"

        http_archive(
            name = "bazel_gazelle",
            sha256 = "960374b7977958b8626102d3512c7f4b1660d61b9668fea3d404ae9ca3d69083",
            strip_prefix = "bazel-gazelle-%s" % bazel_gazelle_version,
            urls = ["https://github.com/bazelbuild/bazel-gazelle/archive/%s.tar.gz" % (bazel_gazelle_version)],
        )

    if not omit_build_stack_rules_proto:
        build_stack_rules_proto_version = "76e30bc0ad6c2f4150f40e593db83eedeb069f1e"

        http_archive(
            name = "build_stack_rules_proto",
            sha256 = "9877e832f73746024063f993dae33b403228e11c438ae628430ccb532a6a1aac",
            strip_prefix = "rules_proto-%s" % build_stack_rules_proto_version,
            url = "https://github.com/stackb/rules_proto/archive/%s.zip" % build_stack_rules_proto_version,
        )

    if not omit_io_bazel_rules_go_version:
        io_bazel_rules_go_version = "0.18.6"

        http_archive(
            name = "io_bazel_rules_go",
            strip_prefix = "rules_go-%s" % io_bazel_rules_go_version,
            sha256 = "a8061d4fcff2018dbd18b355da7d1817ade6c78a7660fbbea6cac400a35faeca",
            url = "https://github.com/bazelbuild/rules_go/archive/%s.zip" % io_bazel_rules_go_version,
        )

    if not omit_com_github_bazelbuild_buildtools:
        build_tools_version = "0.25.0"

        http_archive(
            name = "com_github_bazelbuild_buildtools",
            sha256 = "5474cdb16fe9e1db22006b5f48d534a91b68236d588223439135c43215e93fba",
            strip_prefix = "buildtools-%s" % build_tools_version,
            url = "https://github.com/bazelbuild/buildtools/archive/%s.zip" % build_tools_version,
        )

    if not omit_build_bazel_rules_nodejs:
        build_bazel_rules_nodejs_version = "0.32.1"

        # Not a github source archive as it is not recommended (https://github.com/bazelbuild/rules_nodejs/releases/tag/0.18.4)
        http_archive(
            name = "build_bazel_rules_nodejs",
            urls = ["https://github.com/bazelbuild/rules_nodejs/releases/download/{0}/rules_nodejs-{0}.tar.gz".format(build_bazel_rules_nodejs_version)],
            sha256 = "8a913d257f3a14c0663107607205e3bbf8d5ddd306a0e54ce28c79cb76f9122e",
        )

    if not omit_com_google_errorprone_error_prone_annotations:
        java_import_external(
            name = "com_google_errorprone_error_prone_annotations",
            jar_urls = ["http://central.maven.org/maven2/com/google/errorprone/error_prone_annotations/2.3.3/error_prone_annotations-2.3.3.jar"],
            jar_sha256 = "ec59f1b702d9afc09e8c3929f5c42777dec623a6ea2731ac694332c7d7680f5a",
            srcjar_urls = ["http://central.maven.org/maven2/com/google/errorprone/error_prone_annotations/2.3.3/error_prone_annotations-2.3.3-sources.jar"],
            srcjar_sha256 = "f58446b80b5f1e98bcb74dae5c0710ed8e52baafe5a4bb315f769f306d85634a",
            licenses = ["notice"],  # Apache 2.0
        )

        native.bind(
            # This exact name is required by com_google_protobuf
            name = "error_prone_annotations",
            actual = "@com_google_errorprone_error_prone_annotations",
        )

    if not omit_net_zlib:
        # Mirrors https://github.com/protocolbuffers/protobuf/blob/207d01daa170306370308eb27a5da38d5822a21a/WORKSPACE#L25
        http_archive(
            name = "net_zlib",
            build_file = "@com_google_protobuf//:third_party/zlib.BUILD",
            sha256 = "c3e5e9fdd5004dcb542feda5ee4f0ff0744628baf8ed2dd5d66f8ca1197cb1a1",
            strip_prefix = "zlib-1.2.11",
            urls = ["https://zlib.net/zlib-1.2.11.tar.gz"],
        )

        native.bind(
            name = "zlib",
            actual = "@net_zlib//:zlib",
        )

    if not omit_com_envoyproxy_protoc_gen_validate:
        http_archive(
            name = "com_envoyproxy_protoc_gen_validate",
            sha256 = "149f0e8a1ffce4eaf1f87839f92e82550622967e9ee2fe5e73e534365c59c387",
            strip_prefix = "protoc-gen-validate-%s" % com_envoyproxy_protoc_gen_validate_version,
            url = "https://github.com/envoyproxy/protoc-gen-validate/archive/%s.zip" % com_envoyproxy_protoc_gen_validate_version,
            patches = [
                "@nl_tulipsolutions_tecl//third_party/patches/com_envoyproxy_protoc_gen_validate:0001-Add-description-fields-to-ValidationException.patch",
            ],
        )
