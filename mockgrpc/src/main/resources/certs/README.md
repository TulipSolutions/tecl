# Mock TLS Certificates

**Warning:** These certificates are for local use only and should never be placed in a local keystore.

The certificates enable clients to set up a secure connection to a server running at localhost.
A secure connection is required by some gRPC libraries when sending credentials along with a request (such as a JWT in metadata).
