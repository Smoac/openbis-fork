DELETE 
   FROM scripts 
   WHERE NAME = 'SANOFI_LIBRARY_TEMPLATE';

INSERT
  INTO scripts 
      (id, dbin_id, name, description, pers_id_registerer, entity_kind, script_type, script) 
  
  VALUES
     ( 
       nextval('script_id_seq'), 
       (select max(id) from database_instances),
       'SANOFI_LIBRARY_TEMPLATE',
       'Script for managed property library template',
       (select id from persons where user_id='admin'),
       'EXPERIMENT',
       'MANAGED_PROPERTY',
  E'
from java.text import DecimalFormat
from ch.systemsx.cisd.common.geometry import ConversionUtils

def parseLines(value):
    lines = []
    if value:
        lines = value.strip().split("\\n")
        
    return [ line.split("\\t") for line in lines ]

def getMaxWidth(lines):
  maxWidth = 0
  for line in lines:
    maxWidth = max(maxWidth, len(line))
  return maxWidth

def isControlWell(value):
    return value in ["H", "L"]
    
def isCompoundWell(value):
    try:
        numberParser = DecimalFormat()
        numberParser.parse(value)
        return True
    except:
        return False

def validate(lines):
  for line in lines:
      for value in line:
          normalizedValue = value.strip().upper()
          if normalizedValue:
              if isControlWell(normalizedValue):
                  pass
              elif isCompoundWell(normalizedValue):
                  pass
              else:
                  raise ValidationException("Invalid value found in template : " + normalizedValue)
          

def configureUI():
    propValue = ""
    if not property.isSpecialValue():
        propValue = property.getValue()
    lines = parseLines(propValue)
    tableBuilder = createTableBuilder()
    
    if lines:
        width = getMaxWidth(lines)
        headers = [ " " ] + range(1, width + 1)
        for header in headers:
            tableBuilder.addHeader(str(header), 70)

        for i in range(0, len(lines)):
            rowLetterCode = ConversionUtils.translateRowNumberIntoLetterCode(i+1)
            row = [ rowLetterCode ] + lines[i]
            tableBuilder.addFullRow(row)
 
    property.setOwnTab(True)
    uiDesc = property.getUiDescription()
    uiDesc.useTableOutput(tableBuilder.getTableModel())

    factory = inputWidgetFactory()
    editAction = uiDesc.addTableAction("Edit").setDescription("Edit the library template. "
                                   "Copy the template from Excel and paste it in the text are below.")
    textArea = factory.createMultilineTextInputField("Text")
    textArea.setValue(propValue)
    editAction.addInputWidgets([ textArea ])
    
def updateFromUI(action):
    """in the "Edit" action we simply replace the entire property content"""
    if action.getName() == "Edit":
        newValue = action.getInputWidgetDescriptions()[0].getValue()
        lines = parseLines(newValue)
        validate(lines)
        if not newValue:
            property.setValue("")
        else:
            property.setValue(newValue)
    
');
  
  
UPDATE experiment_type_property_types 
SET 
   is_managed_internally=True,
   script_id=(select id from scripts where name='SANOFI_LIBRARY_TEMPLATE')
WHERE
   prty_id = (select id from property_types where code='LIBRARY_TEMPLATE')
 AND
   exty_id = (select id from experiment_types where code='COMPOUND_HCS')