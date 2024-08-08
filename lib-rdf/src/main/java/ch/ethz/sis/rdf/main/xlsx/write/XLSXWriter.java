package ch.ethz.sis.rdf.main.xlsx.write;

import ch.ethz.sis.rdf.main.Constants;
import ch.ethz.sis.rdf.main.parser.RDFParser;
import ch.ethz.sis.rdf.main.Utils;
import ch.ethz.sis.rdf.main.model.rdf.OntClassObject;
import ch.ethz.sis.rdf.main.model.rdf.ResourceRDF;
import ch.ethz.sis.rdf.main.model.xlsx.VocabularyType;
import ch.ethz.sis.rdf.main.xlsx.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import static ch.ethz.sis.rdf.main.Constants.*;

public class XLSXWriter
{
    RDFSampleTypeHelper rdfSampleTypeHelper;
    RDFPropertyTypeHelper rdfPropertyTypeHelper;
    RDFSampleHelper rdfSampleHelper;
    RDFSpaceHelper rdfSpaceHelper;
    RDFProjectHelper rdfProjectHelper;
    RDFExperimentHelper rdfExperimentHelper;
    RDFExperimentTypeHelper rdfExperimentTypeHelper;
    RDFVocabularyTypeHelper rdfVocabularyTypeHelper;

    public XLSXWriter() {
        this.rdfSampleTypeHelper = new RDFSampleTypeHelper();
        this.rdfPropertyTypeHelper = new RDFPropertyTypeHelper();
        this.rdfSampleHelper = new RDFSampleHelper();
        this.rdfSpaceHelper = new RDFSpaceHelper();
        this.rdfProjectHelper = new RDFProjectHelper();
        this.rdfExperimentHelper = new RDFExperimentHelper();
        this.rdfExperimentTypeHelper = new RDFExperimentTypeHelper();
        this.rdfVocabularyTypeHelper = new RDFVocabularyTypeHelper();
    }

    public void createExcelFile(RDFParser rdfParser, String fileName, String projectIdentifier) {

        projectIdentifier = projectIdentifier == null ? DEFAULT_PRJ : projectIdentifier;
        try (Workbook workbook = new XSSFWorkbook()) {  // Create a new workbook
            // Define a style for headers
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            createVocabularyTypesSheet(workbook, headerStyle, rdfParser);
            createObjectTypesSheet(workbook, headerStyle, rdfParser);
            createExperimentTypesSheet(workbook, headerStyle);
            createSpaceProjExpSheet(workbook, headerStyle, projectIdentifier, rdfParser);
            createObjectsSheet(workbook, headerStyle, projectIdentifier, rdfParser);

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

    private void createVocabularyTypesSheet(Workbook workbook, CellStyle headerStyle, RDFParser rdfParser){
        Sheet sheet = workbook.createSheet(SHEET_TITLE_VOCAB);
        int rowNum = 0;

        List<VocabularyType> vocabularyTypeList = rdfParser.mappedNamedIndividualList;

        for(VocabularyType vocabularyType: vocabularyTypeList){
            rowNum = rdfVocabularyTypeHelper.addVocabularyTypes(sheet, rowNum, headerStyle, vocabularyType);
        }

    }

    private void createObjectTypesSheet(Workbook workbook, CellStyle headerStyle, RDFParser rdfParser){
        Sheet sheetOT = workbook.createSheet(SHEET_TITLE_OBJ_TYPES);

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
        Sheet sheet = workbook.createSheet(Constants.SHEET_TITLE_EXP);  // Create a sheet named "OBJ PROP"
        int rowNum = 0;

        rowNum = rdfExperimentTypeHelper.addExperimentTypeSection(sheet, rowNum, headerStyle);
        rdfExperimentTypeHelper.addExperimentSection(sheet, rowNum, headerStyle);

    }

    private void createSpaceProjExpSheet(Workbook workbook, CellStyle headerStyle, String projectId, RDFParser rdfParser){
        Sheet sheet = workbook.createSheet(SHEET_TITLE_SPACES);  // Create a sheet named "OBJ PROP"
        int rowNum = 0;
        String spaceId = projectId.split(SEP)[1];

        rowNum = rdfSpaceHelper.addSpaceSection(sheet, rowNum, headerStyle, spaceId);
        rowNum = rdfProjectHelper.addProjectSection(sheet, rowNum, headerStyle, spaceId, projectId);
        rowNum = rdfExperimentHelper.addExperimentSection(sheet, rowNum, headerStyle, projectId, rdfParser);

    }

    private void createObjectsSheet(Workbook workbook, CellStyle headerStyle, String projectId, RDFParser rdfParser) {
        Sheet sheet = workbook.createSheet(SHEET_TITLE_OBJS);
        int rowNum = 0;
        List<String> skippedSampleTypes = new ArrayList<>();
        Map<String, List<ResourceRDF>> additionalResources = new HashMap<>();

        for (Map.Entry<String, List<ResourceRDF>> entry : rdfParser.resourcesGroupedByType.entrySet()) {

            boolean isAlias = rdfParser.isAlias(entry.getKey());
            boolean isResource = rdfParser.isResource(entry.getKey());
            boolean isSubClass = rdfParser.isSubClass(entry.getKey());
            System.out.println("Entry: " + entry.getKey() + ", isAlias: " + isAlias + ", isResource: " + isResource + ", isSubClass: " + isSubClass);

            if (isSubClass) {
                System.out.println("    Subclass: " + rdfParser.mappedSubClasses.getOrDefault(entry.getKey(), new ArrayList<>()));
            }
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
                rowNum = rdfSampleHelper.createResourceRows(sheet, rowNum, projectId, resourceRDF, ontClassObject, rdfParser);
            }
            // add empty row for readability
            sheet.createRow(rowNum++);
        }

        Utils.autosizeColumns(sheet, 20);
        // Write the output to a file
        System.out.println("Skipped sample types: " + skippedSampleTypes);
    }
}
