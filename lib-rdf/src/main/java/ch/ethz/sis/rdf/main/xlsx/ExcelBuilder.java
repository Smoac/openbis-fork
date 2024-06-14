package ch.ethz.sis.rdf.main.xlsx;

import ch.ethz.sis.rdf.main.RDFParser;
import ch.ethz.sis.rdf.main.Utils;
import ch.ethz.sis.rdf.main.entity.OntClassObject;
import ch.ethz.sis.rdf.main.entity.PropertyTupleRDF;
import ch.ethz.sis.rdf.main.entity.ResourceRDF;
import org.apache.jena.atlas.lib.Pair;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

public class ExcelBuilder
{

    RDFSampleTypeHelper rdfSampleTypeHelper;
    RDFPropertyTypeHelper rdfPropertyTypeHelper;
    RDFSampleHelper rdfSampleHelper;
    RDFSpaceHelper rdfSpaceHelper;
    RDFProjectHelper rdfProjectHelper;
    RDFExperimentHelper rdfExperimentHelper;


    public ExcelBuilder() {
        this.rdfSampleTypeHelper = new RDFSampleTypeHelper();
        this.rdfPropertyTypeHelper = new RDFPropertyTypeHelper();
        this.rdfSampleHelper = new RDFSampleHelper();
        this.rdfSpaceHelper = new RDFSpaceHelper();
        this.rdfProjectHelper = new RDFProjectHelper();
        this.rdfExperimentHelper = new RDFExperimentHelper();
    }

    public void createExcelFile(RDFParser rdfParser, String fileName) {

        try (Workbook workbook = new XSSFWorkbook()) {  // Create a new workbook
            // Define a style for headers
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            //createVocabularyTypesSheet(workbook, headerStyle);
            createObjectTypesSheet(workbook, headerStyle, rdfParser);
            createSpaceProjExpSheet(workbook, headerStyle, "/DEFAULT/DEFAULT", rdfParser);
            createObjectsSheet(workbook, headerStyle, rdfParser, new HashMap<>());

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
                    ontClassObject.comment);

            // Add object properties section
            rowNumOT = rdfPropertyTypeHelper.addObjectProperties(sheetOT, rowNumOT, headerStyle,
                    ontClassObject,
                    rdfParser.mappedObjectProperty,
                    rdfParser.mappedDataTypes,
                    rdfParser.ontNamespace,
                    rdfParser.ontVersion);
        }
    }

    private void createSpaceProjExpSheet(Workbook workbook, CellStyle headerStyle, String projectId, RDFParser rdfParser){
        Sheet sheet = workbook.createSheet("Space Project Experiment");  // Create a sheet named "OBJ PROP"
        int rowNum = 0;
        String spaceId = projectId.split("/")[1];

        rowNum = rdfSpaceHelper.addSpaceSection(sheet, rowNum, headerStyle, spaceId);
        rowNum = rdfProjectHelper.addProjectSection(sheet, rowNum, headerStyle, spaceId, projectId);
        rdfExperimentHelper.addExperimentSection(sheet, rowNum, headerStyle, projectId, rdfParser);

    }

    private void createObjectsSheet(Workbook workbook, CellStyle headerStyle, RDFParser rdfParser, Map<String, List<ResourceRDF>> typeGroupMap) {
        Sheet sheet = workbook.createSheet("Objects");
        int rowNum = 0;
        List<String> skippedSampleTypes = new ArrayList<>();

        for (Map.Entry<String, List<ResourceRDF>> entry : rdfParser.resourcesGroupedByType.entrySet()) {
            System.out.println(entry);
            if (!rdfParser.classDetailsMap.containsKey(entry.getKey())) {
                skippedSampleTypes.add(entry.getKey());
                continue;
            }
            OntClassObject ontClassObject = rdfParser.classDetailsMap.get(entry.getKey());

            //System.out.println(ontClassObject);
            String objectType = Utils.extractLabel(entry.getKey()).toUpperCase(Locale.ROOT);


            // Create headers and rows for each sample type
            rdfSampleHelper.createSampleHeaders(sheet, rowNum, headerStyle, objectType, ontClassObject);
            rowNum += 4;  // Adjust row number based on headers added
            for (ResourceRDF res : entry.getValue()) {
                rowNum = rdfSampleHelper.createResourceRows(sheet, rowNum, res, ontClassObject);
            }
            // add empty row for readability
            sheet.createRow(rowNum++);
        }
        // Write the output to a file
        System.out.println("Skipped sample types: " + skippedSampleTypes);
    }
}
