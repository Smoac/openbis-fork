execfile("sourceTest/java/ch/systemsx/cisd/etlserver/registrator/all-hooks.py")
def process():
  execfile("sourceTest/java/ch/systemsx/cisd/etlserver/registrator/simple-transaction.py")
  transaction.rollback()
