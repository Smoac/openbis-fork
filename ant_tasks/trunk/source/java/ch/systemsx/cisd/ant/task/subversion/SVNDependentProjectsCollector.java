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

import java.util.HashSet;
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
 * A class which collects all dependent projects of a project path in subversion (including the
 * project itself), following the dependencies specified by Eclipse' <code>.classpath</code> files.
 * <p>
 * Works on both a subversion repository or working copy.
 * 
 * @author Bernd Rinn
 */
class SVNDependentProjectsCollector
{
    private final ISVNProjectPathProvider pathProvider;

    private final ISVNActions actions;

    private final boolean librariesOneByOne;

    /**
     * Creates a new instance for the specified project URL and factory for actions on the
     * Subversion repository.
     */
    public SVNDependentProjectsCollector(ISVNProjectPathProvider pathProvider, ISVNActions actions,
            boolean librariesOneByOne)
    {
        assert pathProvider != null;
        assert actions != null;

        this.pathProvider = pathProvider;
        this.actions = actions;
        this.librariesOneByOne = librariesOneByOne;
    }

    /**
     * @return The set of projects that the project in <var>projectUrl</var> depends on (including
     *         itself).
     * @throws SVNException If there is a problem performing a <code>svn cat</code> command.
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

        private final Set<String> locationsAlreadyVisited;

        ProjectHandler(Set<String> projects, String projectPath)
        {
            this(projects, projectPath, new HashSet<String>());
        }

        ProjectHandler(Set<String> projects, String projectPath, Set<String> locationsAlreadyVisited)
        {
            this.projects = projects;
            this.projectPath = projectPath;
            this.locationsAlreadyVisited = locationsAlreadyVisited;
        }

        @Override
        public IEclipseClasspathLocation createLocation()
        {
            final String displayableLocation =
                    projectPath + "/" + EclipseClasspathReader.CLASSPATH_FILE;
            if (locationsAlreadyVisited.contains(displayableLocation))
            {
                return null;
            }
            locationsAlreadyVisited.add(displayableLocation);
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
                projects.add(librariesOneByOne ? SVNUtilities.getProjectName(entry.getPath())
                        : SVNUtilities.getTopLevelDirectory(entry.getPath()));
            }
        }

        @Override
        public IProjectHandler createHandler(EclipseClasspathEntry entry)
        {
            final String projectName =
                    librariesOneByOne ? SVNUtilities.getProjectName(entry.getPath()) : SVNUtilities
                            .getTopLevelDirectory(entry.getPath());
            return new ProjectHandler(projects, pathProvider.getPath(projectName),
                    locationsAlreadyVisited);
        }

    }
}
