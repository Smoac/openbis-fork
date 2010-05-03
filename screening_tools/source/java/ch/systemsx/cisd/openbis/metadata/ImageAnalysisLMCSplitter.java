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

package ch.systemsx.cisd.openbis.metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.csvreader.CsvReader;

/**
 * Splits a file with image analysis results for all plates into a set of files, one for each plate.
 * 
 * @author Tomasz Pylak
 */
public class ImageAnalysisLMCSplitter
{
    private static final char SEPARATOR = ',';

    public static void main(String[] args) throws FileNotFoundException, IOException
    {
        if (args.length != 1)
        {
            System.err
                    .println("There should be exactly one parameter: <image-analysis-results-file-path>");
            return;
        }
        CsvReader reader = readFile(args[0]);
        boolean ok = reader.readRecord();
        assert ok;
        String[] header = reader.getValues();
        splitPlates(reader, header);
    }

    private static CsvReader readFile(String path) throws FileNotFoundException, IOException
    {
        File file = new File(path);
        if (file.isFile() == false)
        {
            error(file + " does not exist or is not a file.");
        }
        FileInputStream fileInputStream = new FileInputStream(file);

        CsvReader csvReader = new CsvReader(fileInputStream, Charset.defaultCharset());
        csvReader.setDelimiter(SEPARATOR);
        csvReader.setSafetySwitch(false);
        return csvReader;
    }

    private static void error(String msg)
    {
        System.err.println(msg);
        System.exit(1);
    }

    private static void splitPlates(CsvReader reader, String[] header) throws IOException,
            FileNotFoundException
    {
        String prevPlateCode = "";
        OutputStream out = null;
        File parentDir = createPlatesParentDir();
        while (reader.readRecord())
        {
            String[] row = reader.getValues();
            String plateCode = row[0];
            if (plateCode.equals(prevPlateCode) == false)
            {
                if (out != null)
                {
                    out.close();
                }
                System.out.println("Generating " + plateCode);
                out = new FileOutputStream(new File(parentDir, plateCode + ".csv"));
                writeLine(header, out);
                prevPlateCode = plateCode;
            }
            writeLine(row, out);
        }
    }

    private static File createPlatesParentDir()
    {
        File dir = new File("plates");
        dir.mkdirs();
        return dir;
    }

    private static void writeLine(String[] header, OutputStream out) throws IOException
    {
        IOUtils.write(join(header) + "\n", out);
    }

    private static String join(String[] tokens)
    {
        for (int i = 0; i < tokens.length; i++)
        {
            tokens[i] = tokens[i].replace(';', ',');
        }
        return StringUtils.join(tokens, SEPARATOR);
    }
}
