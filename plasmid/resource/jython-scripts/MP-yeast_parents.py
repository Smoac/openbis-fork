""" 
Managed Property Script for handling yeast parents of yeast samples.

@author: Piotr Buczek
"""

import re

""""space that all parents come from (fixed)"""
SPACE = "YEAST_LAB"

"""code attribute name"""
ATR_CODE = "code"

"""labels of table columns"""
LINK_LABEL = "link"
CODE_LABEL = "code"


def _createSampleLink(code):
    """
       Creates sample link XML element for sample with specified 'code'. The element will contain
       given code as 'code' attribute apart from standard 'permId' attribute.
       
       If the sample doesn't exist in DB a fake link will be created with the 'code' as permId.
       
       @return: sample link XML element as string, e.g.:
       - '<Sample code="FRP1" permId="20110309154532868-4219"/>'
       - '<Sample code="FAKE_SAMPLE_CODE" permId="FAKE_SAMPLE_CODE"/>
    """
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
    
    """Create table builder and add columns."""
    tableBuilder = createTableBuilder()
    tableBuilder.addHeader(LINK_LABEL)
    tableBuilder.addHeader(CODE_LABEL, true)

    """The property value should contain XML with list of samples. Add a new row for every sample."""
    elements = list(propertyConverter().convertToElements(property))
    for plasmid in elements:
        code = plasmid.getAttribute(ATR_CODE, "")
   
        row = tableBuilder.addRow()
        row.setCell(LINK_LABEL, plasmid, code)
        row.setCell(CODE_LABEL, code)
        
    """Specify that the property should be shown in a tab and set the table output."""
    property.setOwnTab(True)
    uiDesc = property.getUiDescription()
    uiDesc.useTableOutput(tableBuilder.getTableModel())