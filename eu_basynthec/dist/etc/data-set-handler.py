"""Just import the data into openbis"""
tr = service.transaction(incoming)
dataSet = tr.createNewDataSet()
tr.moveFile(incoming.getAbsolutePath(), dataSet)

