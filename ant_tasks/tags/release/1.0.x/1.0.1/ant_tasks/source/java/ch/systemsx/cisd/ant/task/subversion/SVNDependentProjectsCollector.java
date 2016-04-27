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

import java.util.LinkedHashSet;
import java.util.Set;

import ch.systemsx.cisd.ant.common.DummyProjectHandler;
import ch.systemsx.cisd.ant.common.EclipseClasspathEntry;
import ch.systemsx.cisd.ant.common.EclipseClasspathReader;
import ch.systemsx.cisd.ant.common.IEclipseClasspathLocation;
import ch.systemsx.cisd.ant.common.IProjectHandler;
import ch.systemsx.cisd.ant.common.RecursiveProjectTraverser;
import ch.systemsx.cisd.ant.common.TextBasedEclipseClasspathLocation;

/**
 * A class which collects all dependent projects of a project path in subversion (including the project itself),
 * following the dependencies specified by Eclipse' <code>.classpath</code> files.
 * <p>
 * Works on both a subversion repository or working copy.
 * 
 * @author Bernd Rinn
 */
class SVNDependentProjectsCollector
{
    private final ISVNProjectPathProvider pathProvider;

    private final ISVNActions actions;

    /**
     * Creates a new instance for the specified project URL and factory for actions on the Subversion reporistory.
     */
    public SVNDependentProjectsCollector(ISVNProjectPathProvider pathProvider, ISVNActions actions)
    {
        assert pathProvider != null;
        assert actions != null;

        this.pathProvider = pathProvider;
        this.actions = actions;
    }

    /**
     * @return The set of projects that the project in <var>projectUrl</var> depends on (including itself).
     * @throws SVNException If there is a problem performing an <code>svn cat</code> command.
     */
    public Set<String> collectDependentProjectsFromClasspath() throws SVNException
    {
        final Set<String> projects = new LinkedHashSet<String>();
        projects.add(pathProvider.getProjectName());
        RecursiveProjectTraverser.traverse(new ProjectHandler(projects, pathProvider.getPath()));
        projects.add(SVNUtilities.BUILD_RESOURCES_PROJECT); // Implicit dependency.
        return projects;
    }

    private final class ProjectHandler extends DummyProjectHandler
    {
        private final Set<String> projects;

        private final String projectPath;

        ProjectHandler(Set<String> projects, String projectPath)
        {
            this.projects = projects;
            this.projectPath = projectPath;
        }

        @Override
        public IEclipseClasspathLocation createLocation()
        {
            final String displayableLocation = projectPath + "/" + EclipseClasspathReader.CLASSPATH_FILE;
            try
            {
                final String eclipseClasspath = actions.cat(displayableLocation);
                // This is one of the possibilities what can happen if there is no .classpath file.
                if (eclipseClasspath.length() == 0) 
                {
                    return null;
                }
                return new TextBasedEclipseClasspathLocation(eclipseClasspath, displayableLocation);
            } catch (SVNException ex)
            {
                // This is the other possibility what can happen if there is no .classpath file.
                return null;
            }
        }

        @Override
        public void handleEntry(EclipseClasspathEntry entry)
        {
            if (entry.isSubprojectEntry())
            {
                projects.add(SVNUtilities.getTopLevelDirectory(entry.getPath()));
            }
        }

        @Override
        public IProjectHandler createHandler(EclipseClasspathEntry entry)
        {
            final String projectName = SVNUtilities.getTopLevelDirectory(entry.getPath());
            return new ProjectHandler(projects, pathProvider.getPath(projectName));
        }

    }
}
