# Mock gRPC

This package is simulates the exchange backend by implementing all (currently implemented) gRPC calls.
It is created for helping speed up frontend development, but is also useful in other cases like testing features not
rolled out yet or offline development.

It generates dummy data and requires no additional services to be run (aside from maybe a proxy).

Run the server with the following command:

    $ bazelisk run //mockgrpc

Also existing code examples can then be run against this server using parameters like for example Golang examples:

    $ bazelisk run //examples/go/docs:docs -- localhost 10011

## JDK 9+ required

Unlike the Java code examples for TECL that will work with JDK 8 and higher, to run mockgrpc you will need JDK 9+ to be
installed and on your PATH. For e.g. Debian/Ubuntu this involves:

    $ sudo apt install openjdk-11-jdk-headless
    $ sudo update-alternatives --config javac  # select java-11
    $ sudo update-alternatives --config java  # select java-11
    $ java -version  # should print version 11
    $ javac -version  # should print version 11
