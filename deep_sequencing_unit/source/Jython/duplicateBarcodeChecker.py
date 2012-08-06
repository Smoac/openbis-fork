import smtplib
import string
import copy
 
def sendMail(registratorEmail, indexStrings='', sampleIdentifier='', additionalText=''):
  '''
  Send out an email to the specified recipients
  '''
  COMMASPACE = ", "
  
  loe = ["manuel.kohler@bsse.ethz.ch"]
  if registratorEmail and registratorEmail not in loe:
    loe.append(registratorEmail)
  
  TO = COMMASPACE.join(loe)
  SUBJECT = "WARNING! For pooled sample " + sampleIdentifier
  FROM = "qgf@openbis-dsu.ethz.ch"
  HOST = "smtp0.ethz.ch"
  text = "Sample: "+ sampleIdentifier + "\nBarcode: " + indexStrings + "\n" + additionalText
  BODY = string.join((
        "From: %s" % FROM,
        "To: %s" % TO,
        "Subject: %s" % SUBJECT ,
        "",
        text
        ), "\r\n")
  server = smtplib.SMTP(HOST)
  server.sendmail(FROM, [TO], BODY)
  server.quit()

def checkDuplicateBarcodes():
  '''
  'parents' are a HashSet of SamplePropertyPE
  ''' 
  VOCABULARY_INDEX1 = 'BARCODE'
  VOCABULARY_INDEX2 = 'INDEX2'

  SUCCESS_MESSAGE="OK"
  registratorEmail = entity.entityPE().registrator.email
  poolName = entity.entityPE().sampleIdentifier
  
  listofIndices = []
  listofIndices2 = []
  returnString = " "

  for e in entity.entityPE().parents:
    for s in e.properties:

      if s.entityTypePropertyType.propertyType.simpleCode == VOCABULARY_INDEX1:
        index = s.getVocabularyTerm().code
        listofIndices.append(index)

      if s.entityTypePropertyType.propertyType.simpleCode == VOCABULARY_INDEX2:
        index = s.getVocabularyTerm().code
        listofIndices2.append(index)

    sampleIdentifier = e.sampleIdentifier

  # if dual-indexed
  if listofIndices2:
    dualIndexList = []
    if len(listofIndices) != len(listofIndices2):
      indices = "\nIndex1 :" + str(listofIndices) + "\nIndex2: " + str(listofIndices2) 
      returnString = "Dual indexing assignment is not complete for pool sample " + str(poolName)
      sendMail(registratorEmail, indices , str(poolName), returnString)
      return returnString
    
    # build up a list of dual indices
    for l in range(len(listofIndices)):
      try:
        dualIndexList.insert(l, [listofIndices[l], listofIndices2[l]])
      except:
        pass
     
    # copy, needed by the for loop
    dualIndexListOrig = copy.deepcopy(dualIndexList)

    for dualIndex in dualIndexListOrig:
      dualIndexList.remove(dualIndex)
      if dualIndex in dualIndexList:
        returnString = "WARNING! You assigned duplicate indices \n " + str(dualIndex) + \
                        " in the dual-indexed pooled sample!"
        sendMail(registratorEmail, str(dualIndex), str(poolName), returnString)
        return returnString
      else:
        returnString = SUCCESS_MESSAGE
        return returnString  

  # else single index
  else:
    uniqueList=list(set(listofIndices))
    if len(listofIndices) != len(uniqueList):
      for index in uniqueList:
        listofIndices.remove(index)
      returnString = "WARNING! You assigned duplicate indices \n " + str(listofIndices) + \
                        " in the single-indexed pooled sample!"
      sendMail(registratorEmail, str(listofIndices), str(poolName), returnString)
    else:
      returnString = SUCCESS_MESSAGE
    return returnString

def calculate(): 
    """Main script function. The result will be used as the value of appropriate dynamic property."""
    return checkDuplicateBarcodes()
