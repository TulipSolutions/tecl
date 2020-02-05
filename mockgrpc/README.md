# Mock gRPC

This package is simulates the exchange backend by implementing all (currently implemented) gRPC calls.
It is created for helping speed up frontend development, but is also useful in other cases like testing features not
rolled out yet or offline development.

It generates dummy data and requires no additional services to be run (aside from maybe a proxy).

Run the server with the following command:

    $ bazelisk run //mockgrpc

Also existing code examples can then be run against this server using parameters like for example Golang examples:

    $ bazelisk run //examples/go/docs:docs -- localhost 10011

## JDK 11+ required

Unlike the Java code examples for TECL that will work with JDK 8 and higher, to run mockgrpc you will need at least JDK
11 to be installed and on your PATH. For e.g. Debian/Ubuntu this involves:

    $ sudo apt install openjdk-11-jdk-headless
    $ sudo update-alternatives --config javac  # select java-11
    $ sudo update-alternatives --config java  # select java-11
    $ java -version  # should print version 11
    $ javac -version  # should print version 11

## Error testing

For testing purposes the `PrivateOrder.createOrder` and `PrivateOrder.cancelOrder` mock services will throw errors
if one of the following preconditions are met:

| Precondition                          | Error                    |
| ---                                   | ---                      |
| Market == BTC_USD                     | MarketDisabled           |
| baseAmount digits after delimiter > 8 | InvalidAmountPrecision   |
| price digits after delimiter > 2      | InvalidPricePrecision    |
| baseAmount > 1000.0                   | BaseOrderAmountTooLarge  |
| baseAmount < 1.0                      | BaseOrderAmountTooSmall  |
| price * amount > 1000000.0            | QuoteOrderAmountTooLarge |
| price * amount < 1.0                  | QuoteOrderAmountTooSmall |
| baseAmount == 1337.0                  | InsufficientBalance      |
| tonce % 2 == 1 or orderId % 2 == 1    | OrderIdNotExist          |

See example code how these errors can be parsed.
