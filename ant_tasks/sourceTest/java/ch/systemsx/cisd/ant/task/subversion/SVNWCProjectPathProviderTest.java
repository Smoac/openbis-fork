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

package ch.systemsx.cisd.ant.task.subversion;

import static org.testng.AssertJUnit.assertEquals;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.utilities.FileUtilities;

/**
 * Test cases for the {@link SVNWCProjectPathProvider}.
 * 
 * @author Bernd Rinn
 */
public class SVNWCProjectPathProviderTest
{

    private static final File unitTestRootDirectory = new File("targets" + File.separator + "unit-test-wd");

    private static final File workingDirectory = new File(unitTestRootDirectory, "SVNWCProjectPathProvider");

    @BeforeClass
    public void init()
    {
        LogInitializer.init();
        unitTestRootDirectory.mkdirs();
        assert unitTestRootDirectory.isDirectory();
    }

    @BeforeMethod
    public void setUp()
    {
        FileUtilities.deleteRecursively(workingDirectory);
        workingDirectory.mkdirs();
        workingDirectory.deleteOnExit();
    }

    @Test
    public void testIsRepositoryPath()
    {
        final String project = "someProject";
        final File workingCopyDirectory = new File(workingDirectory, project);
        workingCopyDirectory.mkdir();
        assert workingCopyDirectory.isDirectory();
        workingDirectory.deleteOnExit();
        final ISVNProjectPathProvider provider = new SVNWCProjectPathProvider(workingCopyDirectory);
        assert false == provider.isRepositoryPath();
    }

    @Test
    public void testPathStandardCase()
    {
        final String project = "someProject";
        final String subProject = "subProject";
        final String path = "somePath";
        final File workingCopyDirectory = new File(workingDirectory, project);
        workingCopyDirectory.mkdir();
        assert workingCopyDirectory.isDirectory();
        workingDirectory.deleteOnExit();
        final ISVNProjectPathProvider provider = new SVNWCProjectPathProvider(workingCopyDirectory);
        String expectedPath;
        expectedPath =
                StringUtils.join(Arrays.asList(workingDirectory.getAbsolutePath(), project), File.separator);
        assertEquals(expectedPath, provider.getPath());
        expectedPath =
                StringUtils.join(Arrays.asList(workingDirectory.getAbsolutePath(),
                subProject), File.separator);
        assertEquals(expectedPath, provider.getPath(subProject));
        expectedPath =
                StringUtils.join(Arrays.asList(workingDirectory.getAbsolutePath(),
                subProject, path), File.separator);
        assertEquals(expectedPath, provider.getPath(subProject, path));
    }

    @Test
    public void testPathConversion()
    {
        if (File.separatorChar == '/')
        {
            final String project = "someProject";
            final String subProject = "subProject";
            final String path = "somePath\\two";
            final File workingCopyDirectory = new File(workingDirectory, project);
            workingCopyDirectory.mkdir();
            assert workingCopyDirectory.isDirectory();
            workingDirectory.deleteOnExit();
            final ISVNProjectPathProvider provider = new SVNWCProjectPathProvider(workingCopyDirectory);
            final String expectedPath =
                    StringUtils.join(Arrays.asList(workingDirectory.getAbsolutePath(), subProject,
            "somePath/two"), File.separator);
            assertEquals(expectedPath, provider.getPath(subProject, path));
        } else
        {
            final String project = "someProject";
            final String subProject = "subProject";
            final String path = "somePath/two";
            final File workingCopyDirectory = new File(workingDirectory, project);
            workingCopyDirectory.mkdir();
            final ISVNProjectPathProvider provider = new SVNWCProjectPathProvider(workingCopyDirectory);
            final String expectedPath =
                    StringUtils.join(Arrays.asList(workingDirectory.getAbsolutePath(), subProject,
            "somePath\\two"), File.separator);
            assertEquals(expectedPath, provider.getPath(subProject, path));
        }
    }

}
