package ch.ethz.sis.rdf.main.xlsx;

import ch.ethz.sis.rdf.main.model.xlsx.VocabularyType;
import ch.ethz.sis.rdf.main.model.xlsx.VocabularyTypeOption;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import static ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.ExportableKind.VOCABULARY_TYPE;

public class RDFVocabularyTypeHelper
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

    private enum AttributeValue { // implements IAttribute {
        Code("Code", true),
        Label("Label", true),
        Description("Description", true);

        private final String headerName;

        private final boolean mandatory;

        AttributeValue(String headerName, boolean mandatory) {
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

    protected int addVocabularyTypeSection(Sheet sheet, int rowNum, CellStyle headerStyle, VocabularyType vocabularyType)
    {
        Row row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue(VOCABULARY_TYPE.name());
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

        //TODO add code and desc dynamic
        rowValues.createCell(0).setCellValue(vocabularyType.getCode());     //Code("Code", true),
        rowValues.createCell(1).setCellValue(vocabularyType.getDescription());               //Description("Description", true),

        return rowNum;
    }

    public int addVocabularyTypes(Sheet sheet, int rowNum, CellStyle headerStyle, VocabularyType vocabularyType)
    {

        rowNum = addVocabularyTypeSection(sheet, rowNum, headerStyle, vocabularyType);

        Row vocabularyTypeRowHeaders = sheet.createRow(rowNum++);

        // Populate header row with enum values
        AttributeValue[] fields = AttributeValue.values();
        for (int i = 0; i < fields.length; i++) {
            Cell cell = vocabularyTypeRowHeaders.createCell(i);
            cell.setCellValue(fields[i].getHeaderName());
            cell.setCellStyle(headerStyle);
        }

        for(VocabularyTypeOption vto: vocabularyType.getOptions()){
            System.out.println(vto);
            Row vocabularyTypeRowValues = sheet.createRow(rowNum++);
            vocabularyTypeRowValues.createCell(0).setCellValue(vto.code); //Code("Code", true),
            vocabularyTypeRowValues.createCell(1).setCellValue(vto.label); //Label("Label", true),
            vocabularyTypeRowValues.createCell(2).setCellValue(vto.description); //Description("Description", true),
        }

        sheet.createRow(rowNum++);

        return rowNum;
    }
}
