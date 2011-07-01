import re

from ch.systemsx.cisd.common.mail import From

from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria 
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria import MatchClause, MatchClauseAttribute
from ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1 import MaterialIdentifierCollection
 
from ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto import ScreeningConstants

PLATE_TYPE = "PLATE"

DATA_SET_TYPE = "HCS_IMAGE_RAW"
DATA_SET_BATCH_PROPNAME = "ACQUISITION_BATCH"

OPENBIS_URL = "https://bwl27.sanofi-aventis.com:8443/openbis"

EXPERIMENT_RECIPIENTS_PROPNAME = "EMAIL_RECIPIENTS"

# TODO KE: get all instance admin emails here
DEFAULT_RECIPIENT_LIST = "Matthew.Smicker@sanofi-aventis.com"

def rollback_transaction(service, transaction, runner, ex):
    plateLink = createPlateLink(OPENBIS_URL, plate.getCode())
    errorMessage = ex.getMessage()
    sendEmail("openBIS: Data registration failed", """
    
    Dear Mr./Mrs.
    
      Registering new data for plate %(plateLink)s has failed with error '%(errorMessage)s'.
      
    openBIS
    """ % vars())

def commit_transaction(service, transaction):
    plateLink = createPlateLink(OPENBIS_URL, plate.getCode())
    sendEmail("openBIS: New data registered", """
    
    Dear Mr./Mrs.
    
      New data for the plate %(plateLink)s has been registered.
      
      Have a nice day!
      
    openBIS
    """ % vars())

def sendEmail(title, content):
    if experiment is not None:
       recipientsProp = experiment.getPropertyValue(EXPERIMENT_RECIPIENTS_PROPNAME)
        
    if not recipientsProp:
        recipientsProp = DEFAULT_RECIPIENT_LIST
    
    recipients = [ email.strip() for email in recipientsProp.split(",") ]
    fromAddress = From("openbis@sanofi-aventis.com")
    replyTo = None
    state.mailClient.sendMessage(title, content, replyTo, fromAddress, recipients)

def createPlateLink(openbisUrl, code):
    return "<a href='%(openbisUrl)s#entity=SAMPLE&sample_type=PLATE&action=SEARCH&code=%(code)s'>%(code)s</a>" % vars()

def findPlateByCode(code):
    """
       Finds a plate (openBIS sample) matching a specified bar code.
    """
    criteria = SearchCriteria()
    criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE, PLATE_TYPE))
    criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, code))
    
    searchService = transaction.getSearchService()
    platesFound = list(searchService.searchForSamples(criteria))
    
    if not platesFound:
        raise RuntimeError("No plate with code '%(code)s' found in the openBIS database" % vars())
    
    return platesFound[0]

def parseIncomingDirname(dirName):
    """
       Parses the name of an incoming dataset folder from the format
       'AcquisitionBatch_BarCode_Timestamp' to a tuple (acquisitionBatch, barCode)
    """
    tokens = dirName.split("_")
    if len(tokens) < 2:
        raise RuntimeError("Data set directory name does not match the pattern 'AcquisitionBatch_BarCode_Timestamp': " + dirName)
    
    acquisitionBatch = tokens[0]
    barCode = tokens[1].split('.')[0]
    return (acquisitionBatch, barCode)

def removeDuplicates(list):
    dict = {}
    for item in list:
        dict[item] = item
    return dict.keys()
    
class SanofiMaterial:
    """
       A data structure class holding compound materials as they exist in the Abase (Sanofi) database.
    """
    def __init__(self, wellCode, materialCode, sanofiId, sanofiBatchId):
        self.wellCode = self.normalizeWellCode(wellCode)
        self.materialCode = materialCode
        self.sanofiId = sanofiId
        self.sanofiBatchId = sanofiBatchId
    
    def normalizeWellCode(self, wellCode):
        """ normalizes Sanofi wellCodes openBIS wellCodes e.g. AB007 to AB7 """
        return re.sub("(?<=\w)(0+)(?=\d)", "", wellCode)
            
class PlateInitializer:
    ABASE_DATA_SOURCE = "abase-datasource"
    ABASE_QUERY = """select
                        ptodwellreference WELL_CODE,
                        translate(objdbatchref,'{/:()+','{_____') MATERIAL_CODE,
                        objdbatchref ABASE_COMPOUND_BATCH_ID,
                        objdid ABASE_COMPOUND_ID,
                        olptid ABASE_PLATE_CODE
                        from sysadmin.plteobjd
                        where olptid = ?{1}"""
    
    LIBRARY_TEMPLATE_PROPNAME = "LIBRARY_TEMPLATE"
    
    POSITIVE_CONTROL_TYPE = "POSITIVE_CONTROL"
    NEGATIVE_CONTROL_TYPE = "NEGATIVE_CONTROL"
    
    COMPOUND_WELL_TYPE = "COMPOUND_WELL"
    COMPOUND_WELL_CONCENTRATION_PROPNAME = "CONCENTRATION_M"
    COMPOUND_WELL_MATERIAL_PROPNAME = "COMPOUND"
    
    MATERIAL_TYPE = "COMPOUND"
    MATERIAL_ID_PROPNAME = "COMPOUND_ID"
    MATERIAL_BATCH_ID_PROPNAME = "COMPOUND_BATCH_ID"
            
    def __init__(self, transaction, plate):
        self.transaction = transaction
        self.plate = plate
        self.plateCode = plate.getCode()
        self.experimentId = plate.getExperiment().getExperimentIdentifier()
    
    def wellColumn(self, x):
        numLetters = 26
        if x < numLetters:
            return chr(ord('A') + x)
        else:
            return self.wellColumn((x / numLetters) - 1) + self.wellColumn( x % numLetters) 
        
    def getWellCode(self, x, y):
        return self.wellColumn(x) + str(y + 1)
    
    def getPlateDimensions(self):
        """
          parses the plate geometry property from the form "384_WELLS_16X24" 
          to a tuple of integers (plateHeight, plateWidth) 
        """
        plateGeometryString = self.plate.getPropertyValue(ScreeningConstants.PLATE_GEOMETRY)
        widthByHeight = plateGeometryString.split("_")[-1]
        dimensions = map(int, widthByHeight.split("X"))
        return (dimensions[0], dimensions[1])
    
    def validateLibraryDimensions(self, tsvLines):
        (plateHeight, plateWidth) = self.getPlateDimensions()
        
        numLines = len(tsvLines)
        if plateHeight != len(tsvLines) :
            raise RuntimeError("The geometry property of plate %(plateCode)s (height=%(plateHeight)s)"
                               " does not agree with the value of the %(LIBRARY_TEMPLATE_PROPNAME)s"
                               " property in experiment %(experimentId)s  (height=%(numLines)s)." % vars(self))
            
        for i in range(0, len(tsvLines)):
            lineWidth = len(tsvLines[i])
            if plateWidth != lineWidth:
                raise RuntimeError("The geometry property of plate %s (width=%s)"
                                   " does not agree with the value of the %s"
                                   " property in experiment %s  (line=%s, width=%s)." % \
                                   (plateCode, plateWidth, self.LIBRARY_TEMPLATE_PROPNAME, self.experimentId, i, lineWidth))
        
    def parseLibraryTemplate(self):
        template = experiment.getPropertyValue(self.LIBRARY_TEMPLATE_PROPNAME)
        
        tsvLists = [ line.split("\t")  for line in template.splitlines() ]
        self.validateLibraryDimensions(tsvLists)
        
        library = {}
        for x in range(0, len(tsvLists)):
            for y in range(0, len(tsvLists[0])):
                wellCode = self.getWellCode(x,y)
                library[wellCode] = tsvLists[x][y].strip()
                 
        return library
    
    
    def fetchPlateCompounds(self):
        """
           Fetch well metadata from the Abase database.
           
           @return: a list of tuples (one per well) in the form 
                    (wellCode, openBisCompoundCode, abaseCompoundBatchId, abaseCompoundId). 
                    In case the plate is not found in Abase return None.
        """
        queryService = state.getDataSourceQueryService()
        queryResult = queryService.select(self.ABASE_DATA_SOURCE, self.ABASE_QUERY, [self.plateCode])
        
        sanofiMaterials = []
        for materialMap in list(queryResult):
            wellCode = str(materialMap['WELL_CODE'])
            materialCode = str(materialMap['MATERIAL_CODE'])
            sanofiId = str(materialMap['ABASE_COMPOUND_ID'])
            sanofiBatchId = str(materialMap['ABASE_COMPOUND_BATCH_ID'])
            material = SanofiMaterial(wellCode, materialCode, sanofiId, sanofiBatchId)
            sanofiMaterials.append(material)
            
        queryResult.close()
        
        return sanofiMaterials
    
    def createMaterial(self, sanofiMaterial):
        material = self.transaction.createNewMaterial(sanofiMaterial.materialCode, self.MATERIAL_TYPE)
        material.setPropertyValue(self.MATERIAL_ID_PROPNAME, sanofiMaterial.sanofiId)
        material.setPropertyValue(self.MATERIAL_BATCH_ID_PROPNAME, sanofiMaterial.sanofiBatchId)
        return material
    
    def getOrCreateMaterials(self, library, sanofiMaterials):
        materialCodes = [sanofiMaterial.materialCode for sanofiMaterial in sanofiMaterials]
        materialCodes = removeDuplicates(materialCodes)
        
        materialIdentifiers = MaterialIdentifierCollection()
        for materialCode in materialCodes:
            materialIdentifiers.addIdentifier(self.MATERIAL_TYPE, materialCode)
        searchService = self.transaction.getSearchService() 
        preExistingMaterials = list(searchService.listMaterials(materialIdentifiers))
        
        materialsByCode = {}
        for material in preExistingMaterials:
            materialsByCode[ material.getCode() ] = material
            
        for materialCode in materialCodes:
            if not materialCode in materialsByCode:
                sanofiMaterial = self.getByMaterialCode(materialCode, sanofiMaterials)
                openbisMaterial = self.createMaterial(sanofiMaterial)
                materialsByCode[materialCode] = openbisMaterial 
        
        return materialsByCode
    
    def getByMaterialCode(self, materialCode, sanofiMaterials):
        for sanofiMaterial in sanofiMaterials:
            if materialCode == sanofiMaterial.materialCode:
                return sanofiMaterial
            
        raise RuntimeError("No material found for materialCode " + materialCode)
    
    def getByWellCode(self, wellCode, sanofiMaterials):
        for sanofiMaterial in sanofiMaterials:
            if wellCode == sanofiMaterial.wellCode:
                return sanofiMaterial
            
        raise RuntimeError("No material found for wellCode " + wellCode)
    
    def createWells(self, library, sanofiMaterials, openbisMaterials):
        controlWellTypes = { "H" : self.POSITIVE_CONTROL_TYPE, \
                             "L" : self.NEGATIVE_CONTROL_TYPE};
                             
        for wellCode in library:
           if not library[wellCode]:
               continue
               
           libraryValue = library[wellCode].upper()
           prefixedWellCode = self.plateCode + ":" + wellCode
           
           if libraryValue in controlWellTypes:
               # CONTROL_WELL
               wellType = controlWellTypes[libraryValue]
               well = self.transaction.createNewSample(prefixedWellCode, wellType)
               well.setContainer(self.plate)
           else: 
               # COMPOUND_WELL
               concentration = libraryValue
               try:
                   float(concentration)
               except ValueError:
                   raise RuntimeError("The specified value for well %s in the property "  
                                      " %s of experiment %s is invalid. Allowed values are 'H', 'L'"
                                      " or number, but '%s' was found." % \
                   (wellCode, self.LIBRARY_TEMPLATE_PROPNAME, self.experimentId, libraryValue))
                   
               well = self.transaction.createNewSample(prefixedWellCode, self.COMPOUND_WELL_TYPE)
               well.setContainer(self.plate)
               well.setPropertyValue(self.COMPOUND_WELL_CONCENTRATION_PROPNAME, concentration)
               materialCode = self.getByWellCode(wellCode, sanofiMaterials).materialCode
               material = openbisMaterials[materialCode]
               well.setPropertyValue(self.COMPOUND_WELL_MATERIAL_PROPNAME, material.getMaterialIdentifier())
        

    def createWellsAndMaterials(self):
        library = self.parseLibraryTemplate()
        sanofiMaterials = self.fetchPlateCompounds()
        
        # TODO KE: validate that library and sanofiMaterials data agrees
        openbisMaterials = self.getOrCreateMaterials(library, sanofiMaterials)
        self.createWells(library, sanofiMaterials, openbisMaterials)
 

transaction = service.transaction()
dataSet = transaction.createNewDataSet(DATA_SET_TYPE)

(batchName, barCode) = parseIncomingDirname(incoming.getName())
dataSet.setPropertyValue(DATA_SET_BATCH_PROPNAME, batchName)
plate = findPlateByCode(barCode)
if not plate.getExperiment():
    raise RuntimeError("Plate with code '%(barCode)s' is not associated with experiment" % vars())
experimentId = plate.getExperiment().getExperimentIdentifier()
experiment = transaction.getExperiment(experimentId)

if len(plate.getContainedSamples()) == 0:
    plateInitializer = PlateInitializer(transaction, plate)
    plateInitializer.createWellsAndMaterials()
    
dataSet.setSample(plate)
transaction.moveFile(incoming.getAbsolutePath(), dataSet)

commit_transaction(None, None)