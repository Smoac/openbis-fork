from ch.systemsx.cisd.etlserver.registrator import JythonHookTestTool

jythonHookTestTool = JythonHookTestTool.createFromIncoming(incoming)

def rollback_transaction(service, transaction, algorithmRunner, throwable):
    global didTransactionRollbackHappen
    didTransactionRollbackHappen = True
    jythonHookTestTool.log("rollback_transaction")

def rollback_pre_registration(context, throwable):
    global didPreRegistrationRollbackHappen
    didPreRegistrationRollbackHappen = True
    jythonHookTestTool.log("rollback_pre_registration")

def commit_transaction(service, transaction):
    global didTransactionCommitHappen
    didTransactionCommitHappen = True
    jythonHookTestTool.log("commit_transaction")

def post_storage(context):
    global didPostStorageFunctionRunHappen
    didPostStorageFunctionRunHappen = True
    jythonHookTestTool.log("post_storage")

def pre_metadata_registration(context):
    global didPreRegistrationFunctionRunHappen
    didPreRegistrationFunctionRunHappen = True
    jythonHookTestTool.log("pre_metadata_registration")

def post_metadata_registration(context):
    global didPostRegistrationFunctionRunHappen
    didPostRegistrationFunctionRunHappen = True
    jythonHookTestTool.log("post_metadata_registration")
