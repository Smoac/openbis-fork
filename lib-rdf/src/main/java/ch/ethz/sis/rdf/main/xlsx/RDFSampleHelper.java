package ch.ethz.sis.rdf.main.xlsx;

import ch.ethz.sis.rdf.main.RDFParser;
import ch.ethz.sis.rdf.main.Utils;
import ch.ethz.sis.rdf.main.entity.OntClassObject;
import ch.ethz.sis.rdf.main.entity.PropertyTupleRDF;
import ch.ethz.sis.rdf.main.entity.ResourceRDF;
import org.apache.poi.ss.usermodel.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

    protected int createResourceRows(Sheet sheet, int rowNum, String projectId, ResourceRDF resource, OntClassObject ontClassObject) {
        List<String> allColumns = getAllColumnsList(ontClassObject);
        String resourcePrefix = "https://biomedit.ch/rdf/sphn-resource/";

        Row propertyRowValues = sheet.createRow(rowNum);
        //propertyRowValues.createCell(0).setCellValue(""); // $
        //propertyRowValues.createCell(1).setCellValue(""); // Identifier
        //propertyRowValues.createCell(2).setCellValue(property.getObject()); // Code
        propertyRowValues.createCell(3).setCellValue(projectId.split("/")[1]); // Space
        propertyRowValues.createCell(4).setCellValue(projectId); // Project
        propertyRowValues.createCell(5).setCellValue(projectId + resource.type); // Experiment
        //propertyRowValues.createCell(6).setCellValue(""); // Parents
        //propertyRowValues.createCell(7).setCellValue(""); // Children

        int idxName = allColumns.indexOf("Name");
        if (idxName != -1) {
            propertyRowValues.createCell(idxName).setCellValue(resource.resourceVal);
        }

        for (PropertyTupleRDF property : resource.properties) {
            System.out.println(property);
            propertyRowValues.createCell(1).setCellValue(projectId + "/" + resource.resourceVal); // Identifier
            propertyRowValues.createCell(5).setCellValue(projectId + "/" + Utils.extractLabel(resource.type).toUpperCase(Locale.ROOT) + "_COLLECTION"); // Experiment
            int idx = allColumns.indexOf(Utils.extractLabel(property.getPredicateLabel()));
            if (idx != -1) {
                propertyRowValues.createCell(idx).setCellValue(projectId + "/" + property.getObject().replace(resourcePrefix, ""));
            }
        }

        return rowNum + 1;  // Move to the next row for future entries
    }
}
