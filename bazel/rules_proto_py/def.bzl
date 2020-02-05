load(
    "@com_github_grpc_grpc//bazel:python_rules.bzl",
    com_github_grpc_grpc_py_grpc_library = "py_grpc_library",
    com_github_grpc_grpc_py_proto_library = "py_proto_library",
)

py_grpc_library = com_github_grpc_grpc_py_grpc_library
py_proto_library = com_github_grpc_grpc_py_proto_library
