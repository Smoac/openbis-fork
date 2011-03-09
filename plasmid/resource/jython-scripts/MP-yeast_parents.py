import re

""""space that all parents come from (fixed)"""
SPACE = "YEAST_LAB"

ATR_CODE = "code"

CONNECTION_LABEL = "connection"
LINK_LABEL = "link"
CODE_LABEL = "code"
RELATIONSHIP_LABEL = "relationship"
ANNOTATION_LABEL = "annotation"

def _createSampleLink(code):
    permId = entityInformationProvider().getSamplePermId(SPACE, code)
    if not permId:
        permId = code
    sampleLink = elementFactory().createSampleLink(permId)
    sampleLink.addAttribute(ATR_CODE, code)
    return sampleLink    

"""
Example input:

FRY1, FRY2, FRY3, FRY4
"""
def updateFromBatchInput(bindings):
    input = bindings.get('')
    samples = input.split(',')
    elements = []
    for code in samples:
        sampleLink = _createSampleLink(code.strip())
        elements.append(sampleLink)
    property.value = propertyConverter().convertToString(elements)

def configureUI():
    """create table builder and add columns"""
    tableBuilder = createTableBuilder()
    tableBuilder.addHeader(LINK_LABEL)
    tableBuilder.addHeader(CODE_LABEL)

    """The property value should contain XML with list of samples. Add a new row for every sample."""
    elements = list(propertyConverter().convertToElements(property))
    for plasmid in elements:
        code = plasmid.getAttribute(ATR_CODE, "")
   
        row = tableBuilder.addRow()
        row.setCell(LINK_LABEL, plasmid)
        row.setCell(CODE_LABEL, code)
        
    """specify that the property should be shown in a tab and set the table output"""
    property.setOwnTab(True)
    uiDesc = property.getUiDescription()
    uiDesc.useTableOutput(tableBuilder.getTableModel())