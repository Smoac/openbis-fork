def checkBarcodes():
  '''
  'parents' are a HashSet of SamplePropertyPE
  ''' 
  VOCABULARY_INDEX1 = 'BARCODE'
  VOCABULARY_INDEX2 = 'INDEX2'

  RED = set(['A', 'C'])
  GREEN = set(['T', 'G'])

  SUCCESS_MESSAGE = "OK"
  NO_INDEX = "No Index"
  
  listofIndices = []
  boolList = []
  positionList = []
  returnString = " "

  for e in entity.entityPE().parents:
    for s in e.properties:
      if s.entityTypePropertyType.propertyType.simpleCode == VOCABULARY_INDEX1:
        index = s.getVocabularyTerm().code

        if len(listofIndices) > 0:
          for n in range(0, len(index) - 1):
            listofIndices[n].append(index[n])
        else:
          for n in range(0, len(index) - 1):
            listofIndices.append([index[n]])
       
      # remove any duplicates   
      setofIndices = [set(list) for list in listofIndices]    
 
      # Test whether every element in the set 's' is in the RED set
      boolList = [setofNuc.issubset(RED) for setofNuc in setofIndices]

  if boolList:
    for b in boolList:
      if b:
        positionList.append(boolList.index(b) + 1)
        # set the value to False, because 'index' returns only the first occurrence
        boolList[boolList.index(b)] = False
  else:
   return NO_INDEX

    #  if s.entityTypePropertyType.propertyType.simpleCode == VOCABULARY_INDEX2:
    #   pass 

  if positionList:
    for pos in positionList:
      returnString += "WARNING! The following base position of " + \
                         VOCABULARY_INDEX1 + \
                         " does not have both color channels the index read: " + str(pos) + \
                         "\n" 
  else:
    returnString = SUCCESS_MESSAGE

  return returnString

def calculate(): 
    """Main script function. The result will be used as the value of appropriate dynamic property."""
    return checkBarcodes()
