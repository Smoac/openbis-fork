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

package ch.systemsx.cisd.openbis.plugin.screening.transformers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;

import com.csvreader.CsvReader;

/**
 * Transforms a screening library file and produces files which can be uploaded to openBIS: genes,
 * oligos and plates with wells.
 * 
 * @author Tomasz Pylak
 */
public class ScreeningLibraryTransformer
{
    private final static char SEPARATOR = ',';

    public static void main(String[] args) throws FileNotFoundException, IOException
    {
        if (args.length != 4)
        {
            error("Invalid parameters. Expected: "
                    + "<master-plate-file-path> <experiment-identifier> <plate-geometry> <group>");
        }
        CsvReader csvReader = readFile(args[0]);
        String experimentIdentifier = args[1];
        String plateGeometry = args[2];
        String groupCode = args[3];
        readLibrary(csvReader, experimentIdentifier, plateGeometry, groupCode);
        csvReader.close();
    }

    private static void readLibrary(CsvReader csvReader, String experimentIdentifier,
            String plateGeometry, String groupCode) throws IOException
    {
        System.out.println("Processing...");
        boolean headerPresent = csvReader.readRecord();
        if (headerPresent == false)
        {
            error("header not found");
            return;
        }
        String[] headers = csvReader.getValues();
        IScreeningLibraryColumnExtractor extractor =
                new QiagenScreeningLibraryColumnExtractor(headers);
        LibraryEntityRegistrator registrator =
                new LibraryEntityRegistrator(extractor, experimentIdentifier, plateGeometry,
                        groupCode);
        while (csvReader.readRecord())
        {
            String[] row = csvReader.getValues();
            registrator.register(extractor, row);
        }
        System.out.println("Done, look for results in " + new File(".").getAbsolutePath());
    }

    static CsvReader readFile(String path) throws FileNotFoundException, IOException
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
}
