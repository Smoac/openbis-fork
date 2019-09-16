from org.apache.commons.lang3 import StringUtils
from org.apache.poi.ss.usermodel import CellType
from org.apache.poi.ss.usermodel import WorkbookFactory
from org.apache.poi.ss.util import NumberToTextConverter
from java.io import ByteArrayInputStream


class ExcelToPoiParser(object):

    @staticmethod
    def parse(excel_byte_array):
        workbook = WorkbookFactory.create(ByteArrayInputStream(excel_byte_array))

        definitions = []
        for sheet in workbook.sheetIterator():
            i = 0
            sheet_done = False
            while not sheet_done:
                definition_rows = []
                while not ExcelToPoiParser.is_row_empty(sheet.getRow(i)):
                    row = sheet.getRow(i)
                    definition_rows.append(row)
                    i += 1
                i += 1  # skip empty row

                if not definition_rows:
                    sheet_done = True
                else:
                    definitions.append(definition_rows)

        workbook.close()

        definitions_stripped = []
        for definition in definitions:
            definition_stripped = []
            for row in definition:
                row_stripped = {}
                for cell in row.cellIterator():
                    cell_value = ExcelToPoiParser.extract_string_value_from(cell)
                    cell_value = cell_value if cell_value != '' else None
                    cell_col = cell.getColumnIndex()
                    row_stripped[cell_col] = cell_value
                definition_stripped.append(row_stripped)
            definitions_stripped.append(definition_stripped)

        return definitions_stripped

    @staticmethod
    def extract_string_value_from(cell):
        cell_type = cell.getCellTypeEnum()
        if cell_type == CellType.BLANK:
            return ""
        elif cell_type == CellType.BOOLEAN:
            return str(cell.getBooleanCellValue())
        elif cell_type == CellType.NUMERIC:
            return NumberToTextConverter.toText(cell.getNumericCellValue())
        elif cell_type == CellType.STRING:
            return cell.getStringCellValue()
        elif cell_type == CellType.FORMULA:
            raise SyntaxError("Excel formulas are not supported but one was found in the sheet")
        elif cell_type == CellType.ERROR:
            raise SyntaxError("There is an error in a cell in the excel sheet")
        else:
            raise SyntaxError("Unknown data type of cell in the excel sheet")

    @staticmethod
    def is_row_empty(row):
        if row is None:
            return True
        if row.getLastCellNum <= 0:
            return True

        return all(ExcelToPoiParser.is_cell_empty(cell) for cell in row.cellIterator())

    @staticmethod
    def is_cell_empty(cell):
        if cell is not None and cell.getCellType() != CellType.BLANK and StringUtils.isNotBlank(cell.toString()):
            return False
        return True
