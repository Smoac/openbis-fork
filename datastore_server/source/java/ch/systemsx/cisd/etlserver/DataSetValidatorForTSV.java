/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import ch.systemsx.cisd.etlserver.utils.FileScanner;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;

/**
 * Validator for data sets containing TAB-separated value (TSV) files.
 *
 * @author Franz-Josef Elmer
 */
class DataSetValidatorForTSV implements IDataSetValidator
{
    private static final String PATH_PATTERNS_KEY = "path-patterns";
    
    private final List<FileScanner> fileScanners;

    DataSetValidatorForTSV(Properties properties)
    {
        fileScanners = new ArrayList<FileScanner>();
        String pathPatterns = properties.getProperty(PATH_PATTERNS_KEY);
        if (pathPatterns != null)
        {
            StringTokenizer tokenizer = new StringTokenizer(pathPatterns, ",");
            while (tokenizer.hasMoreTokens())
            {
                String pathPattern = tokenizer.nextToken().trim();
                fileScanners.add(new FileScanner(pathPattern));
            }
        }
    }
    
    public void assertValidDataSet(DataSetType dataSetType, File incomingDataSetFileOrFolder)
    {
        for (FileScanner fileScanner : fileScanners)
        {
            List<File> files = fileScanner.scan(incomingDataSetFileOrFolder);
            for (File file : files)
            {
                assertValidFile(file);
            }
        }
    }
    
    private void assertValidFile(File file)
    {
        
    }

}
