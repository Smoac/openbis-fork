/*
 * Copyright 2011 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.common.parser;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * @author Pawel Glyzewski
 */
public class ExcelRowTokenizer implements ILineTokenizer<Row>
{
    public void init()
    {
    }

    public String[] tokenize(Row row) throws ParsingException
    {
        return tokenizeRow(row);
    }

    public static String[] tokenizeRow(Row row)
    {
        String[] line = new String[row.getLastCellNum()];
        for (Cell cell : row)
        {
            String value = extractCellValue(cell);
            line[cell.getColumnIndex()] = value;
        }

        return line;
    }

    private static String extractCellValue(Cell cell) throws ParsingException
    {
        switch (cell.getCellType())
        {
            case Cell.CELL_TYPE_BLANK:
                return "BLANK";
            case Cell.CELL_TYPE_BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case Cell.CELL_TYPE_NUMERIC:
                return Double.toString(cell.getNumericCellValue());
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();
            case Cell.CELL_TYPE_FORMULA:
                throw new UserFailureException(
                        "Excel formulas are not supported but one was found in cell "
                                + extractCellPosition(cell));
            case Cell.CELL_TYPE_ERROR:
                throw new ParsingException(new String[]
                    { "There is an error in cell " + extractCellPosition(cell) },
                        cell.getRowIndex());
            default:
                throw new ParsingException(new String[]
                    { "Unknown data type of cell " + extractCellPosition(cell) },
                        cell.getRowIndex());
        }
    }

    /** @return cell coordinates in Excel style (e.g. D5) */
    private static String extractCellPosition(Cell cell)
    {
        String col = CellReference.convertNumToColString(cell.getColumnIndex());
        String row = "" + (cell.getRowIndex() + 1);
        return col + row;
    }

    public void destroy()
    {
    }
}
