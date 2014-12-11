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
def process(tr):
    print "START!"
    createDataHierarchy(tr)
    for adaptor in adaptors:
        while adaptor.next():
            entity = adaptor.getEntity()
            if not entity.isInOpenBIS(tr):
                entity.write(tr)
                #print entity.getIdentifier(tr) + " - Updated"
            else:
                pass
                #print entity.getIdentifier(tr) + " - Already up to date"
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
            
            if propertyDefinition is not None and propertyDefinition[3] == DataType.CONTROLLEDVOCABULARY and propertyValue is not None:
                possiblePropertyValue = definitionsVoc.getVocaularyTermCodeForVocabularyAndTermLabel(propertyDefinition[4], propertyValue)
                if possiblePropertyValue is not None:
                    propertyValue = possiblePropertyValue
                else:  #We rely on the Add Hock Terms if is None
                    #Create new vocabulary term
                    vocabulary = tr.getVocabularyForUpdate(propertyDefinition[4])
                    term = tr.createNewVocabularyTerm()
                    codeToUse = re.sub(r'\W+','_',propertyValue)
                    labelToUse = propertyValue
                    if len(codeToUse) is 0:
                        codeToUse = "None" + str(random.random())
                    if len(codeToUse) > 60:
                        codeToUse = codeToUse[:50]
                    term.setCode(codeToUse)
                    term.setLabel(labelToUse)
                    term.setOrdinal(vocabulary.getTerms().size())
                    vocabulary.addTerm(term)
                    #Uses new vocabulary term
                    propertyValue = codeToUse
                    print repr(entity.getCode() + ", CREATED FOR VOCABULARY: " + propertyDefinition[4] + ", FOUND VALUE: " + labelToUse + ", NEW TERM WITH CODE: " + codeToUse)
            
            if propertyDefinition is not None: #Sometimes special fields are added for other purposes, these should not be set
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
antibodyID2Antibody = {}

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
             experiment = getExperimentForUpdate("/INVENTORY/MATERIALS/" + sampleType, sampleType, tr)
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
        setEntityProperties(tr, self.definition, sample, self.values);
    
    def getIdentifier(self, tr):
        return self.values["ANTIBODY_ID_NR"]
    
    def isInOpenBIS(self, tr):
        antibodyID2Antibody[self.values["NAME"]] = self.values
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
                    values["STORAGE_NAME"] = result.getString("location")
                    values["STORAGE_ROW"] = None
                    values["STORAGE_COLUMN"] = None
                    values["STORAGE_BOX_NAME"] = result.getString("box label")
                    values["STORAGE_USER"] = result.getString("frozen by")
                    values["STORAGE_BOX_POSITION"] = result.getString("position")
                    
                    allboxes = []
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
        #Delete old boxes
        for boxNum in range(1, definitions.numberOfStorageGroups+1):
            for propertyCode in definitions.stogageGroupPropertyCodes:
                sample.setPropertyValue(propertyCode + "_" + str(boxNum), None)
        #Add new boxes
        boxNum = 1
        for box in self.values["*BOXESLIST"]:
            boxNum += 1
            for propertyCode, propertyValue in box.iteritems():
                if propertyCode == "STORAGE_NAME":
                    freezerName = definitionsVoc.getVocaularyTermCodeForVocabularyAndTermLabel("FREEZER", propertyValue)
                    if freezerName is None:
                        #print repr("NOT FOUND FEEZER: " + self.values["ANTIBODY_ID_NR"] + " : '" + unicode(propertyValue) + "'")
                        propertyValue = None
                    else:
                        propertyValue = freezerName
                if propertyCode == "STORAGE_USER":
                    storageUser = definitionsVoc.getVocaularyTermCodeForVocabularyAndTermLabel("ALL_LAB_MEMBERS", propertyValue)
                    if storageUser is None:
                        #print repr("NOT FOUND USER: " + self.values["ANTIBODY_ID_NR"] + " : '" + unicode(propertyValue) + "'")
                        propertyValue = None
                    else:
                        propertyValue = storageUser
                
                if propertyValue is not None:
                    propertyValue =  unicode(propertyValue)
                    sample.setPropertyValue(propertyCode + "_" + str(boxNum), propertyValue)
    
    def isBoxPressent(self, boxSignature, tr):
        sample = getSampleForUpdate("/INVENTORY/"+self.values["ANTIBODY_ID_NR"], None, tr)
        if sample is not None:
            for boxNum in range(1, definitions.numberOfStorageGroups+1):
                storedSignature = "";
                for propertyCode in definitions.stogageGroupPropertyCodes:
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
            for propertyCode in definitions.stogageGroupPropertyCodes:
                propertyValue = box[propertyCode]
                if propertyCode == "STORAGE_NAME":
                    propertyValue = definitionsVoc.getVocaularyTermCodeForVocabularyAndTermLabel("FREEZER", propertyValue)
                if propertyCode == "STORAGE_USER":
                    propertyValue = definitionsVoc.getVocaularyTermCodeForVocabularyAndTermLabel("LAB_MEMBERS_INITIALS", propertyValue)
                    
                if propertyValue is not None:
                    propertyValue = unicode(propertyValue)
                    boxSignature += propertyValue
            if not self.isBoxPressent(boxSignature, tr):
                return False
        return True

class CellAdaptor(FileMakerEntityAdaptor):
    
    def init(self):
        self.selectQuery = "SELECT * FROM \"boxit cells\""
        self.definition = definitions.cellDefinition
        FileMakerEntityAdaptor.init(self)
    
    def addEntity(self, values):
        self.entities.append(CellOpenBISDTO(values, self.definition))
        
class CellOpenBISDTO(OpenBISDTO):
    def write(self, tr):
        code = self.values["CELL_ID_NR_COPY"]
        if code is not None and code.startswith("c_"):
            sample = getSampleForUpdate("/INVENTORY/"+code,"CELL", tr)
            setEntityProperties(tr, self.definition, sample, self.values);
    
    def getIdentifier(self, tr):
        code = self.values["CELL_ID_NR_COPY"]
        return code
    
    def isInOpenBIS(self, tr):
        antibodyID2Antibody[self.values["NAME"]] = self.values
        code = self.values["CELL_ID_NR_COPY"]
        if code is not None and code.startswith("c_"):
            sample = getSampleForUpdate("/INVENTORY/"+ code, None, tr)
            if sample is not None:
                lastModificationData = self.values["MODIFICATION_DATE"].strip()
                lastModificationData = str(datetime.strptime(lastModificationData, "%Y-%m-%d"))[:10]
                lastModificationOpenBIS = sample.getPropertyValue("MODIFICATION_DATE")[:10]
                return lastModificationOpenBIS == lastModificationData
            else :
                return False

class StrainAdaptor(FileMakerEntityAdaptor):
    
    def init(self):
        self.selectQuery = "SELECT * FROM \"boxit strains\""
        self.definition = definitions.strainDefinition
        FileMakerEntityAdaptor.init(self)
    
    def addEntity(self, values):
        self.entities.append(StrainOpenBISDTO(values, self.definition))
        
class StrainOpenBISDTO(OpenBISDTO):
    def write(self, tr):
        code = self.values["STRAIN_ID_NR"]
        if code is not None:
            sample = getSampleForUpdate("/INVENTORY/"+code,"STRAIN", tr)
            setEntityProperties(tr, self.definition, sample, self.values);
    
    def getIdentifier(self, tr):
        code = self.values["STRAIN_ID_NR"]
        return code
    
    def isInOpenBIS(self, tr):
        antibodyID2Antibody[self.values["NAME"]] = self.values
        code = self.values["STRAIN_ID_NR"]
        if code is not None:
            sample = getSampleForUpdate("/INVENTORY/"+ code, None, tr)
            if sample is not None:
                lastModificationData = self.values["MODIFICATION_DATE"].strip()
                lastModificationData = str(datetime.strptime(lastModificationData, "%Y-%m-%d"))[:10]
                lastModificationOpenBIS = sample.getPropertyValue("MODIFICATION_DATE")[:10]
                return lastModificationOpenBIS == lastModificationData
            else :
                return False

class PlasmidAdaptor(FileMakerEntityAdaptor):
    
    def init(self):
        self.selectQuery = "SELECT * FROM \"boxit plasmids\""
        self.definition = definitions.plasmidDefinition
        FileMakerEntityAdaptor.init(self)
    
    def addEntity(self, values):
        self.entities.append(PlasmidOpenBISDTO(values, self.definition))
        
class PlasmidOpenBISDTO(OpenBISDTO):
    def write(self, tr):
        code = self.values["PLASMID_ID_NR"]
        if code is not None:
            sample = getSampleForUpdate("/INVENTORY/"+code,"PLASMID", tr)
            setEntityProperties(tr, self.definition, sample, self.values);
    
    def getIdentifier(self, tr):
        code = self.values["PLASMID_ID_NR"]
        return code
    
    def isInOpenBIS(self, tr):
        antibodyID2Antibody[self.values["NAME"]] = self.values
        code = self.values["PLASMID_ID_NR"]
        if code is not None:
            sample = getSampleForUpdate("/INVENTORY/"+ code, None, tr)
            if sample is not None:
                lastModificationData = self.values["MODIFICATION_DATE"].strip()
                lastModificationData = str(datetime.strptime(lastModificationData, "%Y-%m-%d"))[:10]
                lastModificationOpenBIS = sample.getPropertyValue("MODIFICATION_DATE")[:10]
                return lastModificationOpenBIS == lastModificationData
            else :
                return False

class ChemicalAdaptor(FileMakerEntityAdaptor):
    
    def init(self):
        self.selectQuery = "SELECT * FROM \"Chemicals\""
        self.definition = definitions.chemicalDefinition
        FileMakerEntityAdaptor.init(self)
    
    def addEntity(self, values):
        self.entities.append(ChemicalOpenBISDTO(values, self.definition))
        
class ChemicalOpenBISDTO(OpenBISDTO):
    def write(self, tr):
        code = "CHEM_" + self.values["RECORD_NUMBER"]
        if code is not None:
            sample = getSampleForUpdate("/INVENTORY/"+code,"CHEMICAL", tr)
            setEntityProperties(tr, self.definition, sample, self.values);
    
    def getIdentifier(self, tr):
        code = "CHEM_" + self.values["RECORD_NUMBER"]
        return code
    
    def isInOpenBIS(self, tr):
        code = "CHEM_" + self.values["RECORD_NUMBER"]
        if code is not None:
            sample = getSampleForUpdate("/INVENTORY/"+ code, None, tr)
            if sample is not None:
                lastModificationData = self.values["MODIFICATION_DATE"].strip()
                lastModificationData = str(datetime.strptime(lastModificationData, "%Y-%m-%d"))[:10]
                lastModificationOpenBIS = sample.getPropertyValue("MODIFICATION_DATE")[:10]
                return lastModificationOpenBIS == lastModificationData
            else :
                return False

class SirnaAdaptor(FileMakerEntityAdaptor):
    
    def init(self):
        self.selectQuery = "SELECT * FROM \"siRNA\""
        self.definition = definitions.siRNADefinition
        FileMakerEntityAdaptor.init(self)
    
    def addEntity(self, values):
        self.entities.append(SirnaOpenBISDTO(values, self.definition))
        
class SirnaOpenBISDTO(OpenBISDTO):
    def write(self, tr):
        code = self.values["SIRNA_OLIGONUMBER"]
        if code is not None:
            sample = getSampleForUpdate("/INVENTORY/"+code,"SIRNA", tr)
            setEntityProperties(tr, self.definition, sample, self.values);
    
    def getIdentifier(self, tr):
        code = self.values["SIRNA_OLIGONUMBER"]
        return code
    
    def isInOpenBIS(self, tr):
        code = self.values["SIRNA_OLIGONUMBER"]
        if code is not None:
            sample = getSampleForUpdate("/INVENTORY/"+ code, None, tr)
            if sample is not None:
                lastModificationData = self.values["MODIFICATION_DATE"].strip()
                lastModificationData = str(datetime.strptime(lastModificationData, "%Y-%m-%d"))[:10]
                lastModificationOpenBIS = sample.getPropertyValue("MODIFICATION_DATE")[:10]
                return lastModificationOpenBIS == lastModificationData
            else :
                return False

    
class DocumentsAdaptor(FileMakerEntityAdaptor):
    
    def init(self):
        self.selectQuery = "SELECT CAST(file AS VARCHAR(1000)) AS file, info, id_nr, serial, \"date created\", \"date modified\" FROM documents"
        self.definition = definitions.documentDefinition
        FileMakerEntityAdaptor.init(self)
    
    def addEntity(self, values):
        preparedStatement = self.connection.prepareStatement("SELECT GetAs(file, 'FILE') AS fileData FROM documents WHERE serial = ?");
        preparedStatement.setString(1, values["SERIAL"]);
        result = preparedStatement.executeQuery();
        
        if result.next():
            fileData = result.getBytes("fileData");
            values["*DATA"] = fileData
        
        self.entities.append(DocumentOpenBISDTO(values, self.definition))

class DocumentOpenBISDTO(OpenBISDTO):
    def write(self, tr):
        sampleIdentifier = "/INVENTORY/"+self.values["ID_NR"];
        dataSetSample = getSampleForUpdate(sampleIdentifier, None, tr)
        if dataSetSample is not None and self.values["*DATA"] is not None and self.values["FILE"] is not None:
            dataSet = tr.createNewDataSet("DOCUMENT")
            dataSet.setSample(dataSetSample)
            setEntityProperties(tr, self.definition, dataSet, self.values)
            absolutePath = tr.createNewFile(dataSet, self.values["FILE"])
            f = open(absolutePath, 'wb')
            f.write(self.values["*DATA"])
            f.close()
            #incoming = tr.getIncoming()
            #tr.moveFile(incoming.getAbsolutePath(), dataSet)
        else:
            print "Document missing something SERIAL: " + self.values["SERIAL"]
    
    def getIdentifier(self, tr):
        return self.values["SERIAL"]
    
    def getDocumentBySerial(self, tr, serial):
        criteria = SearchCriteria()
        criteria.setOperator(criteria.SearchOperator.MATCH_ANY_CLAUSES)
        criteria.addMatchClause(criteria.MatchClause.createAttributeMatch(criteria.MatchClauseAttribute.TYPE, "DOCUMENT"))
        criteria.addMatchClause(criteria.MatchClause.createPropertyMatch("SERIAL", serial))
        datasets = tr.getSearchService().searchForDataSets(criteria)
        if datasets:
            return datasets[0]
        else:
            return None
    
    def isInOpenBIS(self, tr):
        dataset = self.getDocumentBySerial(tr, self.values["SERIAL"])
        return dataset is not None
        
fmConnString = "jdbc:filemaker://127.0.0.1/"
fmUser = "designer"
fmPass = "seattle"

fmConnStringServer = "jdbc:filemaker://fm.ethz.ch/"
fmUserServer= "sistemp"
fmPassServer = "ibcimsb2014"

#adaptors = [#AntibodyAdaptor(fmConnString, fmUser, fmPass, "BOXIT_antibodies_Peter"), 
            #AntibodyBoxAdaptor(fmConnString, fmUser, fmPass, "BOXIT_antibody_boxes_Peter"),
            #CellAdaptor(fmConnString, fmUser, fmPass, "BOXIT_cells_Peter"),
            #PlasmidAdaptor(fmConnString, fmUser, fmPass, "BOXIT_plasmids_Peter"),
            #StrainAdaptor(fmConnString, fmUser, fmPass, "BOXIT_strains_Peter"),
            #ChemicalAdaptor(fmConnString, fmUser, fmPass, "BOXIT_Main_Menu_Peter"),]
adaptors = [SirnaAdaptor(fmConnString, fmUser, fmPass, "BOXIT_Main_Menu_Peter"),
            DocumentsAdaptor(fmConnString, fmUser, fmPass, "BOXIT_documents_Peter")]

def createDataHierarchy(tr):
    inventorySpace = tr.getSpace("INVENTORY")
    if inventorySpace == None:
        tr.createNewSpace("INVENTORY", None)
        tr.createNewProject("/INVENTORY/MATERIALS")