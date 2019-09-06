# Mock gRPC

This package is simulates the exchange backend by implementing all (currently implemented) gRPC calls.
It is created for helping speed up frontend development, but is also useful in other cases like testing features not
rolled out yet or offline development.

It generates dummy data and requires no additional services to be run (aside from maybe a proxy).

Run the server with the following command:

    $ bazelisk run //mockgrpc

Also existing code examples can then be run against this server using parameters like for example Golang examples:

    $ bazelisk run //examples/go/docs:docs -- localhost 10011
