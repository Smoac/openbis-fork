package ch.ethz.sis.rdf.main.xlsx;

import ch.ethz.sis.rdf.main.model.rdf.OntClassExtension;
import ch.ethz.sis.rdf.main.model.rdf.PropertyTupleRDF;
import ch.ethz.sis.rdf.main.model.xlsx.SamplePropertyType;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.Restriction;
import org.apache.jena.ontology.UnionClass;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class RDFPropertyTypeHelper {
    private enum Attribute {// implements IAttribute {
        Code("Code", true),
        Mandatory("Mandatory", true),
        //DefaultValue("Default Value", false),  // Ignored, only used by PropertyAssignmentImportHelper
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

        Attribute(String headerName, boolean mandatory) {
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

    private int addDefaultNameRow(Sheet sheet, int rowNum){
        Row resRow = sheet.createRow(rowNum++);
        resRow.createCell(0).setCellValue("NAME");  // Code("Code", true),
        resRow.createCell(1).setCellValue(0);  // Mandatory("Mandatory", true),
        resRow.createCell(2).setCellValue(1);  // ShowInEditViews("Show in edit views", true),
        //resRow.createCell(3).setCellValue("No");  // Section("Section", true),
        resRow.createCell(4).setCellValue("Name");  // PropertyLabel("Property label", true),
        resRow.createCell(5).setCellValue("VARCHAR");  // DataType("Data type", true),
        //resRow.createCell(6).setCellValue("No");  // VocabularyCode("Vocabulary code", true),
        resRow.createCell(7).setCellValue("Name");  // Description("Description", true),
        resRow.createCell(13).setCellValue(false);  // MultiValued("Multivalued", false),
        return rowNum;
    }

    public int addObjectProperties(Sheet sheet, int rowNum, CellStyle headerStyle,
            String ontNamespace,
            String ontVersion,
            List<SamplePropertyType> propertyTypeList) {
        Row propTypeRowHeaders = sheet.createRow(rowNum++);

        // Populate header row with enum values
        Attribute[] fields = Attribute.values();
        for (int i = 0; i < fields.length; i++) {
            Cell cell = propTypeRowHeaders.createCell(i);
            cell.setCellValue(fields[i].getHeaderName());
            cell.setCellStyle(headerStyle);
        }

        if (propertyTypeList != null) {
            for (SamplePropertyType propertyType : propertyTypeList) {
                createRow(sheet, rowNum++, propertyType, ontNamespace, ontVersion);
            }
        }

        rowNum = addDefaultNameRow(sheet, rowNum);

        // add empty row
        sheet.createRow(rowNum++);

        return rowNum;
    }

    // Method to create a row in the sheet
    private void createRow(Sheet sheet, int rowNum, SamplePropertyType propertyType, String ontNamespace, String ontVersion) {
        Row resRow = sheet.createRow(rowNum);
        resRow.createCell(0).setCellValue(propertyType.code);  // Code("Code", true),
        resRow.createCell(1).setCellValue(propertyType.isMandatory);  // Mandatory("Mandatory", true),
        resRow.createCell(2).setCellValue(1);  // ShowInEditViews("Show in edit views", true),
        //resRow.createCell(3).setCellValue("No");  // Section("Section", true),
        resRow.createCell(4).setCellValue(propertyType.propertyLabel);  // PropertyLabel("Property label", true),
        resRow.createCell(5).setCellValue(propertyType.dataType);  // DataType("Data type", true),
        if (propertyType.vocabularyCode != null)
        {
            resRow.createCell(6).setCellValue(propertyType.vocabularyCode); // VocabularyCode("Vocabulary code", true),
        }
        resRow.createCell(7).setCellValue(propertyType.propertyLabel + (propertyType.description != null ? ": "+propertyType.description : ""));  // Description("Description", true),
        //resRow.createCell(8).setCellValue(StringUtils.join(propertyType.metadata));  // Metadata("Metadata", false),
        //resRow.createCell(9).setCellValue("No");  // DynamicScript("Dynamic script", false),
        resRow.createCell(10).setCellValue(ontNamespace);  // //OntologyId("Ontology Id", false),
        resRow.createCell(11).setCellValue(ontVersion);  // //OntologyVersion("Ontology Version", false),
        resRow.createCell(12).setCellValue(propertyType.ontologyAnnotationId);  // //OntologyAnnotationId("Ontology Annotation Id", false),
        resRow.createCell(13).setCellValue(propertyType.isMultiValue);  // MultiValued("Multivalued", false),
        //resRow.createCell(14).setCellValue("No");  // Unique("Unique", false),
        //resRow.createCell(15).setCellValue("No");  // Pattern("Pattern", false),
        //resRow.createCell(16).setCellValue("No");  // PatternType("Pattern Type", false);
    }
}
