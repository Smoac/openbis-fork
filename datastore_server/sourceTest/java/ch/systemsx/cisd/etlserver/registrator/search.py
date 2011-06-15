def rollback_transaction(service, transaction, algorithmRunner, throwable):
	global didTransactionRollbackHappen
	didTransactionRollbackHappen = True
	
def rollback_service(service, throwable):
	global didRollbackServiceFunctionRun
	didRollbackServiceFunctionRun = True

tr = service.transaction(incoming, factory)
searchService = tr.getSearchService()
searchService.listExperiments("/space/project", "EXP_TYPE")
searchService.searchForDataSets("PROP", "VALUE", "DATA_SET_TYPE")
searchService.searchForSamples("PROP", "VALUE", "SAMPLE_TYPE")

