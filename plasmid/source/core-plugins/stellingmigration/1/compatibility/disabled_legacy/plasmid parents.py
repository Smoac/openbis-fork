""" 
Managed Property Script for handling PLASMID parents of YEAST samples.

@author: Piotr Buczek
"""

import re


print "###################"
"""space that all parents come from (fixed)"""
SPACE = "YEAST_LAB"

"""input pattern matching one plasmid, e.g.: 
- 'FRP1 (DEL:URA3)', 
- 'FRP2 (INT)', 
- 'FRP3(MOD:URA3)', 
- 'FRP4'
"""
INPUT_PATTERN = """
                 # no '^': allow whitespace at the beginning
    ([^ (]*)     # 1st group: match code of a sample, everything before a space or '(' (e.g. 'FRP')
    (\ *\(       # start of 2nd group (matches an optional relationship type with annotation) 
                 # any spaces followed by a '('
    ([^:]*)      # 3rd group: match relationship type, any character but ':' (e.g. 'DEL', 'INT', 'MOD')
    :?           # optional ':' separator
    (.*)         # 4th group: match annotation, any text (e.g. 'URA3')
    \))?         # end of 2nd (optional) group: closing bracket of relationship details
                 # no '$': allow whitespace at the end
"""

""" due to some weird jython threading issue, we need to compile the pattern outside the function body """
inputPattern = re.compile(INPUT_PATTERN, re.VERBOSE)

"""relationship types shortcuts"""

DEL_REL_TYPE = 'DEL'
INT_REL_TYPE = 'INT'
MOD_REL_TYPE = 'MOD'

"""tuple of supported relationship types as shortcuts"""
REL_TYPES = (DEL_REL_TYPE, INT_REL_TYPE, MOD_REL_TYPE)
"""dictionary from relationship type shortcut to its 'character' representation"""
REL_TYPE_CHARS = {
    DEL_REL_TYPE: u'\u0394', # unicode '∆'
    INT_REL_TYPE: '::', 
    MOD_REL_TYPE: '_' 
}
"""dictionary from relationship type shortcut to its full name/label"""
REL_TYPE_LABELS = {
    DEL_REL_TYPE: 'deletion', 
    INT_REL_TYPE: 'integration', 
    MOD_REL_TYPE: 'modification' 
}

REL_TYPE_LABEL_OTHER = '(other)'
REL_TYPE_LABELS_WITH_NONE = tuple([REL_TYPE_LABEL_OTHER] + REL_TYPE_LABELS.values())

"""names of additional sample XML element attributes"""

ATR_CODE = "code"
ATR_RELATIONSHIP = "rel"
ATR_ANNOTATION = "annotation"

"""labels of table columns"""

CONNECTION_LABEL = "connection"
LINK_LABEL = "link"
CODE_LABEL = "code"
RELATIONSHIP_LABEL = "relationship"
ANNOTATION_LABEL = "annotation"

"""action labels"""

ADD_ACTION_LABEL = "Add"
EDIT_ACTION_LABEL = "Edit"
DELETE_ACTION_LABEL = "Delete"

"""helper functions"""

def _group(pattern, input):
    """@return: groups returned by performing pattern search with given @pattern on given @input"""
    return pattern.search(input).groups()


def _translateToChar(relationship):
    """
       @param relationship: relationship type as a shortcut (@see REL_TYPES), may be null
       @return: character representation of given @relationship, 
                empty string for null
                '[<relationship>]' for unknown relationship
    """
    if relationship:
        if relationship in REL_TYPE_CHARS:
            return REL_TYPE_CHARS[relationship]
        else:
            return "[" + relationship + "]"
    else:
        return ""
    
    
def _translateToLabel(relationship):
    """
       @param relationship: relationship type as a shortcut (@see REL_TYPES), may be null
       @return: full name of given @relationship, 
                empty string for null, 
                '[<relationship>]' for unknown relationship
    """
    if relationship:
        if relationship in REL_TYPE_LABELS:
            return REL_TYPE_LABELS[relationship]
        else:
            return "[" + relationship + "]"
    else:
        return REL_TYPE_LABEL_OTHER    

def _translateFromLabel(relationshipLabel):
    """
       @param relationshipLabel: relationship type as label (@see REL_TYPE_LABELS_WITH_NONE)
       @return: type of given @relationshipLabel, None for REL_TYPE_LABEL_OTHER, 
    """
    if relationshipLabel == REL_TYPE_LABEL_OTHER:
        return None
    elif relationshipLabel == 'deletion':
        return DEL_REL_TYPE
    elif relationshipLabel == 'integration':
        return INT_REL_TYPE
    elif relationshipLabel == 'modification':
        return MOD_REL_TYPE    

def _createConnectionString(code, relationship, annotation):
    """
       @param code: code of a sample
       @param relationship: relationship type as a shortcut (@see REL_TYPES), may be null
       @param annotation: annotation of the relationship, may be null
       @return: string representation of a connection with @relationship translated to a 'character'
    """
    result = code
    if relationship:
        result += _translateToChar(relationship)
    if annotation:
        result += annotation
    return result


def _createSampleLink(code, relationship, annotation):
    """
       Creates sample link XML element for sample with specified @code. The element will contain
       given @code as 'code' attribute apart from standard 'permId' attribute. If specified 
       @relationship or @annotation are not null they will also be contained as attributes.
       
       If the sample doesn't exist in DB a fake link will be created with @code as permId.
       
       @param code: code of a sample
       @param relationship: relationship type as a shortcut (@see REL_TYPES), may be null
       @param annotation: annotation of the relationship, may be null
       @return: sample link XML element as string, e.g.:
       - '<Sample code="FRP1" permId="20110309154532868-4219"/>'
       - '<Sample code="FRP2" permId="20110309154532868-4219" relationship="DEL" annotation="URA3"/>'
       - '<Sample code="FAKE_SAMPLE_CODE" permId="FAKE_SAMPLE_CODE"/>
       - '<Sample code="FRP4" permId="20110309154532868-4219" relationship="INT"/>'
       @raise ValidationException: if the specified relationship type is unknown
    """
    permId = entityInformationProvider().getSamplePermId(SPACE, code)
    if not permId:
        permId = code
    sampleLink = elementFactory().createSampleLink(permId)
    sampleLink.addAttribute(ATR_CODE, code)
    if relationship:
        sampleLink.addAttribute(ATR_RELATIONSHIP, relationship)
        if relationship in REL_TYPES:
            connectionString = _createConnectionString(code, relationship, annotation)
        else:
            raise ValidationException("Unknown relationship: '" + relationship + 
                                      "'. Expected one of: " + REL_TYPES)
    if annotation:
        sampleLink.addAttribute(ATR_ANNOTATION, annotation)
    return sampleLink    

""" MAIN FUNCTIONS """

"""Example input:

FRP1 (DEL:URA3), FRP2 (INT), FRP3 (MOD:URA3), FRP4

Relationship types:
- DEL: deletion
- INT: integration
- MOD: modification
"""

#def showRawValueInForms():
 #   return False
 
#def batchColumnNames():
#    return [CODE_LABEL, RELATIONSHIP_LABEL, ANNOTATION_LABEL]



 
#def updateFromRegistrationForm(bindings):
#    elements = []
#    for item in bindings:
#        code = item.get("CODE")
#        relationship = item.get("RELATIONSHIP")
#        annotation=item.get("ANNOTATION")
#        print "code is", code
#    if code:
#        print "bindings", bindings, "+", code, "+", relationship, "+", annotation

#        sampleLink = _createSampleLink(code, relationship, annotation)
 #       elements.append(sampleLink)
            
#    property.value = propertyConverter().convertToString(elements)
 


def updateFromBatchInput(bindings):
    elements = []
    input = bindings.get('')
    if input is not None:
        plasmids = input.split(',')
        for p in plasmids:
            (code, g, relationship, annotation) = _group(inputPattern, p.strip())
            sampleLink = _createSampleLink(code, relationship, annotation)
            elements.append(sampleLink)
    
    parentsInput = bindings.get(originalColumnNameBindingKey('YEAST_PARENTS'))
    if parentsInput is not None:
        parents = parentsInput.split(',')
        for parent in parents:
            permId = entityInformationProvider().getSamplePermId(SPACE, parent.strip())
            parentPlasmids = entityInformationProvider().getSamplePropertyValue(permId, 'PLASMIDS')
            if parentPlasmids is None:
                continue
            parentElements = list(propertyConverter().convertStringToElements(parentPlasmids))
            for parentLink in parentElements:
                elements.append(parentLink)     
        
    property.value = propertyConverter().convertToString(elements)


def configureUI():
    
    """Create table builder and add columns."""
    tableBuilder = createTableBuilder()
    tableBuilder.addHeader(LINK_LABEL)
    tableBuilder.addHeader(CONNECTION_LABEL)
    tableBuilder.addHeader(CODE_LABEL)
    tableBuilder.addHeader(RELATIONSHIP_LABEL)
    tableBuilder.addHeader(ANNOTATION_LABEL)

    """The property value should contain XML with list of samples. Add a new row for every sample."""
    elements = list(propertyConverter().convertToElements(property))
    for plasmid in elements:
        code = plasmid.getAttribute(ATR_CODE, "")
        relationship = plasmid.getAttribute(ATR_RELATIONSHIP, "")
        annotation = plasmid.getAttribute(ATR_ANNOTATION, "")
   
        row = tableBuilder.addRow()
        row.setCell(CONNECTION_LABEL, _createConnectionString(code, relationship, annotation))
        row.setCell(LINK_LABEL, plasmid, code)
        row.setCell(CODE_LABEL, code)
        row.setCell(RELATIONSHIP_LABEL, _translateToLabel(relationship))
        row.setCell(ANNOTATION_LABEL, annotation)
        
    """Specify that the property should be shown in a tab and set the table output."""
    property.setOwnTab(True)
    uiDescription = property.getUiDescription()
    uiDescription.useTableOutput(tableBuilder.getTableModel())
    
    """
       Define and add actions with input fields used to:
       1. specify attributes of new plasmid relationship,
    """
    addAction = uiDescription.addTableAction(ADD_ACTION_LABEL)\
                             .setDescription('Add new plasmid relationship:')
    widgets = [
        inputWidgetFactory().createTextInputField(CODE_LABEL)\
                            .setMandatory(True)\
                            .setValue('FRP')\
                            .setDescription('Code of plasmid sample, e.g. "FRP1"'),
        inputWidgetFactory().createComboBoxInputField(RELATIONSHIP_LABEL, REL_TYPE_LABELS_WITH_NONE)\
                            .setMandatory(False)\
                            .setValue(REL_TYPE_LABEL_OTHER),
        inputWidgetFactory().createTextInputField(ANNOTATION_LABEL)\
                            .setMandatory(False)\
                            .setDescription('Relationship annotation, e.g. "URA3"'),
    ]
    addAction.addInputWidgets(widgets)
      
    """
       2. modify attributes of a selected plasmid relationship,
    """
    editAction = uiDescription.addTableAction(EDIT_ACTION_LABEL)\
                              .setDescription('Edit selected plasmid relationship:')
    # Exactly 1 row needs to be selected to enable action.
    editAction.setRowSelectionRequiredSingle()            
    widgets = [
        inputWidgetFactory().createTextInputField(CODE_LABEL)\
                            .setMandatory(True)\
                            .setDescription('Code of plasmid sample, e.g. "FRP1"'),
        inputWidgetFactory().createComboBoxInputField(RELATIONSHIP_LABEL, REL_TYPE_LABELS_WITH_NONE)\
                            .setMandatory(False),
        inputWidgetFactory().createTextInputField(ANNOTATION_LABEL)\
                            .setMandatory(False)\
                            .setDescription('Relationship annotation, e.g. "URA3"'),
    ]
    editAction.addInputWidgets(widgets)
    # Bind field name with column name.
    editAction.addBinding(CODE_LABEL, CODE_LABEL)
    editAction.addBinding(RELATIONSHIP_LABEL, RELATIONSHIP_LABEL)
    editAction.addBinding(ANNOTATION_LABEL, ANNOTATION_LABEL)
  
    """
       3. delete selected plasmid relationships.
    """
    deleteAction = uiDescription.addTableAction(DELETE_ACTION_LABEL)\
                                .setDescription('Are you sure you want to delete selected plasmid relationships?')
    # Delete is enabled when at least 1 row is selected.
    deleteAction.setRowSelectionRequired()
    
    
def updateFromUI(action):
    """Extract list of elements from old value of the property."""
    converter = propertyConverter()
    elements = list(converter.convertToElements(property))
  
    """Implement behaviour of user actions."""
    if action.name == ADD_ACTION_LABEL:
        """
           For 'add' action create new plasmid relationship element with values from input fields
           and add it to existing elements.
        """
        code = action.getInputValue(CODE_LABEL)
        relationshipLabel = action.getInputValue(RELATIONSHIP_LABEL)
        relationship = _translateFromLabel(relationshipLabel)
        annotation = action.getInputValue(ANNOTATION_LABEL)
        sampleLink = _createSampleLink(code, relationship, annotation)
        
        elements.append(sampleLink)
    elif action.name == EDIT_ACTION_LABEL:
        """
           For 'edit' action find the plasmid relationship element corresponding to selected row
           and replace it with an element with values from input fields.
        """
        code = action.getInputValue(CODE_LABEL)
        relationshipLabel = action.getInputValue(RELATIONSHIP_LABEL)
        relationship = _translateFromLabel(relationshipLabel)
        annotation = action.getInputValue(ANNOTATION_LABEL)
        sampleLink = _createSampleLink(code, relationship, annotation)
        
        selectedRowId = action.getSelectedRows()[0]
        elements[selectedRowId] = sampleLink
    elif action.name == DELETE_ACTION_LABEL:
        """
           For 'delete' action delete the relationships that correspond to selected rows.
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
