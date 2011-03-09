""" 
Managed Property Script for handling plasmid parents of yeast samples.

@author: Piotr Buczek
"""

import re

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

"""tuple of supported relationship types as shortcuts"""
REL_TYPES = ('DEL', 'INT', 'MOD')
"""dictionary from relationship type shortcut to its 'character' representation"""
REL_TYPE_CHARS = {
    'DEL': '^', 
    'INT': '::', 
    'MOD': '_' 
}
"""dictionary from relationship type shortcut to its full name/label"""
REL_TYPE_LABELS = {
    'DEL': 'deletion', 
    'INT': 'integration', 
    'MOD': 'modification' 
}

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
        return ""    


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
def updateFromBatchInput(bindings):
    
    inputPattern = re.compile(INPUT_PATTERN, re.VERBOSE)
    input = bindings.get('')
    plasmids = input.split(',')
    elements = []
    for p in plasmids:
        (code, g, relationship, annotation) = _group(inputPattern, p.strip())
        sampleLink = _createSampleLink(code, relationship, annotation)
        elements.append(sampleLink)
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
    uiDesc = property.getUiDescription()
    uiDesc.useTableOutput(tableBuilder.getTableModel())