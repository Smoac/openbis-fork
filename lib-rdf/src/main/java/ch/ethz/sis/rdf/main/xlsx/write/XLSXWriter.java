package ch.ethz.sis.rdf.main.xlsx.write;

import ch.ethz.sis.rdf.main.Constants;
import ch.ethz.sis.rdf.main.model.rdf.ModelRDF;
import ch.ethz.sis.rdf.main.Utils;
import ch.ethz.sis.rdf.main.model.rdf.OntClassExtension;
import ch.ethz.sis.rdf.main.model.rdf.ResourceRDF;
import ch.ethz.sis.rdf.main.model.xlsx.SampleType;
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

    public XLSXWriter()
    {
        this.rdfSampleTypeHelper = new RDFSampleTypeHelper();
        this.rdfPropertyTypeHelper = new RDFPropertyTypeHelper();
        this.rdfSampleHelper = new RDFSampleHelper();
        this.rdfSpaceHelper = new RDFSpaceHelper();
        this.rdfProjectHelper = new RDFProjectHelper();
        this.rdfExperimentHelper = new RDFExperimentHelper();
        this.rdfExperimentTypeHelper = new RDFExperimentTypeHelper();
        this.rdfVocabularyTypeHelper = new RDFVocabularyTypeHelper();
    }

    public void write(ModelRDF modelRDF, String fileName, String projectIdentifier)
    {
        projectIdentifier = projectIdentifier == null ? DEFAULT_PRJ : projectIdentifier;
        try (Workbook workbook = new XSSFWorkbook()) {  // Create a new workbook
            // Define a style for headers
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            createVocabularyTypesSheet(workbook, headerStyle, modelRDF);
            //createObjectTypesSheet(workbook, headerStyle, modelRDF);
            createObjectTypesSheet2(workbook, headerStyle, modelRDF);
            createExperimentTypesSheet(workbook, headerStyle);
            createSpaceProjExpSheet(workbook, headerStyle, projectIdentifier, modelRDF);
            //createObjectsSheet(workbook, headerStyle, projectIdentifier, modelRDF);

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

    private void createVocabularyTypesSheet(Workbook workbook, CellStyle headerStyle, ModelRDF modelRDF)
    {
        Sheet sheet = workbook.createSheet(SHEET_TITLE_VOCAB);
        int rowNum = 0;

        List<VocabularyType> vocabularyTypeList = modelRDF.vocabularyTypeList;

        for(VocabularyType vocabularyType: vocabularyTypeList){
            rowNum = rdfVocabularyTypeHelper.addVocabularyTypes(sheet, rowNum, headerStyle, vocabularyType);
        }

    }

    private void createObjectTypesSheet2(Workbook workbook, CellStyle headerStyle, ModelRDF modelRDF)
    {
        Sheet sheetOT = workbook.createSheet(SHEET_TITLE_OBJ_TYPES);

        int rowNumOT = 0;

        for (SampleType sampleType: modelRDF.sampleTypeList)
        {
            // Add SAMPLE_TYPE header row for ClassDetails
            rowNumOT = rdfSampleTypeHelper.addSampleTypeSection(sheetOT, rowNumOT, headerStyle,
                    modelRDF.ontNamespace,
                    modelRDF.ontVersion,
                    sampleType);

            // Add object properties section
            rowNumOT = rdfPropertyTypeHelper.addObjectProperties(sheetOT, rowNumOT, headerStyle,
                    modelRDF.ontNamespace,
                    modelRDF.ontVersion,
                    sampleType.properties);
        }
    }

    private void createObjectTypesSheet(Workbook workbook, CellStyle headerStyle, ModelRDF modelRDF)
    {
        Sheet sheetOT = workbook.createSheet(SHEET_TITLE_OBJ_TYPES);

        int rowNumOT = 0;

        for (OntClassExtension ontClassObject : modelRDF.stringOntClassExtensionMap.values())
        {
            // Add SAMPLE_TYPE header row for ClassDetails
            rowNumOT = rdfSampleTypeHelper.addSampleTypeSection(sheetOT, rowNumOT, headerStyle,
                    modelRDF.ontNamespace,
                    modelRDF.ontVersion,
                    ontClassObject.ontClass.getURI(),
                    ontClassObject.label,
                    ontClassObject.comment,
                    ontClassObject.skosDefinition,
                    ontClassObject.skosNote);

            // Add object properties section
            rowNumOT = rdfPropertyTypeHelper.addObjectProperties(sheetOT, rowNumOT, headerStyle,
                    ontClassObject,
                    modelRDF.objectPropertyMap,
                    modelRDF.RDFtoOpenBISDataType,
                    modelRDF.ontNamespace,
                    modelRDF.ontVersion);
        }
    }

    private void createExperimentTypesSheet(Workbook workbook, CellStyle headerStyle)
    {
        Sheet sheet = workbook.createSheet(Constants.SHEET_TITLE_EXP);  // Create a sheet named "OBJ PROP"
        int rowNum = 0;

        rowNum = rdfExperimentTypeHelper.addExperimentTypeSection(sheet, rowNum, headerStyle);
        rdfExperimentTypeHelper.addExperimentSection(sheet, rowNum, headerStyle);

    }

    private void createSpaceProjExpSheet(Workbook workbook, CellStyle headerStyle, String projectId, ModelRDF modelRDF)
    {
        Sheet sheet = workbook.createSheet(SHEET_TITLE_SPACES);  // Create a sheet named "OBJ PROP"
        int rowNum = 0;
        String spaceId = projectId.split(SEP)[1];

        rowNum = rdfSpaceHelper.addSpaceSection(sheet, rowNum, headerStyle, spaceId);
        rowNum = rdfProjectHelper.addProjectSection(sheet, rowNum, headerStyle, spaceId, projectId);
        rowNum = rdfExperimentHelper.addExperimentSection(sheet, rowNum, headerStyle, projectId, modelRDF);

    }

    private void createObjectsSheet(Workbook workbook, CellStyle headerStyle, String projectId, ModelRDF modelRDF)
    {
        Sheet sheet = workbook.createSheet(SHEET_TITLE_OBJS);
        int rowNum = 0;
        List<String> skippedSampleTypes = new ArrayList<>();
        Map<String, List<ResourceRDF>> additionalResources = new HashMap<>();

        for (Map.Entry<String, List<ResourceRDF>> entry : modelRDF.resourcesGroupedByType.entrySet())
        {

            boolean isAlias = modelRDF.isPresentInVocType(entry.getKey());
            boolean isResource = modelRDF.isResource(entry.getKey());
            boolean isSubClass = modelRDF.isSubClass(entry.getKey());
            System.out.println("Entry: " + entry.getKey() + ", isAlias: " + isAlias + ", isResource: " + isResource + ", isSubClass: " + isSubClass);

            if (isSubClass) {
                System.out.println("    Subclass: " + modelRDF.subClassChanisMap.getOrDefault(entry.getKey(), new ArrayList<>()));
            }
            if (!modelRDF.stringOntClassExtensionMap.containsKey(entry.getKey()) || !modelRDF.subClassChanisMap.containsKey(entry.getKey())) {
                skippedSampleTypes.add(entry.getKey());
                continue;
            }

            OntClassExtension ontClassObject = modelRDF.stringOntClassExtensionMap.get(entry.getKey());

            //System.out.println(ontClassObject);

            // Create headers and rows for each sample type
            String objectType = Utils.extractLabel(entry.getKey()).toUpperCase(Locale.ROOT);
            rowNum = rdfSampleHelper.createSampleHeaders(sheet, rowNum, headerStyle, objectType, ontClassObject);

            for (ResourceRDF resourceRDF : entry.getValue()) {
                //System.out.println(resourceRDF);
                rowNum = rdfSampleHelper.createResourceRows(sheet, rowNum, projectId, resourceRDF, ontClassObject, modelRDF);
            }
            // add empty row for readability
            sheet.createRow(rowNum++);
        }

        Utils.autosizeColumns(sheet, 20);
        // Write the output to a file
        System.out.println("Skipped sample types: " + skippedSampleTypes);
    }
}
