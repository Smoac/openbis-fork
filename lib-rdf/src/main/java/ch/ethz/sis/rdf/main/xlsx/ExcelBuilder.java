package ch.ethz.sis.rdf.main.xlsx;

import ch.ethz.sis.rdf.main.RDFParser;
import ch.ethz.sis.rdf.main.Utils;
import ch.ethz.sis.rdf.main.entity.OntClassObject;
import ch.ethz.sis.rdf.main.entity.ResourceRDF;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class ExcelBuilder {
    RDFSampleTypeHelper rdfSampleTypeHelper;
    RDFPropertyTypeHelper rdfPropertyTypeHelper;
    RDFSampleHelper rdfSampleHelper;
    RDFSpaceHelper rdfSpaceHelper;
    RDFProjectHelper rdfProjectHelper;
    RDFExperimentHelper rdfExperimentHelper;
    RDFExperimentTypeHelper rdfExperimentTypeHelper;

    public ExcelBuilder() {
        this.rdfSampleTypeHelper = new RDFSampleTypeHelper();
        this.rdfPropertyTypeHelper = new RDFPropertyTypeHelper();
        this.rdfSampleHelper = new RDFSampleHelper();
        this.rdfSpaceHelper = new RDFSpaceHelper();
        this.rdfProjectHelper = new RDFProjectHelper();
        this.rdfExperimentHelper = new RDFExperimentHelper();
        this.rdfExperimentTypeHelper = new RDFExperimentTypeHelper();
    }

    public void createExcelFile(RDFParser rdfParser, String fileName, String projectIdentifier) {

        projectIdentifier = projectIdentifier == null ? "/DEFAULT/DEFAULT" : projectIdentifier;
        try (Workbook workbook = new XSSFWorkbook()) {  // Create a new workbook
            // Define a style for headers
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            //createVocabularyTypesSheet(workbook, headerStyle);
            createObjectTypesSheet(workbook, headerStyle, rdfParser);
            createExperimentTypesSheet(workbook, headerStyle);
            createSpaceProjExpSheet(workbook, headerStyle, projectIdentifier, rdfParser);
            //createObjectsSheet(workbook, headerStyle, projectIdentifier, rdfParser);

            // Write the output to a file
            try (FileOutputStream fileOut = new FileOutputStream(fileName)) {
                workbook.write(fileOut);
            } catch (FileNotFoundException e){
                throw new RuntimeException(e);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createVocabularyTypesSheet(Workbook workbook, CellStyle headerStyle){
        Sheet sheet = workbook.createSheet("Vocabulary types");
        int rowNum = 0;
        //addVocabularyTypes(sheetVT, rowNumVT, headerStyle);
    }

    private void createObjectTypesSheet(Workbook workbook, CellStyle headerStyle, RDFParser rdfParser){
        Sheet sheetOT = workbook.createSheet("Object types");  // Create a sheet named "OBJ PROP"

        int rowNumOT = 0;

        for (OntClassObject ontClassObject : rdfParser.classDetailsMap.values()) {

            // Add SAMPLE_TYPE header row for ClassDetails
            rowNumOT = rdfSampleTypeHelper.addSampleTypeSection(sheetOT, rowNumOT, headerStyle,
                    rdfParser.ontNamespace,
                    rdfParser.ontVersion,
                    ontClassObject.ontClass.getURI(),
                    ontClassObject.label,
                    ontClassObject.comment,
                    ontClassObject.skosDefinition,
                    ontClassObject.skosNote);

            // Add object properties section
            rowNumOT = rdfPropertyTypeHelper.addObjectProperties(sheetOT, rowNumOT, headerStyle,
                    ontClassObject,
                    rdfParser.mappedObjectProperty,
                    rdfParser.mappedDataTypes,
                    rdfParser.ontNamespace,
                    rdfParser.ontVersion);
        }
    }

    private void createExperimentTypesSheet(Workbook workbook, CellStyle headerStyle){
        Sheet sheet = workbook.createSheet("Experiment types");  // Create a sheet named "OBJ PROP"
        int rowNum = 0;

        rowNum = rdfExperimentTypeHelper.addExperimentTypeSection(sheet, rowNum, headerStyle);
        rdfExperimentTypeHelper.addExperimentSection(sheet, rowNum, headerStyle);

    }

    private void createSpaceProjExpSheet(Workbook workbook, CellStyle headerStyle, String projectId, RDFParser rdfParser){
        Sheet sheet = workbook.createSheet("Space Project Experiment");  // Create a sheet named "OBJ PROP"
        int rowNum = 0;
        String spaceId = projectId.split("/")[1];

        rowNum = rdfSpaceHelper.addSpaceSection(sheet, rowNum, headerStyle, spaceId);
        rowNum = rdfProjectHelper.addProjectSection(sheet, rowNum, headerStyle, spaceId, projectId);
        rowNum = rdfExperimentHelper.addExperimentSection(sheet, rowNum, headerStyle, projectId, rdfParser);

    }

    private void createObjectsSheet(Workbook workbook, CellStyle headerStyle, String projectId, RDFParser rdfParser) {
        Sheet sheet = workbook.createSheet("Objects");
        int rowNum = 0;
        List<String> skippedSampleTypes = new ArrayList<>();

        for (Map.Entry<String, List<ResourceRDF>> entry : rdfParser.resourcesGroupedByType.entrySet()) {
            if (!rdfParser.classDetailsMap.containsKey(entry.getKey()) || !rdfParser.mappedSubClasses.containsKey(entry.getKey())) {
                skippedSampleTypes.add(entry.getKey());
                continue;
            }

            OntClassObject ontClassObject = rdfParser.classDetailsMap.get(entry.getKey());

            //System.out.println(ontClassObject);

            // Create headers and rows for each sample type
            String objectType = Utils.extractLabel(entry.getKey()).toUpperCase(Locale.ROOT);
            rowNum = rdfSampleHelper.createSampleHeaders(sheet, rowNum, headerStyle, objectType, ontClassObject);

            for (ResourceRDF resourceRDF : entry.getValue()) {
                //System.out.println(resourceRDF);
                rowNum = rdfSampleHelper.createResourceRows(sheet, rowNum, projectId, resourceRDF, ontClassObject, rdfParser.mappedNamedIndividual);
            }
            // add empty row for readability
            sheet.createRow(rowNum++);
        }

        Utils.autosizeColumns(sheet, 20);
        // Write the output to a file
        System.out.println("Skipped sample types: " + skippedSampleTypes);
    }
}
