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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.utilities.FileUtilities;

/**
 * Test cases for the {@link SVNDependentProjectsCollector}.
 * 
 * @author Bernd Rinn
 */
public class SVNDependentProjectCollectorTest
{

    private static final File unitTestRootDirectory =
            new File("targets" + File.separator + "unit-test-wd");

    private static final File workingDirectory =
            new File(unitTestRootDirectory, "SVNDependentProjectCollector");

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

    //
    // Mocks
    //

    private static final class MockSVNRepositoryActions implements ISVNActions
    {
        final Map<String, List<String>> listMap;

        final Map<String, String> pathMap;

        public MockSVNRepositoryActions(final Map<String, List<String>> listMap,
                final Map<String, String> pathMap)
        {
            super();
            this.listMap = listMap;
            this.pathMap = pathMap;
        }

        public String cat(String path) throws SVNException
        {
            System.out.println("CAT - " + path);
            final String result = pathMap.get(path);
            return result == null ? "" : result;
        }

        public List<String> list(String path) throws SVNException
        {
            System.out.println("LIST - " + path);
            List<String> result = listMap.get(path);
            assert result != null : "Path '" + path + "' does not exist.";
            return result;
        }

        public void mkdir(String path, String logMessage) throws SVNException
        {
            throw new AssertionError("Unexpected call mkdir()");
        }

        public void copy(String sourcePath, String sourceRevision, String destinationPath,
                String logMessage) throws SVNException
        {
            throw new AssertionError("Unexpected call copy()");
        }

        public SVNInfoRecord info(String pathOrUrl)
        {
            throw new AssertionError("Unexpected call info()");
        }

        public List<SVNItemStatus> status(String path)
        {
            throw new AssertionError("Unexpected call status()");
        }

        public boolean isMuccAvailable()
        {
            return false;
        }

        public void mucc(String logMessage, String... args) throws SVNException
        {
            throw new AssertionError();
        }
    }

    private SVNDependentProjectsCollector createCollector(
            final ISVNProjectPathProvider pathProvider, final Map<String, List<String>> listMap,
            final Map<String, String> catMap)
    {
        final SVNDependentProjectsCollector collector =
                new SVNDependentProjectsCollector(pathProvider, new MockSVNRepositoryActions(
                        listMap, catMap));
        return collector;
    }

    //
    // Tests
    //

    @Test
    public void testCollectTrunk()
    {
        final SVNRepositoryProjectContext context = new SVNRepositoryProjectContext();
        context.setRepositoryRoot("http://host/repos");
        context.setGroup("group");
        context.setProjectName("proj1");
        final ISVNProjectPathProvider pathProvider = context.getPathProvider();
        final Map<String, List<String>> listMap = new HashMap<String, List<String>>();
        listMap.put("http://host/repos/group/libraries/trunk", Arrays.asList("something"));
        listMap.put("http://host/repos/group/proj1/trunk", Arrays.asList(".classpath"));
        listMap.put("http://host/repos/group/proj2/trunk", Arrays
                .asList("something/", ".classpath"));
        listMap.put("http://host/repos/group/proj3/trunk", Arrays.asList("something/",
                "nothing there"));
        final Map<String, String> catMap = new HashMap<String, String>();
        catMap
                .put(
                        "http://host/repos/group/proj1/trunk/.classpath",
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                                + "<classpath>\n"
                                + "        <classpathentry kind=\"src\" path=\"sourceTest/java\"/>\n"
                                + "        <classpathentry kind=\"src\" path=\"source/java\"/>\n"
                                + "   <classpathentry kind=\"lib\" path=\"/libraries/activation/activation.jar\"/>\n"
                                + "   <classpathentry kind=\"lib\" path=\"/libraries/mail/mail.jar\"/>\n"
                                + "        <classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER\"/>\n"
                                + "        <classpathentry combineaccessrules=\"false\" kind=\"src\" path=\"/proj2\"/>\n"
                                + "        <classpathentry kind=\"output\" path=\"targets/classes\"/>\n"
                                + "</classpath>\n" + "");
        catMap
                .put(
                        "http://host/repos/group/proj2/trunk/.classpath",
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                                + "<classpath>\n"
                                + "        <classpathentry kind=\"src\" path=\"sourceTest/java\"/>\n"
                                + "        <classpathentry kind=\"src\" path=\"source/java\"/>\n"
                                + "        <classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER\"/>\n"
                                + "        <classpathentry combineaccessrules=\"false\" kind=\"src\" path=\"/proj3\"/>\n"
                                + "        <classpathentry kind=\"output\" path=\"targets/classes\"/>\n"
                                + "</classpath>\n" + "");
        SVNDependentProjectsCollector collector = createCollector(pathProvider, listMap, catMap);
        final Set<String> projectsExpected =
                new HashSet<String>(Arrays.asList("proj1", "proj2", "proj3", "libraries",
                        "build_resources"));
        final Set<String> projectsFound = collector.collectDependentProjectsFromClasspath();
        assertEquals(projectsExpected, projectsFound);
    }

    @Test
    public void testCollectBranch()
    {
        final SVNRepositoryProjectContext context = new SVNRepositoryProjectContext();
        context.setRepositoryRoot("http://host/repos");
        context.setGroup("group");
        context.setFeatureBranch("branch");
        context.setProjectName("proj1");
        final ISVNProjectPathProvider pathProvider = context.getPathProvider();
        final Map<String, List<String>> listMap = new HashMap<String, List<String>>();
        listMap.put("http://host/repos/group/proj1/branches/feature/branch/libraries", Arrays
                .asList("something"));
        listMap.put("http://host/repos/group/proj1/branches/feature/branch/proj1", Arrays
                .asList(".classpath"));
        listMap.put("http://host/repos/group/proj1/branches/feature/branch/proj2", Arrays.asList(
                "something/", ".classpath"));
        listMap.put("http://host/repos/group/proj1/branches/feature/branch/proj3", Arrays.asList(
                "something/", "nothing there"));
        final Map<String, String> catMap = new HashMap<String, String>();
        catMap
                .put(
                        "http://host/repos/group/proj1/branches/feature/branch/proj1/.classpath",
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                                + "<classpath>\n"
                                + "        <classpathentry kind=\"src\" path=\"sourceTest/java\"/>\n"
                                + "        <classpathentry kind=\"src\" path=\"source/java\"/>\n"
                                + "   <classpathentry kind=\"lib\" path=\"/libraries/activation/activation.jar\"/>\n"
                                + "   <classpathentry kind=\"lib\" path=\"/libraries/mail/mail.jar\"/>\n"
                                + "        <classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER\"/>\n"
                                + "        <classpathentry combineaccessrules=\"false\" kind=\"src\" path=\"/proj2\"/>\n"
                                + "        <classpathentry kind=\"output\" path=\"targets/classes\"/>\n"
                                + "</classpath>\n" + "");
        catMap
                .put(
                        "http://host/repos/group/proj1/branches/feature/branch/proj2/.classpath",
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                                + "<classpath>\n"
                                + "        <classpathentry kind=\"src\" path=\"sourceTest/java\"/>\n"
                                + "        <classpathentry kind=\"src\" path=\"source/java\"/>\n"
                                + "        <classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER\"/>\n"
                                + "        <classpathentry combineaccessrules=\"false\" kind=\"src\" path=\"/proj3\"/>\n"
                                + "        <classpathentry kind=\"output\" path=\"targets/classes\"/>\n"
                                + "</classpath>\n" + "");
        SVNDependentProjectsCollector collector = createCollector(pathProvider, listMap, catMap);
        final Set<String> projectsExpected =
                new HashSet<String>(Arrays.asList("proj1", "proj2", "proj3", "build_resources",
                        "libraries"));
        final Set<String> projectsFound = collector.collectDependentProjectsFromClasspath();
        assertEquals(projectsExpected, projectsFound);
    }

}
