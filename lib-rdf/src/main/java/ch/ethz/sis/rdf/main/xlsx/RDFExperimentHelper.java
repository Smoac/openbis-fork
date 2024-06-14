package ch.ethz.sis.rdf.main.xlsx;

import ch.ethz.sis.rdf.main.RDFParser;
import ch.ethz.sis.rdf.main.Utils;
import org.apache.jena.atlas.lib.Pair;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.List;
import java.util.Locale;

public class RDFExperimentHelper
{
    private static final String EXPERIMENT_TYPE_FIELD = "Experiment type";
    private static final String EXPERIMENT = "EXPERIMENT";
    private static final String COLLECTION_TYPE = "COLLECTION";

    private enum Attribute { //implements IAttribute {
        Identifier("Identifier", false),
        Code("Code", true),
        Project("Project", true),
        Name("Name", true),
        DefaultObjectType("Default object type", true);

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

    protected int addExperimentSection(Sheet sheet, int rowNum, CellStyle headerStyle, String projectId, RDFParser rdfParser){
        Row row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue(EXPERIMENT);
        row.getCell(0).setCellStyle(headerStyle);

        Row rowType = sheet.createRow(rowNum++);
        rowType.createCell(0).setCellValue(EXPERIMENT_TYPE_FIELD);
        rowType.getCell(0).setCellStyle(headerStyle);

        Row rowTypeValues = sheet.createRow(rowNum++);

        rowTypeValues.createCell(0).setCellValue(COLLECTION_TYPE);

        Row rowHeaders = sheet.createRow(rowNum++);

        // Populate header row with enum values
        Attribute[] fields = Attribute.values();
        for (int i = 0; i < fields.length; i++) {
            Cell cell = rowHeaders.createCell(i);
            cell.setCellValue(fields[i].getHeaderName());
            cell.setCellStyle(headerStyle);
        }

        List<Pair> classes = rdfParser.classDetailsMap.values()
                .stream()
                .map(ontClassObject -> new Pair(ontClassObject.ontClass.getLocalName(), ontClassObject.label))
                .toList();

        for (Pair pair : classes) {

            Row rowValues = sheet.createRow(rowNum++);

            String objectType = pair.getLeft().toString().toUpperCase(Locale.ROOT);
            String code = objectType + "_" + COLLECTION_TYPE;
            String name = pair.getRight().toString() + " Collection";

            rowValues.createCell(0).setCellValue(projectId + "/" + code);   // Identifier("Identifier", false),
            rowValues.createCell(1).setCellValue(code);                     // Code("Code", true),
            rowValues.createCell(2).setCellValue(projectId);                // Project("Project", true),
            rowValues.createCell(3).setCellValue(name);                     // Name("Name", true),
            rowValues.createCell(4).setCellValue(objectType);               // DefaultObjectType("Default object type", true);
        }

        Utils.autosizeColumns(sheet, fields.length);

        // add empty row
        sheet.createRow(rowNum++);

        return rowNum;
    }
}
