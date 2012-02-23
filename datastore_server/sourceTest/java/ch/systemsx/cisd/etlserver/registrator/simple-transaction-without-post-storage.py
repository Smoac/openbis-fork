def rollback_transaction(service, transaction, algorithmRunner, throwable):
    global didTransactionRollbackHappen
    didTransactionRollbackHappen = True

def commit_transaction(service, transaction):
    global didTransactionCommitHappen
    didTransactionCommitHappen = True

def pre_metadata_registration(transaction, context):
    global didPreRegistrationFunctionRunHappen
    didPreRegistrationFunctionRunHappen = True

def post_metadata_registration(transaction, context):
    global didPostRegistrationFunctionRunHappen
    didPostRegistrationFunctionRunHappen = True

transaction = service.transaction(incoming, factory)
dataSet = transaction.createNewDataSet()
transaction.moveFile(incoming.getPath() + '/sub_data_set_1', dataSet)
dataSet.setDataSetType('O1')
dataSet.setExperiment(transaction.getExperiment('/SPACE/PROJECT/EXP'))
