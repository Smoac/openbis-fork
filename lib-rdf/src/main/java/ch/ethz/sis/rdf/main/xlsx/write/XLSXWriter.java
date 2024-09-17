package ch.ethz.sis.rdf.main.xlsx.write;

import ch.ethz.sis.rdf.main.Constants;
import ch.ethz.sis.rdf.main.model.rdf.ModelRDF;
import ch.ethz.sis.rdf.main.Utils;
import ch.ethz.sis.rdf.main.model.xlsx.SampleObject;
import ch.ethz.sis.rdf.main.model.xlsx.SampleObjectProperty;
import ch.ethz.sis.rdf.main.model.xlsx.SampleType;
import ch.ethz.sis.rdf.main.model.xlsx.VocabularyType;
import ch.ethz.sis.rdf.main.xlsx.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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

            if(!modelRDF.vocabularyTypeList.isEmpty()) createVocabularyTypesSheet(workbook, headerStyle, modelRDF);
            createObjectTypesSheet(workbook, headerStyle, modelRDF);
            createExperimentTypesSheet(workbook, headerStyle);
            createSpaceProjExpSheet(workbook, headerStyle, projectIdentifier, modelRDF);
            if(!modelRDF.sampleObjectsGroupedByTypeMap.entrySet().isEmpty()) createObjectsSheet(workbook, headerStyle, projectIdentifier, modelRDF);

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

    private void createObjectTypesSheet(Workbook workbook, CellStyle headerStyle, ModelRDF modelRDF)
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

        for(Map.Entry<String, List<SampleObject>> entry: modelRDF.sampleObjectsGroupedByTypeMap.entrySet())
        {
            List<String> sampleObjectPropertyLabelList = entry.getValue().stream()
                    .flatMap(sampleObject -> sampleObject.getProperties().stream())
                    .map(SampleObjectProperty::getLabel)
                    .collect(Collectors.toSet()).stream()
                    .toList();

            List<String> allColumnList = RDFSampleHelper.getAllColumnsList(sampleObjectPropertyLabelList);

            rowNum = rdfSampleHelper.createSampleHeaders(sheet, rowNum, headerStyle, entry.getKey(), allColumnList);

            for (SampleObject sampleObject : entry.getValue()) {
                System.out.println(sampleObject);
                rowNum = rdfSampleHelper.createResourceRows(sheet, rowNum, projectId, sampleObject, modelRDF, allColumnList);
            }
            sheet.createRow(rowNum++);
        }
        //Utils.autosizeColumns(sheet, 20);
    }
}
