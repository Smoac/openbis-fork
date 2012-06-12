execfile("sourceTest/java/ch/systemsx/cisd/etlserver/registrator/all-hooks.py")

def should_retry_processing(map, error):
  lastResult = transaction.getPersistentMap().get("RETRY_COUNT")
  if (lastResult == None):
    lastResult = 0
  transaction.getPersistentMap().put("RETRY_COUNT", lastResult + 1)
  print "RETRY TRANSACTION PUTS THE SCORE %d" % (lastResult + 1)

def process():
  key = transaction.getPersistentMap().get("RETRY_COUNT");
  dataSet = transaction.createNewDataSet()
  transaction.moveFile(incoming.getPath() + '/sub_data_set_1', dataSet)
  dataSet.setDataSetType('O1')
  dataSet.setExperiment(transaction.getExperiment('/SPACE/PROJECT/EXP'))
  if (key < 20):
    # do here the real throwing of the exception
    raise Exception("error in the script at the retry %d" % key)