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

import static ch.systemsx.cisd.ant.common.EclipseClasspathReader.CLASSPATH_FILE;
import static org.testng.AssertJUnit.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.ant.task.subversion.SVNInfoRecord.Updater;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * @author felmer
 */
@Friend(toClasses = GatherRevisionAndVersionTask.class)
public class GatherRevisionAndVersionTaskTest
{
    private static final File BASE_DIR = new File(".");

    private static final String BASE_REPOSITORY_URL = "http://host/repos";

    private MockSVN svn;

    private SVNProject project1;

    private SVNProject libraries;

    private SVNProject common;

    private SVNProject buildResources;

    //
    // Mocks and helper classes
    //

    private static final class MockPathProvider implements ISVNProjectPathProvider
    {
        private final SVNProject project;

        public MockPathProvider(final SVNProject project)
        {
            this.project = project;
        }

        public boolean isRepositoryPath()
        {
            throw new UnsupportedOperationException();
        }

        public String getRevision()
        {
            throw new UnsupportedOperationException();
        }

        public String getProjectName()
        {
            return project.getName();
        }

        public String getPath(final String subProjectName, final String entityPath)
                throws UserFailureException
        {
            throw new UnsupportedOperationException();
        }

        public String getPath(final String subProjectName) throws UserFailureException
        {
            return subProjectName;
        }

        public String getPath()
        {
            return getProjectName();
        }
    }

    private static final class MockSVN implements ISVNActions
    {
        private final Map<String, List<SVNItemStatus>> statusMap =
                new HashMap<String, List<SVNItemStatus>>();

        private final Map<String, SVNInfoRecord> infoMap = new HashMap<String, SVNInfoRecord>();

        private final Map<String, List<String>> listMap = new HashMap<String, List<String>>();

        private final Map<String, String> contentMap = new HashMap<String, String>();

        SVNInfoRecord register(final String path)
        {
            System.out.println("REGISTER - " + path);
            final SVNInfoRecord infoRecord = new SVNInfoRecord();
            infoMap.put(path, infoRecord);
            return infoRecord;
        }

        void addFiles(final String folder, final String... files)
        {
            System.out.println("ADD - " + folder + ": " + Arrays.asList(files));
            List<String> list = listMap.get(folder);
            if (list == null)
            {
                list = new ArrayList<String>();
                listMap.put(folder, list);
            }
            list.addAll(Arrays.asList(files));
        }

        void registerContent(final String file, final String content)
        {
            contentMap.put(file, content);
        }

        public List<SVNItemStatus> status(final String path)
        {
            System.out.println("svn status " + path);
            return statusMap.get(path);
        }

        public SVNInfoRecord info(final String path)
        {
            System.out.println("INFO - " + path);
            final SVNInfoRecord infoRecord = infoMap.get(path);
            assert infoRecord != null : "Path '" + path + "' does not exist.";
            System.out.println(" -> " + infoRecord.getRepositoryUrl());

            return infoRecord;
        }

        public void copy(final String sourcePath, final String sourceRevision,
                final String destinationPath, final String logMessage) throws SVNException
        {
            throw new AssertionError("Unexpected call copy");
        }

        public void mkdir(final String path, final String logMessage) throws SVNException
        {
            throw new AssertionError("Unexpected call mkdir");
        }

        public List<String> list(final String path) throws SVNException
        {
            System.out.println("LIST - " + path);
            final List<String> result = listMap.get(path);
            assert result != null : "Path '" + path + "' does not exist.";
            System.out.println(" -> " + result);
            return result;
        }

        public String cat(final String path) throws SVNException
        {
            System.out.println("CAT - " + path);
            final String result = contentMap.get(path);
            return result == null ? "" : result;
        }

        public boolean isMuccAvailable()
        {
            return false;
        }

        public void mucc(final String logMessage, final String... args) throws SVNException
        {
            throw new AssertionError();
        }
    }

    private GatherRevisionAndVersionTask createTask(final MockSVN mockSvn,
            final SVNProject svnProject, final String reposURL)
    {
        final GatherRevisionAndVersionTask task = new GatherRevisionAndVersionTask()
            {
                @Override
                ISVNActions createSVNActions()
                {
                    return mockSvn;
                }

                @Override
                ISVNProjectPathProvider createPathProvider(File baseDir)
                {
                    return new MockPathProvider(svnProject);
                }

                @Override
                String getParentDir()
                {
                    return "..";
                }

            };
        final Project project = new Project();
        project.setBaseDir(BASE_DIR);
        task.setProject(project);
        return task;
    }

    private static class SVNProject
    {
        private final String name;

        private final List<String> files = new ArrayList<String>();

        private String classpathContent;

        SVNProject(final String name)
        {
            this.name = name;
        }

        String getName()
        {
            return name;
        }

        SVNProject addFiles(final String... newFiles)
        {
            this.files.addAll(Arrays.asList(newFiles));
            return this;
        }

        SVNProject addClasspathFile(final String content)
        {
            classpathContent = content;
            files.add(CLASSPATH_FILE);
            return this;
        }

        void registerAsTrunk(final MockSVN svn, final int revision, final int lastChangedRevision)
        {
            final String projectBaseDir = name + "/trunk";
            registerProject(svn, projectBaseDir, revision, lastChangedRevision);
        }

        void registerVersion(final MockSVN svn, final String version, final int revision,
                final int lastChangedRevision)
        {
            final String projectBaseDir = version + "/" + name;
            registerProject(svn, projectBaseDir, revision, lastChangedRevision);
        }

        private void registerProject(final MockSVN svn, final String projectBaseDir,
                final int revision, final int lastChangedRevision)
        {
            final SVNInfoRecord info = svn.register(".." + File.separator + name);
            final Updater updater = info.getUpdater();
            updater.setRepositoryUrl(BASE_REPOSITORY_URL + "/" + projectBaseDir);
            updater.setRevision(revision);
            updater.setLastChangedRevision(lastChangedRevision);
            svn.addFiles(name, files.toArray(new String[files.size()]));
            if (classpathContent != null)
            {
                svn.registerContent(name + '/' + CLASSPATH_FILE, classpathContent);
            }
        }

    }

    @BeforeMethod
    public void setUp()
    {
        svn = new MockSVN();
        project1 = new SVNProject("project1").addFiles("helloWorld.txt");
        project1
                .addClasspathFile("<classpath>\n"
                        + "<classpathentry kind=\"src\" path=\"source/java\"/>\n"
                        + "<classpathentry kind=\"lib\" path=\"/libraries/activation/activation.jar\"/>\n"
                        + "<classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER\"/>\n"
                        + "<classpathentry kind=\"src\" path=\"/common\"/>\n" + "</classpath>\n");
        libraries = new SVNProject("libraries").addFiles("lib1.jar");
        common = new SVNProject("common").addFiles("build.xml");
        common
                .addClasspathFile("<classpath>\n"
                        + "<classpathentry kind=\"src\" path=\"source/java\"/>\n"
                        + "<classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER\"/>\n"
                        + "</classpath>\n");
        buildResources = new SVNProject("build_resources");

    }

    //
    // Tests
    //

    @Test(expectedExceptions =
        { BuildException.class })
    public void testVersionOrRevisionIsSet()
    {
        System.out.println("testVersionOrRevisionIsSet()");
        final GatherRevisionAndVersionTask task =
                createTask(new MockSVN(), null, BASE_REPOSITORY_URL);
        task.execute();
    }

    @Test
    public void testRevisionAndTrunkVersionAndDoNotFailOnInconsistencyAndDirty() throws Exception
    {
        System.out.println("testRevisionAndTrunkVersionAndDoNotFailOnInconsistencyAndDirty()");
        project1.registerAsTrunk(svn, 10, 3);
        libraries.registerAsTrunk(svn, 10, 6);
        common.registerAsTrunk(svn, 11, 7);
        buildResources.registerAsTrunk(svn, 10, 5);
        svn.register(".").getUpdater().setRepositoryUrl(
                BASE_REPOSITORY_URL + "/" + project1.getName() + "/trunk");

        final GatherRevisionAndVersionTask task = createTask(svn, project1, BASE_REPOSITORY_URL);
        task.setRevision("myRevision");
        task.setVersion("myVersion");

        task.execute();

        assertEquals("7", task.getProject().getProperty("myRevision"));
        assertEquals(GatherRevisionAndVersionTask.TRUNK_VERSION, task.getProject().getProperty(
                "myVersion"));
    }

    @Test
    public void testRevisionAndVersionAndDoNotFailOnInconsistencyAndDirty()
    {
        System.out.println("testRevisionAndVersionAndDoNotFailOnInconsistencyAndDirty()");
        final String version = "1.2.3";
        project1.registerVersion(svn, version, 10, 3);
        libraries.registerVersion(svn, version, 10, 6);
        common.registerVersion(svn, version, 11, 7);
        buildResources.registerVersion(svn, version, 10, 5);
        svn.register(".").getUpdater().setRepositoryUrl(
                BASE_REPOSITORY_URL + "/" + version + "/" + project1.getName());

        final GatherRevisionAndVersionTask task =
                createTask(svn, project1, BASE_REPOSITORY_URL + "/" + version);
        task.setRevision("myRevision");
        task.setVersion("myVersion");

        task.execute();

        assertEquals("7", task.getProject().getProperty("myRevision"));
        assertEquals(version, task.getProject().getProperty("myVersion"));
    }

}
