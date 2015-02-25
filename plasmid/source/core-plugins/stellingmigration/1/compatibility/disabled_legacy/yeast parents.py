""" 
Managed Property Script for handling YEAST parents of YEAST samples.

@author: Piotr Buczek
"""

import re

""""space that all parents come from (fixed)"""
SPACE = "YEAST_LAB"

"""plasmid_list attribute name"""
ATR_CODE = "code"

"""labels of table columns"""
LINK_LABEL = "link"
CODE_LABEL = "code"

"""action labels"""

ADD_ACTION_LABEL = "Add"
EDIT_ACTION_LABEL = "Edit"
DELETE_ACTION_LABEL = "Delete"

"""helper functions"""

def _createSampleLink(plasmid_list):
    """
       Creates sample link XML element for sample with specified 'plasmid_list'. The element will contain
       given plasmid_list as 'plasmid_list' attribute apart from standard 'permId' attribute.
       
       If the sample doesn't exist in DB a fake link will be created with the 'plasmid_list' as permId.
       
       @return: sample link XML element as string, e.g.:
       - '<Sample plasmid_list="FRP1" permId="20110309154532868-4219"/>'
       - '<Sample plasmid_list="FAKE_SAMPLE_CODE" permId="FAKE_SAMPLE_CODE"/>
    """
    plasmidPath= "/YEAST_LAB/" + plasmid_list
    permId = entityInformationProvider().getSamplePermId(SPACE, plasmid_list)
    if not permId:
        permId = plasmid_list
    sampleLink = elementFactory().createSampleLink(permId)
    
    sampleLink.addAttribute(ATR_CODE, plasmid_list)
    
    return sampleLink    


"""
Example input:

FRY1, FRY2, FRY3, FRY4
"""

#def showRawValueInForms():
 #   return False
 
#def batchColumnNames():
#    return [CODE_LABEL]

#def updateFromRegistrationForm(bindings):
#    elements = []
#    for item in bindings:
#        plasmid_list = item.get('CODE')
#        sampleLink = _createSampleLink(plasmid_list)
 #       elements.append(sampleLink)
            
#    property.value = propertyConverter().convertToString(elements)

 



def updateFromBatchInput(bindings):
    elements = []
    input = bindings.get('')
    if input is not None:
        samples = input.split(',')
        for plasmid_list in samples:
            sampleLink = _createSampleLink(plasmid_list.strip())
            elements.append(sampleLink)
    property.value = propertyConverter().convertToString(elements)


def configureUI():
    """Create table builder and add columns."""
    tableBuilder = createTableBuilder()
    tableBuilder.addHeader(LINK_LABEL)
    tableBuilder.addHeader(CODE_LABEL)

    """The property value should contain XML with list of samples. Add a new row for every sample."""
    elements = list(propertyConverter().convertToElements(property))
    for plasmid in elements:
        plasmid_list = plasmid.getAttribute(ATR_CODE, "")
   
        row = tableBuilder.addRow()
        row.setCell(LINK_LABEL, plasmid, plasmid_list)
        row.setCell(CODE_LABEL, plasmid_list)
        
    """Specify that the property should be shown in a tab and set the table output."""
    property.setOwnTab(True)
    uiDescription = property.getUiDescription()
    uiDescription.useTableOutput(tableBuilder.getTableModel())
    
    """
       Define and add actions with input fields used to:
       1. specify attributes of new yeast parent,
    """
    addAction = uiDescription.addTableAction(ADD_ACTION_LABEL)\
                             .setDescription('Add new plasmid relationship:')
    widgets = [
        inputWidgetFactory().createTextInputField(CODE_LABEL)\
                            .setMandatory(True)\
                            .setValue('FRY')\
                            .setDescription('Code of yeast parent sample, e.g. "FRY1"')
    ]
    addAction.addInputWidgets(widgets)
      
    """
       2. modify attributes of a selected yeast parent,
    """
    editAction = uiDescription.addTableAction(EDIT_ACTION_LABEL)\
                              .setDescription('Edit selected plasmid relationship:')
    # Exactly 1 row needs to be selected to enable action.
    editAction.setRowSelectionRequiredSingle()            
    widgets = [
        inputWidgetFactory().createTextInputField(CODE_LABEL)\
                            .setMandatory(True)\
                            .setDescription('Code of yeast parent sample, e.g. "FRY1"')
    ]
    editAction.addInputWidgets(widgets)
    # Bind field name with column name.
    editAction.addBinding(CODE_LABEL, CODE_LABEL)
  
    """
       3. delete selected yeast parents.
    """
    deleteAction = uiDescription.addTableAction(DELETE_ACTION_LABEL)\
                                .setDescription('Are you sure you want to delete selected yeast parent relationships?')
    # Delete is enabled when at least 1 row is selected.
    deleteAction.setRowSelectionRequired()
    
    
def updateFromUI(action):
    """Extract list of elements from old value of the property."""
    converter = propertyConverter()
    elements = list(converter.convertToElements(property))
  
    """Implement behaviour of user actions."""
    if action.name == ADD_ACTION_LABEL:
        """
           For 'add' action create new yeast parent element with values from input fields
           and add it to existing elements.
        """
        plasmid_list = action.getInputValue(CODE_LABEL)
        sampleLink = _createSampleLink(plasmid_list)
        
        elements.append(sampleLink)
    elif action.name == EDIT_ACTION_LABEL:
        """
           For 'edit' action find the yeast parent element corresponding to selected row
           and replace it with an element with values from input fields.
        """
        plasmid_list = action.getInputValue(CODE_LABEL)
        sampleLink = _createSampleLink(plasmid_list)
        
        selectedRowId = action.getSelectedRows()[0]
        elements[selectedRowId] = sampleLink
    elif action.name == DELETE_ACTION_LABEL:
        """
           For 'delete' action delete yeast parents that correspond to selected rows.
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