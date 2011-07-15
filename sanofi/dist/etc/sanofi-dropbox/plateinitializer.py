import re
import os

import utilfunctions as util

from java.lang import RuntimeException

from ch.systemsx.cisd.common.geometry import Point, ConversionUtils
from ch.systemsx.cisd.openbis.generic.shared.basic.dto.api import ValidationException

from ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1 import MaterialIdentifierCollection
from ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto import ScreeningConstants
from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto import Geometry

    
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
        """ normalizes Sanofi wellCodes to openBIS wellCodes e.g. AB007 to AB7 """
        return re.sub("(?<=\w)(0+)(?=\d)", "", wellCode)
            
class PlateInitializer:
    ABASE_DATA_SOURCE = "abase-datasource"
    ABASE_PRODUCTION_QUERY = """select
                                    ptodwellreference WELL_CODE,
                                    translate(objdbatchref,'{/:()+','{_____') MATERIAL_CODE,
                                    objdbatchref ABASE_COMPOUND_BATCH_ID,
                                    objdid ABASE_COMPOUND_ID,
                                    olptid ABASE_PLATE_CODE
                                from sysadmin.plteobjd
                                    where olptid = ?{1}"""

    # used for integration testing from openBIS team members    
    ABASE_TEST_MODE_QUERY = """select 
                                   WELL_CODE, MATERIAL_CODE, ABASE_COMPOUND_ID, 
                                   ABASE_COMPOUND_BATCH_ID, ABASE_PLATE_CODE 
                               from plates 
                                   where ABASE_PLATE_CODE = ?{1}"""
                        
    LIBRARY_TEMPLATE_PROPNAME = "LIBRARY_TEMPLATE"
    
    POSITIVE_CONTROL_TYPE = "POSITIVE_CONTROL"
    NEGATIVE_CONTROL_TYPE = "NEGATIVE_CONTROL"
    
    COMPOUND_WELL_TYPE = "COMPOUND_BATCH_WELL"
    COMPOUND_WELL_CONCENTRATION_PROPNAME = "CONCENTRATION_M"
    COMPOUND_WELL_MATERIAL_PROPNAME = "COMPOUND"
    
    MATERIAL_TYPE = "COMPOUND"
    MATERIAL_ID_PROPNAME = "COMPOUND_ID"
    MATERIAL_BATCH_ID_PROPNAME = "COMPOUND_BATCH_ID"
            
    def __init__(self, transaction, state, plate, experiment, testMode):
        self.transaction = transaction
        self.state = state
        self.plate = plate
        self.plateCode = plate.getCode()
        self.experiment = experiment
        self.experimentId = experiment.getExperimentIdentifier()
        self.testMode = testMode
        
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
        lines = util.removeTrailingEmptyElements(lines)
        tsvLists = [ line.split("\t") for line in lines ]
                
        self.validateLibraryDimensions(tsvLists)
        
        library = {}
        for x in range(0, len(tsvLists)):
            for y in range(0, len(tsvLists[0])):
                wellCode = self.getWellCode(x,y)
                library[wellCode] = tsvLists[x][y].strip()
                 
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
                                      val('ABASE_COMPOUND_ID'), val('ABASE_COMPOUND_BATCH_ID'))
                
            sanofiMaterials.append(material)
            
        queryResult.close()
        
        return sanofiMaterials
    
    def createMaterial(self, sanofiMaterial):
        material = self.transaction.createNewMaterial(sanofiMaterial.materialCode, self.MATERIAL_TYPE)
        material.setPropertyValue(self.MATERIAL_ID_PROPNAME, sanofiMaterial.sanofiId)
        material.setPropertyValue(self.MATERIAL_BATCH_ID_PROPNAME, sanofiMaterial.sanofiBatchId)
        return material
    
    def getOrCreateMaterials(self, template, sanofiMaterials):
        materialsByCode = {}
        for sanofiMaterial in sanofiMaterials:
            materialsByCode[ sanofiMaterial.materialCode ] = sanofiMaterial

        materialIdentifiers = MaterialIdentifierCollection()
        for materialCode in materialsByCode:
            materialIdentifiers.addIdentifier(self.MATERIAL_TYPE, materialCode)
        searchService = self.transaction.getSearchService() 
        existingMaterials = list(searchService.listMaterials(materialIdentifiers))
        
        existingMaterialsByCode = {}
        for material in existingMaterials:
            existingMaterialsByCode[ material.getCode() ] = material
            
        for materialCode in materialsByCode:
            if not materialCode in existingMaterialsByCode:
                sanofiMaterial = materialsByCode[materialCode]
                openbisMaterial = self.createMaterial(sanofiMaterial)
                existingMaterialsByCode[materialCode] = openbisMaterial 
        
        return existingMaterialsByCode
            
    
    def getByWellCode(self, wellCode, sanofiMaterials):
        for sanofiMaterial in sanofiMaterials:
            if wellCode == sanofiMaterial.wellCode:
                return sanofiMaterial
            
        return None
    
    def isCompoundWell(self, libraryValue):        
       try:
           float(libraryValue)
           return True
       except ValueError:
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
               
           elif self.isCompoundWell(templateValue): 
               # COMPOUND_WELL
               sanofiMaterial = self.getByWellCode(wellCode, sanofiMaterials)
               if sanofiMaterial:
                   # only create when they exist in the ABASE DB
                   well = self.transaction.createNewSample(wellIdentifier, self.COMPOUND_WELL_TYPE)
                   well.setContainer(self.plate)
                   well.setPropertyValue(self.COMPOUND_WELL_CONCENTRATION_PROPNAME, templateValue)
                   materialCode = sanofiMaterial.materialCode
                   material = openbisMaterials[materialCode]
                   well.setPropertyValue(self.COMPOUND_WELL_MATERIAL_PROPNAME, material.getMaterialIdentifier())
               
           else:
               raise ValidationException("The specified value for well '%s' in the property "  
                                  " '%s' of experiment '%s' is invalid. Allowed values are 'H', 'L'"
                                  " or a number, but '%s' was found." % 
               (wellCode, self.LIBRARY_TEMPLATE_PROPNAME, self.experimentId, templateValue))
       
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
