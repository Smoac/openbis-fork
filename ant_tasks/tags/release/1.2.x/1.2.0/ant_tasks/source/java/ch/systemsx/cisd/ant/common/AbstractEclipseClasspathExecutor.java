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

package ch.systemsx.cisd.ant.common;

import java.io.File;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * @author felmer
 */
public abstract class AbstractEclipseClasspathExecutor
{
    public void executeFor(Task task)
    {
        File baseDir = task.getProject().getBaseDir();
        execute(baseDir);
    }

    public void execute(File baseDir)
    {
        File eclipseClasspathFile = new File(baseDir, EclipseClasspathReader.CLASSPATH_FILE);
        if (eclipseClasspathFile.exists() == false)
        {
            handleAbsentsOfClasspathFile(eclipseClasspathFile);
        } else
        {
            try
            {
                IEclipseClasspathLocation loc = new FileBaseEclipseClasspathLocation(eclipseClasspathFile);
                executeEntries(EclipseClasspathReader.readClasspathEntries(loc));
            } catch (BuildException ex)
            {
                throw ex;
            } catch (Exception ex)
            {
                throw new BuildException("Parsing error: " + ex, ex);
            }
        }
    }

    protected abstract void handleAbsentsOfClasspathFile(File eclipseClasspathFile);

    protected abstract void executeEntries(List<EclipseClasspathEntry> entries);
}
