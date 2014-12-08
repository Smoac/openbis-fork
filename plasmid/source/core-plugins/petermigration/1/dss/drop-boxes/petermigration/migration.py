# some_file.py
import sys
import definitions
import re
import random
from datetime import datetime
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.DataType as DataType

import java.lang.Class as Class
import java.sql.Connection as Connection
import java.sql.DriverManager as DriverManager
import java.sql.PreparedStatement as PreparedStatement
import java.sql.ResultSet as ResultSet
import java.sql.SQLException as SQLException
import java.util.ArrayList as ArrayList
import java.util.List as List

##
## Generic Process Method
##
def process(tr):
    print "START!"
    createDataHierarchy(tr)
    for adaptor in adaptors:
        while adaptor.next():
            entity = adaptor.getEntity()
            entity.getIdentifier(tr)
            if not entity.isInOpenBIS(tr):
                entity.write(tr)
                #print entity.getIdentifier(tr) + " - Updated"
            else:
                pass
                #print entity.getIdentifier(tr) + " - Already up to date"
    print "FINISH!"

##
## Generic Adaptor Pattern
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
## Costumer specific logic
##
sampleCache = {}
antibodyID2Antibody = {}

def getSampleForUpdate(sampleIdentifier, sampleType, tr):
    if sampleIdentifier not in sampleCache:
         print "Cache failed " + sampleIdentifier + ":" + str(sampleType)
         sample = tr.getSampleForUpdate(sampleIdentifier)
         if sample is None and sampleType is not None:
             print "Create"
             sample = tr.createNewSample(sampleIdentifier, sampleType)
         
         if sample is not None:
             sampleCache[sampleIdentifier] = sample
    else:
        print "Cache hit " + sampleIdentifier + ":" + str(sampleType)
        
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
    
class AntibodyAdaptor(FileMakerEntityAdaptor):
    
    def init(self):
        self.selectQuery = "SELECT * FROM \"boxit antibodies\""
        self.definition = definitions.antibodyDefinition
        FileMakerEntityAdaptor.init(self)
    
    def addEntity(self, values):
        self.entities.append(AntibodyOpenBISDTO(values, self.definition))
        
class AntibodyOpenBISDTO(OpenBISDTO):
    def write(self, tr):
        sample = getSampleForUpdate("/INVENTORY/"+self.values["ANTIBODY_ID_NR"],"ANTIBODY", tr)
        
        for propertyCode, propertyValue in self.values.iteritems():
            propertyDefinition = definitions.getPropertyDefinitionByCode(self.definition, propertyCode)
            if propertyValue is not None:
                propertyValue =  unicode(propertyValue)
            
            if propertyDefinition[3] == DataType.CONTROLLEDVOCABULARY and propertyValue is not None:
                posiblePropertyValue = definitions.getVocaularyTermCodeForVocabularyAndTermLabel(propertyDefinition[4], propertyValue)
                if posiblePropertyValue is not None:
                    propertyValue = posiblePropertyValue
                else:  #We rely on the Add Hock Terms if is None
                    #Create new vocabulary term
                    vocabulary = tr.getVocabularyForUpdate(propertyDefinition[4])
                    term = tr.createNewVocabularyTerm()
                    codeToUse = re.sub(r'\W+','',propertyValue)
                    labelToUse = propertyValue
                    if len(codeToUse) is 0:
                        codeToUse = "None" + str(random.random())
                    if len(codeToUse) > 60:
                        codeToUse = codeToUse[:60]
                    term.setCode(codeToUse)
                    term.setLabel(labelToUse)
                    term.setOrdinal(vocabulary.getTerms().size())
                    vocabulary.addTerm(term)
                    #Uses new vocabulary term
                    propertyValue = codeToUse
                    #print "CREATED FOR VOCABULARY " + propertyDefinition[4] + " NEW TERM WITH CODE " + codeToUse
            sample.setPropertyValue(propertyCode, propertyValue)
    
    def getIdentifier(self, tr):
        antibodyID2Antibody[self.values["NAME"]] = self.values
        return self.values["ANTIBODY_ID_NR"]
    
    def isInOpenBIS(self, tr):
        code = self.values["ANTIBODY_ID_NR"]
        sample = getSampleForUpdate("/INVENTORY/"+self.values["ANTIBODY_ID_NR"], None, tr)
        if sample is not None:
            lastModificationData = self.values["MODIFICATION_DATE"].strip()
            lastModificationData = str(datetime.strptime(lastModificationData, "%Y-%m-%d"))[:10]
            lastModificationOpenBIS = sample.getPropertyValue("MODIFICATION_DATE")[:10]
            return lastModificationOpenBIS == lastModificationData
        else :
            return False
    
class AntibodyBoxAdaptor(FileMakerEntityAdaptor):
    
    def init(self):
        emptyAntibodyBox = 0
        antibodyBoxes = {}
        
        self.selectQuery = "SELECT * FROM \"antibody boxes\""
        EntityAdaptor.init(self)
        
        preparedStatement = self.connection.prepareStatement(self.selectQuery)
        result = preparedStatement.executeQuery()
        while result.next():
            antibodyID = unicode(result.getString("antibody ID"))
            if antibodyID is not None:
                if antibodyID in antibodyID2Antibody:
                    antibodyNumber = antibodyID2Antibody[antibodyID]["ANTIBODY_ID_NR"]
                    values = {}
                    values["FREEZER"] = result.getString("location")
                    values["STORAGE_ROW"] = None
                    values["STORAGE_COLUMN"] = None
                    values["STORAGE_BOX_NAME"] = result.getString("box label")
                    values["STORAGE_USER"] = result.getString("frozen by")
                    values["STORAGE_BOX_POSITION"] = result.getString("position")
                    
                    allboxes = []
                    print "if key " + antibodyNumber
                    if antibodyNumber in antibodyBoxes:
                        allboxes = antibodyBoxes[antibodyNumber]
                    else:
                        antibodyBoxes[antibodyNumber] = allboxes
                    allboxes.append(values)
                else:
                    #The antibody is not there. What the *#%$&
                    emptyAntibodyBox += 1
            else:
                #The antibody is not there. What the *#%$&
                emptyAntibodyBox += 1
        
        print "Antibody not found: " + str(emptyAntibodyBox)
        
        for antibodyCode, allAntibodyBoxes in antibodyBoxes.iteritems():
            self.addEntity({
                        "ANTIBODY_ID_NR" : antibodyCode,
                        "*BOXESLIST" : allAntibodyBoxes
            })
        
        result.close()
        preparedStatement.close()
    
    def addEntity(self, values):
        self.entities.append(AntibodyBoxOpenBISDTO(values, self.definition))

class AntibodyBoxOpenBISDTO(OpenBISDTO):
    def write(self, tr):
        sample = getSampleForUpdate("/INVENTORY/"+self.values["ANTIBODY_ID_NR"], None, tr)
        
        boxNum = 1
#         for box in self.values["*BOXESLIST"]:
#             print "Box N" + str(boxNum)
#             for propertyCode, propertyValue in box.iteritems():
#                 if propertyCode == "FREEZER":
#                     propertyValue = definitions.getVocaularyTermCodeForVocabularyAndTermLabel("FREEZER", propertyValue)
#                 if propertyValue is not None:
#                     propertyValue =  unicode(propertyValue)
#                     sample.setPropertyValue(propertyCode + "_" + str(boxNum), propertyValue)
#                 boxNum += 1
    
    def isInOpenBIS(self, tr):
        code = self.values["ANTIBODY_ID_NR"]
        sample = getSampleForUpdate("/INVENTORY/"+self.values["ANTIBODY_ID_NR"], None, tr)
        if sample is not None:
            return False
        else :
            return False
    
fmConnString = "jdbc:filemaker://127.0.0.1/"
fmUser = "designer"
fmPass = "seattle"

fmConnStringServer = "jdbc:filemaker://fm.ethz.ch/"
fmUserServer= "sistemp"
fmPassServer = "ibcimsb2014"

adaptors = [AntibodyAdaptor(fmConnString, fmUser, fmPass, "BOXIT_antibodies_Peter.fmp12"), AntibodyBoxAdaptor(fmConnString, fmUser, fmPass, "BOXIT_antibody_boxes_Peter")]

def createDataHierarchy(tr):
    inventorySpace = tr.getSpace("INVENTORY")
    if inventorySpace == None:
        tr.createNewSpace("INVENTORY", None)
        tr.createNewProject("/INVENTORY/MATERIALS")
        tr.createNewExperiment("/INVENTORY/MATERIALS/ANTIBODY",         "ANTIBODY")