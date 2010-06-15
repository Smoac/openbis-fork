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

package ch.systemsx.cisd.openbis.dss.etl.dataaccess.fvec;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.csvreader.CsvReader;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.fvec.CSVToCanonicalFeatureVector.CSVToCanonicalFeatureVectorConfiguration;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.DatasetFileLines;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class CSVToCanonicalFeatureVectorTest extends AssertJUnit
{
    @Test
    public void testConversion()
    {
        try
        {
            CSVToCanonicalFeatureVectorConfiguration config =
                    new CSVToCanonicalFeatureVectorConfiguration("WellName", "WellName", true);
            CSVToCanonicalFeatureVector convertor =
                    new CSVToCanonicalFeatureVector(getDatasetFileLines(), config);
            convertor.convert();
        } catch (IOException ex)
        {
            fail(ex.getMessage());
        }
    }

    /**
     * Return the tabular data as a DatasetFileLines.
     */
    protected DatasetFileLines getDatasetFileLines() throws IOException
    {
        File file =
                new File(
                        "sourceTest/java/ch/systemsx/cisd/openbis/dss/generic/server/graph/CP037-1df.csv");
        CsvReader reader = getCsvReader(file);
        List<String[]> lines = new ArrayList<String[]>();
        while (reader.readRecord())
        {
            lines.add(reader.getValues());
        }

        return new DatasetFileLines(file, "test", lines);

    }

    /**
     * Get a CsvReader for parsing a tabular data file.
     */
    protected CsvReader getCsvReader(File file) throws IOException
    {
        if (file.isFile() == false)
        {
            throw new UserFailureException(file + " does not exist or is not a file.");
        }
        FileInputStream fileInputStream = new FileInputStream(file);

        CsvReader csvReader = new CsvReader(fileInputStream, Charset.defaultCharset());
        csvReader.setDelimiter(';');
        csvReader.setSkipEmptyRecords(true);
        csvReader.setUseComments(true);
        csvReader.setComment('#');

        return csvReader;
    }
}
