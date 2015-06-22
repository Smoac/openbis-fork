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
                    print "EXISTING VALUE:", propertyValue
                else:
                    print "MISSING VALUE FOR:", propertyValue
       
            if propertyDefinition is not None: #Sometimes special fields are added for other purposes, these should not be set
                    if propertyDefinition[0] =="COMPANY":   
                        if propertyValue == "Sgmal-Aldrich":
                            entity.setPropertyValue("COMPANY", "SIGMA-ALDRICH")
                        elif propertyValue =="fluka":
                             entity.setPropertyValue("COMPANY", "FLUKA")
                        elif propertyValue =="Bio rad":
                             entity.setPropertyValue("COMPANY", "BIO-RAD")
                        elif propertyValue =="merk":
                             entity.setPropertyValue("COMPANY", "MERCK")
                        elif propertyValue =="JT Baker":
                             entity.setPropertyValue("COMPANY", "JTBAKER")
                        elif propertyValue =="Sigma":
                             entity.setPropertyValue("COMPANY", "SIGMA-ALDRICH")
                        elif propertyValue =="sigma":
                             entity.setPropertyValue("COMPANY", "SIGMA-ALDRICH")                         
                        elif propertyValue =="BioChemica":
                             entity.setPropertyValue("COMPANY", "BIOCHEMICA")
                        elif propertyValue =="molecular Probes":
                             entity.setPropertyValue("COMPANY", "MOLECULAR_PROBES")
                        elif propertyValue =="SIGMA":
                             entity.setPropertyValue("COMPANY", "SIGMA-ALDRICH")
                        elif propertyValue =="Invitrogen\r\r\r":
                             entity.setPropertyValue("COMPANY", "INVITROGEN")
                        elif propertyValue =="Sigma Aldrich":
                             entity.setPropertyValue("COMPANY", "SIGMA-ALDRICH")
                        elif propertyValue =="pierce":
                             entity.setPropertyValue("COMPANY", "PIERCE")
                        elif propertyValue =="Merck":
                             entity.setPropertyValue("COMPANY", "MERCK")
                        elif propertyValue =="calbiochem":
                             entity.setPropertyValue("COMPANY", "CALBIOCHEM")
                        elif propertyValue =="Biorad":
                             entity.setPropertyValue("COMPANY", "BIO-RAD")
                        elif propertyValue =="bd":
                             entity.setPropertyValue("COMPANY", "BD")
                        elif propertyValue =="AppliChem":
                             entity.setPropertyValue("COMPANY", "APPLICHEM")
                        elif propertyValue =="?":
                             entity.setPropertyValue("COMPANY", "UNKNOWN")
                        else:
                             entity.setPropertyValue("COMPANY", propertyValue)                            
                    else:
                        entity.setPropertyValue(propertyCode, propertyValue)


def setEntityParents(tr, definition, entity, properties):
    for propertyCode, propertyValue in properties.iteritems():
            propertyDefinition = definitions.getPropertyDefinitionByCode(definition, propertyCode)
            if propertyValue is not None:
                propertyValue =  unicode(propertyValue)

            if propertyDefinition[0] == "PCR_3_OLIGO":
                if re.match ("UC", propertyValue):
                    print propertyDefinition[0], propertyValue
                    propertyValue= str.replace("UC", "US", propertyValue)
                    print propertyDefinition[0], propertyValue
                #entity.setParentSampleIdentifiers()            
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
        print "Cache hit " + experimentIdentifier + ":" + str(experimentType)
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
             	experiment = getExperimentForUpdate("/MATERIALS/REAGENTS/ANTIBODIES", sampleType, tr)
             # elif sampleType == "STRAIN":
             # 	experiment = getExperimentForUpdate("/MATERIALS/YEASTS/YEAST_COLLECTION_1", sampleType, tr)              
             elif sampleType == "PLASMID":
              	experiment = getExperimentForUpdate("/MATERIALS/PLASMIDS/PLASMID_COLLECTION_1", sampleType, tr)              
             elif sampleType == "CHEMICAL":
              	experiment = getExperimentForUpdate("/MATERIALS/REAGENTS/CHEMICALS", sampleType, tr) 
             elif sampleType == "RESTRICTION_ENZYME":
                experiment = getExperimentForUpdate("/MATERIALS/REAGENTS/RESTRICTION_ENZYMES", sampleType, tr)              	
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
                if property[0]=="ANNOTATIONS_STATE":
                    values[property[0]] =""
                else:
                    values[property[0]] = result.getString(property[2])
                    
            self.addEntity(values)
        result.close()
        preparedStatement.close()

##
## Customer specific logic: different sample types
##
class FMPeterOpenBISDTO(OpenBISDTO):
        def isSampleCacheable(self):
            return True
        
        def isInOpenBIS(self, tr):
            code = self.getIdentifier(tr)
            if (code is not None) and (' ' not in code):
                if self.isSampleCacheable():
                    sampleID2Sample[self.values["NAME"]] = self.values
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

class FMPeterBoxAdaptor(FileMakerEntityAdaptor):
    selectBoxQuery = None
    entityIdFieldName = None
    entityCodeFieldName = None
    
    def addEntity(self, values):
        self.entities.append(FMPeterEntityBoxOpenBISDTO(values, self.definition))
        
    def init(self):
        #print "Reading boxes for: " + self.__class__.__name__
        emptyBox = 0
        boxes = {}
        EntityAdaptor.init(self)
        preparedStatement = self.connection.prepareStatement(self.selectBoxQuery)
        result = preparedStatement.executeQuery()
        while result.next():
            entityId = unicode(result.getString(self.entityIdFieldName))
            if entityId is not None:
                if entityId in sampleID2Sample:
                    antibodyNumber = sampleID2Sample[entityId][self.entityCodeFieldName]
                    if antibodyNumber is not None:
                        values = {}
                        values["STORAGE_NAME"] = result.getString("location")
                        values["STORAGE_ROW"] = None
                        values["STORAGE_COLUMN"] = None
                        values["STORAGE_BOX_NAME"] = result.getString("box label")
                        values["STORAGE_USER"] = result.getString("frozen by")
                        values["STORAGE_BOX_POSITION"] = result.getString("position")
                        
                        allBoxes = []
                        if antibodyNumber in boxes:
                            allBoxes = boxes[antibodyNumber]
                        else:
                            boxes[antibodyNumber] = allBoxes
                        allBoxes.append(values)
                else:
                    #The antibody is not there. What the *#%$&
                    emptyBox += 1
            else:
                #The antibody is not there. What the *#%$&
                emptyBox += 1
        
        print "- ERROR ADAPTOR Boxes positions with empty entityId for " + self.__class__.__name__ + ":" + str(emptyBox)
        
        for entiyCode, allBoxes in boxes.iteritems():
            self.addEntity({
                        "*CODE" : entiyCode,
                        "*BOXESLIST" : allBoxes
            })
        
        result.close()
        preparedStatement.close()

class FMPeterEntityBoxOpenBISDTO(OpenBISDTO):
    def getIdentifier(self, tr):
        return self.values["*CODE"]
    
    def write(self, tr):
        sample = getSampleForUpdate("/MATERIALS/"+self.values["*CODE"], None, tr)
        print "* INFO Boxes size: " + str(len(self.values["*BOXESLIST"]))
        #Delete old boxes
        for boxNum in range(1, definitions.numberOfStorageGroups+1):
            for propertyCode in definitions.getStorageGroupPropertyCodes():
                sample.setPropertyValue(propertyCode + "_" + str(boxNum), None)
        
        #Add new boxes
        boxNum = 1
        for box in self.values["*BOXESLIST"]:
            boxNum += 1
            for propertyCode, propertyValue in box.iteritems():
                if propertyCode == "STORAGE_NAME":
                    freezerName = definitionsVoc.getVocabularyTermCodeForVocabularyAndTermLabel("FREEZER", propertyValue)
                    if freezerName is None:
                        #print repr("NOT FOUND FEEZER: " + self.values["ANTIBODY_ID_NR"] + " : '" + unicode(propertyValue) + "'")
                        propertyValue = None
                    else:
                        propertyValue = freezerName
                if propertyCode == "STORAGE_USER":
                    storageUser = definitionsVoc.getVocabularyTermCodeForVocabularyAndTermLabel("LAB_MEMBERS", propertyValue)
                    if storageUser is None:
                        #print repr("NOT FOUND USER: " + self.values["ANTIBODY_ID_NR"] + " : '" + unicode(propertyValue) + "'")
                        propertyValue = None
                    else:
                        propertyValue = storageUser
                
                if propertyValue is not None:
                    propertyValue =  unicode(propertyValue)
                    sample.setPropertyValue(propertyCode + "_" + str(boxNum), propertyValue)
    
    def isBoxPressent(self, boxSignature, tr):
        sample = getSampleForUpdate("/MATERIALS/"+self.values["*CODE"], None, tr)
        if sample is not None:
            for boxNum in range(1, definitions.numberOfStorageGroups+1):
                storedSignature = "";
                for propertyCode in definitions.getStorageGroupPropertyCodes():
                    propertyValue = sample.getPropertyValue(propertyCode + "_" + str(boxNum))
                    if propertyValue is not None:
                        propertyValue = unicode(propertyValue)
                        storedSignature += propertyValue
                if storedSignature == boxSignature:
                    #print "Found Box " + storedSignature.encode('ascii', 'ignore')
                    return True
        return False
    
    def isInOpenBIS(self, tr):
        for box in self.values["*BOXESLIST"]:
            boxSignature = "";
            for propertyCode in definitions.getStorageGroupPropertyCodes():
                propertyValue = box[propertyCode]
                if propertyCode == "STORAGE_NAME":
                    propertyValue = definitionsVoc.getVocabularyTermCodeForVocabularyAndTermLabel("FREEZER", propertyValue)
                if propertyCode == "STORAGE_USER":
                    propertyValue = definitionsVoc.getVocabularyTermCodeForVocabularyAndTermLabel("LAB_MEMBERS_INITIALS", propertyValue)
                    
                if propertyValue is not None:
                    propertyValue = unicode(propertyValue)
                    boxSignature += propertyValue
            if not self.isBoxPressent(boxSignature, tr):
                return False
        return True
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
       
        
class AntibodyOpenBISDTO(FMPeterOpenBISDTO):
    def isSampleCacheable(self):
        return False
        
    def write(self, tr):
        code = "AB_" + self.values["REF_NUM"]
        if code is not None:
            sample = getSampleForUpdate("/MATERIALS/"+code,"ANTIBODY", tr)
            setEntityProperties(tr, self.definition, sample, self.values);
    
    def getIdentifier(self, tr):
        code = "AB_"+ self.values["REF_NUM"]
        return code
    


##
## Strains
##
class StrainAdaptor(FileMakerEntityAdaptor):
    
    def init(self):
        self.selectQuery = "SELECT * FROM \"boxit strains\""
        self.definition = definitions.strainDefinition
        FileMakerEntityAdaptor.init(self)
    
    def addEntity(self, values):
        self.entities.append(StrainOpenBISDTO(values, self.definition))
        
class StrainOpenBISDTO(FMPeterOpenBISDTO):
    def write(self, tr):
        code = self.values["STRAIN_ID_NR"]
        if code is not None:
            sample = getSampleForUpdate("/MATERIALS/"+code,"STRAIN", tr)
            setEntityProperties(tr, self.definition, sample, self.values);
    
    def getIdentifier(self, tr):
        code = self.values["STRAIN_ID_NR"]
        return code


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
        
class PlasmidOpenBISDTO(FMPeterOpenBISDTO):
    def write(self, tr):
        code = self.values["NAME"]
        if code is not None:
            sample = getSampleForUpdate("/MATERIALS/"+code,"PLASMID", tr)
            setEntityProperties(tr, self.definition, sample, self.values);
            setEntityParents(tr, self.definition, sample, self.values)
    
    def getIdentifier(self, tr):
        code = self.values["NAME"]
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
        
class OligoOpenBISDTO(FMPeterOpenBISDTO):
    def write(self, tr):
        code = self.values["NAME"]
        if code is not None:
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
       
        
class ChemicalOpenBISDTO(FMPeterOpenBISDTO):
    def isSampleCacheable(self):
        return False
        
    def write(self, tr):
        code = "CHEM_" + self.values["ID"]
        if code is not None:
            sample = getSampleForUpdate("/MATERIALS/"+code,"CHEMICAL", tr)
            setEntityProperties(tr, self.definition, sample, self.values);
    
    def getIdentifier(self, tr):
        code = "CHEM_" + self.values["ID"]
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
        
class EnzymeOpenBISDTO(FMPeterOpenBISDTO):
    def isSampleCacheable(self):
            return False
    
    def write(self, tr):
        code = self.values["NAME"] 
        
        if code is not None:
            sample = getSampleForUpdate("/MATERIALS/"+code,"RESTRICTION_ENZYME", tr)
            setEntityProperties(tr, self.definition, sample, self.values);
    
    def getIdentifier(self, tr):
        code = self.values["NAME"]
        return code


        
fmConnString = "jdbc:filemaker://127.0.0.1/"
#fmConnString = "jdbc:filemaker://fmsrv.ethz.ch/"
fmUser= "admin"
fmPass = "nucleus"

# adaptors = [ AntibodyAdaptor(fmConnString, fmUser, fmPass, "BOXIT_antibodies_Peter"), 
#              AntibodyBoxAdaptor(fmConnString, fmUser, fmPass, "BOXIT_antibody_boxes_Peter"),
#              PlasmidAdaptor(fmConnString, fmUser, fmPass, "BOXIT_plasmids_Peter"),
#              PlasmidBoxAdaptor(fmConnString, fmUser, fmPass, "BOXIT_plasmid_boxes_Peter"),
#              StrainAdaptor(fmConnString, fmUser, fmPass, "BOXIT_strains_Peter"),
#              StrainBoxAdaptor(fmConnString, fmUser, fmPass, "BOXIT_strain_boxes_Peter"),
#              OligoAdaptor(fmConnString, fmUser, fmPass, "BOXIT_oligos_Peter"),
#              OligoBoxAdaptor(fmConnString, fmUser, fmPass, "BOXIT_oligo_boxes_Peter"),
#              CellAdaptor(fmConnString, fmUser, fmPass, "BOXIT_cells_Peter"),
#              CellBoxAdaptor(fmConnString, fmUser, fmPass, "BOXIT_cell_boxes_Peter"),
#              SirnaAdaptor(fmConnString, fmUser, fmPass, "BOXIT_Main_Menu_Peter"),
#              ChemicalAdaptor(fmConnString, fmUser, fmPass, "BOXIT_Main_Menu_Peter"),
#              DocumentsAdaptor(fmConnString, fmUser, fmPass, "BOXIT_documents_Peter")]
           


adaptors = [ 
             #EnzymeAdaptor(fmConnString, fmUser, fmPass, "Weis_Restriction_enzymes")
             #ChemicalAdaptor(fmConnString, fmUser, fmPass, "Weis_Chemicals")
             OligoAdaptor(fmConnString, fmUser, fmPass, "Weis_Oligos"),
             #AntibodyAdaptor(fmConnString, fmUser, fmPass, "Weis _Antibodies")
             PlasmidAdaptor(fmConnString, fmUser, fmPass, "Weis_Plasmids")
             ]
                       
            
def createDataHierarchy(tr):
    inventorySpace = tr.getSpace("MATERIALS")
    if inventorySpace == None:
        tr.createNewSpace("MATERIALS", None)
        tr.createNewProject("/MATERIALS/REAGENTS")
        tr.createNewProject("/MATERIALS/POLYNUCLEOTIDES")
        tr.createNewProject("/MATERIALS/PLASMIDS")
        tr.createNewProject("/MATERIALS/YEASTS")        
        
        