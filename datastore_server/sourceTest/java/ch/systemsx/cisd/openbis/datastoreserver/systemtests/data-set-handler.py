transaction = service.transaction(incoming)

dataSet = transaction.createNewDataSet()
transaction.moveFile(incoming.getAbsolutePath(), dataSet)
