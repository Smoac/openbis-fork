from ch.systemsx.cisd.openbis.dss.generic.shared import ServiceProvider

def process(transaction):
    incoming_path = transaction.getIncoming()
    executeOperation(incoming_path.name)
    
    expid = "/DEFAULT/DEFAULT/ARCHIVING"
    exp = transaction.getExperiment(expid)
     
    if None == exp:
        exp = transaction.createNewExperiment(expid, "UNKNOWN")
        exp.setPropertyValue("DESCRIPTION", 'Archiving')
   
    dataSet = transaction.createNewDataSet()
          
    dataSet.setDataSetType("UNKNOWN")
    dataSet.setExperiment(exp)
    
    transaction.moveFile(incoming_path.getAbsolutePath(), dataSet)
    
def executeOperation(file_name):
    parts = file_name.split()
    data_set_codes = parts[1:]
    service = ServiceProvider.getOpenBISService()
    
    if parts[0] == 'archive':
        service.archiveDataSets(data_set_codes, True)
    elif parts[0] == 'addToArchive':
        service.archiveDataSets(data_set_codes, False)
    elif parts[0] == 'unarchive':
        service.unarchiveDataSets(data_set_codes)
