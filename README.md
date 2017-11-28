# Service Builder

A framework for creating OBOS services.

## Breaking changes in v4

* UserTokenFilterAddon.postVerificationCallback() has been removed. Bind a class to UserTokenAuthenticatedHandler instead.

## Breaking changes in v6
* Updated dependencies to be compatible with Bean Validation 2.0 (JSR 380) Reference Implementation.
* Updated to Mockito version 2.

## Breaking changes in v7
* Updated ActiveMqListenerAddon to require MessageHandler.
* Added annotation for fine grained application token id handling on resource and specific endpoints.
