load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
load("@bazel_tools//tools/build_defs/repo:java.bzl", "java_import_external")
load(
    "@nl_tulipsolutions_tecl//third_party/protoc-gen-validate:version.bzl",
    com_envoyproxy_protoc_gen_validate_version = "version",
)

def repositories(
        omit_nl_tulipsolutions_bazel_tools = False,
        omit_com_salesforce_servicelibs_reactive_grpc = False,
        omit_com_github_spullara_mustache_java_compiler = False,
        omit_bazel_skylib = False,
        omit_io_bazel_rules_closure = False,
        omit_com_google_protobuf = False,
        omit_com_github_grpc_grpc = False,
        omit_io_grpc_grpc_java = False,
        omit_com_github_grpc_grpc_web = False,
        omit_rules_python = False,
        omit_python_headers = False,
        omit_org_llvm_clang = False,
        omit_bazel_gazelle = False,
        omit_build_stack_rules_proto = False,
        omit_io_bazel_rules_go_version = False,
        omit_com_github_bazelbuild_buildtools = False,
        omit_build_bazel_rules_nodejs = False,
        omit_com_google_re2j_re2j = False,
        omit_com_google_errorprone_error_prone_annotations = False,
        omit_com_envoyproxy_protoc_gen_validate = False,
        omit_io_bazel_rules_kotlin = False,
        omit_org_apache_commons_validator = False):
    if not omit_nl_tulipsolutions_bazel_tools and not native.existing_rule("nl_tulipsolutions_bazel_tools"):
        nl_tulipsolutions_bazel_tools_version = "bb63703f5cc8d696882eda9cfb8c7f1b95ebf621"

        http_archive(
            name = "nl_tulipsolutions_bazel_tools",
            sha256 = "6d90cb42257777f9b023c4230b2bcd7816f9e3b602b348e2e0e0c7beafb88342",
            strip_prefix = "tulip-bazel-tools-%s" % nl_tulipsolutions_bazel_tools_version,
            urls = ["https://github.com/TulipSolutions/tulip-bazel-tools/archive/%s.zip" % nl_tulipsolutions_bazel_tools_version],
        )

    if not omit_com_salesforce_servicelibs_reactive_grpc:
        # Latest on master on 2020-01-28
        version = "eb450821f3eae49e755cc9ed8ee46327ba864854"

        http_archive(
            name = "com_salesforce_servicelibs_reactive_grpc",
            strip_prefix = "reactive-grpc-%s" % version,
            sha256 = "402a4288d7ea9c576bcf5ea81a6b3a69e435758b8509140c9e1f4c691980c6e0",
            url = "https://github.com/salesforce/reactive-grpc/archive/%s.zip" % version,
        )

    if not omit_com_github_spullara_mustache_java_compiler:
        java_import_external(
            name = "com_github_spullara_mustache_java_compiler",
            jar_urls = ["https://repo.maven.apache.org/maven2/com/github/spullara/mustache/java/compiler/0.9.6/compiler-0.9.6.jar"],
            jar_sha256 = "c4d697fd3619cb616cc5e22e9530c8a4fd4a8e9a76953c0655ee627cb2d22318",
            srcjar_urls = ["https://repo.maven.apache.org/maven2/com/github/spullara/mustache/java/compiler/0.9.6/compiler-0.9.6-sources.jar"],
            srcjar_sha256 = "fb3cf89e4daa0aaa4e659aca12a8ddb0d7b605271285f3e108201e0a389b4c7a",
            licenses = ["notice"],  # Apache 2.0
        )

    if not omit_bazel_skylib:
        bazel_skylib_version = "1.0.2"

        http_archive(
            name = "bazel_skylib",
            urls = [
                "https://mirror.bazel.build/github.com/bazelbuild/bazel-skylib/releases/download/{v}/bazel-skylib-{v}.tar.gz".format(v = bazel_skylib_version),
                "https://github.com/bazelbuild/bazel-skylib/releases/download/{v}/bazel-skylib-{v}.tar.gz".format(v = bazel_skylib_version),
            ],
            sha256 = "97e70364e9249702246c0e9444bccdc4b847bed1eb03c5a3ece4f83dfe6abc44",
        )

    if not omit_io_bazel_rules_closure:
        io_bazel_rules_closure_version = "614e1ebc38249c6793eab2e078bceb0fb12a1a42"  # master HEAD on 2020-01-28

        http_archive(
            name = "io_bazel_rules_closure",
            sha256 = "d214736912d20293395682d7142411a117f0a17fb4d7e205ccbd438bd4a3738d",
            strip_prefix = "rules_closure-%s" % io_bazel_rules_closure_version,
            urls = ["https://github.com/bazelbuild/rules_closure/archive/%s.zip" % io_bazel_rules_closure_version],
        )

    if not omit_com_google_protobuf:
        protobuf_version = "v3.11.3"

        http_archive(
            name = "com_google_protobuf",
            patches = [
                # Get rid of the annoying build warnings by opting out for Java 7 compatibility:
                # warning: -parameters is not supported for target value 1.7. Use 1.8 or later.
                "@io_bazel_rules_closure//closure:protobuf_drop_java_7_compatibility.patch",
            ],
            patch_args = ["-p1"],
            sha256 = "832c476bb442ca98a59c2291b8a504648d1c139b74acc15ef667a0e8f5e984e7",
            strip_prefix = "protobuf-%s" % protobuf_version[1:],
            urls = ["https://github.com/google/protobuf/archive/%s.zip" % protobuf_version],
        )

    if not omit_com_github_grpc_grpc:
        grpc_version = "1.27.0"

        http_archive(
            name = "com_github_grpc_grpc",
            patches = [
                "@nl_tulipsolutions_tecl//third_party/patches/com_github_grpc_grpc:0001-Allow-depending-on-the-Bazel-Python-rules-from-other.patch",
                "@nl_tulipsolutions_tecl//third_party/patches/com_github_grpc_grpc:0002-use-sha256-for-boringssl-bazel-dependency.patch",
            ],
            sha256 = "038d61ff86c91f44131daef5755dc25ce5131cbc53f1789bd126e924272a6675",
            strip_prefix = "grpc-%s" % grpc_version,
            urls = ["https://github.com/grpc/grpc/archive/v%s.zip" % grpc_version],
        )

    if not omit_io_grpc_grpc_java:
        io_grpc_grpc_java_version = "1.27.0"

        http_archive(
            name = "io_grpc_grpc_java",
            patches = [
                "@nl_tulipsolutions_tecl//third_party/patches/io_grpc_grpc_java:0001-Set-core-internal-visibility-to-public.patch",
            ],
            sha256 = "49a723e1aef022567a5e2c8d6395b908b431329530c1b8024b43eb9ca360fa1e",
            strip_prefix = "grpc-java-%s" % io_grpc_grpc_java_version,
            urls = ["https://github.com/grpc/grpc-java/archive/v%s.zip" % io_grpc_grpc_java_version],
        )

    if not omit_com_github_grpc_grpc_web:
        grpc_web_version = "1.0.5"

        http_archive(
            name = "com_github_grpc_grpc_web",
            sha256 = "3edcb8cd06e7ebd15650e2035e14351efc321991d73d5ff351fb1f58a1871e3f",
            strip_prefix = "grpc-web-%s" % grpc_web_version,
            urls = ["https://github.com/grpc/grpc-web/archive/%s.zip" % grpc_web_version],
        )

    if not omit_rules_python:
        rules_python_version = "38f86fb55b698c51e8510c807489c9f4e047480e"

        http_archive(
            name = "rules_python",
            sha256 = "7d64815f4b22400bed0f1b9da663037e1578573446b7bc78f20f24b2b5459bb9",
            strip_prefix = "rules_python-%s" % rules_python_version,
            url = "https://github.com/bazelbuild/rules_python/archive/%s.zip" % rules_python_version,
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
        bazel_gazelle_version = "v0.20.0"

        http_archive(
            name = "bazel_gazelle",
            urls = [
                "https://storage.googleapis.com/bazel-mirror/github.com/bazelbuild/bazel-gazelle/releases/download/{v}/bazel-gazelle-{v}.tar.gz".format(v = bazel_gazelle_version),
                "https://github.com/bazelbuild/bazel-gazelle/releases/download/{v}/bazel-gazelle-{v}.tar.gz".format(v = bazel_gazelle_version),
            ],
            sha256 = "d8c45ee70ec39a57e7a05e5027c32b1576cc7f16d9dd37135b0eddde45cf1b10",
        )

    if not omit_build_stack_rules_proto:
        build_stack_rules_proto_version = "b2913e6340bcbffb46793045ecac928dcf1b34a5"

        http_archive(
            name = "build_stack_rules_proto",
            patches = [
                "@nl_tulipsolutions_tecl//third_party/patches/com_github_stackb_rules_proto:0001-Update-paths-to-grpc-plugins.patch",
            ],
            sha256 = "618a1c0eb4e75bee3d4f4d759d1dab144dd82150fe2fa195acd8e6b8f530de2a",
            strip_prefix = "rules_proto-%s" % build_stack_rules_proto_version,
            url = "https://github.com/stackb/rules_proto/archive/%s.zip" % build_stack_rules_proto_version,
        )

    if not omit_io_bazel_rules_go_version:
        io_bazel_rules_go_version = "v0.21.2"

        http_archive(
            name = "io_bazel_rules_go",
            urls = [
                "https://mirror.bazel.build/github.com/bazelbuild/rules_go/releases/download/{v}/rules_go-{v}.tar.gz".format(v = io_bazel_rules_go_version),
                "https://github.com/bazelbuild/rules_go/releases/download/{v}/rules_go-{v}.tar.gz".format(v = io_bazel_rules_go_version),
            ],
            sha256 = "f99a9d76e972e0c8f935b2fe6d0d9d778f67c760c6d2400e23fc2e469016e2bd",
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
            jar_urls = ["https://repo.maven.apache.org/maven2/com/google/errorprone/error_prone_annotations/2.3.3/error_prone_annotations-2.3.3.jar"],
            jar_sha256 = "ec59f1b702d9afc09e8c3929f5c42777dec623a6ea2731ac694332c7d7680f5a",
            srcjar_urls = ["https://repo.maven.apache.org/maven2/com/google/errorprone/error_prone_annotations/2.3.3/error_prone_annotations-2.3.3-sources.jar"],
            srcjar_sha256 = "f58446b80b5f1e98bcb74dae5c0710ed8e52baafe5a4bb315f769f306d85634a",
            licenses = ["notice"],  # Apache 2.0
        )

    if not omit_com_envoyproxy_protoc_gen_validate and not omit_com_google_re2j_re2j:
        # gRPC used to provide RE2J as dependency, and protoc-gen-validate still uses this, but we don't want to call
        # pgv_dependencies() in protoc-gen-validate's bazel/repositories.bzl as that pulls in a lot more.
        java_import_external(
            name = "com_google_re2j_re2j",
            jar_urls = ["https://repo.maven.apache.org/maven2/com/google/re2j/re2j/1.2/re2j-1.2.jar"],
            jar_sha256 = "e9dc705fd4c570344b54a7146b2e3a819cdc271a29793f4acc1a93b56a388e59",
            srcjar_urls = ["https://repo.maven.apache.org/maven2/com/google/re2j/re2j/1.2/re2j-1.2-sources.jar"],
            srcjar_sha256 = "43a81e5a7bf2b3119b592910098cca0835f012d2805bcfdade44cdc8f2bdfb48",
            licenses = ["notice"],  # Apache 2.0
        )

    if not omit_com_envoyproxy_protoc_gen_validate:
        http_archive(
            name = "com_envoyproxy_protoc_gen_validate",
            sha256 = "62c89ce3556e6f9b1ecc1b9deed7024798d2519e631316f7f4c3bca37b295bb5",
            strip_prefix = "protoc-gen-validate-%s" % com_envoyproxy_protoc_gen_validate_version,
            url = "https://github.com/envoyproxy/protoc-gen-validate/archive/%s.zip" % com_envoyproxy_protoc_gen_validate_version,
            patches = [
                "@nl_tulipsolutions_tecl//third_party/patches/com_envoyproxy_protoc_gen_validate:0001-Add-description-fields-to-ValidationException.patch",
                "@nl_tulipsolutions_tecl//third_party/patches/com_envoyproxy_protoc_gen_validate:0002-Strip-virtual-imports-directory-from-proto-path.patch",
            ],
            repo_mapping = {
                # Unfortunately, protoc-gen-validate does not use the convention of <maven-repo>_<maven_artifact> in
                # naming their dependencies.
                "@com_google_re2j": "@com_google_re2j_re2j",
            },
        )

    if not omit_io_bazel_rules_kotlin:
        rules_kotlin_version = "4512a83053489326a3643ef9d84e3e15420eb58e"

        http_archive(
            name = "io_bazel_rules_kotlin",
            sha256 = "5108e1fa0ac9012a92e7a5825562284fa756f469b80020d0cd7fa03c44f6bb20",
            strip_prefix = "rules_kotlin-%s" % rules_kotlin_version,
            url = "https://github.com/bazelbuild/rules_kotlin/archive/%s.zip" % rules_kotlin_version,
        )

    if not omit_org_apache_commons_validator:
        java_import_external(
            name = "org_apache_commons_validator",
            jar_urls = ["https://repo.maven.apache.org/maven2/commons-validator/commons-validator/1.6/commons-validator-1.6.jar"],
            jar_sha256 = "bd62795d7068a69cbea333f6dbf9c9c1a6ad7521443fb57202a44874f240ba25",
            srcjar_urls = ["https://repo.maven.apache.org/maven2/commons-validator/commons-validator/1.6/commons-validator-1.6-sources.jar"],
            srcjar_sha256 = "9d4d052237a3b010138b853d8603d996cc3f89a6b3f793c5a50b93481cd8dea2",
            licenses = ["notice"],  # Apache 2.0
        )
