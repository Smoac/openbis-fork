execfile("sourceTest/java/ch/systemsx/cisd/etlserver/registrator/all-hooks.py")

def process():
    mat = transaction.createNewMaterial("JTDH2_TEST", "GENE")