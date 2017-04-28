# some_file.py
import sys
import traceback
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

def getStringValueOrNone(map, key):
    if (key in map) and (map[key] is not None) and (map[key] is not ""):
        return map[key];
    else:
        return None;

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
        
        if adaptor.__class__.__name__ == "PlasmidParentAdaptor":
            print "check hack, dont do please"
            adaptor.init();

        while adaptor.next():
            entity = adaptor.getEntity()
            print "* ENTITY [" + str(entity.getIdentifier(tr)) + "]"
            try:
                if not entity.isInOpenBIS(tr):
                    entity.write(tr)
                else:
                    addNotMigratedEntity(adaptor.__class__.__name__, entity.getIdentifier(tr), "Already in openBIS")
            except Exception, error:
                    print traceback.format_exc()
                    addNotMigratedEntity(adaptor.__class__.__name__, entity.getIdentifier(tr), str(error.args))
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
    for origPropertyCode, propertyValue in properties.iteritems():
            #print repr("setEntityProperties " + origPropertyCode + " : " + unicode(propertyValue))
            propertyCode = origPropertyCode;
            if propertyCode.startswith("+"):
                propertyCode = propertyCode[1:];
            elif propertyCode.startswith("-"):
                continue
            
            propertyDefinition = definitions.getPropertyDefinitionByCode(definition, origPropertyCode)
            #print "Prop definition ", propertyDefinition
            #print "PROP VALUE ", propertyValue
            if propertyDefinition is not None and propertyValue is not None:
                propertyValue =  unicode(propertyValue) 
 
            if propertyDefinition is not None and propertyDefinition[3] == DataType.REAL and propertyValue is not None:
                if propertyValue =="?":
                    propertyValue=""

            if propertyDefinition is not None and propertyDefinition[3] == DataType.CONTROLLEDVOCABULARY and propertyValue is not None:
                possiblePropertyValue = definitionsVoc.getVocabularyTermCodeForVocabularyAndTermLabel(propertyDefinition[4], propertyValue)
                if possiblePropertyValue is not None:
                    propertyValue = possiblePropertyValue
                else:  #We rely on the Add Hock Terms if is None, since there is no API we create a new one
                    #Create new vocabulary term
                    codeToUse = re.sub(r'\W+','_',propertyValue)
                    labelToUse = propertyValue
                    if len(codeToUse) is 0:
                        codeToUse = "None" + str(random.random())
                    if len(codeToUse) > 50:
                        codeToUse = codeToUse[:50]
                    #Uses new vocabulary term
                    newTerm = definitionsVoc.createVocabularyTerm(tr, propertyDefinition[4], codeToUse, labelToUse)
                    propertyValue = newTerm.getCode()
                    #print "NEW CODE", propertyValue
                    print "* WARNING ENTITY [" + entity.getCode() + "]: for Vocabulary [" + propertyDefinition[4] + "], found value not in list: [" + repr(labelToUse) + "]. Created new term with code [" + codeToUse + "]"


            if propertyDefinition is not None: #Sometimes special fields are added for other purposes, these should not be set
                entity.setPropertyValue(propertyCode, propertyValue)



            if propertyDefinition is not None: #Sometimes special fields are added for other purposes, these should not be set
                entity.setPropertyValue(propertyCode, propertyValue)



##
## Generic Pattern
##
class EntityAdaptor:
    entities = None
    entitiesIdx = None
    definition = None
    definitionRepeats = None
    
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

def getNextGlobalSequence(id, startingAt):
    currentSequence = None;
    
    if id in globalSequences:
        currentSequence = globalSequences[id]; #Get existing sequence
    else:
        if startingAt is not None:
            currentSequence = startingAt;
        else:
            currentSequence = 0; # Create a new one

    #Advance and store new step on the sequence
    currentSequence = currentSequence+1;
    globalSequences[id] = currentSequence;
    
    #Return the new sequence number
    return str(currentSequence);

def getExperimentForUpdate(experimentIdentifier, experimentType, tr):
    experimentType ="MATERIALS"
    if experimentIdentifier not in experimentCache:
        print "Cache failed " + experimentIdentifier + ":" + str(experimentType)
        experiment = tr.getExperimentForUpdate(experimentIdentifier)
        if experiment is None and experimentType is not None:
            print "Cache Create " + experimentIdentifier + ":" + str(experimentType)
            experiment = tr.createNewExperiment(experimentIdentifier, experimentType)
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
             if sampleType == "STRAIN":
                  experiment = getExperimentForUpdate("/MATERIALS/STRAINS/STRAIN_COLLECTION_1", sampleType, tr)              
             elif sampleType == "DNA":
                  experiment = getExperimentForUpdate("/MATERIALS/DNA/DNA_COLLECTION_1", sampleType, tr)                         
             elif sampleType == "OLIGO":
                  experiment = getExperimentForUpdate("/MATERIALS/POLYNUCLEOTIDES/OLIGO_COLLECTION_1", sampleType, tr)                  
             sample = tr.createNewSample(sampleIdentifier, sampleType)
             sample.setExperiment(experiment)
         if sample is not None:
             sampleCache[sampleIdentifier] = sample
    else:
        pass
        #print "Cache hit " + sampleIdentifier + ":" + str(sampleType)
        
    if sampleIdentifier not in sampleCache:
         raise None
    else:
         return sampleCache[sampleIdentifier]

class FileMakerEntityAdaptor(EntityAdaptor):
    connection = None
    selectQuery = None
    selectQueryRepetitions = None;
    selectQueryRepetitionsId = None;
        
    def __init__(self, fileMakerConnString, fileMakerUser, fileMakerPass, db):
        Class.forName("com.filemaker.jdbc.Driver").newInstance()
        self.connection = DriverManager.getConnection(fileMakerConnString+db,fileMakerUser, fileMakerPass)
    
    def init(self):
        EntityAdaptor.init(self)
        
        # Default Query (All adaptors should have it)
        preparedStatement = self.connection.prepareStatement(self.selectQuery)
        #print "EXECUTED QUERY: " + self.selectQuery
        result = preparedStatement.executeQuery()
        
        while result.next():
            values = {}
            for property in self.definition:
                if property[0].startswith("+"):
                    pass #Do Nothing
                elif property[0]=="ANNOTATIONS_STATE":
                    values[property[0]] = "";
                else:
                    propertyCode = property[0];
                    #print "propertyCode IS: ", propertyCode
                    propertyValue = result.getString(property[2]);
                    if property[0].startswith("+"):
                        propertyCode = property[0][1:];
                    if property[0].startswith("-"):
                        propertyCode = property[0][1:];
                    values[propertyCode] = result.getString(property[2])
            self.addEntity(values)
        result.close()
        preparedStatement.close()
        # Repetitions Query (Optional)
        
        #Extra Repeats Logic
        if self.definitionRepeats is not None:
            fields = "";
            fieldsNames = [];
            isFirst = True;
            for field in self.definitionRepeats:
                propertyCode = field[0];
                if propertyCode.startswith("+"):
                    continue
                
                if not isFirst:
                    fields = fields + ", ";
                
                fields = fields + field[2];
                fieldsNames.append(propertyCode);
                isFirst = False;
            
            for entity in self.entities:
                entityId = entity.values[self.selectQueryRepetitionsId];
                if entityId is not None:
                    perEntityQuery = "SELECT " + fields + " FROM " + self.selectQueryRepetitions + "= '" + entityId + "'";
                    #print "EXECUTED QUERY: " + perEntityQuery
                    preparedStatement = self.connection.prepareStatement(perEntityQuery)
                    result = preparedStatement.executeQuery()
                    
                    columnCount = result.getMetaData().getColumnCount();
                    while result.next():    
                        for cIdx in range(columnCount):
                            if cIdx is not 0:
                                columName = result.getMetaData().getColumnName(cIdx);
                                fieldName = fieldsNames[cIdx];
                                entity.values[fieldName] = result.getString(cIdx);
                    result.close()
                    preparedStatement.close()
        #

##
## Customer specific logic: different sample types
##
class FMOpenBISDTO(OpenBISDTO):
        def isSampleCacheable(self):
            return True
        
        def isInOpenBIS(self, tr):
            return False
            # code = self.getIdentifier(tr)
            # if (code is not None) and (' ' not in code):
            #     if self.isSampleCacheable():
            #         sample = getSampleForUpdate("/MATERIALS/"+code, None, tr)
            #         if (self.values["NAME"] is not None and len(self.values["NAME"]) > 0 and self.values["NAME"] not in sampleID2Sample):
            #             print "Cached Sample with name: '" + self.values["NAME"] + "' for '" + "/MATERIALS/" + code + "' in dict with size: " + str(len(sampleID2Sample))
            #             parentName = unicode(self.values["NAME"], "utf-8");
            #             sampleID2Sample[parentName] = sample
            #     return False
            # else:
            #     print "* ERROR [" + str(code) + "] - Invalid Code found for '" + self.__class__.__name__ + "'"
            #     raise Exception('Invalid Code found ' + str(code))


##
## Strains
##

globalStrainsCodeCheck = {};

class StrainAdaptor(FileMakerEntityAdaptor):
    def init(self):
        self.selectQuery = "SELECT * FROM \"BCstrains_ETH\""
        
        self.definition = definitions.strainDefinition
        FileMakerEntityAdaptor.init(self)
    
    def addEntity(self, values):
        self.entities.append(StrainOpenBISDTO(values, self.definition))
 
class StrainOpenBISDTO(FMOpenBISDTO):
    code = None;

    def isSampleCacheable(self):
            return False    

    def write(self, tr):
        code = self.getIdentifier(tr);

        sample = getSampleForUpdate("/MATERIALS/"+code,"STRAIN", tr)
        setEntityProperties(tr, self.definition, sample, self.values)
        

    def getIdentifier(self, tr):
        if self.code is not None:
            return self.code;
        elif (self.values["PLASMID_ID"] is not None and self.values["PLASMID_ID"] not in globalStrainsCodeCheck):
            self.code = "BC" + self.values["PLASMID_ID"]
            globalStrainsCodeCheck[self.values["PLASMID_ID"]] = True
        else:
            nextNum = getNextGlobalSequence("BC", 0)
            self.code = "BC_missing_" + nextNum;
            globalStrainsCodeCheck[nextNum] = True
        return self.code



##
## DNA
##



globalDNACodeCheck = {};

class DNAAdaptor(FileMakerEntityAdaptor):
    def init(self):
        self.selectQuery = "SELECT * FROM \"Plasmids\""
        
        self.definition = definitions.DNADefinition
        FileMakerEntityAdaptor.init(self)
    
    def addEntity(self, values):
        self.entities.append(DNAOpenBISDTO(values, self.definition))
 
class DNAOpenBISDTO(FMOpenBISDTO):
    code = None;

    def isSampleCacheable(self):
            return False    

    def write(self, tr):
        code = self.getIdentifier(tr);

        sample = getSampleForUpdate("/MATERIALS/"+code,"DNA", tr)
        setEntityProperties(tr, self.definition, sample, self.values)
        

    def getIdentifier(self, tr):
        if self.code is not None:
            return self.code;
        elif (self.values["PLASMID_ID"] is not None and self.values["PLASMID_ID"] not in globalDNACodeCheck):
            self.code = "DNA" + self.values["PLASMID_ID"]
            globalDNACodeCheck[self.values["PLASMID_ID"]] = True
        else:
            nextNum = getNextGlobalSequence("DNA", 0)
            self.code = "DNA_missing_" + nextNum;
            globalDNACodeCheck[nextNum] = True
        return self.code



##
## Oligos
##
class OligoAdaptor(FileMakerEntityAdaptor):
    def init(self):
        self.selectQuery = "SELECT * FROM \"Oligo_Beat\""
        self.definition = definitions.oligoDefinition
        FileMakerEntityAdaptor.init(self)
    
    def addEntity(self, values):
        self.entities.append(OligoOpenBISDTO(values, self.definition))
        
class OligoOpenBISDTO(FMOpenBISDTO):
    def write(self, tr):
        code = "OLI" + self.values["PRIMER_NUMBER"];
        print "CODE IS:", code
        if code is not None:
            print "/MATERIALS/"+code
            sample = getSampleForUpdate("/MATERIALS/"+code,"OLIGO", tr)
            setEntityProperties(tr, self.definition, sample, self.values)
        else:
            print "OLIGO CODE NOT FOUND! "
    
    def getIdentifier(self, tr):
        code=""
        if (self.values["PRIMER_NUMBER"] is not None):
            code = "OLI" + self.values["PRIMER_NUMBER"]
        else:
            code = "OLI" + getNextGlobalSequence("OLI", None);
        return code



fmConnString = "jdbc:filemaker://127.0.0.1/"
#fmConnString = "jdbc:filemaker://fmsrv.ethz.ch/"
fmUser= "admin"
fmPass = "filegod64+"



adaptors = [
             #OligoAdaptor(fmConnString, fmUser, fmPass, "Christen_Oligo_Collection"),
             #DNAAdaptor(fmConnString, fmUser, fmPass, "Christen_DNA_collection"),
             StrainAdaptor(fmConnString, fmUser, fmPass, "Christen_BCstrains_ETH_2014_1")
             ]





def createDataHierarchy(tr):
    DNAExperiment = tr.getExperiment("/MATERIALS/DNA/DNA_COLLECTION_1")
    if DNAExperiment == None:
        tr.createNewProject("/MATERIALS/STRAINS")
        tr.createNewProject("/MATERIALS/DNA")
        tr.createNewExperiment("/MATERIALS/STRAINS/STRAIN_COLLECTION_1", "MATERIALS")
        tr.createNewExperiment("/MATERIALS/DNA/DNA_COLLECTION_1", "MATERIALS")