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
## Generic Adaptor Pattern
##
class EntityAdaptor:
    entities = None
    entitiesIdx = None
        
    def init(self):
        self.entities = [];
        self.entitiesIdx = -1;
        pass
    
    def next(self):
        if self.entities is None and self.entitiesIdx is None:
            self.init()
        self.entitiesIdx += 1
        if len(self.entities) < self.entitiesIdx:
            return True
        else:
            return False
    
    def getEntity(self):
        return self.entities[self.entitiesIdx]
    
class DTO:
    values = {}
    openBISTransaction = None
    
    def __init__(self, values, openBISTransaction):
        self.values = values
        self.openBISTransaction = openBISTransaction
        
    def isInOpenBIS(self):
        pass
    
    def write(self):
        pass

##
## Costumer specific logic
##

class FileMakerEntityAdaptor(EntityAdaptor):
    connection = None
    selectQuery = None;
    definitions = None;
    
    def __init__(self, fileMakerConnString, fileMakerUser, fileMakerPass):
        Class.forName("com.filemaker.jdbc.Driver").newInstance();
        self.connection = DriverManager.getConnection(fileMakerConnString+"BOXIT_antibodies_Peter.fmp12",fileMakerUser, fileMakerPass);
    
    def init(self):
        self.entities = [];
        self.entitiesIdx = -1;
        
        preparedStatement = self.connection.prepareStatement(self.selectQuery);
        result = preparedStatement.executeQuery();
        
        while result.next():
            entity = {};
            for property in self.definitions:
                entity[property[0]] = result.getString(property[2])
            self.entities.append(entity)
            print entity
        
        result.close();
        preparedStatement.close();
        
class AntibodyAdaptor(FileMakerEntityAdaptor):
    
    def init(self):
        self.selectQuery = "SELECT * FROM \"boxit antibodies\""
        self.definitions = definitions.antibodyDefinition
        FileMakerEntityAdaptor.init(self)

fmConnString = "jdbc:filemaker://127.0.0.1/"
fmUser = "designer"
fmPass = "seattle"

adaptors = [AntibodyAdaptor(fmConnString, fmUser, fmPass)]
##
## Generic Process Method
##
def process(tr):
    for adaptor in adaptors:
        if adaptor.next():
            entity = adaptor.getEntity()
            if not entity.isInOpenBIS():
                entity.write()
    print "FINISH!"