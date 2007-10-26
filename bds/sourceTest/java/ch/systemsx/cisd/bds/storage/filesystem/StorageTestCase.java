/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.bds.storage.filesystem;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.BeforeMethod;

/**
 * An <code>abtract</code> storage test case.
 * 
 * @author Franz-Josef Elmer
 */
public abstract class StorageTestCase
{
    static final File TEST_DIR = new File("targets" + File.separator + "unit-test-wd");

    @BeforeMethod
    public void setup() throws IOException
    {
        TEST_DIR.mkdirs();
        FileUtils.cleanDirectory(TEST_DIR);
    }

}
