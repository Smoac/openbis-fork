def rollback_transaction(service, transaction, algorithmRunner, throwable):
    global didTransactionRollbackHappen
    didTransactionRollbackHappen = True

def commit_transaction(service, transaction):
    global didTransactionCommitHappen
    didTransactionCommitHappen = True

def post_storage(transaction, context):
    global didPostStorageFunctionRunHappen
    didPostStorageFunctionRunHappen = True

def pre_metadata_registration(transaction, context):
    global didPreRegistrationFunctionRunHappen
    didPreRegistrationFunctionRunHappen = True

def post_metadata_registration(transaction, context):
    global didPostRegistrationFunctionRunHappen
    didPostRegistrationFunctionRunHappen = True
