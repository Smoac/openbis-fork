package ch.ethz.sis.rdf.main;

import ch.ethz.sis.rdf.main.mappers.OntClassObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import static ch.ethz.sis.rdf.main.mappers.DatatypeMapper.toOpenBISDataTypes;
import static ch.ethz.sis.rdf.main.mappers.ObjectPropertyMapper.toObjects;

public class ExcelWriter {

    private enum XLSXObjectFieldEnum {
        Code("Code", true),
        Description("Description", true),
        AutoGenerateCodes("Auto generate codes", true),
        ValidationScript("Validation script", true),
        GeneratedCodePrefix("Generated code prefix", true),
        OntologyId("Ontology Id", false),
        OntologyVersion("Ontology Version", false),
        OntologyAnnotationId("Ontology Annotation Id", false);

        private final String headerName;

        private final boolean mandatory;

        XLSXObjectFieldEnum(String headerName, boolean mandatory) {
            this.headerName = headerName;
            this.mandatory = mandatory;
        }

        public String getHeaderName() {
            return headerName;
        }
        public boolean isMandatory() {
            return mandatory;
        }
    }

    private enum XLSXPropertyFieldEnum {
        Code("Code", true),
        Mandatory("Mandatory", true),
        //DefaultValue("Default Value", false),
        ShowInEditViews("Show in edit views", true),
        Section("Section", true),
        PropertyLabel("Property label", true),
        DataType("Data type", true),
        VocabularyCode("Vocabulary code", true),
        Description("Description", true),
        Metadata("Metadata", false),
        DynamicScript("Dynamic script", false),
        OntologyId("Ontology Id", false),
        OntologyVersion("Ontology Version", false),
        OntologyAnnotationId("Ontology Annotation Id", false),
        MultiValued("Multivalued", false),
        Unique("Unique", false),
        Pattern("Pattern", false),
        PatternType("Pattern Type", false);

        private final String headerName;

        private final boolean mandatory;

        XLSXPropertyFieldEnum(String headerName, boolean mandatory) {
            this.headerName = headerName;
            this.mandatory = mandatory;
        }

        public String getHeaderName() {
            return headerName;
        }
        public boolean isMandatory() {
            return mandatory;
        }
    }

    private static int addSampleTypeSection(Sheet sheet, int rowNum, CellStyle headerStyle, OntClassObject ontClassObject){
        Row sampleTypeRow = sheet.createRow(rowNum++);
        sampleTypeRow.createCell(0).setCellValue("SAMPLE_TYPE");
        sampleTypeRow.getCell(0).setCellStyle(headerStyle);

        Row sampleTypeRowHeaders = sheet.createRow(rowNum++);

        // Populate header row with enum values
        XLSXObjectFieldEnum[] fields = XLSXObjectFieldEnum.values();
        for (int i = 0; i < fields.length; i++) {
            Cell cell = sampleTypeRowHeaders.createCell(i);
            cell.setCellValue(fields[i].getHeaderName());
            cell.setCellStyle(headerStyle);
        }

        String code = ontClassObject.label.trim().replaceAll(" ", "").toUpperCase(Locale.ROOT);
        String description = ontClassObject.label + "\n" + ontClassObject.comment;
        Row sampleTypeRowValues = sheet.createRow(rowNum++);
        sampleTypeRowValues.createCell(0).setCellValue(code); //Code("Code", true),
        sampleTypeRowValues.createCell(1).setCellValue(description); //Description("Description", true),
        sampleTypeRowValues.createCell(2).setCellValue(1); //AutoGenerateCodes("Auto generate codes", true),
        //sampleTypeRowValues.createCell(3).setCellValue("");//ValidationScript("Validation script", true),
        sampleTypeRowValues.createCell(4).setCellValue(code.substring(0,3));//GeneratedCodePrefix("Generated code prefix", true);
        sampleTypeRowValues.createCell(5).setCellValue(ontClassObject.ontClass.getURI());  // //OntologyId("Ontology Id", false),
        sampleTypeRowValues.createCell(6).setCellValue(1);  // //OntologyVersion("Ontology Version", false),
        sampleTypeRowValues.createCell(7).setCellValue(ontClassObject.ontClass.getURI());  // //OntologyAnnotationId("Ontology Annotation Id", false),
        return rowNum;
    }

    private static int addObjectProperties(Sheet sheet, int rowNum, CellStyle headerStyle, OntClassObject ontClassObject,
            Map<String, List<String>> mappedObjectProperty, Map<String, List<String>> mappedDataTypes) throws JsonProcessingException {
        Row rowHeaders = sheet.createRow(rowNum++);

        // Populate header row with enum values
        XLSXPropertyFieldEnum[] fields = XLSXPropertyFieldEnum.values();
        for (int i = 0; i < fields.length; i++) {
            Cell cell = rowHeaders.createCell(i);
            cell.setCellValue(fields[i].getHeaderName());
            cell.setCellStyle(headerStyle);
        }

        String sample_type = ontClassObject.label.trim().replaceAll(" ", "").toUpperCase(Locale.ROOT);

        // Iterate over restrictions to create additional rows
        if (ontClassObject.restrictions != null) {
            for (Map.Entry<OntProperty, List<Restriction>> entry : ontClassObject.restrictions.entrySet()) {
                //System.out.println("-- " + entry + " " + entry.getValue());
                boolean isMultivalued = false;
                Map<String, String> metadata = new HashMap<>();
                for (Restriction restriction : entry.getValue()) {
                    //parseRestriction(restriction, null);
                    if (restriction.isMinCardinalityRestriction()) {
                        //System.out.println("   - Min Cardinality Restriction on " + restriction.getOnProperty().getURI() + " value: " + restriction.asMinCardinalityRestriction().getMinCardinality());
                        isMultivalued = true;
                    } else if (restriction.isMaxCardinalityRestriction() && isMultivalued) {
                        isMultivalued = false;
                        //System.out.println("   - Max Cardinality Restriction on " + restriction.getOnProperty().getURI() + " value: " + restriction.asMaxCardinalityRestriction().getMaxCardinality());
                    } else if (restriction.isSomeValuesFromRestriction()) {
                        RDFNode someValuesFrom = restriction.asSomeValuesFromRestriction().getSomeValuesFrom();
                        if (someValuesFrom.isURIResource()) {
                            // Here, you handle URIResource cases, possibly adding them directly as restrictions
                            //System.out.println("     - Class URI Resource: " + someValuesFrom.asResource().getURI());
                            metadata.put("SomeValuesFromRestriction", someValuesFrom.asResource().getURI());
                        } else if (someValuesFrom.isAnon() && someValuesFrom.canAs(OntClass.class)){
                            OntClass anonClass = someValuesFrom.as(OntClass.class);
                            // Recursively handle the anonymous class, be it union, intersection, etc.
                            if (anonClass.isUnionClass()) {
                                UnionClass unionClass = anonClass.asUnionClass();
                                //System.out.println("- Union "+unionClass+" Of: " + unionClass.getOperands().size());
                                //System.out.println(ontClassObject.unions.get(unionClass));
                                metadata.put("SomeValuesFromRestriction", ontClassObject.unions.get(unionClass).toString());
                            }
                        }
                    }
                }
                List <String> possibleCodesTypes = mappedObjectProperty.getOrDefault(entry.getKey().getURI(),  Collections.singletonList(entry.getKey().getURI()));
                //System.out.println("Types: " + possibleCodesTypes + " - SomeValuesFromRestriction: " + metadata);
                String dataType = mappedDataTypes.getOrDefault(entry.getKey().getURI(), Collections.singletonList("SAMPLE:"+possibleCodesTypes.get(0).split("#")[1])).get(0);
                if (possibleCodesTypes.size() > 1) {
                    for (String possibleCode : possibleCodesTypes) {
                        if(metadata.values().contains(possibleCode)){
                            //System.out.println("Possible code: " + possibleCode + " in metadata: " + metadata);
                            dataType = "SAMPLE:"+ possibleCode.split("#")[1];
                        }
                    }
                    /*metadata.values().stream().forEach(restrictionValue -> {
                        if (possibleCodesTypes.contains(restrictionValue)){
                            System.out.println("Code: " + possibleCodesTypes + " - restrictionValue: " + restrictionValue);
                            dataType = restrictionValue.split("#")[1];
                        }
                    });*/
                }

                //System.out.println(code + " " + restriction);
                Row resRow = sheet.createRow(rowNum++);
                resRow.createCell(0).setCellValue(sample_type + "." + entry.getKey().getLocalName().toUpperCase(Locale.ROOT));  // Code("Code", true),
                resRow.createCell(1).setCellValue(0);  // Mandatory("Mandatory", true),
                //resRow.createCell(2).setCellValue("DEFAULT_VALUE");  // //DefaultValue("Default Value", false),
                resRow.createCell(2).setCellValue(1);  // ShowInEditViews("Show in edit views", true),
                //resRow.createCell(3).setCellValue("No");  // Section("Section", true),
                resRow.createCell(4).setCellValue(entry.getKey().getLocalName());  // PropertyLabel("Property label", true),
                resRow.createCell(5).setCellValue(dataType.toUpperCase(Locale.ROOT));  // DataType("Data type", true),
                //resRow.createCell(6).setCellValue("No");  // VocabularyCode("Vocabulary code", true),
                //resRow.createCell(7).setCellValue(entry.getKey().getURI());  // Description("Description", true),
                if (!metadata.isEmpty()) {
                    String jacksonFormat = new ObjectMapper().writeValueAsString(metadata);
                    resRow.createCell(8).setCellValue(jacksonFormat);  // Metadata("Metadata", false),
                }
                //resRow.createCell(9).setCellValue("No");  // DynamicScript("Dynamic script", false),
                resRow.createCell(10).setCellValue(entry.getKey().getURI());  // //OntologyId("Ontology Id", false),
                resRow.createCell(11).setCellValue(0);  // //OntologyVersion("Ontology Version", false),
                resRow.createCell(12).setCellValue(entry.getKey().getURI());  // //OntologyAnnotationId("Ontology Annotation Id", false),
                resRow.createCell(13).setCellValue(isMultivalued);  // MultiValued("Multivalued", false),
                //resRow.createCell(14).setCellValue("No");  // Unique("Unique", false),
                //resRow.createCell(15).setCellValue("No");  // Pattern("Pattern", false),
                //resRow.createCell(16).setCellValue("No");  // PatternType("Pattern Type", false);
            }
        }
        // add empty row
        sheet.createRow(rowNum++);

        return rowNum;
    }

    private static int addVocabularyTypes(Sheet sheet, int rowNum, CellStyle headerStyle){
        Row vocabularyTypeRow = sheet.createRow(rowNum++);
        vocabularyTypeRow.createCell(0).setCellValue("VOCABULARY_TYPE");
        vocabularyTypeRow.getCell(0).setCellStyle(headerStyle);

        Row vocabularyTypeRowHeaders = sheet.createRow(rowNum++);

        // Populate header row with enum values
        List<String> fields = Arrays.asList("Code", "Description");
        for (int i = 0; i < fields.size(); i++) {
            Cell cell = vocabularyTypeRowHeaders.createCell(i);
            cell.setCellValue(fields.get(i));
            cell.setCellStyle(headerStyle);
        }

        String code = "TEST_X";
        String description = "DESCRIPTION Y";

        Row vocabularyTypeRowValues = sheet.createRow(rowNum++);
        vocabularyTypeRowValues.createCell(0).setCellValue(code); //Code("Code", true),
        vocabularyTypeRowValues.createCell(1).setCellValue(description); //Description("Description", true),

        return rowNum;
    }

    public static void createExcelFile(OntModel model, Collection<OntClassObject> classDetailsList, String fileName) throws IOException
    {
        // Mapping of "has" relationship object properties to respective objects
        Map<String, List<String>> mappedObjectProperty = toObjects(model);

        // Mapping of XSD datatypes to custom string types
        Map<String, List<String>> mappedDataTypes = toOpenBISDataTypes(model);

        try (Workbook workbook = new XSSFWorkbook()) {  // Create a new workbook
            // Define a style for headers
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            /*Sheet sheetVT = workbook.createSheet("Vocabulary types");
            int rowNumVT = 0;
            addVocabularyTypes(sheetVT, rowNumVT, headerStyle);*/

            Sheet sheetOT = workbook.createSheet("Object types");  // Create a sheet named "OBJ PROP"

            int rowNumOT = 0;

            for (OntClassObject ontClassObject : classDetailsList) {

                // Add SAMPLE_TYPE header row for ClassDetails
                rowNumOT = addSampleTypeSection(sheetOT, rowNumOT, headerStyle, ontClassObject);

                // Add object properties section
                rowNumOT = addObjectProperties(sheetOT, rowNumOT, headerStyle, ontClassObject, mappedObjectProperty, mappedDataTypes);
            }

            // Auto-size all columns to fit content
            /*for (int i = 0; i < XLSXPropertyFieldEnum.values().length; i++) {
                sheet.autoSizeColumn(i);
            }*/

            // Write the output to a file
            try (FileOutputStream fileOut = new FileOutputStream(fileName)) {
                workbook.write(fileOut);
            }
        }
    }
}
