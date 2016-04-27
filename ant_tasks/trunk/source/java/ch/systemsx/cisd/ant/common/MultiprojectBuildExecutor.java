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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <ul>
 * <li>analyzes the dependency graph for a given starting (root) project</li>
 * <li>sorts the graph topologically (i.e. in a sequence where every project only depends on
 * projects preceding it)</li>
 * <li>Iterates over the sorted list and calls the method {@link #executeProjectBuild(File)}</li>
 * </ul>
 * <p>
 * This guarantees single build execution for every project present in the dependency tree.
 * 
 * @author Kaloyan Enimanev
 */
public abstract class MultiprojectBuildExecutor
{
    private final File rootProjectBaseDir;

    private final Map<String, List<String>> projectDependencyGraph =
            new HashMap<String, List<String>>();

    public MultiprojectBuildExecutor(File rootProjectBaseDir)
    {
        this.rootProjectBaseDir = rootProjectBaseDir;
    }

    /**
     * executes the build for a single project.
     * 
     * @param projectBaseDir the base directory for the project
     */
    protected abstract void executeProjectBuild(File projectBaseDir);

    public void execute()
    {
        depthFirstSearch(rootProjectBaseDir);
        List<String> sortedProjects = TopologicalSort.sort(projectDependencyGraph);
        for (String projectBaseDir : sortedProjects)
        {
            File baseDir = new File(projectBaseDir);
            executeProjectBuild(baseDir);
        }
    }


    protected File getProjectBaseDir(EclipseClasspathEntry entry)
    {
        String path = entry.getPath();
        File subprojectBaseDir = new File(rootProjectBaseDir.getParentFile(), path.substring(1));
        return subprojectBaseDir;
    }

    private void depthFirstSearch(File projectBaseDir)
    {
        String dirName = projectBaseDir.getAbsolutePath();
        if (projectDependencyGraph.containsKey(dirName))
        {
            // project already visited
            return;
        }

        List<String> subprojects = new ArrayList<String>();
        projectDependencyGraph.put(dirName, subprojects);

        List<EclipseClasspathEntry> classpathEntries =
                EclipseProjectUtils.readClasspathEntriesForProject(projectBaseDir);
        for (EclipseClasspathEntry entry : classpathEntries)
        {
            File subProjectBaseDir = getProjectBaseDir(entry);
            File eclipseClassPathFile =
                    EclipseProjectUtils.getEclipseClassPathFile(subProjectBaseDir);
            if (eclipseClassPathFile.exists())
            {
                depthFirstSearch(subProjectBaseDir);
                subprojects.add(subProjectBaseDir.getAbsolutePath());
            }
        }
    }

}
