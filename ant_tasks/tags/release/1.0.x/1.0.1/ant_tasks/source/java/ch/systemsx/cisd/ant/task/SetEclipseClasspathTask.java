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
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Property;

import ch.systemsx.cisd.ant.common.DummyProjectHandler;
import ch.systemsx.cisd.ant.common.EclipseClasspathEntry;
import ch.systemsx.cisd.ant.common.EclipseClasspathReader;
import ch.systemsx.cisd.ant.common.FileBaseEclipseClasspathLocation;
import ch.systemsx.cisd.ant.common.IEclipseClasspathLocation;
import ch.systemsx.cisd.ant.common.IProjectHandler;
import ch.systemsx.cisd.ant.common.RecursiveProjectTraverser;

/**
 * @author felmer
 */
public class SetEclipseClasspathTask extends Property
{
    private String classesFolder;

    public void setClasses(String classesFolder)
    {
        this.classesFolder = classesFolder;
    }

    @Override
    public void execute() throws BuildException
    {
        String propertyName = getName();
        if (propertyName == null)
        {
            throw new BuildException("Attribute 'name' not specified.");
        }
        if (classesFolder == null)
        {
            throw new BuildException("Attribute 'classes' not specified.");
        }
        LinkedHashSet<File> entries = new LinkedHashSet<File>();
        RecursiveProjectTraverser.traverse(new ProjectHandler(entries, getProject().getBaseDir()));
        StringBuilder builder = new StringBuilder();
        for (File entry : entries)
        {
            builder.append(entry.getAbsolutePath()).append(File.pathSeparatorChar);
        }
        addProperty(propertyName, builder.toString());
    }
    
    private final class ProjectHandler extends DummyProjectHandler
    {
        private final Set<File> files;
        private final File projectBaseDir;

        ProjectHandler(Set<File> files, File projectBaseDir)
        {
            this.files = files;
            this.projectBaseDir = projectBaseDir;
        }
        
        @Override
        public void handleEntry(EclipseClasspathEntry entry)
        {
            String relPath = entry.getPath().substring(1);
            File classpathElement = new File(projectBaseDir.getParentFile(), relPath);
            if (entry.getKind().equals(EclipseClasspathEntry.LIB_KIND))
            {
                files.add(classpathElement);
            }        
        }

        @Override
        public IProjectHandler createHandler(EclipseClasspathEntry entry)
        {
            if (entry.getKind().equals(EclipseClasspathEntry.SRC_KIND))
            {
                String relPath = entry.getPath().substring(1);
                File classpathElement = new File(projectBaseDir.getParentFile(), relPath);
                return new ProjectHandler(files, classpathElement);
            }
            return DummyProjectHandler.INSTANCE;
        }

        @Override
        public IEclipseClasspathLocation createLocation()
        {
            File eclipseClasspathFile = new File(projectBaseDir, EclipseClasspathReader.CLASSPATH_FILE);
            if (eclipseClasspathFile.exists())
            {
                return new FileBaseEclipseClasspathLocation(eclipseClasspathFile);
            }
            log("Eclipse class path file does not exist: " + eclipseClasspathFile);
            return null;
        }

        @Override
        public void handleOnEntering()
        {
            files.add(new File(projectBaseDir, classesFolder));
        }
    }
}
