package ch.ethz.sis.rdf.main.xlsx;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.Locale;

public class RDFSampleTypeHelper {

    private enum Attribute {// implements IAttribute {
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

    protected int addSampleTypeSection(Sheet sheet, int rowNum, CellStyle headerStyle, String ontNamespace, String ontVersion, String ontClassURI,
            String label, String comment){
        Row sampleTypeRow = sheet.createRow(rowNum++);
        sampleTypeRow.createCell(0).setCellValue("SAMPLE_TYPE");
        sampleTypeRow.getCell(0).setCellStyle(headerStyle);

        Row sampleTypeRowHeaders = sheet.createRow(rowNum++);

        // Populate header row with enum values
        Attribute[] fields = Attribute.values();
        for (int i = 0; i < fields.length; i++) {
            Cell cell = sampleTypeRowHeaders.createCell(i);
            cell.setCellValue(fields[i].getHeaderName());
            cell.setCellStyle(headerStyle);
        }

        String code = label.trim().replaceAll(" ", "").toUpperCase(Locale.ROOT);
        String description = label + "\n" + comment;

        Row sampleTypeRowValues = sheet.createRow(rowNum++);

        sampleTypeRowValues.createCell(0).setCellValue(code);                   //Code("Code", true),
        sampleTypeRowValues.createCell(1).setCellValue(description);            //Description("Description", true),
        sampleTypeRowValues.createCell(2).setCellValue(1);                      //AutoGenerateCodes("Auto generate codes", true),
        //sampleTypeRowValues.createCell(3).setCellValue("");                       //ValidationScript("Validation script", true),
        sampleTypeRowValues.createCell(4).setCellValue(code.substring(0,3));    //GeneratedCodePrefix("Generated code prefix", true);
        sampleTypeRowValues.createCell(5).setCellValue(ontNamespace);           //OntologyId("Ontology Id", false),
        sampleTypeRowValues.createCell(6).setCellValue(ontVersion);             //OntologyVersion("Ontology Version", false),
        sampleTypeRowValues.createCell(7).setCellValue(ontClassURI);            //OntologyAnnotationId("Ontology Annotation Id", false),

        return rowNum;
    }
}
