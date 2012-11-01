""" 
Managed Property Script for handling General Protocols.


"""

import re

""""space that all parents come from (fixed)"""
SPACE = "YEAST_LAB"

"""code attribute name"""
ATR_CODE = "code"
ATR_NAME = "name"

"""labels of table columns"""
LINK_LABEL = "link"
CODE_LABEL = "code"
NAME_LABEL = "name"

"""action labels"""

ADD_ACTION_LABEL = "Add"
EDIT_ACTION_LABEL = "Edit"
DELETE_ACTION_LABEL = "Delete"

"""helper functions"""

def _createSampleLink(protocols_list):
    """
       Creates sample link XML element for sample with specified 'code'. The element will contain
       given code as 'code' attribute apart from standard 'permId' attribute.
       
       If the sample doesn't exist in DB a fake link will be created with the 'code' as permId.
       
       @return: sample link XML element as string, e.g.:
       - '<Sample code="FRP1" permId="20110309154532868-4219"/>'
       - '<Sample code="FAKE_SAMPLE_CODE" permId="FAKE_SAMPLE_CODE"/>
    """
    protocolPath= "/YLAB-TEST/" + protocols_list
    permId =entityInformationProvider().getSamplePermId(SPACE, protocols_list)
    if not permId:
        permId = protocols_list
    name  = entityInformationProvider().getSamplePropertyValue('NAME')
    print "the name is", name
    sampleLink = elementFactory().createSampleLink(permId)
    
    sampleLink.addAttribute(ATR_CODE, protocols_list)
    sampleLink.addAttribute(ATR_NAME, name) 
    
    return sampleLink    


"""
Example input:

FRC1, FRC2, FRC3, FRC4
"""


def updateBufferFromBatchInput(protocols_list):
    elements = []
    input = protocols_list
    if input is not None:
       for i in protocols_list: 
            sampleLink = _createSampleLink(i.strip())
            elements.append(sampleLink)
    return propertyConverter.convertToString(elements)




def configureUI():
    """Create table builder and add columns."""
    tableBuilder = createTableBuilder()
    tableBuilder.addHeader(LINK_LABEL)
    tableBuilder.addHeader(CODE_LABEL)
    tableBuilder.addHeader(NAME_LABEL) 
 

    """The property value should contain XML with list of samples. Add a new row for every sample."""
    elements = list(propertyConverter().convertToElements(property))
    for protocol in elements:
        protocols_list = protocol.getAttribute(ATR_CODE, "")
        name = protocol.getAttribute(ATR_NAME,"") 
   
        
        row = tableBuilder.addRow()
        row.setCell(LINK_LABEL, protocol, protocols_list)
        row.setCell(CODE_LABEL, protocols_list)
    row.setCell(NAME_LABEL, name)
 
        
    """Specify that the property should be shown in a tab and set the table output."""
    property.setOwnTab(True)
    uiDescription = property.getUiDescription()
    uiDescription.useTableOutput(tableBuilder.getTableModel())
    
    """
       Define and add actions with input fields used to:
       1. specify attributes of new protocol,
    """
    addAction = uiDescription.addTableAction(ADD_ACTION_LABEL)\
                             .setDescription('Add new protocol relationship:')
    widgets = [
        inputWidgetFactory().createTextInputField(CODE_LABEL)\
                            .setMandatory(True)\
                            .setValue('FRC')\
                            .setDescription('Code of protocol, e.g. "FRC1"'),
       
    ]
    addAction.addInputWidgets(widgets)
      
    """
       2. modify attributes of a selected yeast parent,
    """
    editAction = uiDescription.addTableAction(EDIT_ACTION_LABEL)\
                              .setDescription('Edit selected protocol relationship:')
    # Exactly 1 row needs to be selected to enable action.
    editAction.setRowSelectionRequiredSingle()            
    widgets = [
        inputWidgetFactory().createTextInputField(CODE_LABEL)\
                            .setMandatory(True)\
                            .setDescription('Code of protocol sample, e.g. "FRC1"'),
       
    ]
    editAction.addInputWidgets(widgets)  
  # Bind field name with column name.
    editAction.addBinding(CODE_LABEL, CODE_LABEL)


  
    """
       3. delete selected yeast parents.
    """
    deleteAction = uiDescription.addTableAction(DELETE_ACTION_LABEL)\
                                .setDescription('Are you sure you want to delete the selected protocol relationships?')
    # Delete is enabled when at least 1 row is selected.
    deleteAction.setRowSelectionRequired()
    
    
def updateFromUI(action):
    """Extract list of elements from old value of the property."""
    converter = propertyConverter()
    elements = list(converter.convertToElements(property))
  
    """Implement behaviour of user actions."""
    if action.name == ADD_ACTION_LABEL:
        """
           For 'add' action create new protocol element with values from input fields
           and add it to existing elements.
        """
        protocols_list = action.getInputValue(CODE_LABEL)
        sampleLink = _createSampleLink(protocols_list)
        
        elements.append(sampleLink)
    elif action.name == EDIT_ACTION_LABEL:
        """
           For 'edit' action find the protocol element corresponding to selected row
           and replace it with an element with values from input fields.
        """
        protocols_list = action.getInputValue(CODE_LABEL)

        

        sampleLink = _createSampleLink(protocols_list)
        
        

        selectedRowId = action.getSelectedRows()[0]
        elements[selectedRowId] = sampleLink
    elif action.name == DELETE_ACTION_LABEL:
        """
           For 'delete' action delete protocols that correspond to selected rows.
           NOTE: As many rows can be deleted at once it is easier to delete them in reversed order.
        """
        rowIds = list(action.getSelectedRows())
        rowIds.reverse()       
        for rowId in rowIds:
            elements.pop(rowId)      
    else:
        raise ValidationException('action not supported')
      
    """Update value of the managed property to XML string created from modified list of elements."""
    property.value = converter.convertToString(elements)    