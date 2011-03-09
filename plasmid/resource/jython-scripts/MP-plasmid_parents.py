import sys
import re

"""debug sys path"""
#print(sys.path)

'''
FRP1 (DEL:URA3), FRP2 (INT), FRP3 (MOD:URA3), FRP4

Relationship types:

    * DEL: deletion
    * INT: integration
    * MOD: modification
'''

""""space that all parents come from (fixed)"""
SPACE = "YEAST_LAB"

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

def group(pattern, input):
    return pattern.search(input).groups()

EXAMPLES = [
   'FRP1 (DEL:URA3)',
   'FRP2 (INT)',
   'FRP3 (MOD:URA3)',
   'FRP4',
   'FRPS5'
]

REL_TYPES = ('DEL', 'INT', 'MOD')
REL_TYPE_CHARS = {
    'DEL': 'd', 
    'INT': '::', 
    'MOD': '_' 
}
REL_TYPE_LABELS = {
    'DEL': 'deletion', 
    'INT': 'integration', 
    'MOD': 'modification' 
}

ATR_CODE = "code"
ATR_RELATIONSHIP = "rel"
ATR_ANNOTATION = "annotation"

"""helper functions"""

def translateToChar(relationship):
    if relationship in REL_TYPE_CHARS:
        return REL_TYPE_CHARS[relationship]
    else:
        return "[" + relationship + "]"

def createConnectionString(code, relationship, annotation):
    result = code
    if relationship:
        result += translateToChar(relationship)
    if annotation:
        result += annotation
    return result

def createSampleLink(code, relationship, annotation):
    permId = entityInformationProvider().getSamplePermId(SPACE, code)
    if not permId:
        permId = code
    sampleLink = elementFactory().createSampleLink(permId)
    sampleLink.addAttribute(ATR_CODE, code)
    if relationship:
        sampleLink.addAttribute(ATR_RELATIONSHIP, relationship)
        if relationship in REL_TYPES:
            connectionString = createConnectionString(code, relationship, annotation)
        else:
            raise ValidationException("Unknown relationship: '" + relationship + 
                                     "'. Expected one of: " + REL_TYPES)
    if annotation:
        sampleLink.addAttribute(ATR_ANNOTATION, annotation)
    return sampleLink    

"""main functions"""

""" updateFromBatchInput """

def updateFromBatchInput(bindings):
    inputPattern = re.compile(INPUT_PATTERN, re.VERBOSE)
    input = bindings.get('')
    plasmids = input.split(',')
    elements = []
    for p in plasmids:
        (code, g, relationship, annotation) = group(inputPattern, p.strip())
        sampleLink = createSampleLink(code, relationship, annotation)
        elements.append(sampleLink)
    property.value = propertyConverter().convertToString(elements)

""" configureUI """

CONNECTION_LABEL = "connection"
LINK_LABEL = "link"
CODE_LABEL = "code"
RELATIONSHIP_LABEL = "relationship"
ANNOTATION_LABEL = "annotation"

def configureUI():
    inputPattern = re.compile(INPUT_PATTERN, re.VERBOSE)

    """create table builder and add columns"""
    tableBuilder = createTableBuilder()
    tableBuilder.addHeader(CONNECTION_LABEL)
    tableBuilder.addHeader(LINK_LABEL)
    tableBuilder.addHeader(CODE_LABEL)
    tableBuilder.addHeader(RELATIONSHIP_LABEL)
    tableBuilder.addHeader(ANNOTATION_LABEL)

    """The property value should contain XML with list of sample. Add a new row for every sample."""
    elements = list(propertyConverter().convertToElements(property))
    for plasmid in elements:
        permId = plasmid.getPermId()
        code = plasmid.getAttribute(ATR_CODE, "")
        relationship = plasmid.getAttribute(ATR_RELATIONSHIP, "")
        code = plasmid.getAttribute(ATR_CODE, "")
        annotation = plasmid.getAttribute(ATR_ANNOTATION, "")
   
        row = tableBuilder.addRow()
        row.setCell(CONNECTION_LABEL, createConnectionString(code, relationship, annotation))
        row.setCell(LINK_LABEL, plasmid)
        row.setCell(CODE_LABEL, code)
        row.setCell(RELATIONSHIP_LABEL, relationship)
        row.setCell(ANNOTATION_LABEL, annotation)
        
    """specify that the property should be shown in a tab and set the table output"""
    property.setOwnTab(True)
    uiDesc = property.getUiDescription()
    uiDesc.useTableOutput(tableBuilder.getTableModel())