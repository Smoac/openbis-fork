Custom Application Server Services
==================================

## Introduction

On Data Store Server (DSS) aggregation/ingestion services based on Jython scripts can be used to extend openBIS by custom services. These services have full access on data store and Application Server (AS).

Often only access on AS is needed. Going over DSS is a detour. For such cases it is better to write an AS core plugin of type `services`.

## How to write a custom AS service core plugin

Here is the recipe to create an AS core plugin of type `services`:

1. The folder `<core plugin folder>/<module>/<version>/as/services/<core plugin name>` has to be created.

2. In this folder two files have to be created: `plugin.properties` and `script.py`. The properties file should contain:

    **plugin.properties**

    ```
    class = ch.ethz.sis.openbis.generic.server.asapi.v3.helper.service.JythonBasedCustomASServiceExecutor
    script-path = script.py
    ```

3. The script file should have the function `process` with two arguments. The first argument is the context. It contains the methods `getSessionToken()` and `getApplicationService()` which returns an instance of `ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi`. The second argument is a map of key-value pairs. The key is a string and the values is an arbitrary object. Anything returned by the script will be returned to the caller of the service. Here is an example of a script which creates a space:
**script.py**
```py
from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create import SpaceCreation

def process(context, parameters):
    space_creation = SpaceCreation()
    space_creation.code = parameters.get('space_code');
    result = context.applicationService.createSpaces(context.sessionToken, [space_creation]);
    return "Space created: %s" % result
```
Note, that all changes on the AS database will be done in one transaction.

## How to use a custom AS service

The application API version 3 offers the following method to search for
existing services:

    SearchResult<CustomASService> searchCustomASServices(String sessionToken, CustomASServiceSearchCriteria searchCriteria,
                CustomASServiceFetchOptions fetchOptions)

The following Java code example returns all available services:

    SearchResult<CustomASService> services = service.searchCustomASServices(sessionToken, new CustomASServiceSearchCriteria(), new CustomASServiceFetchOptions());

 

With the following method of the API version 3 a specified service can
be executed:

    public Object executeCustomASService(String sessionToken, ICustomASServiceId serviceId, CustomASServiceExecutionOptions options);

The `serviceId` can be obtained from a `CustomASService` object (as
returned by the `searchCustomASServices` method) by the getter method
`getCode()`. It can also be created as an instance of
`CustomASServiceCode`. Note, that the service code is just the core
plugin name.

Parameter bindings (i.e. key-value pairs) are specified in the
`CustomASServiceExecutionOptions` object by invoking for each binding
the method `withParameter()`.

Here is a code example:

```py
CustomASServiceExecutionOptions options = new CustomASServiceExecutionOptions().withParameter("space_code", "my-space");
Object result = service.executeCustomASService(sessionToken, new CustomASServiceCode("space-creator"), options);
System.out.println(result);
```
 
