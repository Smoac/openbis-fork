package ch.ethz.sis.rdf.main.xlsx;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class RDFProjectHelper
{
    private static final String PROJECT = "PROJECT";

    private enum Attribute { // implements IAttribute {
        Identifier("Identifier", false),
        Code("Code", true),
        Space("Space", true),
        Description("Description", false);

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

    protected int addProjectSection(Sheet sheet, int rowNum, CellStyle headerStyle, String spaceId, String projectId){
        Row row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue(PROJECT);
        row.getCell(0).setCellStyle(headerStyle);

        Row rowHeaders = sheet.createRow(rowNum++);

        // Populate header row with enum values
        Attribute[] fields = Attribute.values();
        for (int i = 0; i < fields.length; i++) {
            Cell cell = rowHeaders.createCell(i);
            cell.setCellValue(fields[i].getHeaderName());
            cell.setCellStyle(headerStyle);
        }

        String[] codes = projectId.split("/");
        String code = codes[codes.length - 1];

        Row rowValues = sheet.createRow(rowNum++);

        rowValues.createCell(0).setCellValue(projectId);      // Identifier("Identifier", false),
        rowValues.createCell(1).setCellValue(code);      // Code("Code", true),
        rowValues.createCell(2).setCellValue(spaceId);      // Space("Space", true),
        rowValues.createCell(3).setCellValue("");      // Description("Description", false);

        // add empty row
        sheet.createRow(rowNum++);

        return rowNum;
    }
}
