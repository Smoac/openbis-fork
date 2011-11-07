def rollback_transaction(service, transaction, algorithmRunner, throwable):
	global didTransactionRollbackHappen
	didTransactionRollbackHappen = True

def commit_transaction(service, transaction):
	global didTransactionCommitHappen
	didTransactionCommitHappen = True
	
transaction = service.transaction(incoming, factory)
dataSet = transaction.createNewDataSet()
transaction.moveFile('/non/existent/path', dataSet)
dataSet.setDataSetType('O1')
dataSet.setExperiment(transaction.getExperiment('/SPACE/PROJECT/EXP'))
