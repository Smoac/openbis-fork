execfile("sourceTest/java/ch/systemsx/cisd/etlserver/registrator/all-hooks.py")

# this check is to make the script compatible with V1 and V2
if transaction is None:
  transaction = service.transaction(incoming, factory)
dataSet = transaction.createNewDataSet()
transaction.moveFile('/non/existent/path', dataSet)
dataSet.setDataSetType('O1')
dataSet.setExperiment(transaction.getExperiment('/SPACE/PROJECT/EXP'))
