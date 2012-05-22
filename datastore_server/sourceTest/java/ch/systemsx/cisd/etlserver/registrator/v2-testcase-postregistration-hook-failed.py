from java.lang import IllegalArgumentException

execfile("sourceTest/java/ch/systemsx/cisd/etlserver/registrator/all-hooks.py")

def post_metadata_registration(context):
    jythonHookTestTool.log("post_metadata_registration")
    raise IllegalArgumentException("Fail at post_metadata_registration to cancel registration")

def process():
  execfile("sourceTest/java/ch/systemsx/cisd/etlserver/registrator/simple-transaction.py")

