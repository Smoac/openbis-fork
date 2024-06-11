package ch.ethz.sis.rdf.main.xlsx;

import ch.ethz.sis.rdf.main.RDFParser;
import ch.ethz.sis.rdf.main.entity.OntClassObject;
import ch.ethz.sis.rdf.main.entity.PropertyTupleRDF;
import ch.ethz.sis.rdf.main.entity.ResourceRDF;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

public class ExcelHandler {

    RDFSampleTypeHelper rdfSampleTypeHelper;
    RDFPropertyTypeHelper rdfPropertyTypeHelper;
    RDFSampleHelper rdfSampleHelper;

    public ExcelHandler() {
        this.rdfSampleTypeHelper = new RDFSampleTypeHelper();
        this.rdfPropertyTypeHelper = new RDFPropertyTypeHelper();
        this.rdfSampleHelper = new RDFSampleHelper();
    }

    private String extractLabel(String uri) {
        int hashIndex = uri.indexOf('#');
        if (hashIndex != -1)
        {
            return uri.substring(hashIndex + 1);
        } else
        {
            return uri;
        }
    }

    private List<String> getAllColumnsList(OntClassObject ontClassObject) {
        System.out.println("ontClassObject.propertyTuples: " + ontClassObject.propertyTuples);

        List<String> sampleTypeCols =  ontClassObject.propertyTuples.stream().map(PropertyTupleRDF::getPredicateLabel).toList();
        System.out.println("sampleTypeCols: "+ sampleTypeCols);

        List<String> defaultCols = new ArrayList<>(Stream.of(RDFSampleHelper.Attribute.values())
                .map(RDFSampleHelper.Attribute::name)
                .toList());

        System.out.println("defaultCols: "+ defaultCols);

        defaultCols.addAll(sampleTypeCols);
        return defaultCols;
    }

    private void createSampleTypeHeaders(Sheet sheet, int rowNum, CellStyle headerStyle, String sampleTypeKey, List<String> allColumns) {
        // Create header row for SAMPLE
        Row headerSampleRow = sheet.createRow(rowNum);
        Cell cellSample = headerSampleRow.createCell(0);
        cellSample.setCellValue("SAMPLE");
        cellSample.setCellStyle(headerStyle);

        // Create header row for Sample Type
        Row headerSampleTypeRow = sheet.createRow(rowNum + 1);
        Cell cellSampleType = headerSampleTypeRow.createCell(0);
        cellSampleType.setCellValue("Sample type");
        cellSampleType.setCellStyle(headerStyle);

        // Add Sample Type Value
        Row sampleTypeRow = sheet.createRow(rowNum + 2);
        sampleTypeRow.createCell(0).setCellValue(sampleTypeKey.toUpperCase(Locale.ROOT));

        // Create header row for Sample Type columns
        Row sampleTypeRowHeaders = sheet.createRow(rowNum + 3);

        for (int i = 0; i < allColumns.size(); i++)
        {
            Cell cell = sampleTypeRowHeaders.createCell(i);
            cell.setCellValue(allColumns.get(i));
            cell.setCellStyle(headerStyle);
        }
    }

    private int createResourceRows(Sheet sheet, int rowNum, ResourceRDF resource, List<String> allColumns) {
        Row propertyRowValues = sheet.createRow(rowNum);
        propertyRowValues.createCell(0).setCellValue(""); // $
        propertyRowValues.createCell(1).setCellValue(""); // Identifier
        propertyRowValues.createCell(2).setCellValue(""); // Code
        propertyRowValues.createCell(3).setCellValue("SPHN"); // Space
        propertyRowValues.createCell(4).setCellValue("/SPHN/DEFAULT"); // Project
        propertyRowValues.createCell(5).setCellValue("/SPHN/DEFAULT/DEFAULT"); // Experiment
        //propertyRowValues.createCell(6).setCellValue(1); // AutoGenerateCode
        propertyRowValues.createCell(6).setCellValue(""); // Parents
        propertyRowValues.createCell(7).setCellValue(""); // Children
        propertyRowValues.createCell(8).setCellValue(""); // $Name
        int idxName = allColumns.indexOf("Name");
        if (idxName != -1)
        {
            propertyRowValues.createCell(idxName).setCellValue(resource.resourceVal);
        }

        for (PropertyTupleRDF property : resource.properties)
        {
            int idx = allColumns.indexOf(extractLabel(property.getPredicateLabel()));
            if (idx != -1)
            {
                propertyRowValues.createCell(idx).setCellValue(property.getObject());
            }
        }

        return rowNum + 1;  // Move to the next row for future entries
    }

    private void autosizeColumns(Sheet sheet, int numCols) {
        for (int i = 0; i < numCols; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createObjectsSheet(Workbook workbook, CellStyle headerStyle, RDFParser rdfParser, Map<String, List<ResourceRDF>> typeGroupMap) {
        Sheet sheet = workbook.createSheet("Objects");
        int rowNum = 0;
        int maxNumCol = 0;
        List<String> skippedSampleTypes = new ArrayList<>();

        for (Map.Entry<String, List<ResourceRDF>> entry : rdfParser.resourcesGroupedByType.entrySet()) {
            if (!rdfParser.classDetailsMap.containsKey(entry.getKey())) {
                skippedSampleTypes.add(entry.getKey());
                continue;
            }
            OntClassObject ontClassObject = rdfParser.classDetailsMap.get(entry.getKey());

            System.out.println(ontClassObject);
            String objectType = extractLabel(entry.getKey()).toUpperCase(Locale.ROOT);

            List<String> allColumns = getAllColumnsList(ontClassObject);

            maxNumCol = Math.max(allColumns.size(), maxNumCol);

            // Create headers and rows for each sample type
            createSampleTypeHeaders(sheet, rowNum, headerStyle, objectType, allColumns);
            rowNum += 4;  // Adjust row number based on headers added
            for (ResourceRDF res : entry.getValue()) {
                rowNum = createResourceRows(sheet, rowNum, res, allColumns);
            }
            // add empty row for readability
            sheet.createRow(rowNum++);
        }
        // Autosize columns after all data is written to minimize performance impact
        autosizeColumns(sheet, maxNumCol);
        // Write the output to a file
        System.out.println("Skipped sample types: " + skippedSampleTypes);
    }

    public void createObjectTypesSheet(Workbook workbook, CellStyle headerStyle, RDFParser rdfParser){
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

    public void createExcelFile(RDFParser rdfParser, String fileName) {

        try (Workbook workbook = new XSSFWorkbook()) {  // Create a new workbook
            // Define a style for headers
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            /*Sheet sheetVT = workbook.createSheet("Vocabulary types");
            int rowNumVT = 0;
            addVocabularyTypes(sheetVT, rowNumVT, headerStyle);*/
            createObjectTypesSheet(workbook, headerStyle, rdfParser);
            //createObjectsSheet(workbook, headerStyle, rdfParser, new HashMap<>());

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
}
