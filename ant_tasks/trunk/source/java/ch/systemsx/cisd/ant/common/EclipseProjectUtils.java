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

package ch.systemsx.cisd.ant.common;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kaloyan Enimanev
 */
public class EclipseProjectUtils
{

    /**
     * @return the Eclipse ".classpath" file for a project.
     */
    public static File getEclipseClassPathFile(File baseDir)
    {
        return new File(baseDir, EclipseClasspathReader.CLASSPATH_FILE);
    }

    /**
     * @return the Eclipse classpath entries for a project.
     */
    public static List<EclipseClasspathEntry> readClasspathEntriesForProject(File projectBaseDir)
    {
        List<EclipseClasspathEntry> entries = new ArrayList<EclipseClasspathEntry>();
        File eclipseClasspathFile = getEclipseClassPathFile(projectBaseDir);
        IEclipseClasspathLocation loc = new FileBaseEclipseClasspathLocation(eclipseClasspathFile);
        entries.addAll(EclipseClasspathReader.readClasspathEntries(loc));

        final File shadowDependenciesFile =
                new File(projectBaseDir, EclipseClasspathReader.SHADOW_DEPENDENCIES_FILE);
        // The shadow dependencies file is optional.
        if (shadowDependenciesFile.exists())
        {
            final IEclipseClasspathLocation loc2 =
                    new FileBaseEclipseClasspathLocation(shadowDependenciesFile);
            entries.addAll(EclipseClasspathReader.readClasspathEntries(loc2));
        }
        return entries;
    }

}
