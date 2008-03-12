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

    public void setFailOnDirty(boolean failOnDirty)
    {
        this.failOnDirty = failOnDirty;
    }

    public void setFailOnInconsistency(boolean failOnInconsistentRevisions)
    {
        this.failOnInconsistency = failOnInconsistentRevisions;
    }

    public void setRevision(String revisionProperty)
    {
        this.revisionProperty = revisionProperty;
    }

    public void setVersion(String versionProperty)
    {
        this.versionProperty = versionProperty;
    }

    public void setClean(String cleanProperty)
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
        for (String projectName : projectNames)
        {
            final String path = parentDir + File.separatorChar + projectName;
            final SVNInfoRecord info = actions.info(path);
            addVersion(versions, info);
            int lastChangedRevision = info.getLastChangedRevision();
            int revision = info.getRevision();
            projectRevisions.append(projectName).append('.').append(revision).append(' ');
            maxLastChangedRevision = Math.max(maxLastChangedRevision, lastChangedRevision);
            minRevision = Math.min(minRevision, revision);
            maxRevision = Math.max(maxRevision, revision);
            if (failOnDirty || cleanProperty != null)
            {
                List<SVNItemStatus> stati = actions.status(path);
                for (SVNItemStatus status : stati)
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
        checkFailure(versions, projectRevisions, dirtyProjects, minRevision, maxRevision, maxLastChangedRevision);
        setRevisionProperty(maxLastChangedRevision);
        setVersionProperty(versions);
        setCleanProperty(dirtyProjects);
    }

    /**
     * <em>Can be overwritten in unit tests.</em>
     */
    // @Private
    ISVNActions createSVNActions()
    {
        return new SVNActions(new AntTaskSimpleLoggerAdapter(this));
    }

    /**
     * <em>Can be overwritten in unit tests.</em>
     */
    // @Private
    ISVNProjectPathProvider createPathProvider(File baseDir)
    {
        return new SVNWCProjectPathProvider(baseDir);
    }

    /**
     * <em>Can be overwritten in unit tests.</em>
     */
    // @Private
    String getParentDir()
    {
        return getProject().getBaseDir().getParentFile().getAbsolutePath();
    }

    private Set<String> collectProjects(ISVNActions actions)
    {
        final ISVNProjectPathProvider pathProvider = createPathProvider(getProject().getBaseDir());
        return new SVNDependentProjectsCollector(pathProvider, actions).collectDependentProjectsFromClasspath();
    }

    private void setRevisionProperty(int maxLastChangedRevision)
    {
        if (revisionProperty != null)
        {
            addProperty(revisionProperty, Integer.toString(maxLastChangedRevision));
        }
    }

    private void setVersionProperty(HashSet<String> versions)
    {
        if (versionProperty != null)
        {
            if (versions.size() == 0)
            {
                throw new BuildException("Couldn't determine version.");
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

    private void checkFailure(HashSet<String> versions, StringBuilder projectRevisions, StringBuilder dirtyProjects,
            int minRevision, int maxRevision, int lastChangedRevision)
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
            throw new BuildException("Versions of local copies of the projects are inconsistent: " + versions);
        }
        if (maxRevision < lastChangedRevision)
        {
            throw new BuildException("Maximum revision < last changed revision: " + maxRevision + " < "
                    + lastChangedRevision);
        }
    }

    private void addVersion(HashSet<String> versions, SVNInfoRecord info)
    {
        String repositoryURL = info.getRepositoryUrl();
        if (repositoryURL.endsWith("/trunk"))
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
                    versions.add(repositoryURL.substring(startIndex + 1, endIndex));
                }
            }
        }
    }

}
