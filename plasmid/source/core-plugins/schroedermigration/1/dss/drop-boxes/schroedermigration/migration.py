# some_file.py
import sys
import definitions
import definitionsVoc
import re
import random
from datetime import datetime
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria, SearchSubCriteria
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.DataType as DataType

import java.lang.Class as Class
import java.sql.DriverManager as DriverManager

##
## Generic Process Method
##
notMigratedEntities = {
                       "ENTITY_TYPE" : {"ENTITY ID" : {"ERROR" : "TIMES" }}
                      }

def addNotMigratedEntity(type, entityID, error):
    if type not in notMigratedEntities:
        notMigratedEntities[type] = {}
    if entityID not in notMigratedEntities[type]:
        notMigratedEntities[type][entityID] = {}
    if error not in notMigratedEntities[type][entityID]:
        notMigratedEntities[type][entityID][error] = 1
    else:
        notMigratedEntities[type][entityID][error] += 1

def printNotMigratedEntities():
    print "--- Not Migrated Entities Report"
    for type in notMigratedEntities:
        print "Type: [" + str(type) + "]"
        for id in notMigratedEntities[type]:
            for error in notMigratedEntities[type][id]:
                print "Id: [" + str(id) + "] Error: " + str(error) + " Times: " + str(notMigratedEntities[type][id][error])
    print "---"

def process(tr):
    print "START!"
    createDataHierarchy(tr)
    for adaptor in adaptors:
        print "- ADAPTOR [" + adaptor.__class__.__name__ + "] START"
        while adaptor.next():
            entity = adaptor.getEntity()
            print "* ENTITY [" + str(entity.getIdentifier(tr)) + "]"
#             try:
            if not entity.isInOpenBIS(tr):
                entity.write(tr)
                    #print entity.getIdentifier(tr) + " - Updated"
            else:
                addNotMigratedEntity(adaptor.__class__.__name__, entity.getIdentifier(tr), "Already in openBIS")
#             except Exception, error:
#                     addNotMigratedEntity(adaptor.__class__.__name__, entity.getIdentifier(tr), str(error.args))
                    #print entity.getIdentifier(tr) + " - Already up to date"
        print "- ADAPTOR [" + adaptor.__class__.__name__ + "] FINISH"
    print "REPORT START"
    printNotMigratedEntities()
    definitionsVoc.printCreatedTerms()
    print "REPORT FINISH"
    print "FINISH!"

##
## Help Methods
##
def setEntityProperties(tr, definition, entity, properties):
    for propertyCode, propertyValue in properties.iteritems():
            #print "CODE ---->" + propertyCode
            propertyDefinition = definitions.getPropertyDefinitionByCode(definition, propertyCode)
            #print "DEF ---->" + str(propertyDefinition)
            
            if (propertyValue is not None) and (propertyDefinition is not None) and ((propertyDefinition[3] == DataType.VARCHAR) or (propertyDefinition[3] == DataType.MULTILINE_VARCHAR)):
                propertyValue =  unicode(propertyValue)

            if (propertyValue is not None) and (propertyDefinition is not None) and (propertyDefinition[3] == DataType.TIMESTAMP):
                date_val = datetime.strptime(propertyValue, "%Y-%m-%d")
                propertyValue = datetime.strftime(date_val, "%Y-%m-%d")
            
            if (propertyValue is not None) and (propertyDefinition is not None) and (propertyDefinition[3] == DataType.CONTROLLEDVOCABULARY):
                possiblePropertyValue = definitionsVoc.getVocabularyTermCodeForVocabularyAndTermLabel(propertyDefinition[4], propertyValue)
                if possiblePropertyValue is not None:
                    propertyValue = possiblePropertyValue
                else:  #We rely on the Add Hock Terms if is None, since there is no API we create a new one
                    #Create new vocabulary term
                    codeToUse = re.sub(r'\W+','_',propertyValue)
                    labelToUse = propertyValue
                    if len(codeToUse) is 0:
                        codeToUse = "None" + str(random.random())
                    if len(codeToUse) > 60:
                        codeToUse = codeToUse[:50]
                    #Uses new vocabulary term
                    newTerm = definitionsVoc.createVocabularyTerm(tr, propertyDefinition[4], codeToUse, labelToUse)
                    propertyValue = newTerm.getCode()
                    print "* WARNING ENTITY [" + entity.getCode() + "]: for Vocabulary [" + propertyDefinition[4] + "], found value not in list: [" + repr(labelToUse) + "]. Created new term with code [" + codeToUse + "]"
            
            if (propertyDefinition is not None): #Sometimes special fields are added for other purposes, these should not be set
                entity.setPropertyValue(propertyCode, propertyValue)

##
## Generic Pattern
##
class EntityAdaptor:
    entities = None
    entitiesIdx = None
    definition = None
    
    def init(self):
        self.entities = []
        self.entitiesIdx = -1
        pass
    
    def next(self):
        if self.entities is None and self.entitiesIdx is None:
            self.init()
        self.entitiesIdx += 1
        if len(self.entities) > self.entitiesIdx:
            return True
        else:
            return False
    
    def addEntity(self, values):
        self.entities.append(OpenBISDTO(values, self.definition))
    
    def getEntity(self):
        return self.entities[self.entitiesIdx]
    
class OpenBISDTO:
    values = {}
    definition = None
    
    def __init__(self, values, definition):
        self.values = values
        self.definition = definition
    
    def getIdentifier(self, tr):
        pass
    
    def isInOpenBIS(self, tr):
        pass
    
    def write(self, tr):
        pass

##
## Costumer specific logic: generic part
##
experimentCache = {}
sampleCache = {}
sampleID2Sample = {}
globalSequences = {};

def getNextGlobalSequence(id):
    currentSequence = None;
    
    if id in globalSequences:
        currentSequence = globalSequences[id]; #Get existing sequence
    else:
        currentSequence = 0; # Create a new one
    
    #Advance and store new step on the sequence
    currentSequence = currentSequence+1;
    globalSequences[id] = currentSequence;
    
    #Return the new sequence number
    return str(currentSequence);

def getExperimentForUpdate(experimentIdentifier, experimentType, tr):
    if experimentIdentifier not in experimentCache:
        #print "Cache failed " + experimentIdentifier + ":" + str(experimentType)
        experiment = tr.getExperimentForUpdate(experimentIdentifier)
        if experiment is None and experimentType is not None:
            #print "Cache Create " + experimentIdentifier + ":" + str(experimentType)
            experiment = tr.createNewExperiment(experimentIdentifier,         experimentType)
        if experiment is not None:
             experimentCache[experimentIdentifier] = experiment
    else:
        pass
        #print "Cache hit " + experimentIdentifier + ":" + str(experimentType)
    if experimentIdentifier not in experimentCache:
         return None
    else:
         return experimentCache[experimentIdentifier]
     
def getSampleForUpdate(sampleIdentifier, sampleType, tr):
    if sampleIdentifier not in sampleCache:
         #print "Cache failed " + sampleIdentifier + ":" + str(sampleType)
         sample = tr.getSampleForUpdate(sampleIdentifier)
         if sample is None and sampleType is not None:
             #print "Cache Create " + sampleIdentifier + ":" + str(sampleType)
             if sampleType == "CELL_LINE":
             	experiment = getExperimentForUpdate("/MATERIALS/CELL_LINES/CELL_LINE_COLLECTION_1", "MATERIALS", tr)             
             sample = tr.createNewSample(sampleIdentifier, sampleType)
             sample.setExperiment(experiment)
         if sample is not None:
             sampleCache[sampleIdentifier] = sample
    else:
        pass
        #print "Cache hit " + sampleIdentifier + ":" + str(sampleType)
        
    if sampleIdentifier not in sampleCache:
         return None
    else:
         return sampleCache[sampleIdentifier]

class FileMakerEntityAdaptor(EntityAdaptor):
    connection = None
    selectQuery = None
    
    def __init__(self, fileMakerConnString, fileMakerUser, fileMakerPass, db):
        Class.forName("com.filemaker.jdbc.Driver").newInstance()
        self.connection = DriverManager.getConnection(fileMakerConnString+db,fileMakerUser, fileMakerPass)
    
    def init(self):
        EntityAdaptor.init(self)
        
        preparedStatement = self.connection.prepareStatement(self.selectQuery)
        result = preparedStatement.executeQuery()
        
        while result.next():
            values = {}
            for property in self.definition:
                values[property[0]] = result.getString(property[2])
            self.addEntity(values)
        result.close()
        preparedStatement.close()

##
## Customer specific logic: different sample types
##
class FMSchroederOpenBISDTO(OpenBISDTO):
        def isInOpenBIS(self, tr):
            code = self.getIdentifier(tr)
            if code is not None:
                sample = getSampleForUpdate("/MATERIALS/"+code, None, tr)
                if sample is not None:
                    lastModificationData = self.values["MODIFICATION_DATE"].strip()
                    lastModificationData = str(datetime.strptime(lastModificationData, "%Y-%m-%d"))[:10]
                    lastModificationOpenBIS = sample.getPropertyValue("MODIFICATION_DATE")[:10]
                    return lastModificationOpenBIS == lastModificationData
                else :
                    return False
            else:
                print "* ERROR [" + str(code) + "] - Invalid Code found for '" + self.__class__.__name__ + "'"
                raise Exception('Invalid Code found ' + str(code))

# class FMSchroederBoxAdaptor(FileMakerEntityAdaptor):
#     selectBoxQuery = None
#     entityIdFieldName = None
#     entityCodeFieldName = None
#     
#     def addEntity(self, values):
#         self.entities.append(FMSchroederEntityBoxOpenBISDTO(values, self.definition))
#         
#     def init(self):
#         #print "Reading boxes for: " + self.__class__.__name__
#         emptyBox = 0
#         boxes = {}
#         EntityAdaptor.init(self)
#         preparedStatement = self.connection.prepareStatement(self.selectBoxQuery)
#         result = preparedStatement.executeQuery()
#         while result.next():
#             entityId = unicode(result.getString(self.entityIdFieldName))
#             if entityId is not None:
#                 if entityId in sampleID2Sample:
#                     antibodyNumber = sampleID2Sample[entityId][self.entityCodeFieldName]
#                     if antibodyNumber is not None:
#                         values = {}
#                         drawer = result.getString("drawer");
#                         row = None
#                         column = None
#                         if (drawer is not None) and (len(drawer) > 0) and ("/" in drawer):
#                             row = drawer.split("/")[0];
#                             column = drawer.split("/")[1];
#                         
#                         values["STORAGE_CODE"] = result.getString("location")
#                         values["STORAGE_RACK_ROW"] = row # syntax is y/x: x is the row
#                         values["STORAGE_RACK_COLUMN"] = column # syntax is y/x: y is the tower, i.e. the column
#                         values["STORAGE_BOX_NAME"] = result.getString("box label")
#                         values["STORAGE_BOX_SIZE"] = result.getString("box size")
#                         values["STORAGE_USER"] = result.getString("owner")
#                         values["STORAGE_POSITION"] = result.getString("position")
#                         
#                         allBoxes = []
#                         if antibodyNumber in boxes:
#                             allBoxes = boxes[antibodyNumber]
#                         else:
#                             boxes[antibodyNumber] = allBoxes
#                         allBoxes.append(values)
#                 else:
#                     #The antibody is not there. What the *#%$&
#                     emptyBox += 1
#             else:
#                 #The antibody is not there. What the *#%$&
#                 emptyBox += 1
#         
#         print "- ERROR ADAPTOR Boxes positions with empty entityId for " + self.__class__.__name__ + ":" + str(emptyBox)
#         
#         for entiyCode, allBoxes in boxes.iteritems():
#             self.addEntity({
#                         "*CODE" : entiyCode,
#                         "*BOXESLIST" : allBoxes
#             })
#         
#         result.close()
#         preparedStatement.close()

# class FMSchroederEntityBoxOpenBISDTO(OpenBISDTO):
#     def getIdentifier(self, tr):
#         return self.values["*CODE"]
#     
#     def write(self, tr):
#         sample = getSampleForUpdate("/MATERIALS/"+self.values["*CODE"], None, tr)
#         print "* INFO Boxes size: " + str(len(self.values["*BOXESLIST"]))
#         #Delete old boxes
#         for boxNum in range(1, definitions.numberOfStorageGroups+1):
#             for propertyCode in definitions.getStorageGroupPropertyCodes():
#                 sample.setPropertyValue(propertyCode + "_" + str(boxNum), None)
#         
#         #Add new boxes
#         boxNum = 0
#         for box in self.values["*BOXESLIST"]:
#             boxNum += 1
#             for propertyCode, propertyValue in box.iteritems():
#                 if propertyCode == "STORAGE_NAME":
#                     freezerName = definitionsVoc.getVocabularyTermCodeForVocabularyAndTermLabel("FREEZER", propertyValue)
#                     if freezerName is None:
#                         #print repr("NOT FOUND FEEZER: " + self.values["ANTIBODY_ID_NR"] + " : '" + unicode(propertyValue) + "'")
#                         propertyValue = None
#                     else:
#                         propertyValue = freezerName
#                 
#                 if propertyValue is not None:
#                     propertyValue =  unicode(propertyValue)
#                     sample.setPropertyValue(propertyCode + "_" + str(boxNum), propertyValue)
#     
#     def isBoxPressent(self, boxSignature, tr):
#         sample = getSampleForUpdate("/MATERIALS/"+self.values["*CODE"], None, tr)
#         if sample is not None:
#             for boxNum in range(1, definitions.numberOfStorageGroups+1):
#                 storedSignature = "";
#                 for propertyCode in definitions.getStorageGroupPropertyCodes():
#                     propertyValue = sample.getPropertyValue(propertyCode + "_" + str(boxNum))
#                     if propertyValue is not None:
#                         propertyValue = unicode(propertyValue)
#                         storedSignature += propertyValue
#                 if storedSignature == boxSignature:
#                     #print "Found Box " + storedSignature.encode('ascii', 'ignore')
#                     return True
#         return False
#     
#     def isInOpenBIS(self, tr):
#         for box in self.values["*BOXESLIST"]:
#             boxSignature = "";
#             for propertyCode in definitions.getStorageGroupPropertyCodes():
#                 propertyValue = box[propertyCode]
#                 if propertyCode == "STORAGE_NAME":
#                     propertyValue = definitionsVoc.getVocabularyTermCodeForVocabularyAndTermLabel("FREEZER", propertyValue)
#                 #if propertyCode == "STORAGE_USER":
#                     #propertyValue = definitionsVoc.getVocabularyTermCodeForVocabularyAndTermLabel("LAB_MEMBERS_INITIALS", propertyValue)
#                     
#                 if propertyValue is not None:
#                     propertyValue = unicode(propertyValue)
#                     boxSignature += propertyValue
#             if not self.isBoxPressent(boxSignature, tr):
#                 return False
#         return True


##
## Cells
##
class CellAdaptor(FileMakerEntityAdaptor):
    def init(self):
        self.selectQuery = "SELECT * FROM \"cells\""
        self.definition = definitions.CELL_LINE
        FileMakerEntityAdaptor.init(self)
    
    def addEntity(self, values):
        self.entities.append(CellOpenBISDTO(values, self.definition))
        
class CellOpenBISDTO(FMSchroederOpenBISDTO):
    def write(self, tr):
        code = self.values["*CODE"]
        if code is not None:
            sample = getSampleForUpdate("/MATERIALS/"+code,"CELL_LINE", tr)
            setEntityProperties(tr, self.definition, sample, self.values);
            global sampleID2Sample
            sampleID2Sample[self.values["NAME"]] = sample
        
    def getIdentifier(self, tr):
        if "*CODE" not in self.values:
            self.values["*CODE"] = "CELL_LINE_" + getNextGlobalSequence("CELL_LINE");
            print "New Code Generated: " + self.values["*CODE"]
        return self.values["*CODE"];

# class CellBoxAdaptor(FMSchroederBoxAdaptor):
#     selectBoxQuery = "SELECT * FROM \"cell boxes\""
#     entityIdFieldName = "cell ID"
#     entityCodeFieldName = "NAME"

fmConnString = "jdbc:filemaker://127.0.0.1/"
#fmConnString = "jdbc:filemaker://fm.ethz.ch/"
fmUser= "Admin"
fmPass = ""

adaptors = [ 

            CellAdaptor(fmConnString, fmUser, fmPass, "boxit-cells")
            #CellBoxAdaptor(fmConnString, fmUser, fmPass, "boxit-cells_boxes"),
             ]
           
def createDataHierarchy(tr):
    pass

        
        