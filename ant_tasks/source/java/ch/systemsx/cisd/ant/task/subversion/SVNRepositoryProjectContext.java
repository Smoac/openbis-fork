package ch.systemsx.cisd.ant.task.subversion;

import java.util.Arrays;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * A class for assembling and storing the subversion repository definition of a project.
 * 
 * @author Bernd Rinn
 */
class SVNRepositoryProjectContext
{
    /** A pattern that release branches must match. */
    private static final Pattern releaseBranchPattern = Pattern.compile(SVNUtilities.RELEASE_BRANCH_PATTERN_STRING);

    /** A pattern that release tags must match. */
    private static final Pattern releaseTagPattern = Pattern.compile(SVNUtilities.RELEASE_TAG_PATTERN_STRING);

    /** The root of the repository url, including the protocol. */
    private String repositoryRoot = SVNUtilities.DEFAULT_REPOSITORY_ROOT;

    /** The group that this project belongs to. */
    private String group = SVNUtilities.DEFAULT_GROUP;

    /** The type of version (trunk, branch or tag) of the project. */
    private SVNProjectVersionType versionType = SVNProjectVersionType.TRUNK;

    /** The name of the version (e.g. branch name) of the project. */
    private String version = SVNUtilities.DEFAULT_VERSION;

    /** The name of the project. */
    private String projectName;

    /** The revision in the repository to check out. */
    private String revision = SVNUtilities.HEAD_REVISION;

    /**
     * @return The root url of the subversion repository. Defaults to <code>svn+ssh://source.systemsx.ch/repos</code>.
     */
    public String getRepositoryRoot()
    {
        return repositoryRoot;
    }

    /**
     * @see #getRepositoryRoot()
     */
    public void setRepositoryRoot(String repositoryRoot)
    {
        assert repositoryRoot != null;

        if (repositoryRoot.length() == 0)
        {
            this.repositoryRoot = SVNUtilities.DEFAULT_REPOSITORY_ROOT;
            return;
        }
        this.repositoryRoot = SVNUtilities.normalizeUrl(repositoryRoot);
    }

    /**
     * @return The name of the group that the project belongs to. Defaults to <code>cisd</code>.
     */
    public String getGroup()
    {
        return group;
    }

    /**
     * @see #getGroup()
     * @throws UserFailureException If the <var>groupName</var> contains an illegal character.
     */
    public void setGroup(String groupName) throws UserFailureException
    {
        assert groupName != null;

        SVNUtilities.checkGroupName(groupName);

        this.group = groupName;
    }

    /**
     * @return The name of the project, or <code>null</code> if no project name has been set.
     */
    public String getProjectName()
    {
        return projectName;
    }

    /**
     * @see #getProjectName()
     * @throws UserFailureException If the <var>projectName</var> is empty or contains an illegal character.
     */
    public void setProjectName(String projectName) throws UserFailureException
    {
        assert projectName != null;

        SVNUtilities.checkProjectName(projectName);

        this.projectName = projectName;
    }

    /**
     * @return The version of the project. If it ends in <code>.x</code>, it will refer to a branch. Defaults to
     *         {@link SVNProjectVersionType#TRUNK}.
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * Sets the version to <code>trunk</code>.
     */
    public void setTrunkVersion()
    {
        this.versionType = SVNProjectVersionType.TRUNK;
        this.version = SVNUtilities.DEFAULT_VERSION;
    }
    
    /**
     * Sets the {@link SVNProjectVersionType} to {@link SVNProjectVersionType#RELEASE_BRANCH} and the version to
     * <var>branchName</var>.
     * 
     * @throws UserFailureException If the <var>branchName</var> does not match the pattern for release branches.
     */
    public void setReleaseBranch(String branchName) throws UserFailureException
    {
        assert branchName != null;

        if (false == isReleaseBranch(branchName))
        {
            throw new UserFailureException("Branch name '" + branchName + "' does not match the pattern.");
        }
        this.versionType = SVNProjectVersionType.RELEASE_BRANCH;
        this.version = branchName;
    }

    /**
     * @return <code>true</code> if <var>versionName</var> is a release branch.
     */
    public boolean isReleaseBranch(String versionName)
    {
        return releaseBranchPattern.matcher(versionName).matches();
    }

    /**
     * Sets the {@link SVNProjectVersionType} to {@link SVNProjectVersionType#FEATURE_BRANCH} and the version to
     * <var>branchName</var>.
     * 
     * @throws UserFailureException If the <var>branchName</var> is empty or contains an illegal character.
     */
    public void setFeatureBranch(String branchName) throws UserFailureException
    {
        assert branchName != null;

        if (version.length() == 0)
        {
            throw new UserFailureException("Branch name must not be empty.");
        }
        if (version.indexOf('/') >= 0)
        {
            throw new UserFailureException("Branch name '" + branchName + "' contains illegal charachter '/'.");
        }
        this.versionType = SVNProjectVersionType.FEATURE_BRANCH;
        this.version = branchName;
    }

    /**
     * Sets the {@link SVNProjectVersionType} to {@link SVNProjectVersionType#RELEASE_TAG} and the version to
     * <var>tagName</var>.
     * 
     * @throws UserFailureException If the <var>tagName</var> does not match the pattern for release tags.
     */
    public void setReleaseTag(String tagName) throws UserFailureException
    {
        assert tagName != null;

        if (false == isReleaseTag(tagName))
        {
            throw new UserFailureException("Tag name '" + tagName + "' does not match the pattern.");
        }
        this.versionType = SVNProjectVersionType.RELEASE_TAG;
        this.version = tagName;
    }

    /**
     * @return <code>true</code> if <var>versionName</var> is a release tag.
     */
    public boolean isReleaseTag(String versionName)
    {
        return releaseTagPattern.matcher(versionName).matches();
    }

    /**
     * @return The type of the version of this project.
     */
    public SVNProjectVersionType getVersionType()
    {
        return versionType;
    }

    /**
     * @return The revision that this definition corresponds to. Either <code>HEAD</code> or a revision number. We use
     *         this as a working and a PEG revision.
     */
    public String getRevision()
    {
        return revision;
    }

    /**
     * @see #getRevision()
     */
    public void setRevision(String revision)
    {
        if (SVNUtilities.HEAD_REVISION.equals(revision))
        {
            this.revision = SVNUtilities.HEAD_REVISION;
        } else
        {
            try
            {
                Integer.parseInt(revision);
            } catch (NumberFormatException ex)
            {
                throw UserFailureException.fromTemplate("Revision '%s' is invalid.", revision);
            }
            this.revision = revision;
        }
    }

    /**
     * @return The repository url (including the project path for branches and tags) described by this subversion
     *         project definition.
     * @throws UserFailureException If the project name hasn't been set.
     */
    public String getRepositoryUrl() throws UserFailureException
    {
        if (null == getProjectName())
        {
            throw new UserFailureException("No project name specified.");
        }

        switch (versionType)
        {
            case TRUNK:
                return StringUtils.join(Arrays.asList(getRepositoryRoot(), getGroup()), "/");
            case RELEASE_BRANCH:
                return StringUtils.join(Arrays.asList(getRepositoryRoot(), getGroup(), getProjectName(),
                "branches/release", getVersion()), "/");
            case RELEASE_TAG:
                final String branchName = SVNUtilities.getBranchForTag(getVersion());
                return StringUtils.join(Arrays.asList(getRepositoryRoot(), getGroup(), getProjectName(),
                "tags/release", branchName, getVersion()), "/");
            case FEATURE_BRANCH:
                return StringUtils.join(Arrays.asList(getRepositoryRoot(), getGroup(), getProjectName(),
                "branches/feature", getVersion()), "/");
            default:
                throw new AssertionError("Unknown version type '" + versionType + "'.");
        }
    }

    /**
     * @return The project path provider defined by this context.
     * @throws UserFailureException If the project name has not yet been set.
     */
    public ISVNProjectPathProvider getPathProvider() throws UserFailureException
    {
        if (null == getProjectName())
        {
            throw new UserFailureException("No project name specified.");
        }

        return new ISVNProjectPathProvider()
            {
                public String getPath(String subProjectName) throws UserFailureException
                {
                    assert subProjectName != null;

                    if (subProjectName.length() == 0)
                    {
                        throw new UserFailureException("Sub-project name must not be empty.");
                    }
                    if (subProjectName.indexOf('/') >= 0 || subProjectName.indexOf('\\') >= 0)
                    {
                        throw UserFailureException.fromTemplate("Sub-project '%s' contains invalid characters.",
                                subProjectName);
                    }

                    if (SVNProjectVersionType.TRUNK == versionType)
                    {
                        return StringUtils.join(Arrays.asList(getRepositoryUrl(), subProjectName,
                        SVNUtilities.DEFAULT_VERSION), "/");
                    } else
                    {
                        return StringUtils.join(Arrays.asList(getRepositoryUrl(), subProjectName), "/");
                    }
                }

                public String getPath()
                {
                    return getPath(SVNRepositoryProjectContext.this.getProjectName());
                }

                public String getPath(String subProjectName, String entityPath) throws UserFailureException
                {
                    assert subProjectName != null;
                    assert entityPath != null;

                    if (entityPath.length() == 0)
                    {
                        throw new UserFailureException("Entity path must not be empty.");
                    }
                    return getPath(subProjectName) + "/" + normalize(entityPath);
                }

                private String normalize(String entityPath)
                {
                    // In the repository we use always Unix path convention.
                    return entityPath.replace('\\', '/');
                }

                public String getProjectName()
                {
                    return SVNRepositoryProjectContext.this.getProjectName();
                }

                public String getRevision()
                {
                    return SVNRepositoryProjectContext.this.getRevision();
                }

                public boolean isRepositoryPath()
                {
                    return true;
                }

            }; // new SVNProjectPathProvider()
    }

}