workspace(name = "nl_tulipsolutions_tecl")

load("//bazel:repositories.bzl", "repositories")

repositories()

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

load("@build_bazel_rules_nodejs//:defs.bzl", "node_repositories", "npm_install")

node_repositories()

npm_install(
    name = "npm",
    package_json = "//:package.json",
    package_lock_json = "//:package-lock.json",
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
