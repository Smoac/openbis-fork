from java.lang import IllegalArgumentException

transaction = service.transaction(incoming)



#test for LMS-2879
sample = transaction.getSample("/CISD/CP-TEST-1")    
sa = transaction.getSampleForUpdate(sample.getSampleIdentifier())

sa.setPropertyValue('COMMENT', 'comment')
try:
  sa.setPropertyValue('DYNAMIC_DESCRIPTION', 'description')
  raise 'Setting of dynamic property should result in an exception'
except IllegalArgumentException, isx:
  print isx
  #ok

dataSet = transaction.createNewDataSet()
exp = dataSet.getExperiment()
samp = dataSet.getSample()
if exp is None or samp is None:
  raise IllegalArgumentException("data-set-handler.py: No Experiment or Sample specified")
transaction.moveFile(incoming.getAbsolutePath(), dataSet)
