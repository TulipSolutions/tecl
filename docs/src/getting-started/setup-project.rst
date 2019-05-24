.. _getting_started_setup_project:

Part 1: Setting up a project
============================

Welcome to the first part of the Getting Started tutorial.
The goals of this part are: installing the dependencies (such as :term:`Protobuf` and :term:`gRPC`), obtaining our
client library (:term:`TECL`) and setting up the starting point of your project.

The first part comes in two variants:

.. toctree::
   :maxdepth: 1

   From source <from-source>
   Bazel <bazel>

The *From source* variant walks you through a traditional approach, using straight git sources and your programming
language's native way of dependency management.
The option to use :term:`Bazel` is recommended if you're familiar with it.

.. admonition:: Programming language selector
   :class: hint

   On the top of the page, a programming language selector allows you to view content relevant for it.
   By default it is set to *Go*, but we also offer examples in  *Java*, *Node.JS* and *Python* (3.6+).

   Content in the context of your programming language selection is denoted by a dashed gray line on the left.

After having set up your project, continue on to part two,
:ref:`an example in using the public API: streaming the public orderbook <getting_started_streaming_public_orderbook>`.
