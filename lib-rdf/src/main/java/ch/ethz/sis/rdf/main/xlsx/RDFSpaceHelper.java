package ch.ethz.sis.rdf.main.xlsx;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;


public class RDFSpaceHelper
{
    private enum Attribute { // implements IAttribute {
        Code("Code", true),
        Description("Description", true);

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

    public int addSpaceSection(Sheet sheet, int rowNum, CellStyle headerStyle, String spaceId){
        Row row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue("SPACE");
        row.getCell(0).setCellStyle(headerStyle);

        Row rowHeaders = sheet.createRow(rowNum++);

        // Populate header row with enum values
       Attribute[] fields = Attribute.values();
        for (int i = 0; i < fields.length; i++) {
            Cell cell = rowHeaders.createCell(i);
            cell.setCellValue(fields[i].getHeaderName());
            cell.setCellStyle(headerStyle);
        }

        Row rowValues = sheet.createRow(rowNum++);

        rowValues.createCell(0).setCellValue(spaceId);       //Code("Code", true),
        rowValues.createCell(1).setCellValue("");            //Description("Description", true),

        // add empty row
        sheet.createRow(rowNum++);

        return rowNum;
    }
}
