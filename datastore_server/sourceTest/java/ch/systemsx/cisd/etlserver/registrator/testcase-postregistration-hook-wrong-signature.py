execfile("sourceTest/java/ch/systemsx/cisd/etlserver/registrator/all-hooks.py")

def post_metadata_registration(context, unnecessary_argument):
    global didPostRegistrationFunctionRunHappen
    didPostRegistrationFunctionRunHappen = True

execfile("sourceTest/java/ch/systemsx/cisd/etlserver/registrator/simple-transaction.py")

