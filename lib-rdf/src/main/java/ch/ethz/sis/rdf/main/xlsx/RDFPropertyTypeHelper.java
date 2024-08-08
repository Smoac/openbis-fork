package ch.ethz.sis.rdf.main.xlsx;

import ch.ethz.sis.rdf.main.model.rdf.OntClassObject;
import ch.ethz.sis.rdf.main.model.rdf.PropertyTupleRDF;
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

    private boolean evaluateRestrictions(List<Restriction> restrictions, boolean isMultivalued, Map<String, String> metadata, OntClassObject ontClassObject) {
        for (Restriction restriction : restrictions) {
            isMultivalued = evaluateRestriction(restriction, isMultivalued, metadata, ontClassObject);
        }
        return isMultivalued;
    }

    private boolean evaluateRestriction(Restriction restriction, boolean isMultivalued, Map<String, String> metadata, OntClassObject ontClassObject){
        if (restriction.isMinCardinalityRestriction()) {
            //System.out.println("   - Min Cardinality Restriction on " + restriction.getOnProperty().getURI() + " value: " + restriction.asMinCardinalityRestriction().getMinCardinality());
            return true;
        } else if (restriction.isMaxCardinalityRestriction() && isMultivalued) {
            return false;
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
        return isMultivalued;
    }

    // Method to create a row in the sheet
    private void createRow(Sheet sheet, int rowNum, PropertyTupleRDF propertyTupleRDF, boolean isMultivalued, String ontNamespace, String ontVersion) {
        Row resRow = sheet.createRow(rowNum);
        resRow.createCell(0).setCellValue(propertyTupleRDF.getPredicateLabel().toUpperCase(Locale.ROOT));  // Code("Code", true),
        resRow.createCell(1).setCellValue(0);  // Mandatory("Mandatory", true),
        resRow.createCell(2).setCellValue(1);  // ShowInEditViews("Show in edit views", true),
        //resRow.createCell(3).setCellValue("No");  // Section("Section", true),
        resRow.createCell(4).setCellValue(propertyTupleRDF.getPredicateLabel());  // PropertyLabel("Property label", true),
        resRow.createCell(5).setCellValue(propertyTupleRDF.getObject().toUpperCase(Locale.ROOT));  // DataType("Data type", true),
        //resRow.createCell(6).setCellValue("No");  // VocabularyCode("Vocabulary code", true),
        resRow.createCell(7).setCellValue(propertyTupleRDF.getPredicate());  // Description("Description", true),
        /*if (!metadata.isEmpty()) {
            String jacksonFormat = new ObjectMapper().writeValueAsString(metadata);
            resRow.createCell(8).setCellValue(jacksonFormat);  // Metadata("Metadata", false),
        }*/
        //resRow.createCell(9).setCellValue("No");  // DynamicScript("Dynamic script", false),
        resRow.createCell(10).setCellValue(ontNamespace);  // //OntologyId("Ontology Id", false),
        resRow.createCell(11).setCellValue(ontVersion);  // //OntologyVersion("Ontology Version", false),
        resRow.createCell(12).setCellValue(propertyTupleRDF.getPredicate());  // //OntologyAnnotationId("Ontology Annotation Id", false),
        resRow.createCell(13).setCellValue(isMultivalued);  // MultiValued("Multivalued", false),
        //resRow.createCell(14).setCellValue("No");  // Unique("Unique", false),
        //resRow.createCell(15).setCellValue("No");  // Pattern("Pattern", false),
        //resRow.createCell(16).setCellValue("No");  // PatternType("Pattern Type", false);
    }

    public int addObjectProperties(Sheet sheet, int rowNum, CellStyle headerStyle,
            OntClassObject ontClassObject,
            Map<String, List<String>> mappedObjectProperty,
            Map<String, List<String>> mappedDataTypes,
            String ontNamespace, String ontVersion) {
        Row propTypeRowHeaders = sheet.createRow(rowNum++);

        // Populate header row with enum values
        Attribute[] fields = Attribute.values();
        for (int i = 0; i < fields.length; i++) {
            Cell cell = propTypeRowHeaders.createCell(i);
            cell.setCellValue(fields[i].getHeaderName());
            cell.setCellStyle(headerStyle);
        }

        if (ontClassObject.propertyTuples != null) {
            for (PropertyTupleRDF propertyTupleRDF : ontClassObject.propertyTuples) {
                AtomicBoolean isMultivalued = new AtomicBoolean(false);
                Map<String, String> metadata = new HashMap<>();

                if (ontClassObject.restrictions != null) {
                    ontClassObject.restrictions.entrySet()
                            .stream()
                            .filter(entry -> entry.getKey().equals(propertyTupleRDF.getPredicate()))
                            .findFirst()
                            .ifPresent(entry -> isMultivalued.set(
                                    evaluateRestrictions(entry.getValue(), isMultivalued.get(), metadata, ontClassObject)));
                }

                createRow(sheet, rowNum++, propertyTupleRDF, isMultivalued.get(), ontNamespace, ontVersion);
            }
        }

        rowNum = addDefaultNameRow(sheet, rowNum);

        // add empty row
        sheet.createRow(rowNum++);

        return rowNum;
    }
}
