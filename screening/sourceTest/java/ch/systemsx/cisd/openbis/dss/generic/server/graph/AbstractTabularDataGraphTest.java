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

package ch.systemsx.cisd.openbis.dss.generic.server.graph;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.csvreader.CsvReader;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.DatasetFileLines;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public abstract class AbstractTabularDataGraphTest extends AbstractFileSystemTestCase
{
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
     * Return the tabular data as a DatasetFileLines.
     */
    protected DatasetFileLines getBigNumberDatasetFileLines() throws IOException
    {
        File file =
                new File(
                        "sourceTest/java/ch/systemsx/cisd/openbis/dss/generic/server/graph/BigNumbers.csv");
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

    /**
     * Return an output stream for writing the image.
     */
    protected OutputStream getOutputStream(File outputFile)
    {
        try
        {
            return new FileOutputStream(outputFile);
        } catch (FileNotFoundException ex)
        {
            fail("Could not create output file");

            // never gets here
            return null;
        }
    }

    /**
     * Return a file for writing an image to. The file will be deleted when the test completes. Use
     * this for normal testing.
     */
    protected File getImageOutputFile()
    {
        return new File(workingDirectory, "test.png");
    }

    /**
     * Return a file for writing an image to. The file will <b>not</b> be deleted when the test
     * completes. Use this to view the result of the graph; useful for tweaking appearance.
     */
    protected File getTestImageOutputFile()
    {
        return getTestImageOutputFile("test.png");
    }

    /**
     * Return a file for writing an image to. The file will <b>not</b> be deleted when the test
     * completes. Use this to view the result of the graph; useful for tweaking appearance.
     * 
     * @param fileName The name for the output file
     */
    protected File getTestImageOutputFile(String fileName)
    {
        // For Testing, put it on the Desktop
        return new File(new File(TARGETS_DIRECTORY + "/" + UNIT_TEST_WORKING_DIRECTORY), fileName);
    }

}