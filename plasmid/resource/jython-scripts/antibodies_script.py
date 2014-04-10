""" 
Managed Property Script for handling General Antibodies.


"""

import re

""""space that all parents come from (fixed)"""
SPACE = "YEAST_LAB"

"""code attribute name"""
ATR_CODE = "code"
ATR_NAME = "name"
ATR_QUANTITY = "quantity"

"""labels of table columns"""
LINK_LABEL = "link"
CODE_LABEL = "code"
NAME_LABEL = "name"
QUANTITY_LABEL = "quantity"

"""action labels"""

ADD_ACTION_LABEL = "Add"
EDIT_ACTION_LABEL = "Edit"
DELETE_ACTION_LABEL = "Delete"

"""helper functions"""

def _createSampleLink(antibodies_list, quantity_list):
    """
       Creates sample link XML element for sample with specified 'code'. The element will contain
       given code as 'code' attribute apart from standard 'permId' attribute.
       
       If the sample doesn't exist in DB a fake link will be created with the 'code' as permId.
       
       @return: sample link XML element as string, e.g.:
       - '<Sample code="FRP1" permId="20110309154532868-4219"/>'
       - '<Sample code="FAKE_SAMPLE_CODE" permId="FAKE_SAMPLE_CODE"/>
    """
    antibodyPath= "/YEAST_LAB/" + antibodies_list
    permId =entityInformationProvider().getSamplePermId(SPACE, antibodies_list)
    if not permId:
        permId = antibodies_list
    name  = entityInformationProvider().getSamplePropertyValue(permId, 'NAME')
    print "the name is", name
    sampleLink = elementFactory().createSampleLink(permId)
    
    sampleLink.addAttribute(ATR_CODE, antibodies_list)
    sampleLink.addAttribute(ATR_NAME, name) 
    sampleLink.addAttribute(ATR_QUANTITY, quantity_list)
    
    return sampleLink    


"""
Example input:

FRC1, FRC2, FRC3, FRC4
"""


def showRawValueInForms():
    return False
 
def batchColumnNames():
    return [CODE_LABEL]
 
def updateFromRegistrationForm(bindings):
    elements = []
    for item in bindings:
        antibodies_list = item.get('CODE')
        quantity_list = item.get('QUANTITY')
    if antibodies_list:
          sampleLink = _createSampleLink(antibodies_list, quantity_list)
          elements.append(sampleLink)
            
    property.value = propertyConverter().convertToString(elements)




def configureUI():
    """Create table builder and add columns."""
    tableBuilder = createTableBuilder()
    tableBuilder.addHeader(LINK_LABEL)
    tableBuilder.addHeader(CODE_LABEL)
    tableBuilder.addHeader(NAME_LABEL)
    tableBuilder.addHeader(QUANTITY_LABEL) 
 

    """The property value should contain XML with list of samples. Add a new row for every sample."""
    elements = list(propertyConverter().convertToElements(property))
    for antibody in elements:
        antibodies_list = antibody.getAttribute(ATR_CODE, "")
        name = antibody.getAttribute(ATR_NAME,"") 
        quantity_list=antibody.getAttribute(ATR_QUANTITY, "")
   
        
        row = tableBuilder.addRow()
        row.setCell(LINK_LABEL, antibody, antibodies_list)
        row.setCell(CODE_LABEL, antibodies_list)
        row.setCell(NAME_LABEL, name)
        row.setCell(QUANTITY_LABEL, quantity_list)
        
 
        
    """Specify that the property should be shown in a tab and set the table output."""
    property.setOwnTab(True)
    uiDescription = property.getUiDescription()
    uiDescription.useTableOutput(tableBuilder.getTableModel())
    
    """
       Define and add actions with input fields used to:
       1. specify attributes of new antibody,
    """
    addAction = uiDescription.addTableAction(ADD_ACTION_LABEL)\
                             .setDescription('Add new antibody relationship:')
    widgets = [
        inputWidgetFactory().createTextInputField(CODE_LABEL)\
                            .setMandatory(True)\
                            .setValue('FRAB')\
                            .setDescription('Code of antibody, e.g. "FRAB1"'),
        inputWidgetFactory().createTextInputField(QUANTITY_LABEL)\
                            .setMandatory(True)\
                            .setDescription('Quantity')       
    ]
    addAction.addInputWidgets(widgets)
      
    """
       2. modify attributes of a selected yeast parent,
    """
    editAction = uiDescription.addTableAction(EDIT_ACTION_LABEL)\
                              .setDescription('Edit selected antibody relationship:')
    # Exactly 1 row needs to be selected to enable action.
    editAction.setRowSelectionRequiredSingle()            
    widgets = [
        inputWidgetFactory().createTextInputField(CODE_LABEL)\
                            .setMandatory(True)\
                            .setDescription('Code of antibody sample, e.g. "FRAB1"'),
        inputWidgetFactory().createTextInputField(QUANTITY_LABEL)\
                            .setMandatory(True)\
                            .setDescription('Quantity')       
    ]
    editAction.addInputWidgets(widgets)  
  # Bind field name with column name.
    editAction.addBinding(CODE_LABEL, CODE_LABEL)
    editAction.addBinding(QUANTITY_LABEL, QUANTITY_LABEL) 

  
    """
       3. delete selected yeast parents.
    """
    deleteAction = uiDescription.addTableAction(DELETE_ACTION_LABEL)\
                                .setDescription('Are you sure you want to delete the selected antibody relationships?')
    # Delete is enabled when at least 1 row is selected.
    deleteAction.setRowSelectionRequired()
    
    
def updateFromUI(action):
    """Extract list of elements from old value of the property."""
    converter = propertyConverter()
    elements = list(converter.convertToElements(property))
  
    """Implement behaviour of user actions."""
    if action.name == ADD_ACTION_LABEL:
        """
           For 'add' action create new antibody element with values from input fields
           and add it to existing elements.
        """
        antibodies_list = action.getInputValue(CODE_LABEL)
        quantity_list = action.getInputValue(QUANTITY_LABEL)
        sampleLink = _createSampleLink(antibodies_list, quantity_list)
        
        elements.append(sampleLink)
    elif action.name == EDIT_ACTION_LABEL:
        """
           For 'edit' action find the antibody element corresponding to selected row
           and replace it with an element with values from input fields.
        """
        antibodies_list = action.getInputValue(CODE_LABEL)
        quantity_list = action.getInputValue(QUANTITY_LABEL)
        

        sampleLink = _createSampleLink(antibodies_list, quantity_list)
        
        

        selectedRowId = action.getSelectedRows()[0]
        elements[selectedRowId] = sampleLink
    elif action.name == DELETE_ACTION_LABEL:
        """
           For 'delete' action delete antibodies that correspond to selected rows.
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

#def updateFromBatchInput(bindings):
#    elements = []
#    input = bindings.get('')
#    if input is not None:
#        commentEntry = _createCommentEntry(input)
#        elements.append(commentEntry)
#        property.value = propertyConverter().convertToString(elements)  
        
        
def updateFromBatchInput(bindings):
    elements = []
    input = bindings.get('')
    if input is not None:
        antibodies = input.split(',')
        for antibody in antibodies:
            (code, quantity) = _extractCodeAndQuantity(antibody)
            commentEntry = _createCommentEntry(input)
            sampleLink = _createSampleLink(code, quantity)
            elements.append(sampleLink, commentEntry)
            
    property.value = propertyConverter().convertToString(elements)
    
def _extractCodeAndQuantity(antibody):
    codeAndQuantity = chemical.split(':')
    if (len(codeAndQuantity) == 2):
        return (codeAndQuantity[0].strip(), codeAndQuantity[1].strip())
    else:
        return (codeAndQuantity[0].strip(), "n.a.")
