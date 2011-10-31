import re
import os

import utils

from java.lang import RuntimeException
from java.text import DecimalFormat

from ch.systemsx.cisd.common.geometry import Point, ConversionUtils
from ch.systemsx.cisd.openbis.generic.shared.basic.dto.api import ValidationException

from ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1 import MaterialIdentifierCollection
from ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto import ScreeningConstants
from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto import Geometry

    
class SanofiMaterial:
    """
       A data structure class holding compound materials as they exist in the Abase (Sanofi) database.
    """
    def __init__(self, wellCode, compoundBatchId, compoundId):
        self.wellCode = self.normalizeWellCode(wellCode)
        self.compoundBatchId = compoundBatchId
        self.compoundId = compoundId
    
    def normalizeWellCode(self, wellCode):
        """ normalizes Sanofi wellCodes to openBIS wellCodes e.g. AB007 to AB7 """
        return re.sub("(?<=\w)(0+)(?=\d)", "", wellCode)
            
class PlateInitializer:
    ABASE_DATA_SOURCE = "abase-datasource"
#    ABASE_PRODUCTION_QUERY = """select
#                                    ptodwellreference WELL_CODE,
#                                    translate(objdbatchref,'{/:()+','{_____') MATERIAL_CODE,
#                                    translate(objdid,'{/:()+','{_____') ABASE_COMPOUND_ID,
#                                    olptid ABASE_PLATE_CODE
#                                from sysadmin.plteobjd
#                                    where olptid = ?{1}"""

    ABASE_PRODUCTION_QUERY = """select
                                    ptodwellreference WELL_CODE,
                                    objdbatchref MATERIAL_CODE,
                                    objdid ABASE_COMPOUND_ID,
                                    olptid ABASE_PLATE_CODE
                                from sysadmin.plteobjd
                                    where olptid = ?{1}"""

    # used for integration testing from openBIS team members    
    ABASE_TEST_MODE_QUERY = """select 
                                   WELL_CODE, MATERIAL_CODE, ABASE_COMPOUND_ID, 
                                   ABASE_PLATE_CODE 
                               from plates 
                                   where ABASE_PLATE_CODE = ?{1}"""
                        
    LIBRARY_TEMPLATE_PROPNAME = "LIBRARY_TEMPLATE"
    
    POSITIVE_CONTROL_TYPE = "POSITIVE_CONTROL"
    NEGATIVE_CONTROL_TYPE = "NEGATIVE_CONTROL"
    
    COMPOUND_WELL_TYPE = "COMPOUND_WELL"
    COMPOUND_WELL_CONCENTRATION_PROPNAME = "CONCENTRATION_M"
    COMPOUND_WELL_MATERIAL_PROPNAME = "COMPOUND_BATCH"
    
    MATERIAL_BATCH_TYPE = "COMPOUND_BATCH"
    MATERIAL_COMPOUND_TYPE = "COMPOUND"
    MATERIAL_ID_PROPNAME = "COMPOUND"
        
    def __init__(self, transaction, state, plate, experiment, testMode):
        self.transaction = transaction
        self.state = state
        self.plate = plate
        self.plateCode = plate.getCode()
        self.experiment = experiment
        self.experimentId = experiment.getExperimentIdentifier()
        self.testMode = testMode
        self.numberParser = DecimalFormat()
        
    def getWellCode(self, x, y):
        return ConversionUtils.convertToSpreadsheetLocation(Point(x,y))
    
    def getPlateGeometryDimensions(self):
        plateGeometryString = self.plate.getPropertyValue(ScreeningConstants.PLATE_GEOMETRY)
        geometry = Geometry.createFromPlateGeometryString(plateGeometryString)
        return (geometry.height, geometry.width)
    
    def validateLibraryDimensions(self, tsvLines):
        (plateHeight, plateWidth) = self.getPlateGeometryDimensions()
        
        numLines = len(tsvLines)
        if plateHeight < len(tsvLines) :
            raise ValidationException("The property %s of experiment '%s' contains %s rows, but the"
                               " geometry of plate '%s' allows a maximum of %s rows. You should either reduce"
                               " the number of rows in the library template or change the plate geometry." % 
                               (self.LIBRARY_TEMPLATE_PROPNAME, self.experimentId, numLines, self.plateCode, plateHeight))
            
        for i in range(0, len(tsvLines)):
            lineWidth = len(tsvLines[i])
            if plateWidth < lineWidth:
                raise ValidationException("The property %s of experiment '%s' contains %s columns in row %s, but the"
                                   " geometry of plate '%s' allows a maximum of %s columns. You should either reduce"
                                   " the number of columns in the library template or change the plate geometry." % 
                                   (self.LIBRARY_TEMPLATE_PROPNAME, self.experimentId, lineWidth, (i + 1), self.plateCode, plateHeight))
        
    def parseLibraryTemplate(self):
        template = self.experiment.getPropertyValue(self.LIBRARY_TEMPLATE_PROPNAME)
        if not template:
            raise ValidationException("Experiment %s has no library template value in property %s" \
                               % (self.experimentId, self.LIBRARY_TEMPLATE_PROPNAME))
        
        lines = template.splitlines()
        lines = utils.removeTrailingEmptyElements(lines)
        tsvLists = [ line.split("\t") for line in lines ]
                
        self.validateLibraryDimensions(tsvLists)
        
        library = {}
        for x in range(0, len(tsvLists)):
            for y in range(0, len(tsvLists[0])):
                wellCode = self.getWellCode(x,y)
                library[wellCode] = tsvLists[x][y].strip().upper()
                 
        return library
    
    def upperCaseKeys(self, map):
        result = {}
        for entry in map.entrySet():
            result[entry.key.upper()] = entry.value
        return result
    
    def fetchPlateCompounds(self):
        """
           Fetch well metadata from the Abase database.
           
           @return: a list of tuples (one per well) in the form 
                    (wellCode, openBisCompoundCode, abaseCompoundBatchId, abaseCompoundId). 
                    In case the plate is not found in Abase return None.
        """
        if self.testMode:
            query = self.ABASE_TEST_MODE_QUERY
        else:
            query = self.ABASE_PRODUCTION_QUERY
            
        plateCode = self.plateCode.split(".")[0]
        queryService = self.state.getDataSourceQueryService()
        queryResult = queryService.select(self.ABASE_DATA_SOURCE, query, [plateCode])
        
        if not queryResult:
            raise ValidationException("No information for plate '%s' stored in the ABASE DB." % (self.plateCode))
        
        sanofiMaterials = []
        for resultMap in list(queryResult):
            materialMap = self.upperCaseKeys(resultMap)
            def val(code):
                if code in materialMap:
                    return str(materialMap[code])
                else:
                    raise RuntimeException("No column '%s' in the query results from the ABASE Database" % (code))
                
            material = SanofiMaterial(val('WELL_CODE'), val('MATERIAL_CODE'), \
                                      val('ABASE_COMPOUND_ID'))
                
            sanofiMaterials.append(material)
            
        queryResult.close()
        
        return sanofiMaterials
    
    def createCompoundBatchMaterial(self, sanofiMaterial, compoundMaterialByCode):
        material = self.transaction.createNewMaterial(sanofiMaterial.compoundBatchId, self.MATERIAL_BATCH_TYPE)
        compoundMaterial = compoundMaterialByCode[sanofiMaterial.compoundId]
        material.setPropertyValue(self.MATERIAL_ID_PROPNAME, compoundMaterial.getMaterialIdentifier())
        return material
    
    def createCompoundMaterial(self, sanofiMaterial):
        material = self.transaction.createNewMaterial(sanofiMaterial.compoundId, self.MATERIAL_COMPOUND_TYPE)
        return material

    def findExistingMaterials(self, materialType, materialCodes):
        """
           Returns a dictionary with entries (code: IMaterialImmutable) for the specified material codes.
        """
        materialIdentifiers = MaterialIdentifierCollection()
        for materialCode in materialCodes:
            materialIdentifiers.addIdentifier(materialType, materialCode)
        searchService = self.transaction.getSearchService()
        
        materialList = list(searchService.listMaterials(materialIdentifiers))
        return dict([ (material.getCode(), material) for material in materialList ])

    def createNonExistingMaterials(self, sanofiMaterialsByCode, existingMaterialsByCode, createFunction):
        createdMaterials = {}
        for materialCode in sanofiMaterialsByCode:
            if not materialCode in existingMaterialsByCode:
                sanofiMaterial = sanofiMaterialsByCode[materialCode]
                createdMaterials[materialCode] = createFunction(sanofiMaterial)
        
        return createdMaterials
        

    def getOrCreateMaterials(self, template, sanofiMaterials):
        materialsByBatchId = {}
        materialsByCompoundId = {}
        
        for sanofiMaterial in sanofiMaterials:
            materialsByBatchId[ sanofiMaterial.compoundBatchId ] = sanofiMaterial
            materialsByCompoundId[ sanofiMaterial.compoundId ] = sanofiMaterial

        existingCompoundMaterialsByCode = self.findExistingMaterials(self.MATERIAL_COMPOUND_TYPE, materialsByCompoundId)
        createdCompoundMaterialsByCode = self.createNonExistingMaterials(materialsByCompoundId, existingCompoundMaterialsByCode, self.createCompoundMaterial)
        compoundMaterialsByCode = dict(existingCompoundMaterialsByCode)
        compoundMaterialsByCode.update(createdCompoundMaterialsByCode)
        
        existingBatchMaterialsByCode = self.findExistingMaterials(self.MATERIAL_BATCH_TYPE, materialsByBatchId)
        createdBatchMaterialsByCode = self.createNonExistingMaterials(materialsByBatchId, existingBatchMaterialsByCode, lambda material: self.createCompoundBatchMaterial(material, compoundMaterialsByCode))
        
        batchMaterialsByCode = dict(existingBatchMaterialsByCode)
        batchMaterialsByCode.update(createdBatchMaterialsByCode)
        return batchMaterialsByCode
            
    
    def getByWellCode(self, wellCode, sanofiMaterials):
        for sanofiMaterial in sanofiMaterials:
            if wellCode == sanofiMaterial.wellCode:
                return sanofiMaterial
            
        return None
    
    def parseConcentration(self, value):
        number = self.numberParser.parse(value.replace("+", ""))
        return str(number)
    
    def isCompoundWell(self, libraryValue):
       try:
           self.parseConcentration(libraryValue)
           return True
       except:
           return False
       
    def createWells(self, template, sanofiMaterials, openbisMaterials):
        controlWellTypes = { "H" : self.POSITIVE_CONTROL_TYPE, 
                             "L" : self.NEGATIVE_CONTROL_TYPE};
                             
        for wellCode in template:
           if template[wellCode] in ["", "-"]:
               continue
               
           templateValue = template[wellCode].upper()
           wellIdentifier = self.plate.getSampleIdentifier() + ":" + wellCode
           
           if templateValue in controlWellTypes:
               # CONTROL_WELL
               wellType = controlWellTypes[templateValue]
               well = self.transaction.createNewSample(wellIdentifier, wellType)
               well.setContainer(self.plate)
               
           else: 
               # COMPOUND_WELL
               sanofiMaterial = self.getByWellCode(wellCode, sanofiMaterials)
               if sanofiMaterial:
                   # only create when they exist in the ABASE DB
                   well = self.transaction.createNewSample(wellIdentifier, self.COMPOUND_WELL_TYPE)
                   well.setContainer(self.plate)
                   concentration = self.parseConcentration(templateValue)
                   well.setPropertyValue(self.COMPOUND_WELL_CONCENTRATION_PROPNAME, concentration)
                   materialCode = sanofiMaterial.compoundBatchId
                   material = openbisMaterials[materialCode]
                   well.setPropertyValue(self.COMPOUND_WELL_MATERIAL_PROPNAME, material.getMaterialIdentifier())
       
    def validate(self, template, sanofiMaterials):
        for sanofiMaterial in sanofiMaterials:
            wellCode = sanofiMaterial.wellCode
            templateValue = template.get(wellCode, None)
            
            if not templateValue or not self.isCompoundWell(templateValue): 
               val = templateValue and ("'%s'" % templateValue) or "no value"
               raise ValidationException("Error registering library for plate '%s'. The ABASE DB contains"
                                  " a material definition for well '%s', but no valid concentration"
                                  " was found in the library template of experiment '%s'. The library"
                                  " template should contain a number for '%s' but %s was found" % 
                                  (self.plateCode, wellCode, self.experimentId, wellCode, val))
        

    def createWellsAndMaterials(self):
        template = self.parseLibraryTemplate()
        sanofiMaterials = self.fetchPlateCompounds()
        self.validate(template, sanofiMaterials)
        
        openbisMaterials = self.getOrCreateMaterials(template, sanofiMaterials)
        self.createWells(template, sanofiMaterials, openbisMaterials)
