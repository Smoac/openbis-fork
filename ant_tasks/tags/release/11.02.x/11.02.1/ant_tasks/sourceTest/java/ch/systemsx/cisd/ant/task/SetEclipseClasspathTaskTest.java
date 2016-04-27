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

package ch.systemsx.cisd.ant.task;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.ant.common.EclipseClasspathEntry;
import ch.systemsx.cisd.ant.common.EclipseClasspathReader;

/**
 * @author felmer
 */
public class SetEclipseClasspathTaskTest extends AssertJUnit
{
    private static final String PROPERTY_NAME = "ecp";

    private static final String TARGET_CLASSES = "target/classes";

    private static final File TEMP_WORKSPACE = new File("temporaryTestEclipseWorkspace");

    private static final File PROJECT_A = new File(TEMP_WORKSPACE, "projectA");

    private static final File PROJECT_B = new File(TEMP_WORKSPACE, "projectB");

    private static final File PROJECT_C = new File(TEMP_WORKSPACE, "projectC");

    private static final File PROJECT_D = new File(TEMP_WORKSPACE, "projectD");

    private static final class EclipseClasspath
    {
        private final List<EclipseClasspathEntry> entries = new ArrayList<EclipseClasspathEntry>();

        void addLibEntry(String path)
        {
            entries.add(new EclipseClasspathEntry(EclipseClasspathEntry.LIB_KIND, path));
        }

        void addSourceEntry(String path)
        {
            entries.add(new EclipseClasspathEntry(EclipseClasspathEntry.SRC_KIND, path));
        }

        void saveTo(File file) throws IOException
        {
            FileWriter writer = null;
            try
            {
                writer = new FileWriter(new File(file, EclipseClasspathReader.CLASSPATH_FILE));
                PrintWriter printWriter = new PrintWriter(writer);
                printWriter.println("<classpath>");
                for (EclipseClasspathEntry entry : entries)
                {
                    printWriter.printf("  <classpathentry kind=\"%s\" path=\"%s\"/>\n", entry
                            .getKind(), entry.getPath());
                }
                printWriter.println("</classpath>");
            } catch (IOException ex)
            {
                throw ex;
            } finally
            {
                if (writer != null)
                {
                    writer.close();
                }
            }
        }
    }

    @BeforeClass
    public void setUpTestFiles() throws IOException
    {
        TEMP_WORKSPACE.mkdir();
        PROJECT_A.mkdir();
        EclipseClasspath eclipseClasspath = new EclipseClasspath();
        eclipseClasspath.addLibEntry("/a.jar");
        eclipseClasspath.saveTo(PROJECT_A);
        PROJECT_B.mkdir();
        eclipseClasspath = new EclipseClasspath();
        eclipseClasspath.addSourceEntry("/" + PROJECT_A.getName());
        eclipseClasspath.addLibEntry("/b.jar");
        eclipseClasspath.saveTo(PROJECT_B);
        PROJECT_C.mkdir();
        eclipseClasspath = new EclipseClasspath();
        eclipseClasspath.addSourceEntry("/" + PROJECT_A.getName());
        eclipseClasspath.addLibEntry("/c.jar");
        eclipseClasspath.saveTo(PROJECT_C);
        PROJECT_D.mkdir();
        eclipseClasspath = new EclipseClasspath();
        eclipseClasspath.addSourceEntry("blabla");
        eclipseClasspath.addSourceEntry("/" + PROJECT_B.getName());
        eclipseClasspath.addSourceEntry("/" + PROJECT_C.getName());
        eclipseClasspath.addLibEntry("/d.jar");
        eclipseClasspath.saveTo(PROJECT_D);
    }

    @AfterClass
    public void deleteTestFiles()
    {
        delete(TEMP_WORKSPACE);
    }

    private void delete(File file)
    {
        if (file.isDirectory())
        {
            File[] files = file.listFiles();
            for (File child : files)
            {
                delete(child);
            }
        }
        file.delete();
    }

    @Test
    public void testMissingName()
    {
        SetEclipseClasspathTask task = new SetEclipseClasspathTask();
        try
        {
            task.execute();
            fail("BuildException expected because of missing attribute 'name'.");
        } catch (BuildException ex)
        {
            assertTrue(ex.getMessage().indexOf("name") >= 0);
        }
    }

    @Test
    public void testMissingClassesFolder()
    {
        SetEclipseClasspathTask task = new SetEclipseClasspathTask();
        task.setName(PROPERTY_NAME);
        try
        {
            task.execute();
            fail("BuildException expected because of missing attribute 'classes'.");
        } catch (BuildException ex)
        {
            assertTrue(ex.getMessage().indexOf("classes") >= 0);
        }
    }

    @Test
    public void testProjectWithoutDependencies()
    {
        SetEclipseClasspathTask task = new SetEclipseClasspathTask();
        task.setName(PROPERTY_NAME);
        task.setClasses(TARGET_CLASSES);
        Project project = new Project();
        project.setBaseDir(PROJECT_A);
        task.setProject(project);

        task.execute();

        StringBuilder builder = new StringBuilder();
        builder.append(new File(PROJECT_A, TARGET_CLASSES).getAbsoluteFile()).append(
                File.pathSeparatorChar);
        builder.append(new File(TEMP_WORKSPACE, "a.jar").getAbsoluteFile()).append(
                File.pathSeparatorChar);
        assertEquals(new String(builder), project.getProperty(PROPERTY_NAME));
    }

    @Test
    public void testProjectWithOneDependency()
    {
        SetEclipseClasspathTask task = new SetEclipseClasspathTask();
        task.setName(PROPERTY_NAME);
        task.setClasses(TARGET_CLASSES);
        Project project = new Project();
        project.setBaseDir(PROJECT_B);
        task.setProject(project);

        task.execute();

        StringBuilder builder = new StringBuilder();
        builder.append(new File(PROJECT_B, TARGET_CLASSES).getAbsoluteFile()).append(
                File.pathSeparatorChar);
        builder.append(new File(PROJECT_A, TARGET_CLASSES).getAbsoluteFile()).append(
                File.pathSeparatorChar);
        builder.append(new File(TEMP_WORKSPACE, "a.jar").getAbsoluteFile()).append(
                File.pathSeparatorChar);
        builder.append(new File(TEMP_WORKSPACE, "b.jar").getAbsoluteFile()).append(
                File.pathSeparatorChar);
        assertEquals(new String(builder), project.getProperty(PROPERTY_NAME));
    }

    @Test
    public void testProjectWithTwoBranchTwoLevelDependency()
    {
        SetEclipseClasspathTask task = new SetEclipseClasspathTask();
        task.setName(PROPERTY_NAME);
        task.setClasses(TARGET_CLASSES);
        Project project = new Project();
        project.setBaseDir(PROJECT_D);
        task.setProject(project);

        task.execute();

        StringBuilder builder = new StringBuilder();
        builder.append(new File(PROJECT_D, TARGET_CLASSES).getAbsoluteFile()).append(
                File.pathSeparatorChar);
        builder.append(new File(PROJECT_B, TARGET_CLASSES).getAbsoluteFile()).append(
                File.pathSeparatorChar);
        builder.append(new File(PROJECT_A, TARGET_CLASSES).getAbsoluteFile()).append(
                File.pathSeparatorChar);
        builder.append(new File(TEMP_WORKSPACE, "a.jar").getAbsoluteFile()).append(
                File.pathSeparatorChar);
        builder.append(new File(TEMP_WORKSPACE, "b.jar").getAbsoluteFile()).append(
                File.pathSeparatorChar);
        builder.append(new File(PROJECT_C, TARGET_CLASSES).getAbsoluteFile()).append(
                File.pathSeparatorChar);
        builder.append(new File(TEMP_WORKSPACE, "c.jar").getAbsoluteFile()).append(
                File.pathSeparatorChar);
        builder.append(new File(TEMP_WORKSPACE, "d.jar").getAbsoluteFile()).append(
                File.pathSeparatorChar);
        assertEquals(new String(builder), project.getProperty(PROPERTY_NAME));
    }
}
