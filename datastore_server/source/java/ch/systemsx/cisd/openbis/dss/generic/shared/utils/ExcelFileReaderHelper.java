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

package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * @author Piotr Buczek
 */
public class ExcelFileReaderHelper
{

    public static void main(String[] args) throws Exception
    {
        String filename = args[0];
        File file = new File(filename);
        List<String[]> lines = loadExcelFile(file, true);
        for (String[] line : lines)
        {
            System.err.println(Arrays.toString(line));
        }
    }

    public static List<String[]> loadExcelFile(File file, boolean ignoreComments)
            throws IOException
    {
        final String extension = FilenameUtils.getExtension(file.getName()).toLowerCase();
        final FileInputStream stream = new FileInputStream(file);
        Workbook wb = null;
        if ("xls".equals(extension))
        {
            POIFSFileSystem poifsFileSystem = new POIFSFileSystem(stream);
            HSSFWorkbook hssfWorkbook = new HSSFWorkbook(poifsFileSystem);
            wb = hssfWorkbook;
            // HSSFWorkbook wb = new HSSFWorkbook(poifsFileSystem);

            ExcelExtractor extractor = new ExcelExtractor(hssfWorkbook);
            // HSSFFormulaEvaluator.evaluateAllFormulaCells(hssfWorkbook);
            //
            extractor.setIncludeBlankCells(true);
            extractor.setIncludeSheetNames(false);
            String text = extractor.getText();
            System.out.println(text);// returns TSV file

        } else if ("xlsx".equals(extension))
        {
            // spaces are not ignored
            XSSFWorkbook xssfWorkbook = new XSSFWorkbook(stream);
            wb = xssfWorkbook;

            // ExcelExtractor extractor = new ExcelExtractor(xssfWorkbook);
            //
            XSSFExcelExtractor extractor = new XSSFExcelExtractor(xssfWorkbook);
            // XSSFFormulaEvaluator evaluator = new XSSFFormulaEvaluator(xssfWorkbook);
            // XSSFFormulaEvaluator.evaluateAllFormulaCells(xssfWorkbook);

            extractor.setIncludeSheetNames(false);
            String text = extractor.getText();
            System.out.println(text);// returns TSV file
        } else
        {
            System.err.println(extension);
            throw new IllegalArgumentException(
                    "Expected an Excel file with 'xls' or 'xlsx' extension, got " + file.getName());
        }

        final Sheet sheet = wb.getSheetAt(0);

        return loadLines(sheet, ignoreComments);
    }

    // if the line starts with this character and comments should be ignored, the line is ignored
    private static final String COMMENT = "#";

    public static List<String[]> loadLines(Sheet sheet, boolean ignoreComments) throws IOException
    {
        final List<String[]> lines = new ArrayList<String[]>();

        boolean firstLine = true;
        // NOTE: the following code is pretty ugly - it is because poi API is very limited
        int headerSize = 0;
        for (Row row : sheet)
        {
            if (ignoreComments && row.getCell(0) != null
                    && row.getCell(0).toString().startsWith(COMMENT))
            {
                continue; // ignore lines with comments
            } else if (firstLine)
            {
                int maxColumnIndex = extractMaxColumnIndex(row);
                headerSize = maxColumnIndex + 1;
                String[] header = new String[headerSize];
                for (Cell cell : row)
                {
                    String value = extractCellValue(cell);
                    System.out.println("cell " + extractCellPosition(cell) + ":" + value);
                    header[cell.getColumnIndex()] = value;
                }
                lines.add(header);
                firstLine = false;
            } else
            {
                String[] line = new String[headerSize];
                for (Cell cell : row)
                {
                    String value = extractCellValue(cell);
                    System.out.println("cell " + extractCellPosition(cell) + ":" + value);
                    if (cell.getColumnIndex() >= line.length)
                    {
                        continue; // ignore for now
                    }
                    line[cell.getColumnIndex()] = value;
                }
                lines.add(line);
            }
        }

        for (String[] line : lines)
        {
            System.err.println(Arrays.toString(line));
        }
        return lines;
    }

    private static int extractMaxColumnIndex(Row row)
    {
        int maxColumnIndex = 0;
        for (Cell cell : row)
        {
            maxColumnIndex = cell.getColumnIndex();
        }
        return maxColumnIndex;
    }

    private static String extractCellValue(Cell cell)
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
                throw new UserFailureException("There is an error in cell "
                        + extractCellPosition(cell));
            default:
                throw new UserFailureException("Unknown data type of cell "
                        + extractCellPosition(cell));
        }
    }

    /** @return cell coordinates in Excel style (e.g. D5) */
    private static String extractCellPosition(Cell cell)
    {
        String col = CellReference.convertNumToColString(cell.getColumnIndex());
        String row = "" + (cell.getRowIndex() + 1);
        return col + row;
    }
}
