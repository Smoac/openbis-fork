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
        print "- ADAPTOR [" + adaptor.__class__.__name__ + "] START"
        while adaptor.next():
            entity = adaptor.getEntity()
            print "* ENTITY [" + str(entity.getIdentifier(tr)) + "]"
            if not entity.isInOpenBIS(tr):
                entity.write(tr)
                #print entity.getIdentifier(tr) + " - Updated"
            else:
                pass
                #print entity.getIdentifier(tr) + " - Already up to date"
        print "- ADAPTOR [" + adaptor.__class__.__name__ + "] FINISH"
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
                possiblePropertyValue = definitionsVoc.getVocabularyTermCodeForVocabularyAndTermLabel(propertyDefinition[4], propertyValue)
                if possiblePropertyValue is not None:
                    propertyValue = possiblePropertyValue
                else:  #We rely on the Add Hock Terms if is None, since there is no API we create a new one
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
                    print repr("* ENTITY [" + entity.getCode() + "]: for Vocabulary [" + propertyDefinition[4] + "], found value not in list: [" + labelToUse + "]. Created new term with code [" + codeToUse + "]")
            
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
sampleID2Sample = {}

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
class FMPeterOpenBISDTO(OpenBISDTO):
        def isSampleCacheable(self):
            return True
        
        def isInOpenBIS(self, tr):
            code = self.getIdentifier(tr)
            if (code is not None) and (' ' not in code):
                if self.isSampleCacheable():
                    sampleID2Sample[self.values["NAME"]] = self.values
                sample = getSampleForUpdate("/INVENTORY/"+code, None, tr)
                if sample is not None:
                    lastModificationData = self.values["MODIFICATION_DATE"].strip()
                    lastModificationData = str(datetime.strptime(lastModificationData, "%Y-%m-%d"))[:10]
                    lastModificationOpenBIS = sample.getPropertyValue("MODIFICATION_DATE")[:10]
                    return lastModificationOpenBIS == lastModificationData
                else :
                    return False
            else:
                print "* ERROR [" + str(code) + "] - Invalid Code found for '" + self.__class__.__name__ + "'"
                return True 

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
        
        print "- ADAPTOR Boxes positions with empty entityId for " + self.__class__.__name__ + ":" + str(emptyBox)
        
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
        sample = getSampleForUpdate("/INVENTORY/"+self.values["*CODE"], None, tr)
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
                    storageUser = definitionsVoc.getVocabularyTermCodeForVocabularyAndTermLabel("ALL_LAB_MEMBERS", propertyValue)
                    if storageUser is None:
                        #print repr("NOT FOUND USER: " + self.values["ANTIBODY_ID_NR"] + " : '" + unicode(propertyValue) + "'")
                        propertyValue = None
                    else:
                        propertyValue = storageUser
                
                if propertyValue is not None:
                    propertyValue =  unicode(propertyValue)
                    sample.setPropertyValue(propertyCode + "_" + str(boxNum), propertyValue)
    
    def isBoxPressent(self, boxSignature, tr):
        sample = getSampleForUpdate("/INVENTORY/"+self.values["*CODE"], None, tr)
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
        self.selectQuery = "SELECT * FROM \"boxit antibodies\""
        self.definition = definitions.antibodyDefinition
        FileMakerEntityAdaptor.init(self)
    
    def addEntity(self, values):
        self.entities.append(AntibodyOpenBISDTO(values, self.definition))
        
class AntibodyOpenBISDTO(FMPeterOpenBISDTO):
    def write(self, tr):
        sample = getSampleForUpdate("/INVENTORY/"+self.values["ANTIBODY_ID_NR"],"ANTIBODY", tr)
        setEntityProperties(tr, self.definition, sample, self.values);
    
    def getIdentifier(self, tr):
        return self.values["ANTIBODY_ID_NR"]
    
class AntibodyBoxAdaptor(FMPeterBoxAdaptor):
    selectBoxQuery = "SELECT * FROM \"antibody boxes\""
    entityIdFieldName = "antibody ID"
    entityCodeFieldName = "ANTIBODY_ID_NR"

##
## Cells
##
class CellAdaptor(FileMakerEntityAdaptor):
    
    def init(self):
        self.selectQuery = "SELECT * FROM \"boxit cells\""
        self.definition = definitions.cellDefinition
        FileMakerEntityAdaptor.init(self)
    
    def addEntity(self, values):
        self.entities.append(CellOpenBISDTO(values, self.definition))
        
class CellOpenBISDTO(FMPeterOpenBISDTO):
    def write(self, tr):
        code = self.values["CELL_ID_NR_COPY"]
        if code is not None and code.startswith("c_"):
            sample = getSampleForUpdate("/INVENTORY/"+code,"CELL", tr)
            setEntityProperties(tr, self.definition, sample, self.values);
    
    def getIdentifier(self, tr):
        code = self.values["CELL_ID_NR_COPY"]
        return code

class CellBoxAdaptor(FMPeterBoxAdaptor):
    selectBoxQuery = "SELECT * FROM \"cell boxes\""
    entityIdFieldName = "cell ID"
    entityCodeFieldName = "CELL_ID_NR_COPY"

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
            sample = getSampleForUpdate("/INVENTORY/"+code,"STRAIN", tr)
            setEntityProperties(tr, self.definition, sample, self.values);
    
    def getIdentifier(self, tr):
        code = self.values["STRAIN_ID_NR"]
        return code

class StrainBoxAdaptor(FMPeterBoxAdaptor):
    selectBoxQuery = "SELECT * FROM \"strain boxes\""
    entityIdFieldName = "strain ID"
    entityCodeFieldName = "STRAIN_ID_NR"
##
## Plasmids
##
class PlasmidAdaptor(FileMakerEntityAdaptor):
    
    def init(self):
        self.selectQuery = "SELECT * FROM \"boxit plasmids\""
        self.definition = definitions.plasmidDefinition
        FileMakerEntityAdaptor.init(self)
    
    def addEntity(self, values):
        self.entities.append(PlasmidOpenBISDTO(values, self.definition))
        
class PlasmidOpenBISDTO(FMPeterOpenBISDTO):
    def write(self, tr):
        code = self.values["PLASMID_ID_NR"]
        if code is not None:
            sample = getSampleForUpdate("/INVENTORY/"+code,"PLASMID", tr)
            setEntityProperties(tr, self.definition, sample, self.values);
    
    def getIdentifier(self, tr):
        code = self.values["PLASMID_ID_NR"]
        return code

class PlasmidBoxAdaptor(FMPeterBoxAdaptor):
    selectBoxQuery = "SELECT * FROM \"plasmid boxes\""
    entityIdFieldName = "plasmid ID"
    entityCodeFieldName = "PLASMID_ID_NR"

##
## Oligos
##
class OligoAdaptor(FileMakerEntityAdaptor):
    
    def init(self):
        self.selectQuery = "SELECT * FROM \"boxit oligos\""
        self.definition = definitions.oligoDefinition
        FileMakerEntityAdaptor.init(self)
    
    def addEntity(self, values):
        self.entities.append(OligoOpenBISDTO(values, self.definition))
        
class OligoOpenBISDTO(FMPeterOpenBISDTO):
    def write(self, tr):
        code = self.values["OLIGO_ID_NR"]
        if code is not None:
            sample = getSampleForUpdate("/INVENTORY/"+code,"OLIGO", tr)
            setEntityProperties(tr, self.definition, sample, self.values);
    
    def getIdentifier(self, tr):
        code = self.values["OLIGO_ID_NR"]
        return code

class OligoBoxAdaptor(FMPeterBoxAdaptor):
    selectBoxQuery = "SELECT * FROM \"oligo boxes\""
    entityIdFieldName = "oligo ID"
    entityCodeFieldName = "OLIGO_ID_NR"

##
## Chemical
##
class ChemicalAdaptor(FileMakerEntityAdaptor):
    
    def init(self):
        self.selectQuery = "SELECT * FROM \"Chemicals\""
        self.definition = definitions.chemicalDefinition
        FileMakerEntityAdaptor.init(self)
    
    def addEntity(self, values):
        self.entities.append(ChemicalOpenBISDTO(values, self.definition))
        
class ChemicalOpenBISDTO(FMPeterOpenBISDTO):
    def isSampleCacheable(self):
        return False
        
    def write(self, tr):
        code = "CHEM_" + self.values["RECORD_NUMBER"]
        if code is not None:
            sample = getSampleForUpdate("/INVENTORY/"+code,"CHEMICAL", tr)
            setEntityProperties(tr, self.definition, sample, self.values);
    
    def getIdentifier(self, tr):
        code = "CHEM_" + self.values["RECORD_NUMBER"]
        return code

##
## Sirna
##
class SirnaAdaptor(FileMakerEntityAdaptor):
    def init(self):
        self.selectQuery = "SELECT * FROM \"siRNA\""
        self.definition = definitions.siRNADefinition
        FileMakerEntityAdaptor.init(self)
    
    def addEntity(self, values):
        self.entities.append(SirnaOpenBISDTO(values, self.definition))
        
class SirnaOpenBISDTO(FMPeterOpenBISDTO):
    def isSampleCacheable(self):
            return False
    
    def write(self, tr):
        code = "SI_" + self.values["SIRNA_OLIGONUMBER"]
        if code is not None:
            sample = getSampleForUpdate("/INVENTORY/"+code,"SIRNA", tr)
            setEntityProperties(tr, self.definition, sample, self.values);
    
    def getIdentifier(self, tr):
        code = "SI_" +self.values["SIRNA_OLIGONUMBER"]
        return code

##
## Documents
##
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
            if dataSetSample is None:
                print "* ERROR No sample found for document"
            if self.values["*DATA"] is None:
                print "* ERROR No Data found for file"
            if self.values["FILE"] is None:
                print "* ERROR No file name found for file"
    
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

adaptors = [ AntibodyAdaptor(fmConnString, fmUser, fmPass, "BOXIT_antibodies_Peter"), 
             AntibodyBoxAdaptor(fmConnString, fmUser, fmPass, "BOXIT_antibody_boxes_Peter"),
             PlasmidAdaptor(fmConnString, fmUser, fmPass, "BOXIT_plasmids_Peter"),
             PlasmidBoxAdaptor(fmConnString, fmUser, fmPass, "BOXIT_plasmid_boxes_Peter"),
             StrainAdaptor(fmConnString, fmUser, fmPass, "BOXIT_strains_Peter"),
             StrainBoxAdaptor(fmConnString, fmUser, fmPass, "BOXIT_strain_boxes_Peter"),
             OligoAdaptor(fmConnString, fmUser, fmPass, "BOXIT_oligos_Peter"),
             OligoBoxAdaptor(fmConnString, fmUser, fmPass, "BOXIT_oligo_boxes_Peter"),
             CellAdaptor(fmConnString, fmUser, fmPass, "BOXIT_cells_Peter"),
             CellBoxAdaptor(fmConnString, fmUser, fmPass, "BOXIT_cell_boxes_Peter"),
             SirnaAdaptor(fmConnString, fmUser, fmPass, "BOXIT_Main_Menu_Peter"),
             ChemicalAdaptor(fmConnString, fmUser, fmPass, "BOXIT_Main_Menu_Peter"),
             DocumentsAdaptor(fmConnString, fmUser, fmPass, "BOXIT_documents_Peter")]
           
            
            
def createDataHierarchy(tr):
    inventorySpace = tr.getSpace("INVENTORY")
    if inventorySpace == None:
        tr.createNewSpace("INVENTORY", None)
        tr.createNewProject("/INVENTORY/MATERIALS")