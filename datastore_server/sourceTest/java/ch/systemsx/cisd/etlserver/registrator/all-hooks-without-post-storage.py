def rollback_transaction(service, transaction, algorithmRunner, throwable):
    global didTransactionRollbackHappen
    didTransactionRollbackHappen = True

def commit_transaction(service, transaction):
    global didTransactionCommitHappen
    didTransactionCommitHappen = True

def pre_metadata_registration(context):
    global didPreRegistrationFunctionRunHappen
    didPreRegistrationFunctionRunHappen = True

def post_metadata_registration(context):
    global didPostRegistrationFunctionRunHappen
    didPostRegistrationFunctionRunHappen = True
