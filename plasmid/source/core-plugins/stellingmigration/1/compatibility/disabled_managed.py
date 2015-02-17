def configureUI():
    tableBuilder = createTableBuilder()
    
    ##
    ## Headers
    ##
    tableBuilder.addHeader("message")
    usedTableHeaders = {"message" : True}
    samples = list(propertyConverter().convertToElements(property))
    for sample in samples:
        for annotation in sample.getAttributes():
            if annotation not in usedTableHeaders:
                tableBuilder.addHeader(annotation)
                usedTableHeaders[annotation] = True
    
    property.setOwnTab(True)
    uiDescription = property.getUiDescription()
    uiDescription.useTableOutput(tableBuilder.getTableModel())
    
    ##
    ## Data
    ## 
    row = tableBuilder.addRow()
    row.setCell("message", "Please use the new managed properties annotations, legacy functionality is disabled.")
    
    for sample in samples:
        row = tableBuilder.addRow()
        for annotation in sample.getAttributes():
            row.setCell(annotation, sample.getAttribute(annotation))
    

def updateFromUI(action):
    raise Exception("Please use the new managed properties annotations, legacy functionality is disabled.")

##
## Batch Import Methods
##
def batchColumnNames():
    allTypes = []
    return allTypes

def updateFromBatchInput(bindings):
    elements = []
    input = bindings.get('')
    if input is not None:
        raise Exception("Please use the new managed properties annotations, legacy functionality is disabled.")