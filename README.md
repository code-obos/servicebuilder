# Service Builder

In OBOS we decided to write our own microservice-chassis instead of using spring/j2ee/dropwizard. The reasoning was that
a microservice-chassis is just a bundling of microframeworks, a problem that is not to hard if you choose the right
microframeworks. We have found that this approach provides several advantages:
 * Fine grained control to tailor integration of microframeworks to our organizational-specific needs 
 * High degree of standardization in services
 * Room for innovation in code infrastructure
 
The code is tailored specific for our needs, it is dependent on our libraries and must be tailored to your needs.
  We wanted to share this code as an example/template of writing your own microservice-chassis.

## Version 4
###### Breaking changes
* UserTokenFilterAddon.postVerificationCallback() has been removed. Bind a class to UserTokenAuthenticatedHandler instead.

## Version 6
###### Breaking changes
* Updated dependencies to be compatible with Bean Validation 2.0 (JSR 380) Reference Implementation.
* Updated to Mockito version 2.

## Version 7
###### Breaking changes
* Updated ActiveMqListenerAddon to require MessageHandler.
###### New features
* Added annotation for fine grained application token id handling on resource and specific endpoints.
* Added elasticsearch client addon

## Version 8
###### Breaking changes
* Removed apiBasePath from SwaggerAddon.
* Updated ApplicationTokenFilterAddon to require all whitelisted app ids in property file.
###### New features
* Added support for overriding basepath with the header X-Forwarded-Path in SwaggerAddon.
* Added support for iterators in ElasticsearchIndexAddon.

## Version 9
###### Breaking changes
* Removed support for application ranges in AppTokenFilterAddon
###### New features
* Added support for custom configuration of new elasticsearch indexes

## Version 10
###### Breaking changes
* Removed support for JDBI2
###### New features
* Added support for JDBI3
