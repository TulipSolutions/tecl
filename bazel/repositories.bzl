load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
load("@bazel_tools//tools/build_defs/repo:java.bzl", "java_import_external")
load(
    "@nl_tulipsolutions_tecl//third_party/protoc-gen-validate:version.bzl",
    com_envoyproxy_protoc_gen_validate_version = "version",
)

def repositories(
        omit_com_salesforce_servicelibs_reactive_grpc = False,
        omit_com_github_spullara_mustache_java_compiler = False,
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
        omit_zlib = False,
        omit_com_google_errorprone_error_prone_annotations = False,
        omit_com_envoyproxy_protoc_gen_validate = False,
        omit_io_bazel_rules_kotlin = False,
        omit_org_apache_commons_validator = False):
    if not omit_com_salesforce_servicelibs_reactive_grpc:
        # Latest on master on 2019-06-25
        version = "e7aa649e9a7d4af87fa17ccd6100c53758ee717d"

        http_archive(
            name = "com_salesforce_servicelibs_reactive_grpc",
            strip_prefix = "reactive-grpc-%s" % version,
            sha256 = "89ce5f010e517437a997107621cba355009d8397c76864e8ee1714f063482875",
            url = "https://github.com/salesforce/reactive-grpc/archive/%s.zip" % version,
        )

    if not omit_com_github_spullara_mustache_java_compiler:
        java_import_external(
            name = "com_github_spullara_mustache_java_compiler",
            jar_urls = ["http://central.maven.org/maven2/com/github/spullara/mustache/java/compiler/0.9.6/compiler-0.9.6.jar"],
            jar_sha256 = "c4d697fd3619cb616cc5e22e9530c8a4fd4a8e9a76953c0655ee627cb2d22318",
            srcjar_urls = ["http://central.maven.org/maven2/com/github/spullara/mustache/java/compiler/0.9.6/compiler-0.9.6-sources.jar"],
            srcjar_sha256 = "fb3cf89e4daa0aaa4e659aca12a8ddb0d7b605271285f3e108201e0a389b4c7a",
            licenses = ["notice"],  # Apache 2.0
        )

    if not omit_bazel_skylib:
        # 0.9.0 is incompatible with rules_go 0.19.1
        # https://github.com/bazelbuild/rules_go/issues/2157
        bazel_skylib_version = "0.8.0"

        http_archive(
            name = "bazel_skylib",
            strip_prefix = "bazel-skylib-%s" % bazel_skylib_version,
            sha256 = "2ea8a5ed2b448baf4a6855d3ce049c4c452a6470b1efd1504fdb7c1c134d220a",
            urls = ["https://github.com/bazelbuild/bazel-skylib/archive/%s.tar.gz" % bazel_skylib_version],
        )

    if not omit_com_google_protobuf:
        protobuf_version = "v3.9.0"

        http_archive(
            name = "com_google_protobuf",
            sha256 = "8eb5ca331ab8ca0da2baea7fc0607d86c46c80845deca57109a5d637ccb93bb4",
            strip_prefix = "protobuf-%s" % protobuf_version[1:],
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
        grpc_web_version = "1.0.5"

        http_archive(
            name = "com_github_grpc_grpc_web",
            sha256 = "3edcb8cd06e7ebd15650e2035e14351efc321991d73d5ff351fb1f58a1871e3f",
            strip_prefix = "grpc-web-%s" % grpc_web_version,
            urls = ["https://github.com/grpc/grpc-web/archive/%s.zip" % grpc_web_version],
        )

    if not omit_io_bazel_rules_closure:
        io_bazel_rules_closure_version = "29ec97e7c85d607ba9e41cab3993fbb13f812c4b"  # master HEAD on 2019-07-25

        http_archive(
            name = "io_bazel_rules_closure",
            sha256 = "e15a2c8db4da16d8ae3d55b3e303bed944bc2303b92c92bd4769b84dd711d123",
            strip_prefix = "rules_closure-%s" % io_bazel_rules_closure_version,
            urls = ["https://github.com/bazelbuild/rules_closure/archive/%s.zip" % io_bazel_rules_closure_version],
        )

    if not omit_io_bazel_rules_python:
        rules_python_version = "640e88a6ee6b949ef131a9d512e2f71c6e0e858c"

        http_archive(
            name = "io_bazel_rules_python",
            sha256 = "e6d511fbbb71962823a58e8b06f5961ede02737879113acd5a50b0c2fe58d2be",
            strip_prefix = "rules_python-%s" % rules_python_version,
            url = "https://github.com/bazelbuild/rules_python/archive/%s.zip" % rules_python_version,
        )

    # Six is a dependency for the Python Protobuf rules
    if not omit_six_archive:
        six_archive_build_file_content = """
genrule(
  name = "copy_six",
  srcs = ["six-1.12.0/six.py"],
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
            sha256 = "d16a0141ec1a18405cd4ce8b4613101da75da0e9a7aec5bdd4fa804d0e0eba73",
            urls = ["https://pypi.python.org/packages/source/s/six/six-1.12.0.tar.gz"],
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
        org_llvm_clang_version = "8.0.0"

        http_archive(
            name = "org_llvm_clang",
            sha256 = "0f5c314f375ebd5c35b8c1d5e5b161d9efaeff0523bac287f8b4e5b751272f51",
            strip_prefix = "clang+llvm-%s-x86_64-linux-gnu-ubuntu-18.04" % org_llvm_clang_version,
            url = "http://releases.llvm.org/{0}/clang+llvm-{0}-x86_64-linux-gnu-ubuntu-18.04.tar.xz".format(org_llvm_clang_version),
            build_file_content = """
sh_binary(
    name = "clang_format",
    srcs = ["bin/clang-format"],
    visibility = ['//visibility:public'],
)
""",
        )

    if not omit_bazel_gazelle:
        bazel_gazelle_version = "0.18.1"

        http_archive(
            name = "bazel_gazelle",
            sha256 = "40f6b81c163d190ce7e16ea734ee748ad45e371306a46653fcab93aecda5c0da",
            strip_prefix = "bazel-gazelle-%s" % bazel_gazelle_version,
            urls = ["https://github.com/bazelbuild/bazel-gazelle/archive/%s.tar.gz" % (bazel_gazelle_version)],
        )

    if not omit_build_stack_rules_proto:
        build_stack_rules_proto_version = "d9a123032f8436dbc34069cfc3207f2810a494ee"

        http_archive(
            name = "build_stack_rules_proto",
            sha256 = "ff20827de390a86857cf921f757437cf407db4e5acb39b660467bd8c4d294a8b",
            strip_prefix = "rules_proto-%s" % build_stack_rules_proto_version,
            url = "https://github.com/stackb/rules_proto/archive/%s.zip" % build_stack_rules_proto_version,
        )

    if not omit_io_bazel_rules_go_version:
        io_bazel_rules_go_version = "0.19.1"

        http_archive(
            name = "io_bazel_rules_go",
            strip_prefix = "rules_go-%s" % io_bazel_rules_go_version,
            sha256 = "c9cc6a02113781bb1b1039c289d457dfd832ce2c07dc94dc5bee83160165cffa",
            url = "https://github.com/bazelbuild/rules_go/archive/%s.zip" % io_bazel_rules_go_version,
        )

    if not omit_com_github_bazelbuild_buildtools:
        build_tools_version = "0.29.0"

        http_archive(
            name = "com_github_bazelbuild_buildtools",
            sha256 = "05eb52437fb250c7591dd6cbcfd1f9b5b61d85d6b20f04b041e0830dd1ab39b3",
            strip_prefix = "buildtools-%s" % build_tools_version,
            url = "https://github.com/bazelbuild/buildtools/archive/%s.zip" % build_tools_version,
        )

    if not omit_build_bazel_rules_nodejs:
        build_bazel_rules_nodejs_version = "0.34.0"

        # Not a github source archive as it is not recommended (https://github.com/bazelbuild/rules_nodejs/releases/tag/0.18.4)
        http_archive(
            name = "build_bazel_rules_nodejs",
            urls = ["https://github.com/bazelbuild/rules_nodejs/releases/download/{0}/rules_nodejs-{0}.tar.gz".format(build_bazel_rules_nodejs_version)],
            sha256 = "7c4a690268be97c96f04d505224ec4cb1ae53c2c2b68be495c9bd2634296a5cd",
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

    if not omit_zlib:
        # Mirrors https://github.com/protocolbuffers/protobuf/blob/6a59a2ad1f61d9696092f79b6d74368b4d7970a3/protobuf_deps.bzl
        http_archive(
            name = "zlib",
            build_file = "@com_google_protobuf//:third_party/zlib.BUILD",
            sha256 = "c3e5e9fdd5004dcb542feda5ee4f0ff0744628baf8ed2dd5d66f8ca1197cb1a1",
            strip_prefix = "zlib-1.2.11",
            urls = ["https://zlib.net/zlib-1.2.11.tar.gz"],
        )

    if not omit_com_envoyproxy_protoc_gen_validate:
        http_archive(
            name = "com_envoyproxy_protoc_gen_validate",
            sha256 = "6e99561d01f758489a3ebdf68f049bda9fb47d6bdf99da31513851b4aa15c4e8",
            strip_prefix = "protoc-gen-validate-%s" % com_envoyproxy_protoc_gen_validate_version,
            url = "https://github.com/envoyproxy/protoc-gen-validate/archive/%s.zip" % com_envoyproxy_protoc_gen_validate_version,
            patches = [
                "@nl_tulipsolutions_tecl//third_party/patches/com_envoyproxy_protoc_gen_validate:0001-Add-description-fields-to-ValidationException.patch",
            ],
        )

    if not omit_io_bazel_rules_kotlin:
        rules_kotlin_version = "40efd46789a06f06655e867dcaec2a84815461df"

        http_archive(
            name = "io_bazel_rules_kotlin",
            sha256 = "686d4dfb0f555a6b5cd587f11f34e464320065aafd052dfee4157105b8f91870",
            strip_prefix = "rules_kotlin-%s" % rules_kotlin_version,
            url = "https://github.com/bazelbuild/rules_kotlin/archive/%s.zip" % rules_kotlin_version,
        )

    if not omit_org_apache_commons_validator:
        java_import_external(
            name = "org_apache_commons_validator",
            jar_urls = ["http://central.maven.org/maven2/commons-validator/commons-validator/1.6/commons-validator-1.6.jar"],
            jar_sha256 = "bd62795d7068a69cbea333f6dbf9c9c1a6ad7521443fb57202a44874f240ba25",
            srcjar_urls = ["http://central.maven.org/maven2/commons-validator/commons-validator/1.6/commons-validator-1.6-sources.jar"],
            srcjar_sha256 = "9d4d052237a3b010138b853d8603d996cc3f89a6b3f793c5a50b93481cd8dea2",
            licenses = ["notice"],  # Apache 2.0
        )
