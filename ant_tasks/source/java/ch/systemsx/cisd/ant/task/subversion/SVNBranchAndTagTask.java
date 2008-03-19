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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * An ant task that allows branching of projects with all dependent projects (using Eclipse' <code>.classpath</code>
 * file to find out about the dependencies).
 * 
 * @author Bernd Rinn
 */
public class SVNBranchAndTagTask extends Task
{

    /** The source context of the project in the subversion repository. */
    final private SVNRepositoryProjectContext sourceContext = new SVNRepositoryProjectContext();

    /** The destination context of the project in the subversion repository. */
    final private SVNRepositoryProjectContext destinationContext =
            new SVNRepositoryProjectContext();

    /** If set to <code>true</code>, both a branch will be created and a tag from this branch. */
    private boolean branchIfNecessary = false;

    /**
     * An ant wrapper for the {@link SVNRepositoryProjectContext}.
     */
    private static final class SVNRepositoryProjectContextAntWrapper
    {
        private final ISVNProjectPathProvider pathProvider;

        private final String version;

        private final String revision;

        private final String repositoryUrl;

        private final String tagRepositoryUrl;

        SVNRepositoryProjectContextAntWrapper(SVNRepositoryProjectContext context)
                throws BuildException
        {
            this(context, null);
        }

        SVNRepositoryProjectContextAntWrapper(SVNRepositoryProjectContext context,
                SVNRepositoryProjectContext contextToAssignFrom) throws BuildException
        {
            if (null != contextToAssignFrom)
            {
                assign(context, contextToAssignFrom);
            }
            try
            {
                this.version = context.getVersion();
                this.revision = context.getRevision();
                this.repositoryUrl = context.getRepositoryUrl();
                if (SVNProjectVersionType.RELEASE_BRANCH == context.getVersionType())
                {
                    SVNRepositoryProjectContext tagContext = new SVNRepositoryProjectContext();
                    assign(tagContext, context);
                    tagContext.setReleaseTag(SVNUtilities.getFirstTagForBranch(this.version));
                    this.tagRepositoryUrl = SVNUtilities.getParent(tagContext.getRepositoryUrl());
                } else
                {
                    this.tagRepositoryUrl = null;
                }
                this.pathProvider = createPathProvider(context);
            } catch (UserFailureException ex)
            {
                throw new BuildException(ex);
            }
        }

        private static ISVNProjectPathProvider createPathProvider(
                SVNRepositoryProjectContext context) throws UserFailureException
        {
            return context.getPathProvider();
        }

        private static void assign(SVNRepositoryProjectContext context,
                SVNRepositoryProjectContext contextToAssignFrom) throws UserFailureException
        {
            context.setRepositoryRoot(contextToAssignFrom.getRepositoryRoot());
            context.setGroup(contextToAssignFrom.getGroup());
            context.setProjectName(contextToAssignFrom.getProjectName());
        }

        ISVNProjectPathProvider getPathProvider()
        {
            return pathProvider;
        }

        String getVersion()
        {
            return version;
        }

        String getRepositoryUrl()
        {
            return repositoryUrl;
        }

        String getTagRepositoryUrl()
        {
            return tagRepositoryUrl;
        }

        String getRevision()
        {
            return revision;
        }
    }

    /**
     * Requires the following properties:
     * <ul>
     * <li> projecname - the name of the project in the subversion repository to branch from </li>
     * <li> branch - the name of the branch to create </li>
     * </ul>
     */
    @Override
    public void execute() throws BuildException
    {
        final SVNProjectVersionType versionTypeToCreate = destinationContext.getVersionType();
        if (SVNProjectVersionType.TRUNK == versionTypeToCreate)
        {
            throw new BuildException("No branch/tag name specified.");
        }
        final ISVNActions svn = createSVNActions();
        final SVNRepositoryProjectContextAntWrapper source =
                new SVNRepositoryProjectContextAntWrapper(sourceContext);
        final SVNRepositoryProjectContextAntWrapper destination =
                new SVNRepositoryProjectContextAntWrapper(destinationContext, sourceContext);

        switch (versionTypeToCreate)
        {
            case RELEASE_TAG:
                createTagInSvn(svn, source, createBranchContext(destinationContext), destination,
                        branchIfNecessary);
                break;
            case RELEASE_BRANCH:
            case FEATURE_BRANCH:
                createBranchInSvn(svn, source, destination);
                break;
            default:
                throw new AssertionError("Invalid version type.");
        }

    }

    private static void createTagInSvn(final ISVNActions svn,
            final SVNRepositoryProjectContextAntWrapper source,
            final SVNRepositoryProjectContextAntWrapper branch,
            final SVNRepositoryProjectContextAntWrapper tag, boolean branchIfNecessary)
            throws BuildException
    {
        if (false == branchExists(svn, branch))
        {
            if (branchIfNecessary)
            {
                if (false == tag.getVersion().equals(
                        SVNUtilities.getFirstTagForBranch(branch.getVersion())))
                {
                    throw new BuildException(
                            "Supposed to create branch for tag but tag doesn't start a branch (tag='"
                                    + tag.getVersion() + "').");
                }
                createBranchInSvn(svn, source, branch);
            } else
            {
                throw new BuildException("Branch from which to tag is not yet defined. ("
                        + branch.getVersion() + ")");
            }
        }
        final String tagName = tag.getVersion();
        final String logMessage = "Create tag '" + tagName + "'";
        svn.copy(branch.getRepositoryUrl(), branch.getRevision(), tag.getRepositoryUrl(),
                logMessage);
    }

    private static SVNRepositoryProjectContextAntWrapper createBranchContext(
            SVNRepositoryProjectContext tagContext) throws BuildException
    {
        try
        {
            final String branchName = SVNUtilities.getBranchForTag(tagContext.getVersion());
            SVNRepositoryProjectContext context = new SVNRepositoryProjectContext();
            context.setReleaseBranch(branchName);
            return new SVNRepositoryProjectContextAntWrapper(context, tagContext);
        } catch (UserFailureException ex)
        {
            throw new BuildException(ex);
        }
    }

    private static void createBranchInSvn(final ISVNActions svn,
            final SVNRepositoryProjectContextAntWrapper source,
            final SVNRepositoryProjectContextAntWrapper destination)
    {
        final ISVNProjectPathProvider sourcePathProvider = source.getPathProvider();
        final ISVNProjectPathProvider destinationPathProvider = destination.getPathProvider();
        final Set<String> dependentProjects =
                new SVNDependentProjectsCollector(source.getPathProvider(), svn)
                        .collectDependentProjectsFromClasspath();

        final String branchName = destination.getVersion();
        final String logMessage = "Create branch '" + branchName + "'";
        try
        {
            if (branchExists(svn, destination))
            {
                throw new BuildException("The branch '" + branchName + "' already exists.");
            }
            if (svn.isMuccAvailable())
            {
                List<String> commandLine = new ArrayList<String>();
                commandLine.addAll(Arrays.asList("mkdir", destination.getRepositoryUrl()));
                if (null != destination.getTagRepositoryUrl())
                {
                    commandLine.addAll(Arrays.asList("mkdir", destination.getTagRepositoryUrl()));
                }
                for (String subProjectName : dependentProjects)
                {
                    commandLine.addAll(Arrays.asList("cp", sourcePathProvider.getRevision(),
                            sourcePathProvider.getPath(subProjectName), destinationPathProvider
                                    .getPath(subProjectName)));
                }
                svn.mucc(logMessage, commandLine.toArray(new String[commandLine.size()]));
            } else
            {
                svn.mkdir(destination.getRepositoryUrl(), logMessage);
                if (null != destination.getTagRepositoryUrl())
                {
                    svn.mkdir(destination.getTagRepositoryUrl(), logMessage);
                }
                for (String subProjectName : dependentProjects)
                {
                    svn.copy(sourcePathProvider.getPath(subProjectName), sourcePathProvider
                            .getRevision(), destinationPathProvider.getPath(subProjectName),
                            logMessage);
                }
            }
        } catch (SVNException ex)
        {
            throw new BuildException(ex);
        }
    }

    private static boolean branchExists(final ISVNActions svn,
            final SVNRepositoryProjectContextAntWrapper branch)
    {
        final String branchName = branch.getVersion();
        final String branchUrl = branch.getRepositoryUrl();
        final String parentUrl = SVNUtilities.getParent(branchUrl);
        assert parentUrl != null;
        assert branchName.equals(branchUrl.substring(parentUrl.length() + 1));
        final Set<String> branchSet = new HashSet<String>(svn.list(parentUrl));
        final boolean exists = branchSet.contains(branchName + "/");
        return exists;
    }

    /**
     * <em>Can be overwritten in unit tests.</em>
     */
    // @Private
    ISVNActions createSVNActions()
    {
        return new SVNActions(new AntTaskSimpleLoggerAdapter(this));
    }

    public void setName(String projectName)
    {
        try
        {
            sourceContext.setProjectName(projectName);
        } catch (UserFailureException ex)
        {
            throw new BuildException(ex);
        }
    }

    public void setRevision(String revision)
    {
        try
        {
            sourceContext.setRevision(revision);
        } catch (UserFailureException ex)
        {
            throw new BuildException(ex);
        }
    }

    public void setGroup(String groupName)
    {
        try
        {
            sourceContext.setGroup(groupName);
        } catch (UserFailureException ex)
        {
            throw new BuildException(ex);
        }
    }

    public void setRepositoryRoot(String repositoryRoot)
    {
        try
        {
            sourceContext.setRepositoryRoot(repositoryRoot);
        } catch (UserFailureException ex)
        {
            throw new BuildException(ex);
        }
    }

    /**
     * Sets the name of the release branch to create.
     */
    public void setReleaseBranch(String branchName)
    {
        assert branchName != null;

        if (SVNProjectVersionType.TRUNK != destinationContext.getVersionType())
        {
            throw new BuildException(String.format(
                    "Version type has already been set (Version: '%s', Type: '%s').",
                    destinationContext.getVersion(), destinationContext.getVersionType()));
        }

        try
        {
            destinationContext.setReleaseBranch(branchName);
        } catch (UserFailureException ex)
        {
            throw new BuildException(ex);
        }
    }

    /**
     * Sets the name of the feature branch to create.
     */
    public void setFeatureBranch(String branchName)
    {
        assert branchName != null;

        if (SVNProjectVersionType.TRUNK != destinationContext.getVersionType())
        {
            throw new BuildException(String.format(
                    "Version type has already been set (Version: '%s', Type: '%s').",
                    destinationContext.getVersion(), destinationContext.getVersionType()));
        }

        try
        {
            destinationContext.setFeatureBranch(branchName);
        } catch (UserFailureException ex)
        {
            throw new BuildException(ex);
        }
    }

    /**
     * Sets the name of the feature branch to create.
     */
    public void setReleaseTag(String tagName)
    {
        assert tagName != null;

        if (SVNProjectVersionType.TRUNK != destinationContext.getVersionType())
        {
            throw new BuildException(String.format(
                    "Version type has already been set (Version: '%s', Type: '%s').",
                    destinationContext.getVersion(), destinationContext.getVersionType()));
        }

        try
        {
            destinationContext.setReleaseTag(tagName);
        } catch (UserFailureException ex)
        {
            throw new BuildException(ex);
        }
    }

    /**
     * Sets whether it should be branched if the branch does not already exist when tagging.
     */
    public void setBranchIfNecessary(boolean branchAndTag)
    {
        this.branchIfNecessary = branchAndTag;
    }

}
