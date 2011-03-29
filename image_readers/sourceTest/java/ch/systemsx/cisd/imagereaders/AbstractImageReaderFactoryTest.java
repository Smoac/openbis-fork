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

package ch.systemsx.cisd.imagereaders;

import java.io.File;

import org.testng.AssertJUnit;

/**
 * Unit test for {@link ImageReaderFactory}.
 * 
 * @author Kaloyan Enimanev
 */
public abstract class AbstractImageReaderFactoryTest extends AssertJUnit
{

    protected static final String IMAGES_DIR = "./sourceTest/resources/images/";

    protected static final String VALID_SUBDIR = "valid";

    protected static final String INVALID_SUBDIR = "invalid";


    protected File getImageFileForLibrary(String libraryName, String fileName)
    {
        return new File(getValidImagesDir(libraryName), fileName);
    }

    protected File getValidImagesDir(String libraryName)
    {
        return new File(IMAGES_DIR + libraryName.toLowerCase(), VALID_SUBDIR);
    }

    protected File getInvalidImagesDir(String libraryName)
    {
        return new File(IMAGES_DIR + libraryName.toLowerCase(), INVALID_SUBDIR);
    }
}
