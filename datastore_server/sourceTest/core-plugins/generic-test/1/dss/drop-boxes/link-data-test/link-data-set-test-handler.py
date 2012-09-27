def process(transaction):
    linkds = transaction.createNewDataSet("LINK_TYPE")
    linkds.setExperiment(transaction.getExperiment("/CISD/NEMO/EXP1"))
    linkds.setExternalCode("EX_CODE")
    externalDMS = transaction.getExternalDataManagementSystem("DMS_1")
    if (externalDMS is None):
        raise "External data management system with code DMS_1, has not been found in the database"
    linkds.setExternalDataManagementSystem(externalDMS)

