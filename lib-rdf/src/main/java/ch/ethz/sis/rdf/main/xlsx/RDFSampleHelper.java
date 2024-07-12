package ch.ethz.sis.rdf.main.xlsx;

import ch.ethz.sis.rdf.main.parser.RDFParser;
import ch.ethz.sis.rdf.main.Utils;
import ch.ethz.sis.rdf.main.entity.OntClassObject;
import ch.ethz.sis.rdf.main.entity.PropertyTupleRDF;
import ch.ethz.sis.rdf.main.entity.ResourceRDF;
import ch.ethz.sis.rdf.main.entity.VocabularyType;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.poi.ss.usermodel.*;

import java.util.*;
import java.util.stream.Stream;

public class RDFSampleHelper {
    public enum Attribute { // implements IAttribute {
        $("$", false),
        Identifier("Identifier", false),
        Code("Code", false),
        Space("Space", false),
        Project("Project", false),
        Experiment("Experiment", false),
        Parents("Parents", false),
        Children("Children", false),
        Name("Name", false);

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

    private final String RESOURCE_PREFIX = "https://biomedit.ch/rdf/sphn-resource/";

    private List<String> getAllColumnsList(OntClassObject ontClassObject) {
        //System.out.println("ontClassObject.propertyTuples: " + ontClassObject.propertyTuples);

        List<String> sampleTypeCols =  ontClassObject.propertyTuples.stream().map(PropertyTupleRDF::getPredicateLabel).toList();
        //System.out.println("sampleTypeCols: "+ sampleTypeCols);

        List<String> defaultCols = new ArrayList<>(Stream.of(RDFSampleHelper.Attribute.values())
                .map(RDFSampleHelper.Attribute::name)
                .toList());

        //System.out.println("defaultCols: "+ defaultCols);

        defaultCols.addAll(sampleTypeCols);
        return defaultCols;
    }

    protected int createSampleHeaders(Sheet sheet, int rowNum, CellStyle headerStyle, String sampleTypeKey, OntClassObject ontClassObject) {
        List<String> allColumns = getAllColumnsList(ontClassObject);

        // Create header row for SAMPLE
        Row headerSampleRow = sheet.createRow(rowNum++);
        Cell cellSample = headerSampleRow.createCell(0);
        cellSample.setCellValue("SAMPLE");
        cellSample.setCellStyle(headerStyle);

        // Create header row for Sample Type
        Row headerSampleTypeRow = sheet.createRow(rowNum++);
        Cell cellSampleType = headerSampleTypeRow.createCell(0);
        cellSampleType.setCellValue("Sample type");
        cellSampleType.setCellStyle(headerStyle);

        // Add Sample Type Value
        Row sampleTypeRow = sheet.createRow(rowNum++);
        sampleTypeRow.createCell(0).setCellValue(sampleTypeKey.toUpperCase(Locale.ROOT));

        // Create header row for Sample Type columns
        Row sampleTypeRowHeaders = sheet.createRow(rowNum++);

        for (int i = 0; i < allColumns.size(); i++)
        {
            Cell cell = sampleTypeRowHeaders.createCell(i);
            cell.setCellValue(allColumns.get(i));
            cell.setCellStyle(headerStyle);
        }

        return rowNum;
    }

    protected boolean isResource(String uri){
        return uri.startsWith(RESOURCE_PREFIX);
    }
    
    protected int createResourceRows(Sheet sheet, int rowNum, String projectId, ResourceRDF resource, OntClassObject ontClassObject, RDFParser rdfParser) {
        List<String> allColumns = getAllColumnsList(ontClassObject);

        Row propertyRowValues = sheet.createRow(rowNum);
        //propertyRowValues.createCell(0).setCellValue(""); // $
        //propertyRowValues.createCell(1).setCellValue(""); // Identifier
        propertyRowValues.createCell(2).setCellValue(resource.resourceVal); // Code
        propertyRowValues.createCell(3).setCellValue(projectId.split("/")[1]); // Space
        propertyRowValues.createCell(4).setCellValue(projectId); // Project
        propertyRowValues.createCell(5).setCellValue(projectId + resource.type); // Experiment
        //propertyRowValues.createCell(6).setCellValue(""); // Parents
        //propertyRowValues.createCell(7).setCellValue(""); // Children

        int idxName = allColumns.indexOf("Name");
        if (idxName != -1) {
            propertyRowValues.createCell(idxName).setCellValue(resource.resourceVal);
        }

        Map<String, List<VocabularyType>> mappedLabelNamedIndividual = rdfParser.mappedNamedIndividual;

        for (PropertyTupleRDF property : resource.properties) {
            boolean isAlias = rdfParser.isAlias(property.getObject());
            boolean isResource = isResource(property.getObject());
            boolean isSubClass = rdfParser.isSubClass(property.getObject());
            System.out.println("PropertyTupleRDF: " + property + ", isAlias: " + isAlias + ", isResource: " + isResource + ", isSubClass: " + isSubClass);
            propertyRowValues.createCell(1).setCellValue(projectId + "/" + resource.resourceVal); // Identifier
            propertyRowValues.createCell(5).setCellValue(projectId + "/" + Utils.extractLabel(resource.type).toUpperCase(Locale.ROOT) + "_COLLECTION"); // Experiment
            int idx = allColumns.indexOf(Utils.extractLabel(property.getPredicateLabel()));
            if (idx != -1) {
                if (mappedLabelNamedIndividual.containsKey(property.getObject())){
                    propertyRowValues.createCell(idx).setCellValue(mappedLabelNamedIndividual.get(property.getObject()).toString());
                } else if (property.getObject().contains(RESOURCE_PREFIX)) {
                    propertyRowValues.createCell(idx).setCellValue(projectId + "/" + property.getObject().replace(RESOURCE_PREFIX, ""));
                } else {
                    if (!property.getObject().contains("^^")){
                        propertyRowValues.createCell(idx).setCellValue(property.getObject().replace(RESOURCE_PREFIX, ""));
                    } else {
                        //convertRDFLiteral(property.getObject().replace(RESOURCE_PREFIX, ""), propertyRowValues, idx);
                        String rdfLiteral = property.getObject().replace(RESOURCE_PREFIX, "");

                        int separatorIndex = rdfLiteral.indexOf("^^");

                        String lexicalValue = rdfLiteral.substring(0, separatorIndex);
                        String datatypeURI = rdfLiteral.substring(separatorIndex + 2);

                        Literal literal = ResourceFactory.createTypedLiteral(lexicalValue);

                        if (XSDDatatype.XSDdateTime.getURI().equals(datatypeURI)) {
                            Date date = (Date) literal.getValue();
                            System.out.println("----- DATE: " + date);
                            propertyRowValues.createCell(idx).setCellValue((Date) literal.getValue());
                        } else if (XSDDatatype.XSDdouble.getURI().equals(datatypeURI)) {
                            propertyRowValues.createCell(idx).setCellValue((Double) literal.getValue());
                        } else if (XSDDatatype.XSDint.getURI().equals(datatypeURI)) {
                            propertyRowValues.createCell(idx).setCellValue((int) literal.getValue());
                        } else if (XSDDatatype.XSDboolean.getURI().equals(datatypeURI)) {
                            propertyRowValues.createCell(idx).setCellValue((boolean) literal.getValue());
                        }
                    }
                }
            }
        }

        return rowNum + 1;  // Move to the next row for future entries
    }

    public void convertRDFLiteral(String rdfLiteral, Row propertyRowValues, int idx) {
        try {
            // Example inputs:
            // "2004-10-16T19:14:57+00:00"^^xsd:dateTime
            // "5.523E3"^^xsd:double

            int separatorIndex = rdfLiteral.indexOf("^^");

            String lexicalValue = rdfLiteral.substring(0, separatorIndex);
            String datatypeURI = rdfLiteral.substring(separatorIndex + 2);

            Literal literal = ResourceFactory.createTypedLiteral(lexicalValue);

            if (XSDDatatype.XSDdateTime.getURI().equals(datatypeURI)) {
                propertyRowValues.createCell(idx).setCellValue((Date) literal.getValue());
            } else if (XSDDatatype.XSDdouble.getURI().equals(datatypeURI)) {
                propertyRowValues.createCell(idx).setCellValue((Double) literal.getValue());
            } else if (XSDDatatype.XSDint.getURI().equals(datatypeURI)) {
                propertyRowValues.createCell(idx).setCellValue((int) literal.getValue());
            } else if (XSDDatatype.XSDboolean.getURI().equals(datatypeURI)) {
                propertyRowValues.createCell(idx).setCellValue((boolean) literal.getValue());
            }

        } catch (DatatypeFormatException e) {
            throw new IllegalArgumentException("Failed to parse the RDF literal.", e);
        }
    }
}
