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
            try:
                if not entity.isInOpenBIS(tr):
                    entity.write(tr)
                    #print entity.getIdentifier(tr) + " - Updated"
                else:
                    addNotMigratedEntity(adaptor.__class__.__name__, entity.getIdentifier(tr), "Already in openBIS")
            except Exception, error:
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
    for propertyCode, propertyValue in properties.iteritems():
            if propertyCode.startswith("+"):
                continue;
            if propertyCode.startswith("-"):
                continue
            
            propertyDefinition = definitions.getPropertyDefinitionByCode(definition, propertyCode)
            if propertyValue is not None:
                propertyValue =  unicode(propertyValue) 

            if propertyDefinition is not None and propertyDefinition[3] == DataType.TIMESTAMP and propertyValue is not None:
                date_val = datetime.strptime(propertyValue, "%Y")
                propertyValue = datetime.strftime(date_val, "%Y-%m-%d")
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
                    if len(codeToUse) > 60:
                        codeToUse = codeToUse[:50]
                    #Uses new vocabulary term
                    newTerm = definitionsVoc.createVocabularyTerm(tr, propertyDefinition[4], codeToUse, labelToUse)
                    propertyValue = newTerm.getCode()
                    print "* WARNING ENTITY [" + entity.getCode() + "]: for Vocabulary [" + propertyDefinition[4] + "], found value not in list: [" + repr(labelToUse) + "]. Created new term with code [" + codeToUse + "]"

            if propertyDefinition[2] == "pKW Number":
                propertyValue = "pKW" + propertyValue

            if propertyDefinition[2] == "UC Number":
                if re.search("US", propertyValue):
                    propertyValue =propertyValue.replace("US", "UC").strip(" ")                             
            
            if propertyDefinition is not None: #Sometimes special fields are added for other purposes, these should not be set
                entity.setPropertyValue(propertyCode, propertyValue)




            if propertyDefinition is not None and propertyDefinition[3] == DataType.CONTROLLEDVOCABULARY and propertyValue is not None:
                possiblePropertyValue = definitionsVoc.getVocabularyTermCodeForVocabularyAndTermLabel(propertyDefinition[4], propertyValue)
                if possiblePropertyValue is not None:
                    propertyValue = possiblePropertyValue.strip(" ")
                else:
                    print "MISSING VALUE for: ", propertyDefinition[0], ": VALUE ", propertyValue, "POSS VALUE:",  possiblePropertyValue
                
       
            #         if propertyDefinition[0] =="COMPANY" and propertyValue is not None:   
            #             if propertyValue == "Sgmal-Aldrich":
            #                 entity.setPropertyValue("COMPANY", "SIGMA-ALDRICH")
            #             elif propertyValue =="fluka":
            #                  entity.setPropertyValue("COMPANY", "FLUKA")
            #             elif propertyValue =="Bio rad":
            #                  entity.setPropertyValue("COMPANY", "BIO-RAD")
            #             elif propertyValue =="merk":
            #                  entity.setPropertyValue("COMPANY", "MERCK")
            #             elif propertyValue =="JT Baker":
            #                  entity.setPropertyValue("COMPANY", "JTBAKER")
            #             elif propertyValue =="Sigma":
            #                  entity.setPropertyValue("COMPANY", "SIGMA-ALDRICH")
            #             elif propertyValue =="sigma":
            #                  entity.setPropertyValue("COMPANY", "SIGMA-ALDRICH")                         
            #             elif propertyValue =="BioChemica":
            #                  entity.setPropertyValue("COMPANY", "BIOCHEMICA")
            #             elif propertyValue =="molecular Probes":
            #                  entity.setPropertyValue("COMPANY", "MOLECULAR_PROBES")
            #             elif propertyValue =="SIGMA":
            #                  entity.setPropertyValue("COMPANY", "SIGMA-ALDRICH")
            #             elif propertyValue =="Invitrogen\r\r\r":
            #                  entity.setPropertyValue("COMPANY", "INVITROGEN")
            #             elif propertyValue =="Sigma Aldrich":
            #                  entity.setPropertyValue("COMPANY", "SIGMA-ALDRICH")
            #             elif propertyValue =="pierce":
            #                  entity.setPropertyValue("COMPANY", "PIERCE")
            #             elif propertyValue =="Merck":
            #                  entity.setPropertyValue("COMPANY", "MERCK")
            #             elif propertyValue =="calbiochem":
            #                  entity.setPropertyValue("COMPANY", "CALBIOCHEM")
            #             elif propertyValue =="Biorad":
            #                  entity.setPropertyValue("COMPANY", "BIO-RAD")
            #             elif propertyValue =="bd":
            #                  entity.setPropertyValue("COMPANY", "BD")
            #             elif propertyValue =="AppliChem":
            #                  entity.setPropertyValue("COMPANY", "APPLICHEM")
            #             elif propertyValue =="?":
            #                  entity.setPropertyValue("COMPANY", "UNKNOWN")
            #             else:
            #                  entity.setPropertyValue("COMPANY", propertyValue)                            
                    #===============================================================================
                    # if propertyDefinition[0] =="LAB_MEMBERS" and propertyValue is not None:   
                    #     print "LAB_MEMBERS", propertyDefinition[0], propertyValue 
                    #     if propertyValue == "A.C. Ström" || propertyValue == "AC. Ström" || propertyValue == "A-C. Strom":
                    #          entity.setPropertyValue("AC_STROM",   "A.C. Strom")
                    #     elif propertyValue =="anneke":
                    #          entity.setPropertyValue("A_HIBBEL",   "A. Hibbel")
                    #     elif propertyValue =="B Zeitler" ||  propertyValue =="B.Zeitler":
                    #          entity.setPropertyValue("B_ZEITLER",   "B. Zeitler")
                    #     elif propertyValue =="Ben Monpetit" || propertyValue == "B.Montpetit":
                    #          entity.setPropertyValue("B_MONTPETIT",   "B. Montpetit")                                                      
                    #     elif propertyValue =="C.Brune" || propertyValue =="Christiane Brune" :
                    #          entity.setPropertyValue("C_BRUNE",   "C. Brune")                      
                    #     elif propertyValue =="C.Derrer" || propertyValue =="Carina":
                    #          entity.setPropertyValue(v)
                    #      elif propertyValue =="Carmen, Elisa" || propertyValue =="E.Dultz" || propertyValue =="Elisa" || propertyValue =="Elisa Dultz":
                    #          entity.setPropertyValue("ELISA_DULTZ",   "E. Dultz")                          
                    #      elif propertyValue =="anneke":
                    #          entity.setPropertyValue("A_HIBBEL",   "A. Hibbel")                          
                    #     elif propertyValue =="anneke":
                    #          entity.setPropertyValue("A_HIBBEL",   "A. Hibbel")                           
                    #===============================================================================
                                                                      
                    if propertyDefinition[0] =="BACTERIAL_STRAIN" and propertyValue is not None:   
                        print "BAC STRAIN", propertyDefinition[0], propertyValue 
                        if propertyValue == "XL10 Gold":
                             entity.setPropertyValue("BACTERIAL_STRAIN", "XL10-GOLD")
                        elif propertyValue =="STABL2":
                             entity.setPropertyValue("BACTERIAL_STRAIN", "STBL2")
                        elif propertyValue =="E. coli":
                             entity.setPropertyValue("BACTERIAL_STRAIN", "E_COLI")                                 
                        elif propertyValue == "?":
                             entity.setPropertyValue("BACTERIAL_STRAIN", "UNKNOWN")
                        else:
                  #We rely on the Add Hock Terms if is None, since there is no API we create a new one
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
                            
                    elif propertyDefinition[0] =="DRUG_RES" and propertyValue is not None:
                        print  "DRUG RES is: ", propertyDefinition[0], "=",  propertyValue
                        if propertyValue == "CAM ":
                            print "CAM IS:", propertyValue
                            entity.setPropertyValue("DRUG_RES", "CAM")
                        else:
                             entity.setPropertyValue("DRUG_RES", propertyValue)      

            if propertyDefinition is not None: #Sometimes special fields are added for other purposes, these should not be set
                entity.setPropertyValue(propertyCode, propertyValue)


                                
                 
def setPlasmidParents(tr, definition, entity, properties):
    for propertyCode, propertyValue in properties.iteritems():
       
            propertyDefinition = definitions.getPropertyDefinitionByCode(definition, propertyCode)
            if propertyValue is not None and propertyDefinition[0] == "PCR_3_OLIGO" or propertyDefinition[0] == "PCR_5_OLIGO":
                propertyValue =  unicode(propertyValue)
                if re.match ("UC# ", propertyValue) and not re.search("/", propertyValue):
                    parentCode = "/MATERIALS/"+ propertyValue.replace("UC# ", "UC").strip(" ")
                    currentParentsList = entity.getParentSampleIdentifiers()
                    currentParentsList.add(parentCode)
                    entity.setParentSampleIdentifiers(currentParentsList)
                elif re.match ("UC#", propertyValue) and not re.search("/", propertyValue):
                    parentCode= "/MATERIALS/"+ propertyValue.replace("UC#", "UC").strip(" ")
                    currentParentsList = entity.getParentSampleIdentifiers()
                    currentParentsList.add(parentCode)
                    entity.setParentSampleIdentifiers(currentParentsList)
                elif re.match ("UC #", propertyValue) and not re.search("/", propertyValue):
                    parentCode= "/MATERIALS/"+ propertyValue.replace("UC #", "UC").strip(" ")
                    currentParentsList = entity.getParentSampleIdentifiers()
                    currentParentsList.add(parentCode)
                    entity.setParentSampleIdentifiers(currentParentsList)
                elif re.match ("UC ", propertyValue) and not re.search("/", propertyValue):
                    parentCode="/MATERIALS/"+  propertyValue.replace("UC ", "UC").strip(" ")
                    currentParentsList = entity.getParentSampleIdentifiers()
                    currentParentsList.add(parentCode)
                    entity.setParentSampleIdentifiers(currentParentsList)
                elif re.match ("UC1388cc", propertyValue):
                    parentCode="/MATERIALS/"+  propertyValue.replace("UC1388cc", "UC1388").strip(" ")
                    currentParentsList = entity.getParentSampleIdentifiers()
                    currentParentsList.add(parentCode)
                    entity.setParentSampleIdentifiers(currentParentsList) 
                elif re.match ("UC2396c", propertyValue):
                    parentCode="/MATERIALS/"+  propertyValue.replace("UC2396c", "UC2396").strip(" ")
                    currentParentsList = entity.getParentSampleIdentifiers()
                    currentParentsList.add(parentCode)
                    entity.setParentSampleIdentifiers(currentParentsList)                                               
                elif re.match ("UC", propertyValue) and not re.search("/", propertyValue):
                    parentCode= "/MATERIALS/"+ propertyValue.strip(" ")
                    currentParentsList = entity.getParentSampleIdentifiers()
                    currentParentsList.add(parentCode)
                    entity.setParentSampleIdentifiers(currentParentsList)
                elif re.match ("CH", propertyValue):
                    parentCode="/MATERIALS/"+ propertyValue.strip(" ")
                    currentParentsList = entity.getParentSampleIdentifiers()
                    currentParentsList.add(parentCode)
                    entity.setParentSampleIdentifiers(currentParentsList)
                elif re.match ("Ch", propertyValue):
                    parentCode="/MATERIALS/"+ propertyValue.replace("Ch", "CH").strip(" ")
                    currentParentsList = entity.getParentSampleIdentifiers()
                    currentParentsList.add(parentCode)
                    entity.setParentSampleIdentifiers(currentParentsList)
                else:
                    print  "PARENT NOT SET: ", propertyDefinition[0] , propertyValue
             
 


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
    experimentType ="MATERIAL"
    if experimentIdentifier not in experimentCache:
        print "Cache failed " + experimentIdentifier + ":" + str(experimentType)
        experiment = tr.getExperimentForUpdate(experimentIdentifier)
        if experiment is None and experimentType is not None:
            print "Cache Create " + experimentIdentifier + ":" + str(experimentType)
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
             if sampleType == "ANTIBODY":
                 experiment = getExperimentForUpdate("/MATERIALS/REAGENTS/ANTIBODY_COLLECTION", sampleType, tr)
             elif sampleType == "STRAIN":
                  experiment = getExperimentForUpdate("/MATERIALS/STRAINS/YEAST_STRAINS", sampleType, tr)              
             elif sampleType == "PLASMID":
                  experiment = getExperimentForUpdate("/MATERIALS/PLASMIDS/PLASMID_COLLECTION_1", sampleType, tr)              
             elif sampleType == "CHEMICAL":
                  experiment = getExperimentForUpdate("/MATERIALS/REAGENTS/CHEMICAL_COLLECTION", sampleType, tr) 
             elif sampleType == "RESTRICTION_ENZYME":
                experiment = getExperimentForUpdate("/MATERIALS/REAGENTS/RESTRICTION_ENZYME_COLLECTION", sampleType, tr)                  
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
         return None
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
        result = preparedStatement.executeQuery()
        
        while result.next():
            values = {}
            for property in self.definition:
                if property[0].startswith("+"):
                    pass #Do Nothing
                elif property[0]=="ANNOTATIONS_STATE":
                    values[property[0]] =""
                else:
                    propertyCode = property[0];
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
                elif propertyCode.startswith("-"):
                    propertyCode = propertyCode[1:];
                
                if not isFirst:
                    fields = fields + ", ";
                
                fields = fields + field[2];
                fieldsNames.append(propertyCode);
                isFirst = False;
            
            for entity in self.entities:
                entityId = entity.values[self.selectQueryRepetitionsId];
                if entityId is not None:
                    perEntityQuery = "SELECT " + fields + " FROM " + self.selectQueryRepetitions + "= '" + entityId + "'";
                    print perEntityQuery
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
            code = self.getIdentifier(tr)
            if (code is not None) and (' ' not in code):
                if self.isSampleCacheable():
                    sampleID2Sample[self.values["NAME"]] = self.values
                sample = getSampleForUpdate("/MATERIALS/"+code, None, tr)
                return False
            else:
                print "* ERROR [" + str(code) + "] - Invalid Code found for '" + self.__class__.__name__ + "'"
                raise Exception('Invalid Code found ' + str(code))


# class FMEntityMultipleValuesOpenBISDTO(OpenBISDTO):
#     def getIdentifier(self, tr):
#         return self.values["*CODE"]
#     
#     def write(self, tr):
#         sample = getSampleForUpdate("/MATERIALS/"+self.values["*CODE"], None, tr)
#         print "* INFO MultipleValueses size: " + str(len(self.values["*MultipleValuesESLIST"]))
#         #Delete old MultipleValueses
#         for MultipleValuesNum in range(1, definitions.numberOfRepetitions+1):
#             for propertyCode in definitions.getRepetitionPropertyCodes():
#                 sample.setPropertyValue(propertyCode + "_" + str(MultipleValuesNum), None)
#          
#         #Add new MultipleValueses
#         MultipleValuesNum = 0
#         for MultipleValues in self.values["*MultipleValuesESLIST"]:
#             MultipleValuesNum += 1
#             for propertyCode, propertyValue in MultipleValues.iteritems():
#                 if propertyValue is not None:
#                     propertyValue = unicode(propertyValue)
#                     sample.setPropertyValue(propertyCode + "_" + str(MultipleValuesNum), propertyValue)
#  
#         #1. Setting MAT
#         buildedPropertyValue = ""
#         mat = sample.getPropertyValue("MAT")
#         if mat is not None:
#             if re.search("a", mat):
#                 mat = mat.replace("a", u"\u03B1")
#             if re.search("A", mat):
#                 mat = mat.replace("A", "a")                
#             buildedPropertyValue = buildedPropertyValue + "MAT " + unicode(mat) + " "
#          
#         #2. Adding unmarked mutations
#         for listItemsMap in self.values["*MultipleValuesESLIST"]:
#             unmarkedMutation = listItemsMap["UNMARKED_MUTATIONS"]
#             if unmarkedMutation is not None:
#                 buildedPropertyValue = buildedPropertyValue + unicode(unmarkedMutation + " ");
#          
#         #3. Add pairs of disruptions and markers
#         for listItemsMap in self.values["*MultipleValuesESLIST"]:
#             disruptions = listItemsMap["DISRUPTIONS"]
#             markers = listItemsMap["MARKERS"]
#             #print "disruptions, markers", disruptions, markers
#             if (disruptions is not None and disruptions is not "") or (markers is not None and markers is not ""):
#                 if disruptions is None:
#                     disruptions = ""
#                 if markers is None:
#                     markers = ""
#                 buildedPropertyValue = buildedPropertyValue + unicode(disruptions) + "::" + unicode(markers) + " "
#         print repr("GENOTYPE" + unicode(buildedPropertyValue))
#         sample.setPropertyValue("GENOTYPE", buildedPropertyValue)
#     
#     def isMultipleValuesPressent(self, MultipleValuesSignature, tr):
#         sample = getSampleForUpdate("/MATERIALS/"+self.values["*CODE"], None, tr)
#         if sample is not None:
#             for MultipleValuesNum in range(1, definitions.numberOfRepetitions+1):
#                 storedSignature = "";
#                 for propertyCode in definitions.getRepetitionPropertyCodes():
#                     propertyValue = sample.getPropertyValue(propertyCode + "_" + str(MultipleValuesNum))
#                   
#                     if propertyValue is not None:
#                         propertyValue = unicode(propertyValue)
#                         storedSignature += propertyValue 
#                 if storedSignature == MultipleValuesSignature:
#                     #print "Found MultipleValues " + storedSignature.encode('ascii', 'ignore')
#                     return True
#         return False
#     
#     def isInOpenBIS(self, tr):
#         return False


##
## Antibodies
##

class AntibodyAdaptor(FileMakerEntityAdaptor):
    
    def init(self):
        self.selectQuery = "SELECT * FROM \"Weis Lab  Antibodies\""
        self.definition = definitions.antibodyDefinition
        FileMakerEntityAdaptor.init(self)
    
    def addEntity(self, values):
        self.entities.append(AntibodyOpenBISDTO(values, self.definition))
       
        
class AntibodyOpenBISDTO(FMOpenBISDTO):
    def isSampleCacheable(self):
        return False
        
    def write(self, tr):
        if self.values["REF_NUM"] is not None:

            code = "AB" + self.values["REF_NUM"]
            if code is not None:
                sample = getSampleForUpdate("/MATERIALS/"+code,"ANTIBODY", tr)
                setEntityProperties(tr, self.definition, sample, self.values);
        else:
            print  "Sample ", self.values["REF_NUM"], " does not have a REF_NUM"
    
    def getIdentifier(self, tr):
        if self.values["REF_NUM"]:
            code = "AB"+ self.values["REF_NUM"]
            return code
    


##
## Strains
##
class StrainAdaptor(FileMakerEntityAdaptor):
    def init(self):
        self.definition = definitions.strainDefinition
        self.selectQuery = "SELECT * FROM \"Weis Lab Yeast Strains\"";
        
        #Extra Repeats Logic
        self.definitionRepeats = definitions.strainDefinitionRepetitions
        self.selectQueryRepetitions = " \"Weis Lab Yeast Strains\" WHERE \"KWY number\" ";
        self.selectQueryRepetitionsId = "NAME"
        #
        
        FileMakerEntityAdaptor.init(self)
    
    def addEntity(self, values):
        self.entities.append(StrainOpenBISDTO(values, self.definition))
        
class StrainOpenBISDTO(FMOpenBISDTO):
    def write(self, tr):
        print str(self.values)
        #Normal Definitions
        code = self.values["NAME"]
        sample = getSampleForUpdate("/MATERIALS/"+code,"STRAIN", tr)
        setEntityProperties(tr, self.definition, sample, self.values)
        #Add additional properties, merging the repetition properties
        # TO-DO Tomorrow
        #
        
            
    def getIdentifier(self, tr):
        return self.values["NAME"]

##
## Plasmids
##
class PlasmidAdaptor(FileMakerEntityAdaptor):
    
    def init(self):
        self.selectQuery = "SELECT * FROM \"Weis Lab Plasmids\""
        self.definition = definitions.plasmidDefinition
        FileMakerEntityAdaptor.init(self)
    
    def addEntity(self, values):
        self.entities.append(PlasmidOpenBISDTO(values, self.definition))
        
class PlasmidOpenBISDTO(FMOpenBISDTO):
    def write(self, tr):
        code = "PKW" + self.values["NAME"]
        if code is not None:
            sample = getSampleForUpdate("/MATERIALS/"+code,"PLASMID", tr)
            setEntityProperties(tr, self.definition, sample, self.values)
            setPlasmidParents(tr, self.definition, sample, self.values)
            #print "SETPARENTS", setPlasmidParents(tr, self.definition, sample, self.values)
    
    def getIdentifier(self, tr):
        code = "PKW" +self.values["NAME"]
        return code



##
## Oligos
##
class OligoAdaptor(FileMakerEntityAdaptor):
    
    def init(self):
        self.selectQuery = "SELECT * FROM \"WLO\""
        self.definition = definitions.oligoDefinition
        FileMakerEntityAdaptor.init(self)
    
    def addEntity(self, values):
        self.entities.append(OligoOpenBISDTO(values, self.definition))
        
class OligoOpenBISDTO(FMOpenBISDTO):
    def write(self, tr):
        code = self.values["NAME"]
        if code is not None:
            if re.search("CHCH", code): 
                code = code.replace("CHCH", "CH")                
                sample = getSampleForUpdate("/MATERIALS/"+code,"OLIGO", tr)
            elif re.search("CH", code):
                sample = getSampleForUpdate("/MATERIALS/"+code,"OLIGO", tr)                
            elif re.search("US", code):
                code = code.replace("US", "UC")
                sample = getSampleForUpdate("/MATERIALS/"+code,"OLIGO", tr)
            setEntityProperties(tr, self.definition, sample, self.values);
    
    def getIdentifier(self, tr):
        code = self.values["NAME"]
        return code



##
## Chemical
##
class ChemicalAdaptor(FileMakerEntityAdaptor):
    
    def init(self):
        self.selectQuery = "SELECT * FROM \"Table\""
        self.definition = definitions.chemicalDefinition
        FileMakerEntityAdaptor.init(self)
    
    def addEntity(self, values):
        self.entities.append(ChemicalOpenBISDTO(values, self.definition))
       
        
class ChemicalOpenBISDTO(FMOpenBISDTO):
    def isSampleCacheable(self):
        return False
        
    def write(self, tr):
        code = "CHEM" + self.values["ID"]
        if code is not None:
            sample = getSampleForUpdate("/MATERIALS/"+code,"CHEMICAL", tr)
            setEntityProperties(tr, self.definition, sample, self.values);
    
    def getIdentifier(self, tr):
        code = "CHEM" + self.values["ID"]
        return code

##
## Restriction enzymes
##
class EnzymeAdaptor(FileMakerEntityAdaptor):
    def init(self):
        self.selectQuery = "SELECT * FROM \"Restriction_Enzymes\""
        
        self.definition = definitions.RestrictionEnzymeDefinition
        FileMakerEntityAdaptor.init(self)
    
    def addEntity(self, values):
        self.entities.append(EnzymeOpenBISDTO(values, self.definition))
        
class EnzymeOpenBISDTO(FMOpenBISDTO):
    
    def isSampleCacheable(self):
            return False
    
    def write(self, tr):
        code = self.getIdentifier(tr);
        print "New Code To Be Written: " + code
        sample = getSampleForUpdate("/MATERIALS/"+code,"RESTRICTION_ENZYME", tr)
        setEntityProperties(tr, self.definition, sample, self.values);
    
    def getIdentifier(self, tr):
        if "CODE" not in self.values:
            self.values["CODE"] = "RE" + getNextGlobalSequence("RE");
            print "New Code Generated: " + self.values["CODE"]
        return self.values["CODE"];


        
fmConnString = "jdbc:filemaker://127.0.0.1/"
#fmConnString = "jdbc:filemaker://fmsrv.ethz.ch/"
fmUser= "admin"
fmPass = "nucleus"



adaptors = [ 
             #EnzymeAdaptor(fmConnString, fmUser, fmPass, "Weis_Restriction_enzymes"),
             #ChemicalAdaptor(fmConnString, fmUser, fmPass, "Weis_Chemicals"),
             #AntibodyAdaptor(fmConnString, fmUser, fmPass, "Weis _Antibodies"),
             #OligoAdaptor(fmConnString, fmUser, fmPass, "Weis_Oligos"),
             #PlasmidAdaptor(fmConnString, fmUser, fmPass, "Weis_Plasmids"),
             StrainAdaptor(fmConnString, fmUser, fmPass, "Weis_Yeast_Strains")
             ]
                       
            
def createDataHierarchy(tr):
    inventorySpace = tr.getSpace("MATERIALS")
    if inventorySpace == None:
        tr.createNewSpace("MATERIALS", None)
        tr.createNewProject("/MATERIALS/REAGENTS")
        tr.createNewProject("/MATERIALS/POLYNUCLEOTIDES")
        tr.createNewProject("/MATERIALS/PLASMIDS")
        tr.createNewProject("/MATERIALS/STRAINS")        
        
        