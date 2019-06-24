workspace(
    name = "nl_tulipsolutions_tecl",
    managed_directories = {"@npm": ["node_modules"]},
)

load("//bazel:repositories.bzl", "repositories")

repositories()

load("@bazel_skylib//lib:versions.bzl", "versions")

versions.check(minimum_bazel_version = "0.27.0")

load("@io_bazel_rules_kotlin//kotlin:kotlin.bzl", "kotlin_repositories", "kt_register_toolchains")

kotlin_repositories()

kt_register_toolchains()

load("@io_bazel_rules_python//python:pip.bzl", "pip_import", "pip_repositories")

pip_import(
    name = "docs_deps",
    requirements = "//bazel/rules_sphinx:requirements.txt",
)

load("@docs_deps//:requirements.bzl", docs_pip_install = "pip_install")

docs_pip_install()

pip_import(
    name = "grpc_python_dependencies",
    requirements = "//python:requirements.txt",
)

load("@grpc_python_dependencies//:requirements.bzl", grpc_python_pip_install = "pip_install")

grpc_python_pip_install()

pip_import(
    name = "flake8_deps",
    requirements = "//bazel/rules_flake8:requirements.txt",
)

load("@flake8_deps//:requirements.bzl", flake8_pip_install = "pip_install")

flake8_pip_install()

load("@io_bazel_rules_go//go:deps.bzl", "go_register_toolchains", "go_rules_dependencies")

go_rules_dependencies()

go_register_toolchains()

load("@com_github_bazelbuild_buildtools//buildifier:deps.bzl", "buildifier_dependencies")

buildifier_dependencies()

load("@com_github_grpc_grpc//bazel:grpc_deps.bzl", "grpc_deps")

grpc_deps()

load("@io_grpc_grpc_java//:repositories.bzl", "grpc_java_repositories")

grpc_java_repositories(
    omit_com_google_protobuf = True,
)

load("@build_bazel_rules_nodejs//:defs.bzl", "node_repositories", "yarn_install")

node_repositories()

yarn_install(
    name = "npm",
    package_json = "//:package.json",
    yarn_lock = "//:yarn.lock",
)

load("@bazel_gazelle//:deps.bzl", "gazelle_dependencies")

gazelle_dependencies()

load("@bazel_gazelle//:deps.bzl", "go_repository")

add_license_version = "22550fa7c1b07a27e810565721ac49469615e05b"

go_repository(
    name = "com_github_google_addlicense",
    importpath = "github.com/google/addlicense",
    sha256 = "beeea8a9e2950a23b0bb1e22d94cd58bea9cfaf1d321ef9f2ac2707d92ce7ab3",
    strip_prefix = "addlicense-%s" % add_license_version,
    urls = ["https://github.com/google/addlicense/archive/%s.zip" % add_license_version],
)

load("@com_salesforce_servicelibs_reactive_grpc//bazel:repositories.bzl", reactive_grpc_repositories = "repositories")

reactive_grpc_repositories(
    omit_com_github_spullara_mustache_java_compiler = True,
    omit_io_grpc_grpc_java = True,
)
