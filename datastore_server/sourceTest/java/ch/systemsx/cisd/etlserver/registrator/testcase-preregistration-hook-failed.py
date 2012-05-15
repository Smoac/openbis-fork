from java.lang import IllegalArgumentException

execfile("sourceTest/java/ch/systemsx/cisd/etlserver/registrator/all-hooks.py")

def pre_metadata_registration(context):
    global didPreRegistrationFunctionRunHappen
    didPreRegistrationFunctionRunHappen = True
    raise IllegalArgumentException("Fail at pre_metadata_registration to cancel registration")

execfile("sourceTest/java/ch/systemsx/cisd/etlserver/registrator/simple-transaction.py")

