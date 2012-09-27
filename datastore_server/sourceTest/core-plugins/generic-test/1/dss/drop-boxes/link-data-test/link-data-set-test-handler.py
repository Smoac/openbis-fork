def process(transaction):
    linkds = transaction.createNewDataSet("LINK_TYPE")
    linkds.setExperiment(transaction.getExperiment("/CISD/NEMO/EXP1"))
    linkds.setExternalCode("EX_CODE")
    linkds.setExternalDataManagementSystem(transaction.getExternalDataManagementSystem("DMS_1"))

