# some_file.py
import sys
import definitions
import re
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.DataType as DataType


import java.lang.Class as Class;
import java.sql.Connection as Connection;
import java.sql.DriverManager as DriverManager;
import java.sql.PreparedStatement as PreparedStatement;
import java.sql.ResultSet as ResultSet;
import java.sql.SQLException as SQLException;
import java.util.ArrayList as ArrayList;
import java.util.List as List;

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
    print "FINISH!"

##
## Generic Adaptor Pattern
##
class EntityAdaptor:
    entities = None
    entitiesIdx = None
    definition = None;
    
    def init(self):
        self.entities = [];
        self.entitiesIdx = -1;
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
        
    def isInOpenBIS(self, tr):
        pass
    
    def write(self, tr):
        pass

##
## Costumer specific logic
##

class FileMakerEntityAdaptor(EntityAdaptor):
    connection = None
    selectQuery = None;
    
    def __init__(self, fileMakerConnString, fileMakerUser, fileMakerPass, db):
        Class.forName("com.filemaker.jdbc.Driver").newInstance();
        self.connection = DriverManager.getConnection(fileMakerConnString+db,fileMakerUser, fileMakerPass);
    
    def init(self):
        EntityAdaptor.init(self)
        
        preparedStatement = self.connection.prepareStatement(self.selectQuery);
        result = preparedStatement.executeQuery();
        
        while result.next():
            values = {};
            for property in self.definition:
                values[property[0]] = result.getString(property[2])
            self.addEntity(values)
        result.close();
        preparedStatement.close();
    
class AntibodyAdaptor(FileMakerEntityAdaptor):
    
    def init(self):
        self.selectQuery = "SELECT * FROM \"boxit antibodies\""
        self.definition = definitions.antibodyDefinition
        FileMakerEntityAdaptor.init(self)
    
    def addEntity(self, values):
        self.entities.append(AntibodyOpenBISDTO(values, self.definition))
        
class AntibodyOpenBISDTO(OpenBISDTO):
    def write(self, tr):
        sample = tr.createNewSample("/INVENTORY/"+self.values["ANTIBODY_ID_NR"], "ANTIBODY");
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
                        codeToUse = "None"
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
    
    def isInOpenBIS(self, tr):
        sample = tr.getSample("/INVENTORY/"+self.values["ANTIBODY_ID_NR"])
        return sample is not None
    
fmConnString = "jdbc:filemaker://127.0.0.1/"
fmUser = "designer"
fmPass = "seattle"

adaptors = [AntibodyAdaptor(fmConnString, fmUser, fmPass, "BOXIT_antibodies_Peter.fmp12")]

def createDataHierarchy(tr):
    inventorySpace = tr.getSpace("INVENTORY");
    if inventorySpace == None:
        tr.createNewSpace("INVENTORY", None);
        tr.createNewProject("/INVENTORY/MATERIALS");
        tr.createNewExperiment("/INVENTORY/MATERIALS/ANTIBODY",         "ANTIBODY");