# some_file.py
import sys
import definitions
import definitionsVoc
import re
import random
from datetime import datetime
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria, SearchSubCriteria
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.DataType as DataType
import uuid

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
    # Set Parents
    for sampleName in sampleID2Sample:
            sample = sampleID2Sample[sampleName]
            sampleParentName = sample.getPropertyValue("PARENTAL_CELL")
            if sampleParentName != None and sampleParentName != "":
                if sampleParentName in sampleID2Sample:
                    sampleParent = sampleID2Sample[sampleParentName]
                    if sample is not sampleParent:
                        sample.setParentSampleIdentifiers([sampleParent.getSampleIdentifier()]);
                    else:
                        print "Sample depends on itself ===> '" + str(sampleParentName) + "'"
                else:
                    print "Missing sample parent ===> '" + sampleName + "' depends on '" + str(sampleParentName) + "'"
    #
    print "REPORT START"
    printNotMigratedEntities()
    definitionsVoc.printCreatedTerms()
    print "INVESTIGATORS MISSING"
    global investigatorsMissing
    print str(investigatorsMissing)
    print "REPORT FINISH"
    print "FINISH!"

def hasColumn(rs, columnName):
    rsmd = rs.getMetaData()
    columns = rsmd.getColumnCount()
    for x in range(1, columns + 1):
        foundColumnName = rsmd.getColumnName(x);
        if columnName == foundColumnName:
            return True
    return False

##
## Help Methods
##
def setEntityProperties(tr, definition, entity, properties):
    for propertyCode, propertyValue in properties.iteritems():
            #print "CODE ---->" + propertyCode
            propertyDefinition = definitions.getPropertyDefinitionByCode(definition, propertyCode)
            #print "DEF ---->" + str(propertyDefinition)
            
            if (propertyValue is not None) and (propertyDefinition is not None) and ((propertyDefinition[3] == DataType.INTEGER) or (propertyDefinition[3] == DataType.REAL)):
                propertyValue =  propertyValue.strip(' ')
            
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
            
            if (propertyDefinition is not None) and (not propertyCode.startswith("*")): #Sometimes special fields are added for other purposes, these should not be set
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
         sample = None #tr.getSampleForUpdate(sampleIdentifier)
         if sample is None and sampleType is not None:
             #print "Cache Create " + sampleIdentifier + ":" + str(sampleType)
             experiment = None
             if sampleType == "CELL_LINE":
             	experiment = getExperimentForUpdate("/MATERIALS/CELL_LINES/CELL_LINE_COLLECTION_1", "MATERIALS", tr)             
             sample = tr.createNewSample(sampleIdentifier, sampleType)
             if experiment is not None:
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
    columnPrefix = None
    
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
                propertyCode = property[0]
                columnName = property[2]
                if self.columnPrefix != None:
                    columnName = self.columnPrefix + columnName
                if hasColumn(result, columnName):
                    propertyValue = result.getString(columnName)
                    if propertyValue != None:
                        propertyValue = propertyValue.strip("\r")
                    values[propertyCode] = propertyValue
            print str(values);
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
        return self.values["*CODE"];

class CellBoxPositionAdaptor(FileMakerEntityAdaptor):
    def init(self):
        self.selectQuery = "SELECT * FROM \"cell boxes\""
        self.definition = definitions.STORAGE_POSITION
        self.columnPrefix = "cell ID data box:"
        FileMakerEntityAdaptor.init(self)
    
    def addEntity(self, values):
        self.entities.append(CellBoxPositionOpenBISDTO(values, self.definition))
        
        
racks = {
             u"N2" : "N2",
             u"-140°C R5.38" : "MINUS140_R5.38"
         }

investigators = {
            u"Timm_Schroeder" : "timmsc",
            u"Martin_Etzrodt" : "etzromar",
            u"Simon_Hastreiter" : "hassimon",
            u"Laura_Skylaki" : "skylakis",
            u"Dirk_Löffler" : "ldirk",
            u"Konstantinos_Kokkaliaris" : "kokkalik",
            u"Oliver_Hilsenbeck" : "hilsenbo",
            u"Philip_Dettinger" : "philipd",
            u"Andreas_Reimann" : "anreiman",
            u"Leo_Kunz" : "kunzl",
            u"Nouraiz_Ahmed" : "noahmed",
            u"Ren_Shimamoto" : "rens",
            u"Weijia_Wang" : "wwang",
            u"Geethika_Arekatla" : "garekatl",
            u"Tobias_Kull" : "tkull",
            u"Arne_Wehling" : "awehling",
            u"Yang_Zhang" : "yangzhan",
            u"Jeffrey_Bernitz" : "jbernitz"
        }

investigatorsMissing = {}
class CellBoxPositionOpenBISDTO(FMSchroederOpenBISDTO):
    def write(self, tr):
        code = self.values["*CODE"]
        if code is not None:
            sample = getSampleForUpdate("/STORAGE/"+code,"STORAGE_POSITION", tr)
            sampleParent = sampleID2Sample[self.values["*CELL_ID"]];
            
            setEntityProperties(tr, self.definition, sample, self.values);
            drawer = self.values["*DRAWER"];
            row = None
            column = None
            if (drawer is not None) and (len(drawer) > 0) and ("/" in drawer):
                row = drawer.split("/")[0];
                column = drawer.split("/")[1];
            
            if self.values["*LOCATION"] == u"-140°C R5.38" and row == None and column == None:
                row = "1"
                column = "1"
            
            if self.values["*LOCATION"] != None:
                sample.setPropertyValue("STORAGE_CODE", racks[self.values["*LOCATION"]])
            
            if row != None:
                sample.setPropertyValue("STORAGE_RACK_ROW", row.strip(" ")) # syntax is y/x: x is the row
            if column != None:
                sample.setPropertyValue("STORAGE_RACK_COLUMN", column.strip(" ")) # syntax is y/x: y is the tower, i.e. the column
            sample.setPropertyValue("STORAGE_BOX_NAME", self.values["*BOX_NAME"])
            sample.setPropertyValue("STORAGE_BOX_SIZE", self.values["*BOX_SIZE"])
            sample.setPropertyValue("STORAGE_BOX_POSITION", self.values["*POSITION"])
            
            boxOwner = None
            if sampleParent.getPropertyValue("INVESTIGATOR") != None and sampleParent.getPropertyValue("INVESTIGATOR") not in investigators:
                global investigatorsMissing
                investigatorsMissing[sampleParent.getPropertyValue("INVESTIGATOR")] = True
            if sampleParent.getPropertyValue("INVESTIGATOR") in investigators:
                boxOwner = investigators[sampleParent.getPropertyValue("INVESTIGATOR")]
            elif self.values["*FROZEN_BY"] != None:
                boxOwner = self.values["*FROZEN_BY"]
            else:
                boxOwner = self.values["*OWNER"]
            sample.setPropertyValue("STORAGE_USER", boxOwner)
            
            # Set parent
            sample.setParentSampleIdentifiers([sampleParent.getSampleIdentifier()]);
        
    def getIdentifier(self, tr):
        if "*CODE" not in self.values:
            self.values["*CODE"] = str(uuid.uuid4());
        return self.values["*CODE"];

fmConnString = "jdbc:filemaker://127.0.0.1/"
#fmConnString = "jdbc:filemaker://fm.ethz.ch/"
fmUser= "Admin"
fmPass = ""

adaptors = [ 
            CellAdaptor(fmConnString, fmUser, fmPass, "boxit-cells"),
            CellBoxPositionAdaptor(fmConnString, fmUser, fmPass, "boxit-cell-boxes")
             ]
           
def createDataHierarchy(tr):
    storageCollection = getExperimentForUpdate("/ELN_SETTINGS/STORAGES/STORAGES_COLLECTION", "COLLECTION", tr)
    storageN2 = getSampleForUpdate("/ELN_SETTINGS/N2","STORAGE", tr)
    storageN2.setExperiment(storageCollection)
    storageN2.setPropertyValue("NAME", "N2")
    storageN2.setPropertyValue("ROW_NUM", "10")
    storageN2.setPropertyValue("COLUMN_NUM", "10")
    storageN2.setPropertyValue("BOX_NUM", "9999")
    storageN2.setPropertyValue("STORAGE_SPACE_WARNING", "80")
    storageN2.setPropertyValue("BOX_SPACE_WARNING", "80")
    storageN2.setPropertyValue("STORAGE_VALIDATION_LEVEL", "BOX_POSITION")
    storage180 = getSampleForUpdate("/ELN_SETTINGS/MINUS140_R5.38","STORAGE", tr)
    storage180.setExperiment(storageCollection)
    storage180.setPropertyValue("NAME", "-140°C R5.38")
    storage180.setPropertyValue("ROW_NUM", "1")
    storage180.setPropertyValue("COLUMN_NUM", "1")
    storage180.setPropertyValue("BOX_NUM", "9999")
    storage180.setPropertyValue("STORAGE_SPACE_WARNING", "80")
    storage180.setPropertyValue("BOX_SPACE_WARNING", "80")
    storage180.setPropertyValue("STORAGE_VALIDATION_LEVEL", "BOX_POSITION")

        
        