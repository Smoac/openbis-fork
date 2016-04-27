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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.*;

import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.utilities.CollectionIO;
import ch.systemsx.cisd.common.utilities.FileUtilities;

/**
 * Test cases for the {@link SVNRecursiveCheckoutTask}.
 * 
 * @author Bernd Rinn
 */
public class SVNRecursiveCheckoutTaskTest
{

    private static final File unitTestRootDirectory = new File("targets" + File.separator + "unit-test-wd");

    private static final File workingDirectory = new File(unitTestRootDirectory, "SVNRecursiveCheckoutTask");

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

    /**
     * A class that tracks what checkout commands are called.
     */
    private static class SVNCheckoutMock implements ISVNCheckout
    {

        final String repositoryUrl;

        final String prefix;
        
        final String workingCopyDir;

        final List<String> checkedOutPaths;

        final List<String> checkedOutProjects;
        
        final List<String> checkedOutRevision;

        SVNCheckoutMock(String repositoryUrl, String workingCopyDir)
        {
            this.repositoryUrl = repositoryUrl;
            this.prefix = repositoryUrl + "/";
            this.workingCopyDir = workingCopyDir;
            this.checkedOutPaths = new ArrayList<String>();
            this.checkedOutProjects = new ArrayList<String>();
            this.checkedOutRevision = new ArrayList<String>();
        }

        public void checkout(String path, String projectName, String revision) throws SVNException
        {
            assert path.startsWith(prefix);
            
            checkedOutPaths.add(path.substring(prefix.length()));
            checkedOutProjects.add(projectName);
            checkedOutRevision.add(revision);
        }

        public String getDirectoryToCheckout()
        {
            return workingCopyDir;
        }

    }

    private static class SVNRecursiveCheckoutTaskCheckoutMock extends SVNRecursiveCheckoutTask
    {

        SVNCheckoutMock mock;

        @Override
        ISVNCheckout createSVNCheckout(String repositoryUrl, String workingCopyDir)
        {
            assert (mock == null);
            mock = new SVNCheckoutMock(repositoryUrl, workingCopyDir);
            return mock;
        }

    }

    @Test
    public void testNoClasspathFile()
    {
        final File dir = new File(workingDirectory, "projectNoClasspath");
        dir.delete();
        dir.deleteOnExit();
        assert dir.mkdir();

        final SVNRecursiveCheckoutTaskCheckoutMock task = new SVNRecursiveCheckoutTaskCheckoutMock();
        task.setDir(dir.getAbsolutePath());
        final String reposUrl = "http://somehost/somerepos";
        final String projPath = "someproj";
        task.setRepositoryRoot(reposUrl);
        task.setName(projPath);
        task.setFeatureBranch("someBranch");
        task.setProject(new Project()); // Required for log not to throw a NPE.
        task.execute();
        assertEquals(2, task.mock.checkedOutPaths.size());
        assertEquals(projPath, task.mock.checkedOutPaths.get(0));
        // That is a hidden dependency that is always resolved.
        assertEquals(SVNUtilities.BUILD_RESOURCES_PROJECT, task.mock.checkedOutPaths.get(1));
        assertEquals(projPath, task.mock.checkedOutProjects.get(0));
        assertEquals(SVNUtilities.BUILD_RESOURCES_PROJECT, task.mock.checkedOutProjects.get(1));
        assertEquals(SVNUtilities.HEAD_REVISION, task.mock.checkedOutRevision.get(0));
        assertEquals(SVNUtilities.HEAD_REVISION, task.mock.checkedOutRevision.get(1));
    }

    @DataProvider(name = "reposUrl")
    public Object[][] getRepositoryUrl()
    {
        return new Object[][]
            {
                { "http://somehost/somerepos", "projectWithClasspath", false },
                { "http://somehost/somerepos/trunk", "projectWithClasspath", true } };
    }

    @Test(dataProvider = "reposUrl")
    public void testClasspathFile(String reposUrl, String projPath, boolean isTrunk)
    {
        final String classPathFileContent =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                        + "<classpath>\n"
                        + "   <classpathentry kind=\"src\" path=\"sourceTest/java\"/>\n"
                        + "   <classpathentry kind=\"src\" path=\"source/java\"/>\n"
                        + "   <classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER\"/>\n"
                        + "   <classpathentry kind=\"lib\" path=\"/libraries/activation/activation.jar\"/>\n"
                        + "   <classpathentry kind=\"lib\" path=\"/libraries/commons-cli/commons-cli.jar\" sourcepath=\"/libraries/commons-cli/src.zip\"/>\n"
                        + "   <classpathentry kind=\"lib\" path=\"/libraries/log4j/log4j.jar\" sourcepath=\"/libraries/log4j/src.zip\"/>\n"
                        + "   <classpathentry kind=\"lib\" path=\"/libraries/mail/mail.jar\"/>\n"
                        + "   <classpathentry kind=\"lib\" path=\"/libraries/testng/testng-jdk15.jar\" sourcepath=\"/libraries/testng/src.zip\"/>\n"
                        + "   <classpathentry combineaccessrules=\"false\" kind=\"src\" path=\"/common\"/>\n"
                        + "   <classpathentry kind=\"output\" path=\"targets/classes\"/>\n" + "</classpath>\n";
        final File someProj = new File(workingDirectory, projPath);
        someProj.delete();
        someProj.deleteOnExit();
        assert someProj.mkdir();

        final SVNRecursiveCheckoutTaskCheckoutMock task = new SVNRecursiveCheckoutTaskCheckoutMock();
        final File classPathFile = new File(someProj, ".classpath");
        classPathFile.deleteOnExit();
        CollectionIO.writeIterable(classPathFile, Collections.singleton(classPathFileContent));
        task.setDir(someProj.getParentFile().getAbsolutePath());
        task.setRepositoryRoot(reposUrl);
        task.setName(projPath);
        if (false == isTrunk)
        {
            task.setFeatureBranch("someBranch");
        }
        task.setProject(new Project()); // Required for log not to throw a NPE.
        task.execute();

        final List<String> expectedPaths =
                isTrunk ? Arrays.asList(projPath + "/trunk", SVNUtilities.BUILD_RESOURCES_PROJECT
                        + "/trunk", "libraries/trunk", "common/trunk") : Arrays.asList(projPath,
                                SVNUtilities.BUILD_RESOURCES_PROJECT, "libraries", "common");
        assertEquals(expectedPaths, task.mock.checkedOutPaths);
        final List<String> expectedProjects =
            Arrays.asList(projPath, SVNUtilities.BUILD_RESOURCES_PROJECT, "libraries", "common");
        assertEquals(expectedProjects, task.mock.checkedOutProjects);
        assertEquals(task.mock.checkedOutProjects.size(), task.mock.checkedOutRevision.size());
        for (int i = 0; i < task.mock.checkedOutRevision.size(); ++i)
        {
            assertEquals("Entry " + i, SVNUtilities.HEAD_REVISION, task.mock.checkedOutRevision.get(i));
        }
    }

    @Test(dataProvider = "reposUrl")
    public void testTwoClasspathFiles(String reposUrl, String projPath, boolean isTrunk)
    {
        final String classPathFileContent1 =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                        + "<classpath>\n"
                        + "   <classpathentry kind=\"src\" path=\"sourceTest/java\"/>\n"
                        + "   <classpathentry kind=\"src\" path=\"source/java\"/>\n"
                        + "   <classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER\"/>\n"
                        + "   <classpathentry kind=\"lib\" path=\"/libraries/activation/activation.jar\"/>\n"
                        + "   <classpathentry kind=\"lib\" path=\"/libraries/commons-cli/commons-cli.jar\" sourcepath=\"/libraries/commons-cli/src.zip\"/>\n"
                        + "   <classpathentry kind=\"lib\" path=\"/libraries/log4j/log4j.jar\" sourcepath=\"/libraries/log4j/src.zip\"/>\n"
                        + "   <classpathentry kind=\"lib\" path=\"/libraries/mail/mail.jar\"/>\n"
                        + "   <classpathentry kind=\"lib\" path=\"/libraries/testng/testng-jdk15.jar\" sourcepath=\"/libraries/testng/src.zip\"/>\n"
                        + "   <classpathentry combineaccessrules=\"false\" kind=\"src\" path=\"/common\"/>\n"
                        + "   <classpathentry kind=\"output\" path=\"targets/classes\"/>\n" + "</classpath>\n";
        final String classPathFileContent2 =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                        + "<classpath>\n"
                        + "   <classpathentry kind=\"src\" path=\"sourceTest/java\"/>\n"
                        + "   <classpathentry kind=\"src\" path=\"source/java\"/>\n"
                        + "   <classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER\"/>\n"
                        + "   <classpathentry kind=\"lib\" path=\"/libraries/activation/activation.jar\"/>\n"
                        + "   <classpathentry kind=\"lib\" path=\"/libraries/commons-cli/commons-cli.jar\" sourcepath=\"/libraries/commons-cli/src.zip\"/>\n"
                        + "   <classpathentry kind=\"lib\" path=\"/libraries/log4j/log4j.jar\" sourcepath=\"/libraries/log4j/src.zip\"/>\n"
                        + "   <classpathentry kind=\"lib\" path=\"/libraries/mail/mail.jar\"/>\n"
                        + "   <classpathentry kind=\"lib\" path=\"/libraries/testng/testng-jdk15.jar\" sourcepath=\"/libraries/testng/src.zip\"/>\n"
                        + "   <classpathentry combineaccessrules=\"false\" kind=\"src\" path=\"/common\"/>\n"
                        + "   <classpathentry kind=\"output\" path=\"targets/classes\"/>\n"
                        + "   <classpathentry combineaccessrules=\"false\" kind=\"src\" path=\"/common2\"/>\n"
                        + "</classpath>\n";
        final File someProj = new File(workingDirectory, projPath);
        someProj.delete();
        someProj.deleteOnExit();
        assert someProj.mkdir();
        final File someDependantProj = new File(workingDirectory, "common");
        someDependantProj.delete();
        someDependantProj.deleteOnExit();
        assert someDependantProj.mkdir();

        final SVNRecursiveCheckoutTaskCheckoutMock task = new SVNRecursiveCheckoutTaskCheckoutMock();
        final File classPathFile1 = new File(someProj, ".classpath");
        classPathFile1.deleteOnExit();
        CollectionIO.writeIterable(classPathFile1, Collections.singleton(classPathFileContent1));
        final File classPathFile2 = new File(someDependantProj, ".classpath");
        classPathFile2.deleteOnExit();
        CollectionIO.writeIterable(classPathFile2, Collections.singleton(classPathFileContent2));
        task.setDir(someProj.getParentFile().getAbsolutePath());
        task.setRepositoryRoot(reposUrl);
        task.setName(projPath);
        if (false == isTrunk)
        {
            task.setFeatureBranch("someBranch");
        }
        task.setProject(new Project()); // Required for log not to throw a NPE.
        final String revision = "42";
        task.setRevision(revision);
        task.execute();

        final List<String> expectedPaths =
                isTrunk ? Arrays.asList(projPath + "/trunk", SVNUtilities.BUILD_RESOURCES_PROJECT
                        + "/trunk", "libraries/trunk", "common/trunk", "common2/trunk") : Arrays.asList(projPath,
                                SVNUtilities.BUILD_RESOURCES_PROJECT, "libraries", "common", "common2");
        CollectionIO.writeIterable(System.out, task.mock.checkedOutPaths);
        assertEquals(expectedPaths, task.mock.checkedOutPaths);
        final List<String> expectedProjects =
            Arrays.asList(projPath, SVNUtilities.BUILD_RESOURCES_PROJECT, "libraries", "common", "common2");
        assertEquals(expectedProjects, task.mock.checkedOutProjects);
        assertEquals(task.mock.checkedOutProjects.size(), task.mock.checkedOutRevision.size());
        for (int i = 0; i < task.mock.checkedOutRevision.size(); ++i)
        {
            assertEquals("Entry " + i, revision, task.mock.checkedOutRevision.get(i));
        }
    }
    
    @Test(expectedExceptions={BuildException.class})
    public void testSetInvalidRevision()
    {
        SVNRecursiveCheckoutTask task = new SVNRecursiveCheckoutTask();
        task.setName("some_project");
        task.setRevision("some invalid revision");
    }

}
