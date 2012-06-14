execfile("sourceTest/java/ch/systemsx/cisd/etlserver/registrator/all-hooks.py")
def should_retry_processing(map, error):
    return False

def process():
    execfile("sourceTest/java/ch/systemsx/cisd/etlserver/registrator/simple-transaction.py")