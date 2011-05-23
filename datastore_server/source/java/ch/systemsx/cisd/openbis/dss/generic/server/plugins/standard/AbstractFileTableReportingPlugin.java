/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.csvreader.CsvReader;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.parser.ParserException;
import ch.systemsx.cisd.common.parser.ParsingException;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.CodeAndLabelUtil;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DatasetFileLines;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ExcelFileReaderHelper;
import ch.systemsx.cisd.openbis.generic.shared.basic.TableCellUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.util.SimpleTableModelBuilder;

/**
 * Abstract class for reporting plugins which read from CSV or TSV files.
 * 
 * @author Tomasz Pylak
 */
abstract public class AbstractFileTableReportingPlugin extends AbstractTableModelReportingPlugin
{
    protected static final char TAB_SEPARATOR = '\t';

    protected static final char SEMICOLON_SEPARATOR = ';';

    private static final long serialVersionUID = 1L;

    private static final String SEPARATOR_PROPERTY_KEY = "separator";

    public static final String IGNORE_COMMENTS_PROPERTY_KEY = "ignore-comments";

    public static final String IGNORE_TRAILING_EMPTY_CELLS_PROPERTY_KEY =
            "ignore-trailing-empty-cells";

    public static final String EXCEL_SHEET_PROPERTY_KEY = "excel-sheet"; // sheet index or name

    private static final String defaultExcelSheet = "0"; // 1st sheet (0 based)

    // if the line starts with this character and comments should be ignored, the line is ignored
    private static final char COMMENT = '#';

    private final char separator;

    private final boolean ignoreComments;

    private final boolean ignoreTrailingEmptyCells;

    private final String excelSheet;

    protected AbstractFileTableReportingPlugin(Properties properties, File storeRoot,
            char defaultSeparator)
    {
        super(properties, storeRoot);
        this.separator =
                PropertyUtils.getChar(properties, SEPARATOR_PROPERTY_KEY, defaultSeparator);
        this.ignoreComments =
                PropertyUtils.getBoolean(properties, IGNORE_COMMENTS_PROPERTY_KEY, true);
        this.ignoreTrailingEmptyCells =
                PropertyUtils.getBoolean(properties, IGNORE_TRAILING_EMPTY_CELLS_PROPERTY_KEY,
                        false);
        this.excelSheet =
                PropertyUtils.getProperty(properties, EXCEL_SHEET_PROPERTY_KEY, defaultExcelSheet);
    }

    /**
     * Loads {@link DatasetFileLines} from the specified tab file.
     * 
     * @throws IOExceptionUnchecked if a {@link IOException} has occurred.
     */
    protected DatasetFileLines loadFromFile(DatasetDescription dataset, final File file)
            throws ParserException, ParsingException, IllegalArgumentException,
            IOExceptionUnchecked
    {
        assert file != null : "Given file must not be null";
        assert file.isFile() : "Given file '" + file.getAbsolutePath() + "' is not a file.";

        if (isExcelFile(file))
        {
            try
            {
                Sheet sheet = getExcelSheet(file);
                return load(dataset, sheet, file);
            } catch (final IOException ex)
            {
                throw new IOExceptionUnchecked(ex);
            }
        } else
        {
            CsvReader reader = null;
            try
            {
                reader = readFile(file, ignoreComments, separator);
                return load(dataset, reader, file);
            } catch (final IOException ex)
            {
                throw new IOExceptionUnchecked(ex);
            } finally
            {
                if (reader != null)
                {
                    reader.close();
                }
            }
        }
    }

    private static boolean isExcelFile(File file)
    {
        return FilenameUtils.isExtension(file.getName().toLowerCase(), new String[]
            { "xls", "xlsx" });
    }

    private Sheet getExcelSheet(File file) throws IOException
    {
        Workbook wb = getExcelWorkbook(file);
        try
        {
            int index = Integer.parseInt(excelSheet);
            return wb.getSheetAt(index); // will throw exception if index is out of range
        } catch (NumberFormatException ex)
        {
            Sheet sheet = wb.getSheet(excelSheet);
            if (sheet == null)
            {
                throw new UserFailureException(file.getName() + " doesn't contain sheet named "
                        + excelSheet);
            }
            return sheet;
        }
    }

    private Workbook getExcelWorkbook(File file) throws IOException
    {
        final String extension = FilenameUtils.getExtension(file.getName()).toLowerCase();
        final FileInputStream stream = new FileInputStream(file);
        if ("xls".equals(extension))
        {
            POIFSFileSystem poifsFileSystem = new POIFSFileSystem(stream);
            return new HSSFWorkbook(poifsFileSystem);
        } else if ("xlsx".equals(extension))
        {
            return new XSSFWorkbook(stream);
        } else
        {
            throw new IllegalArgumentException(
                    "Expected an Excel file with 'xls' or 'xlsx' extension, got " + file.getName());
        }
    }

    private static CsvReader readFile(File file, boolean ignoreComments, char separator)
            throws IOException
    {
        if (file.isFile() == false)
        {
            throw new UserFailureException(file + " does not exist or is not a file.");
        }
        FileInputStream fileInputStream = new FileInputStream(file);

        CsvReader csvReader = new CsvReader(fileInputStream, Charset.defaultCharset());
        csvReader.setDelimiter(separator);
        csvReader.setSkipEmptyRecords(true);
        if (ignoreComments)
        {
            csvReader.setUseComments(true);
            csvReader.setComment(COMMENT);
        }
        return csvReader;
    }

    /**
     * Loads data from the specified sheet.
     * 
     * @throws IOException
     */
    private DatasetFileLines load(DatasetDescription dataset, Sheet sheet, File file)
            throws IOException
    {
        assert sheet != null : "Unspecified sheet";

        List<String[]> lines = ExcelFileReaderHelper.loadLines(sheet, ignoreComments);
        return new DatasetFileLines(file, dataset.getDataSetCode(), lines, ignoreTrailingEmptyCells);
    }

    /**
     * Loads data from the specified reader.
     */
    protected DatasetFileLines load(final DatasetDescription dataset, final CsvReader reader,
            final File file) throws ParserException, ParsingException, IllegalArgumentException,
            IOException
    {
        assert reader != null : "Unspecified reader";

        List<String[]> lines = new ArrayList<String[]>();
        while (reader.readRecord())
        {
            lines.add(reader.getValues());
        }
        return new DatasetFileLines(file, dataset.getDataSetCode(), lines, ignoreTrailingEmptyCells);
    }

    protected TableModel createTableModel(DatasetFileLines lines)
    {
        SimpleTableModelBuilder tableBuilder = new SimpleTableModelBuilder();
        for (String title : lines.getHeaderLabels())
        {
            CodeAndLabel codeAndTitle = CodeAndLabelUtil.create(title);
            tableBuilder.addHeader(codeAndTitle.getLabel(), codeAndTitle.getCode());
        }
        for (String[] line : lines.getDataLines())
        {
            List<ISerializableComparable> row = new ArrayList<ISerializableComparable>();
            for (String token : line)
            {
                row.add(TableCellUtil.createTableCell(token));
            }
            tableBuilder.addRow(row);
        }
        return tableBuilder.getTableModel();
    }

    protected static TableModel createTransposedTableModel(DatasetFileLines lines)
    {
        int columns = lines.getHeaderLabels().length;
        int rows = lines.getDataLines().size() + 1;
        String[][] all = new String[columns][rows];
        for (int r = 0; r < rows; r++)
        {
            for (int c = 0; c < columns; c++)
            {
                all[c][r] =
                        (r == 0) ? lines.getHeaderLabels()[c] : lines.getDataLines().get(r - 1)[c];
            }
        }
        SimpleTableModelBuilder tableBuilder = new SimpleTableModelBuilder();
        for (int r = 0; r < rows; r++)
        {
            tableBuilder.addHeader(all[0][r]);
        }
        for (int c = 1; c < columns; c++)
        {
            List<ISerializableComparable> row = new ArrayList<ISerializableComparable>();
            for (int r = 0; r < rows; r++)
            {
                row.add(TableCellUtil.createTableCell(all[c][r]));
            }
            tableBuilder.addRow(row);
        }
        return tableBuilder.getTableModel();
    }
}
