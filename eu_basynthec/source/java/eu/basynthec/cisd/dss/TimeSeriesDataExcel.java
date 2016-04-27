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

package eu.basynthec.cisd.dss;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Workbook;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ExcelFileReader;

/**
 * An abstraction for accessing time series data following the BaSynthec conventions from an Excel file.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class TimeSeriesDataExcel
{
    private static final String OPENBIS_METADATA_SHEET_NAME = "openbis-metadata";

    private static final String OPENBIS_DATA_SHEET_NAME = "openbis-data";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            TimeSeriesDataExcel.class);

    /**
     * Create a time series data from the given file name.
     * 
     * @param fileName
     * @return A TimeSeriesDataExcel or null if one could not be created.
     */
    public static TimeSeriesDataExcel createTimeSeriesDataExcel(String fileName)
    {
        File file = new File(fileName);
        Workbook workbook;
        try
        {
            workbook = ExcelFileReader.getExcelWorkbook(file);
            ExcelFileReader fileReader = new ExcelFileReader(workbook, true);
            return new TimeSeriesDataExcel(file, fileReader);
        } catch (IllegalArgumentException ex)
        {
            operationLog.error("Could not open file [" + fileName + "] as Excel data.", ex);
        } catch (IOException ex)
        {
            operationLog.error("Could not open file [" + fileName + "] as Excel data.", ex);
        }

        return null;
    }

    private final File file;

    private final ExcelFileReader fileReader;

    /**
     * Constructor
     */
    private TimeSeriesDataExcel(File file, ExcelFileReader fileReader)
    {
        super();
        this.file = file;
        this.fileReader = fileReader;
    }

    /**
     * Get the raw lines of the metadata sheet.
     */
    public List<String[]> getRawMetadataLines()
    {
        try
        {
            return fileReader.readLines(OPENBIS_METADATA_SHEET_NAME);
        } catch (IOException ex)
        {
            operationLog.error("Could not read data from [file: " + file.getPath() + ", sheet: "
                    + OPENBIS_METADATA_SHEET_NAME + "]", ex);
        }
        return new ArrayList<String[]>();
    }

    /**
     * Get the raw lines of the data sheet.
     */
    public List<String[]> getRawDataLines()
    {
        try
        {
            return fileReader.readLines(OPENBIS_DATA_SHEET_NAME);
        } catch (IOException ex)
        {
            operationLog.error("Could not read data from [file: " + file.getPath() + ", sheet: "
                    + OPENBIS_DATA_SHEET_NAME + "]", ex);
        }
        return new ArrayList<String[]>();
    }

    /**
     * Return the metadata has a hashmap, with all keys uppercased.
     * <p>
     * Assumes the metadata sheet corresponds to the following format: [Property] [Value] [... stuff that can be ignored], that is the property name
     * is in column 1 and property value is in column 2, and everything else can be ignored.
     */
    public Map<String, String> getMetadataMap()
    {
        HashMap<String, String> metadataMap = new HashMap<String, String>();
        List<String[]> metadataLines = getRawMetadataLines();

        // Skip the first line, this is just the header
        for (int i = 1; i < metadataLines.size(); ++i)
        {
            String[] line = metadataLines.get(i);
            String key = line[0];
            if (key == null)
            {
                continue;
            }
            String value = line[1];
            if ("BLANK".equals(value))
            {
                value = null;
            }
            metadataMap.put(line[0].toUpperCase(), value);
        }

        return metadataMap;
    }
}
