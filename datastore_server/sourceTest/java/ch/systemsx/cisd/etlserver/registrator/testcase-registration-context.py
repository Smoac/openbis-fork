execfile("sourceTest/java/ch/systemsx/cisd/etlserver/registrator/simple-transaction.py")

transaction.getPersistentMap().put("body","1")

global contextTestFailed
contextTestFailed = None

def rollback_transaction(service, transaction, algorithmRunner, throwable):
    global didTransactionRollbackHappen
    didTransactionRollbackHappen = True

def pre_metadata_registration(context):
    
    assert_context_content(context, "pre_metadata_registration", "body", "1");
    assert_context_content(context, "pre_metadata_registration", "pre_metadata_registration", None);
    assert_context_content(context, "pre_metadata_registration", "post_storage", None);

    context.put("pre_metadata_registration", "2")
    global didPreRegistrationFunctionRunHappen
    didPreRegistrationFunctionRunHappen = True

def post_metadata_registration(context):

    assert_context_content(context, "post_metadata_registration", "body", "1");
    assert_context_content(context, "post_metadata_registration", "pre_metadata_registration", "2");
    assert_context_content(context, "post_metadata_registration", "post_storage", None);

    context.put("post_metadata_registration", "3")
    global didPostRegistrationFunctionRunHappen
    didPostRegistrationFunctionRunHappen = True

def post_storage(context):

    assert_context_content(context, "post_storage", "body", "1");
    assert_context_content(context, "post_storage", "pre_metadata_registration", "2");
    assert_context_content(context, "post_storage", "post_metadata_registration", "3");

    context.put("post_storage", "4")
    global didPostStorageFunctionRunHappen
    didPostStorageFunctionRunHappen = True
    
def assert_context_content(context, caller, name, expected):
    value = context.get(name)
    if (value != expected):
        global contextTestFailed
        if (value != None and expected != None and type(value) != type(expected)):
            value = "%s:%s" % (type(value), value)
            expected = "%s:%s" % (type(expected), expected)
        contextTestFailed = "in %s the value of %s should have been '%s', but was '%s'" % (caller, name, expected, value) 
