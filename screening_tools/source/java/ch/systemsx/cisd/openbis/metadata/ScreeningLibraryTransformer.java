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
