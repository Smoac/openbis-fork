# some_file.py
import sys
import definitions

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
    fileMakerConnString = None
    fileMakerUser = None
    fileMakerPass = None
    
    def __init__(self, fileMakerConnString, fileMakerUser, fileMakerPass):
        self.fileMakerConnString = fileMakerConnString
        self.fileMakerUser = fileMakerUser
        self.fileMakerPass = fileMakerPass
        
class AntibodyAdaptor(FileMakerEntityAdaptor):
    def init(self):
        self.entities = [];
        self.entitiesIdx = -1;
        pass

adaptors = [AntibodyAdaptor(None, None, None)]
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