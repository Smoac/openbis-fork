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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Property;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.ant.task.subversion.SVNItemStatus.StatusFlag;

/**
 * @author felmer
 */
public class GatherRevisionAndVersionTask extends Property
{
    static final String TRUNK_VERSION = "SNAPSHOT";

    private String versionProperty;

    private String revisionProperty;

    private String cleanProperty;

    private boolean failOnDirty;

    private boolean failOnInconsistency;

    public void setFailOnDirty(final boolean failOnDirty)
    {
        this.failOnDirty = failOnDirty;
    }

    public void setFailOnInconsistency(final boolean failOnInconsistentRevisions)
    {
        this.failOnInconsistency = failOnInconsistentRevisions;
    }

    public void setRevision(final String revisionProperty)
    {
        this.revisionProperty = revisionProperty;
    }

    public void setVersion(final String versionProperty)
    {
        this.versionProperty = versionProperty;
    }

    public void setClean(final String cleanProperty)
    {
        this.cleanProperty = cleanProperty;
    }

    @Override
    public void execute() throws BuildException
    {
        if (versionProperty == null && revisionProperty == null)
        {
            throw new BuildException("Neither version nor revision property is defined.");
        }
        final ISVNActions actions = createSVNActions();
        final Set<String> projectNames = collectProjects(actions);
        final HashSet<String> versions = new HashSet<String>();
        final StringBuilder projectRevisions = new StringBuilder();
        final StringBuilder dirtyProjects = new StringBuilder();
        int maxLastChangedRevision = 0;
        int minRevision = Integer.MAX_VALUE;
        int maxRevision = 0;
        final String parentDir = getParentDir();
        for (final String projectName : projectNames)
        {
            final String path = parentDir + File.separatorChar + projectName;
            final SVNInfoRecord info = actions.info(path);
            addVersion(versions, info);
            final int lastChangedRevision = info.getLastChangedRevision();
            final int revision = info.getRevision();
            projectRevisions.append(projectName).append('.').append(revision).append(' ');
            maxLastChangedRevision = Math.max(maxLastChangedRevision, lastChangedRevision);
            minRevision = Math.min(minRevision, revision);
            maxRevision = Math.max(maxRevision, revision);
            if (failOnDirty || cleanProperty != null)
            {
                final List<SVNItemStatus> stati = actions.status(path);
                for (final SVNItemStatus status : stati)
                {
                    if (status.getFlag() != StatusFlag.UNVERSIONED)
                    {
                        log(projectName + " is dirty: " + status.getFlag() + " " + status.getPath());
                        dirtyProjects.append(projectName).append(' ');
                        break;
                    }
                }
            }
        }
        checkFailure(versions, projectRevisions, dirtyProjects, minRevision, maxRevision,
                maxLastChangedRevision);
        setRevisionProperty(maxLastChangedRevision);
        setVersionProperty(versions);
        setCleanProperty(dirtyProjects);
    }

    /**
     * <em>Can be overwritten in unit tests.</em>
     */
    @Private
    ISVNActions createSVNActions()
    {
        return new SVNActions(new AntTaskSimpleLoggerAdapter(this));
    }

    /**
     * <em>Can be overwritten in unit tests.</em>
     */
    @Private
    ISVNProjectPathProvider createPathProvider(final File baseDir)
    {
        return new SVNWCProjectPathProvider(baseDir);
    }

    /**
     * <em>Can be overwritten in unit tests.</em>
     */
    @Private
    String getParentDir()
    {
        return getProject().getBaseDir().getParentFile().getAbsolutePath();
    }

    private Set<String> collectProjects(final ISVNActions actions)
    {
        final ISVNProjectPathProvider pathProvider = createPathProvider(getProject().getBaseDir());
        return new SVNDependentProjectsCollector(pathProvider, actions, true)
                .collectDependentProjectsFromClasspath();
    }

    private void setRevisionProperty(final int maxLastChangedRevision)
    {
        if (revisionProperty != null)
        {
            addProperty(revisionProperty, Integer.toString(maxLastChangedRevision));
        }
    }

    private void setVersionProperty(final HashSet<String> versions)
    {
        if (versionProperty != null)
        {
            if (versions.size() == 0)
            {
                throw new BuildException("Couldn't determine version.");
            }
            if (versions.size() > 1)
            {
                throw new BuildException("Versions are inconsistent.");
            }
            addProperty(versionProperty, versions.iterator().next());
        }
    }

    private void setCleanProperty(final StringBuilder dirtyProjects)
    {
        if (cleanProperty != null)
        {
            addProperty(cleanProperty, dirtyProjects.length() == 0 ? "clean" : "dirty");
        }
    }

    private void checkFailure(final HashSet<String> versions, final StringBuilder projectRevisions,
            final StringBuilder dirtyProjects, final int minRevision, final int maxRevision,
            final int lastChangedRevision)
    {
        if (failOnInconsistency && maxRevision != minRevision)
        {
            throw new BuildException("Revisions of local copies of the projects are inconsistent: "
                    + projectRevisions.toString().trim());
        }
        if (failOnDirty && dirtyProjects.length() > 0)
        {
            throw new BuildException("Dirty projects: " + dirtyProjects.toString().trim());
        }
        if (failOnInconsistency && versions.size() > 1)
        {
            throw new BuildException("Versions of local copies of the projects are inconsistent: "
                    + versions);
        }
        if (maxRevision < lastChangedRevision)
        {
            throw new BuildException("Maximum revision < last changed revision: " + maxRevision
                    + " < " + lastChangedRevision);
        }
    }

    private void addVersion(final HashSet<String> versions, final SVNInfoRecord info)
    {
        final String repositoryURL = info.getRepositoryUrl();
        if (repositoryURL.indexOf("/trunk") >= 0)
        {
            versions.add(TRUNK_VERSION);
        } else
        {
            int endIndex = repositoryURL.lastIndexOf('/');
            if (endIndex >= 0)
            {
                int startIndex = repositoryURL.lastIndexOf('/', endIndex - 1);
                if (startIndex >= 0)
                {
                    String version = repositoryURL.substring(startIndex + 1, endIndex);
                    if (SVNUtilities.LIBRARIES.equals(version))
                    {
                        endIndex = startIndex;
                        startIndex = repositoryURL.lastIndexOf('/', endIndex - 1);
                        version = repositoryURL.substring(startIndex + 1, endIndex);
                    }
                    versions.add(version);
                }
            }
        }
    }

}
