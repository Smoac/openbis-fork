""" 
Managed Property Script for handling CHEMICALS.


"""

import re

""""space that all parents come from (fixed)"""
SPACE = "YEAST_LAB"

"""code attribute name"""
ATR_CODE = "code"
ATR_CONC = "concentration"
ATR_NAME="name"

"""labels of table columns"""
LINK_LABEL = "link"
CODE_LABEL = "code"
CONC_LABEL = "concentration"
NAME_LABEL="name"

"""action labels"""

ADD_ACTION_LABEL = "Add"
EDIT_ACTION_LABEL = "Edit"
DELETE_ACTION_LABEL = "Delete"

"""helper functions"""

def _createSampleLink(chemicals_list, concentration_list):
    """
       Creates sample link XML element for sample with specified 'code'. The element will contain
       given code as 'code' attribute apart from standard 'permId' attribute.
       
       If the sample doesn't exist in DB a fake link will be created with the 'code' as permId.
       
       @return: sample link XML element as string, e.g.:
       - '<Sample code="FRP1" permId="20110309154532868-4219"/>'
       - '<Sample code="FAKE_SAMPLE_CODE" permId="FAKE_SAMPLE_CODE"/>
    """
    chemicalPath= "/YEAST_LAB/" + chemicals_list
    permId =entityInformationProvider().getSamplePermId(SPACE, chemicals_list)
    if not permId:
        permId = chemicals_list
    name  = entityInformationProvider().getSamplePropertyValue(permId, 'NAME')    
    sampleLink = elementFactory().createSampleLink(permId)
    
    sampleLink.addAttribute(ATR_CODE, chemicals_list)
    sampleLink.addAttribute(ATR_NAME, name) 
    sampleLink.addAttribute(ATR_CONC, concentration_list)

    return sampleLink    


"""
Example input:

FRC1, FRC2, FRC3, FRC4
"""

def configureUI():
    """Create table builder and add columns."""
    tableBuilder = createTableBuilder()
    tableBuilder.addHeader(LINK_LABEL)
    tableBuilder.addHeader(CODE_LABEL)
    tableBuilder.addHeader(CONC_LABEL)
    tableBuilder.addHeader(NAME_LABEL)

    """The property value should contain XML with list of samples. Add a new row for every sample."""
    elements = list(propertyConverter().convertToElements(property))
    for chemical in elements:
        chemicals_list = chemical.getAttribute(ATR_CODE, "")
        name = chemical.getAttribute(ATR_NAME,"")
        concentration_list=chemical.getAttribute(ATR_CONC, "")

        
        row = tableBuilder.addRow()
        row.setCell(LINK_LABEL, chemical, chemicals_list)
        row.setCell(CODE_LABEL, chemicals_list)
        row.setCell(NAME_LABEL, name)
        row.setCell(CONC_LABEL, concentration_list)
        
    """Specify that the property should be shown in a tab and set the table output."""
    property.setOwnTab(True)
    uiDescription = property.getUiDescription()
    uiDescription.useTableOutput(tableBuilder.getTableModel())
    
    """
       Define and add actions with input fields used to:
       1. specify attributes of new chemical,
    """
    addAction = uiDescription.addTableAction(ADD_ACTION_LABEL)\
                             .setDescription('Add new chemical relationship:')
    widgets = [
        inputWidgetFactory().createTextInputField(CODE_LABEL)\
                            .setMandatory(True)\
                            .setValue('FRC')\
                            .setDescription('Code of chemical, e.g. "FRC1"'),
        inputWidgetFactory().createTextInputField(CONC_LABEL)\
                            .setMandatory(True)\
                            .setDescription('Concentration')
    ]
    addAction.addInputWidgets(widgets)
      
    """
       2. modify attributes of a selected yeast parent,
    """
    editAction = uiDescription.addTableAction(EDIT_ACTION_LABEL)\
                              .setDescription('Edit selected chemical relationship:')
    # Exactly 1 row needs to be selected to enable action.
    editAction.setRowSelectionRequiredSingle()            
    widgets = [
        inputWidgetFactory().createTextInputField(CODE_LABEL)\
                            .setMandatory(True)\
                            .setDescription('Code of chemical sample, e.g. "FRC1"'),
        inputWidgetFactory().createTextInputField(CONC_LABEL)\
                            .setMandatory(True)\
                            .setDescription('Concentration of the chemical sample, e.g. "1M"'),
    ]
    editAction.addInputWidgets(widgets)  
  # Bind field name with column name.
    editAction.addBinding(CODE_LABEL, CODE_LABEL)
    editAction.addBinding(CONC_LABEL, CONC_LABEL) 


  
    """
       3. delete selected yeast parents.
    """
    deleteAction = uiDescription.addTableAction(DELETE_ACTION_LABEL)\
                                .setDescription('Are you sure you want to delete the selected chemical relationships?')
    # Delete is enabled when at least 1 row is selected.
    deleteAction.setRowSelectionRequired()
    
    
def updateFromUI(action):
    """Extract list of elements from old value of the property."""
    converter = propertyConverter()
    elements = list(converter.convertToElements(property))
  
    """Implement behaviour of user actions."""
    if action.name == ADD_ACTION_LABEL:
        """
           For 'add' action create new chemical element with values from input fields
           and add it to existing elements.
        """
        chemicals_list = action.getInputValue(CODE_LABEL)
        concentration_list = action.getInputValue(CONC_LABEL)
        sampleLink = _createSampleLink(chemicals_list, concentration_list)
        
        elements.append(sampleLink)
    elif action.name == EDIT_ACTION_LABEL:
        """
           For 'edit' action find the chemical element corresponding to selected row
           and replace it with an element with values from input fields.
        """
        chemicals_list = action.getInputValue(CODE_LABEL)
        concentration_list = action.getInputValue(CONC_LABEL)

        

        sampleLink = _createSampleLink(chemicals_list, concentration_list)
        
        

        selectedRowId = action.getSelectedRows()[0]
        elements[selectedRowId] = sampleLink
    elif action.name == DELETE_ACTION_LABEL:
        """
           For 'delete' action delete chemicals that correspond to selected rows.
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
    
def updateFromBatchInput(bindings):
    elements = []
    input = bindings.get('')
    if input is not None:
        chemicals = input.split(',')
        for chemical in chemicals:
            (code, concentration) = _extractCodeAndConcentration(chemical)
            sampleLink = _createSampleLink(code, concentration)
            elements.append(sampleLink)
            
    property.value = propertyConverter().convertToString(elements)
    
def _extractCodeAndConcentration(chemical):
    codeAndConcentration = chemical.split(':')
    if (len(codeAndConcentration) == 2):
        return (codeAndConcentration[0].strip(), codeAndConcentration[1].strip())
    else:
        return (codeAndConcentration[0].strip(), "n.a.")
