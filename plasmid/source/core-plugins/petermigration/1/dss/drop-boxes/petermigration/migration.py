# some_file.py
import sys
import definitions

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
    definitions = None;
    
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
        self.entities.append(OpenBISDTO(values, self.definitions))
    
    def getEntity(self):
        return self.entities[self.entitiesIdx]
    
class OpenBISDTO:
    values = {}
    definitions = None
    
    def __init__(self, values, definitions):
        self.values = values
        self.definitions = definitions
        
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
            for property in self.definitions:
                values[property[0]] = result.getString(property[2])
            self.addEntity(values)
        result.close();
        preparedStatement.close();
    
class AntibodyAdaptor(FileMakerEntityAdaptor):
    
    def init(self):
        self.selectQuery = "SELECT * FROM \"boxit antibodies\""
        self.definitions = definitions.antibodyDefinition
        FileMakerEntityAdaptor.init(self)

    def isInOpenBIS(self, tr):
        sample = tr.getSample("/INVENTORY/"+self.values["ANTIBODY_ID_NR"]);
        return sample is not None
    
    def addEntity(self, values):
        self.entities.append(AntibodyOpenBISDTO(values, self.definitions))
        
class AntibodyOpenBISDTO(OpenBISDTO):
    def write(self, tr):
        sample = tr.createNewSample("/INVENTORY/"+self.values["ANTIBODY_ID_NR"], "ANTIBODY");
    
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