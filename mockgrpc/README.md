# Mock gRPC

This package is simulates the exchange backend by implementing all (currently implemented) gRPC calls.
It is created for helping speed up frontend development.
It generates dummy data and requires no additional services to be run (aside from maybe a proxy).

Run the server with the following command:

    $ bazel run //mockgrpc
