from java.lang import IllegalArgumentException

transaction = service.transaction(incoming)

dataSet = transaction.createNewDataSet()
exp = dataSet.getExperiment()
samp = dataSet.getSample()
if exp is None or samp is None:
  raise IllegalArgumentException("data-set-handler.py: No Experiment or Sample specified")
transaction.moveFile(incoming.getAbsolutePath(), dataSet)
