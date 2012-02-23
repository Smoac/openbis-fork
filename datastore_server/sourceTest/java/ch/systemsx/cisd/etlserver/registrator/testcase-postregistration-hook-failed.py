from java.lang import IllegalArgumentException

execfile("sourceTest/java/ch/systemsx/cisd/etlserver/registrator/all-hooks.py")

def post_metadata_registration(transaction, context):
    global didPostRegistrationFunctionRunHappen
    didPostRegistrationFunctionRunHappen = True
    raise IllegalArgumentException("Fail at post_metadata_registration to cancel registration")

execfile("sourceTest/java/ch/systemsx/cisd/etlserver/registrator/simple-transaction.py")

