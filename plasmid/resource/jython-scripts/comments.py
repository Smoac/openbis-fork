from java.util import Date

"""
Example XML property value handled by this script:
<root>
  <commentEntry date="2011-02-20 14:15:28 GMT+01:00" person="buczekp">Here is the 1st  entry text.<commentEntry>
  <commentEntry date="2011-02-20 14:16:28 GMT+01:00" person="kohleman">Here is the 2nd  entry text - a warning!<commentEntry>
  <commentEntry date="2011-02-20 14:17:28 GMT+01:00" person="tpylak">Here is the 3rd  entry text - an error!!!<commentEntry>
  <commentEntry date="2011-02-20 14:18:28 GMT+01:00" person="brinn">Here is the 4th  entry text - an error!!!<commentEntry>
  <commentEntry date="2011-02-20 14:19:28 GMT+01:00" person="felmer">Here is the 5th  entry text - a warning!<commentEntry>
</root>
"""
  
COMMENT_ENTRY_ELEMENT_LABEL = 'commentEntry'

""" labels of table columns and corresponding input fields """
DATE_LABEL = 'Date'
PERSON_LABEL = 'Person'
COMMENT_TEXT_LABEL = 'Comment Text'
  
""" names of attributes of XML elements for comment entries """
DATE_ATTRIBUTE = 'date'
PERSON_ATTRIBUTE = 'person'
  
""" action labels (shown as button labels in UI) """
ADD_ACTION_LABEL = 'Add Comment Entry'
EDIT_ACTION_LABEL = 'Edit'
DELETE_ACTION_LABEL = 'Delete'
  
  
def configureUI():
    """Create table builder and add headers of columns."""
    builder = createTableBuilder()
    builder.addHeader(DATE_LABEL, 250) # date and comment text values are long, override default width (150)
    builder.addHeader(PERSON_LABEL)
    builder.addHeader(COMMENT_TEXT_LABEL, 400)
      
    """
       Extract XML elements from property value to a Python list.
       For each element (comment entry) add add a row to the table.  
    """
    elements = list(propertyConverter().convertToElements(property))
    for commentEntry in elements:
        row = builder.addRow()
        row.setCell(DATE_LABEL, Date(long(commentEntry.getAttribute(DATE_ATTRIBUTE))))
        row.setCell(PERSON_LABEL, commentEntry.getAttribute(PERSON_ATTRIBUTE))
        row.setCell(COMMENT_TEXT_LABEL, commentEntry.getData())
  
    """Specify that the property should be shown in a tab and set the table output."""
    property.setOwnTab(True)
    uiDescription = property.getUiDescription()
    uiDescription.useTableOutput(builder.getTableModel())
  
    """
       Define and add actions with input fields used to:
       1. specify attributes of new comment entry,
    """
    addAction = uiDescription.addTableAction(ADD_ACTION_LABEL)\
                             .setDescription('Add a new comment entry:')
    widgets = [
        inputWidgetFactory().createMultilineTextInputField(COMMENT_TEXT_LABEL)\
                            .setMandatory(True)
    ]
    addAction.addInputWidgets(widgets)
      
    """
       2. modify attributes of a selected comment entry,
    """
    editAction = uiDescription.addTableAction(EDIT_ACTION_LABEL)\
                              .setDescription('Edit selected comment entry:')
    # Exactly 1 row needs to be selected to enable action.
    editAction.setRowSelectionRequiredSingle()            
    widgets = [
        inputWidgetFactory().createMultilineTextInputField(COMMENT_TEXT_LABEL).setMandatory(True)
    ]
    editAction.addInputWidgets(widgets)
    # Bind field name with column name.
    editAction.addBinding(COMMENT_TEXT_LABEL, COMMENT_TEXT_LABEL)
  
    """
       3. delete selected comment entries.
    """
    deleteAction = uiDescription.addTableAction(DELETE_ACTION_LABEL)\
                                .setDescription('Are you sure you want to delete selected comment entry?')
    # Delete is enabled when at least 1 row is selected.
    deleteAction.setRowSelectionRequired()
    
  
def updateFromUI(action):
    """Extract list of elements from old value of the property."""
    converter = propertyConverter()
    elements = list(converter.convertToElements(property))
  
    """Implement behaviour of user actions."""
    if action.name == ADD_ACTION_LABEL:
        """
           For 'add' action create new comment entry element with values from input fields
           and add it to existing elements.
        """
        element = elementFactory().createElement(COMMENT_ENTRY_ELEMENT_LABEL)
        """Fill element attributes with appropriate values."""
        element.addAttribute(DATE_ATTRIBUTE, str(Date().getTime()))            # current date
        element.addAttribute(PERSON_ATTRIBUTE, action.getPerson().getUserId()) # invoker the action
        """Retrieve values from input fields filled by user on the client side."""

        """Set comment text as a text element, not an attribute."""
        element.setData(action.getInputValue(COMMENT_TEXT_LABEL))
        """Add the new entry to the end of the element list."""
        elements.append(element)
    elif action.name == EDIT_ACTION_LABEL:
        """
           For 'edit' action find the comment entry element corresponding to selected row
           and replace it with an element with values from input fields.
        """
        
        selectedRowId = action.getSelectedRows()[0]
        xmlUser = elements[selectedRowId].getAttribute(PERSON_ATTRIBUTE)
        if action.getPerson().getUserId() == xmlUser:
          elements[selectedRowId].setData(action.getInputValue(COMMENT_TEXT_LABEL))
        else:
          raise ValidationException('Comment creator and current user differ:\n' + str(xmlUser) + "!=" + str(action.getPerson().getUserId()) )
    elif action.name == DELETE_ACTION_LABEL:
        """
           For 'delete' action delete the entries that correspond to selected rows.
           NOTE: As many rows can be deleted at once it is easier to delete them in reversed order.
        """
        rowIds = list(action.getSelectedRows())
        rowIds.reverse()       
        for rowId in rowIds:
              xmlUser = elements[rowId].getAttribute(PERSON_ATTRIBUTE)
              if action.getPerson().getUserId() == xmlUser:
                elements.pop(rowId)
              else:
                raise ValidationException('Comment creator and current user differ:\n' +  str(xmlUser) + "!=" + str(action.getPerson().getUserId()) )
    else:
        raise ValidationException('action not supported')
      
    """Update value of the managed property to XML string created from modified list of elements."""
    property.value = converter.convertToString(elements)

def _createCommentEntry(comment_text_list):
    #if comment_text_list is not None:
    commentEntry = elementFactory().createElement(COMMENT_ENTRY_ELEMENT_LABEL)
                     
    commentEntry.addAttribute(PERSON_ATTRIBUTE, person.getUserId())
    commentEntry.addAttribute(DATE_ATTRIBUTE,str(Date().getTime()))
    commentEntry.setData(comment_text_list)
    return commentEntry   

def showRawValueInForms():
    return False
 
def batchColumnNames():
    return [COMMENT_ENTRY_ELEMENT_LABEL]
 
def updateFromRegistrationForm(bindings):
    elements = []
    for item in bindings:
        comment_text_list = item.get('COMMENTENTRY')
        sampleLink = _createCommentEntry(comment_text_list)
        elements.append(sampleLink)
            
    property.value = propertyConverter().convertToString(elements)



        
def updateFromBatchInput(bindings):
    elements = []
    input = bindings.get('')
    if input is not None:
        commentEntry = _createCommentEntry(input)
        elements.append(commentEntry)
        property.value = propertyConverter().convertToString(elements)
