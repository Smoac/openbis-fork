package ch.ethz.sis.rdf.main.xlsx;

import ch.ethz.sis.rdf.main.model.rdf.ModelRDF;
import ch.ethz.sis.rdf.main.model.xlsx.SampleObject;
import ch.ethz.sis.rdf.main.model.xlsx.SampleObjectProperty;
import ch.ethz.sis.rdf.main.model.xlsx.VocabularyTypeOption;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.poi.ss.usermodel.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RDFSampleHelper
{
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

    public static List<String> getAllColumnsList(List<String> sampleObjectPropertyLabelList)
    {
        List<String> defaultCols = new ArrayList<>(Stream.of(RDFSampleHelper.Attribute.values())
                .map(RDFSampleHelper.Attribute::name)
                .toList());
        defaultCols.addAll(sampleObjectPropertyLabelList);
        return defaultCols;
    }

    public int createSampleHeaders(Sheet sheet, int rowNum, CellStyle headerStyle, String sampleTypeKey, List<String> allColumnList)
    {

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

        for (int i = 0; i < allColumnList.size(); i++)
        {
            Cell cell = sampleTypeRowHeaders.createCell(i);
            cell.setCellValue(allColumnList.get(i));
            cell.setCellStyle(headerStyle);
        }

        return rowNum;
    }

    public int createResourceRows(Sheet sheet, int rowNum, String projectId, SampleObject sampleObject, ModelRDF modelRDF, List<String> allColumnList)
    {
        System.out.println("    createResourceRows: " + sampleObject);
        Row propertyRowValues = sheet.createRow(rowNum);
        //propertyRowValues.createCell(0).setCellValue(""); // $
        propertyRowValues.createCell(1).setCellValue(projectId + "/" + sampleObject.code); // Identifier
        propertyRowValues.createCell(2).setCellValue(sampleObject.code); // Code
        propertyRowValues.createCell(3).setCellValue(projectId.split("/")[1]); // Space
        propertyRowValues.createCell(4).setCellValue(projectId); // Project
        propertyRowValues.createCell(5).setCellValue(projectId + "/" + sampleObject.type.toUpperCase(Locale.ROOT) + "_COLLECTION"); // Experiment
        //propertyRowValues.createCell(6).setCellValue(""); // Parents
        //propertyRowValues.createCell(7).setCellValue(""); // Children

        int idxName = allColumnList.indexOf("Name");
        if (idxName != -1) {
            propertyRowValues.createCell(idxName).setCellValue(sampleObject.code);
        }

        List<String> vocabularyOptionList = modelRDF.vocabularyTypeList.stream()
                .flatMap(vocabularyType -> vocabularyType.getOptions().stream())
                .map(VocabularyTypeOption::getDescription)
                .toList();

        for (SampleObjectProperty sampleObjectProperty : sampleObject.properties)
        {
            //propertyRowValues.createCell(1).setCellValue(projectId + "/" + sampleObject.code); // Identifier
            //propertyRowValues.createCell(5).setCellValue(projectId + "/" + sampleObject.type.toUpperCase(Locale.ROOT) + "_COLLECTION"); // Experiment
            int idx = allColumnList.indexOf(sampleObjectProperty.label);
            if (idx != -1) {
                //System.out.println("MAPPED: " + sampleObjectProperty + ", CONTAINS: " + vocabularyOptionList.contains(sampleObjectProperty.value) + ", OBJ: " + sampleObjectProperty.value);
                if (vocabularyOptionList.contains(sampleObjectProperty.valueURI)){
                    propertyRowValues.createCell(idx).setCellValue(sampleObjectProperty.value.toUpperCase(Locale.ROOT));
                } else {
                    if (!sampleObjectProperty.value.contains("^^")){
                        propertyRowValues.createCell(idx).setCellValue(projectId + "/" +sampleObjectProperty.value);
                    } else {
                        //convertRDFLiteral(property.getObject().replace(RESOURCE_PREFIX, ""), propertyRowValues, idx);
                        String rdfLiteral = sampleObjectProperty.value;

                        int separatorIndex = rdfLiteral.indexOf("^^");

                        String lexicalValue = rdfLiteral.substring(0, separatorIndex);
                        String datatypeURI = rdfLiteral.substring(separatorIndex + 2);

                        Literal literal = ResourceFactory.createTypedLiteral(lexicalValue);

                        if (XSDDatatype.XSDdateTime.getURI().equals(datatypeURI)) {
                            Date date = (Date) literal.getValue();
                            //System.out.println("----- DATE: " + date);
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

    public void convertRDFLiteral(String rdfLiteral, Row propertyRowValues, int idx)
    {
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
