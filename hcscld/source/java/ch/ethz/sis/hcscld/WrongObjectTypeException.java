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

package ch.ethz.sis.hcscld;

import java.io.File;

/**
 * An exception indicating that an {@link ObjectType} has been used that is incompatible with a
 * given dataset.
 * 
 * @author Bernd Rinn
 */
public class WrongObjectTypeException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    private final String dataSetCodeExpected;

    private final String dataSetCodeFound;

    private final File fileExpected;

    private final File fileFound;

    WrongObjectTypeException(String dataSetCodeExpected, File fileExpected,
            String dataSetCodeFound, File fileFound)
    {
        super(String.format("Dataset: [expected: %s, found: %s], File: [expected: %s, found: %s]",
                dataSetCodeExpected, dataSetCodeFound, fileExpected.getPath(), fileFound.getPath()));
        this.dataSetCodeExpected = dataSetCodeExpected;
        this.fileExpected = fileExpected;
        this.dataSetCodeFound = dataSetCodeFound;
        this.fileFound = fileFound;
    }

    public String getDataSetCodeExpected()
    {
        return dataSetCodeExpected;
    }

    public String getDataSetCodeFound()
    {
        return dataSetCodeFound;
    }

    public File getFileExpected()
    {
        return fileExpected;
    }

    public File getFileFound()
    {
        return fileFound;
    }

}
