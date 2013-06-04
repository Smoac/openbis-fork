/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class SpaceAttributeMappingManagerTest extends AbstractFileSystemTestCase
{
    private File as1;
    private File as2;

    @BeforeMethod
    public void prepareTestFiles()
    {
        as1 = new File(workingDirectory, "a-s1");
        as1.mkdir();
        as2 = new File(workingDirectory, "a-s2");
        as2.mkdir();
    }

    @Test
    public void test()
    {
        File mappingFile = new File(workingDirectory, "mapping.txt");
        FileUtilities.writeToFile(mappingFile, "Space\tLive Share\tArchive Folder\n"
                + "s1\t2\t" + as1 + "\n"
                + "s2\t4\t" + as2);
        
        SpaceAttributeMappingManager mappingManager = new SpaceAttributeMappingManager(mappingFile.getPath(), false);
        
        Map<String, File> foldersMap = mappingManager.getFoldersMap();
        assertEquals(as1.getPath(), foldersMap.get("S1").getPath());
        assertEquals(as2.getPath(), foldersMap.get("S2").getPath());
    }

}
