/*
 * Copyright 2007 ETH Zuerich, CISD.
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.ant.common.AbstractEclipseClasspathExecutor;
import ch.systemsx.cisd.ant.common.EclipseClasspathEntry;
import ch.systemsx.cisd.ant.common.StringUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * An <code>ant</code> task that allows to recursively check out a project that follows the CISD
 * project dependency rules. It is distinguished between <code>trunk</code> and <code>tag</code>
 * mode.
 * <p>
 * In <code>trunk</code> we assume the following subversion layout:
 * 
 * <pre>
 * .../&lt;projectname&gt;/trunk
 * ...
 * .../&lt;dependent_projectname1&gt;/trunk
 * ...
 * .../&lt;dependent_projectname2&gt;/trunk
 * </pre>
 * 
 * where the <code>&lt;dependent_projectname&lt;n&gt;&gt;</code> are referenced from
 * <code>.../&lt;projectname&gt;/trunk/.classpath</code>.
 * <p>
 * For releases we assume the following layout in <code>tag</code> mode (applies to both tags and
 * branches) :
 * 
 * <pre>
 * .../&lt;projectname&gt;/tags/&lt;versiontemplate&gt;/&lt;detailedversion&gt;/&lt;projectname&gt;
 * ...
 * .../&lt;projectname&gt;/tags/&lt;versiontemplate&gt;/&lt;detailedversion&gt;/&lt;dependent_projectname1&gt;
 * ...
 * .../&lt;projectname&gt;/tags/&lt;versiontemplate&gt;/&lt;detailedversion&gt;/&lt;dependent_projectname2&gt;
 * </pre>
 * 
 * For our regularly sprint releases we only tag the version and do not create a branch of it. For
 * this we have the following structure:
 * 
 * <pre>
 * .../&lt;projectname&gt;/tags/sprint/S&lt;sprintnumber&gt;/&lt;projectname&gt;
 * ...
 * .../&lt;projectname&gt;/tags/sprint/S&lt;sprintnumber&gt;/&lt;dependent_projectname1&gt;
 * ...
 * .../&lt;projectname&gt;/tags/sprint/S&lt;sprintnumber&gt;/&lt;dependent_projectname2&gt;
 * </pre>
 * 
 * <em><strong>Note:</strong> This schema requires the Eclipse project names and the subversion project names to be the
 * same!</em>
 * 
 * @author Bernd Rinn
 */
public class SVNRecursiveCheckoutTask extends Task
{

    /**
     * The logger for this task.
     */
    private final AntTaskSimpleLoggerAdapter LOGGER = new AntTaskSimpleLoggerAdapter(this);

    /**
     * The set of projects that have already been checked out. We need to keep track of it, because
     * the same project can be referenced from several other projects.
     */
    private final Set<String> projectsAlreadyCheckedOut = new HashSet<String>();

    /** The directory to check all related projects out. */
    private String dir;

    /**
     * The context of the project in the subversion repository.
     */
    private final SVNRepositoryProjectContext context = new SVNRepositoryProjectContext();

    /**
     * A class that checks the classpath entries on whether they require a checkout of a new
     * project.
     * 
     * @author Bernd Rinn
     */
    private final class SVNCheckoutDependentExecutor extends AbstractEclipseClasspathExecutor
    {
        private static final String CLASSYCLE_LIBRARY = "libraries/classycle";

        private static final String COBERTURA_LIBRARY = "libraries/cobertura";

        private final ISVNCheckout checkoutCmd;

        SVNCheckoutDependentExecutor(final ISVNCheckout checkoutCmd)
        {
            this.checkoutCmd = checkoutCmd;
        }

        @Override
        protected void executeEntries(final List<EclipseClasspathEntry> entries)
        {
            assert entries != null;
            final ISVNProjectPathProvider pathProvider = context.getPathProvider();

            for (final EclipseClasspathEntry entry : entries)
            {
                final String path = entry.getPath();
                if (path.startsWith("/"))
                {
                    final String projectName = SVNUtilities.getProjectName(path);
                    if (false == projectsAlreadyCheckedOut.contains(projectName))
                    {
                        checkOutProject(projectName, pathProvider, false);
                    }
                }
            }
            if (false == projectsAlreadyCheckedOut.contains(CLASSYCLE_LIBRARY))
            {
                checkOutProject(CLASSYCLE_LIBRARY, pathProvider, false);
            }
            // Make old build scripts work
            if (false == projectsAlreadyCheckedOut.contains(COBERTURA_LIBRARY))
            {
                checkOutProject(COBERTURA_LIBRARY, pathProvider, true);
            }
        }

        private void checkOutProject(final String projectName,
                final ISVNProjectPathProvider pathProvider, boolean ignoreException)
        {
            projectsAlreadyCheckedOut.add(projectName);
            final String projectToCheckOut = pathProvider.getPath(projectName);
            try
            {
                checkoutCmd.checkout(projectToCheckOut, projectName, pathProvider.getRevision());
            } catch (final SVNException ex)
            {
                if (ignoreException)
                {
                    return;
                } else
                {
                    throw new BuildException(ex.getMessage(), ex.getCause());
                }
            }
            final File projectWorkingCopyDir =
                    new File(checkoutCmd.getDirectoryToCheckout(), projectName);
            new SVNCheckoutDependentExecutor(checkoutCmd).execute(projectWorkingCopyDir);
        }

        @Override
        protected void handleAbsentsOfClasspathFile(final File eclipseClasspathFile)
        {
            if (eclipseClasspathFile.getPath().indexOf(SVNUtilities.LIBRARIES) < 0)
            {
                log("No Eclipse .classpath file found in '" + eclipseClasspathFile.getParent()
                        + "'.");
            }
        }

    }

    /**
     * Requires the following properties:
     * <ul>
     * <li>url - the URL of the subversion repository</li>
     * <li>dir - the directory to check out all related projects to</li>
     * </ul>
     */
    @Override
    public void execute() throws BuildException
    {
        final String repositoryUrl = context.getRepositoryUrl();
        final ISVNProjectPathProvider pathProvider = context.getPathProvider();
        final String workingCopyDir = dir;
        if (null == workingCopyDir)
        {
            throw new BuildException("Attribute 'dir' not specified.");
        }
        try
        {
            final ISVNCheckout checkoutCmd = createSVNCheckout(repositoryUrl, workingCopyDir);
            checkoutCmd.checkout(pathProvider.getPath(), pathProvider.getProjectName(),
                    pathProvider.getRevision());
            // Always check out "build_resources", though it is not specified as dependency in
            // .classpath.
            checkoutCmd.checkout(pathProvider.getPath(SVNUtilities.BUILD_RESOURCES_PROJECT),
                    SVNUtilities.BUILD_RESOURCES_PROJECT, pathProvider.getRevision());
            // Replace cisd-ant-tasks.jar with current one from trunk
            final ISVNFileExporter exporterCmd = createSVNFileExporter();
            final File libDir =
                    new File(new File(checkoutCmd.getDirectoryToCheckout(),
                            SVNUtilities.BUILD_RESOURCES_PROJECT), "lib");
            exporterCmd.export(StringUtils.join(Arrays.asList(context.getRepositoryRoot(),
                    context.getGroup(), SVNUtilities.BUILD_RESOURCES_PROJECT,
                    SVNUtilities.DEFAULT_VERSION, "lib", SVNUtilities.CISD_ANT_TASKS_FILE), "/"),
                    libDir);
            new SVNCheckoutDependentExecutor(checkoutCmd).execute(new File(workingCopyDir,
                    pathProvider.getProjectName()));
        } catch (final SVNException ex)
        {
            throw new BuildException(ex.getMessage(), ex.getCause());
        }
    }

    /**
     * <em>Can be overwritten in unit tests.</em>
     */
    @Private
    ISVNCheckout createSVNCheckout(final String repositoryUrl, final String workingCopyDir)
    {
        return new SVNCheckout(LOGGER, workingCopyDir);
    }

    /**
     * <em>Can be overwritten in unit tests.</em>
     */
    @Private
    ISVNFileExporter createSVNFileExporter()
    {
        return new SVNFileExporter(LOGGER);
    }

    /**
     * Sets the directory where the working copies of all dependent prejects will be checked out.
     */
    public void setDir(final String dir)
    {
        assert dir != null;

        this.dir = dir;
    }

    /**
     * Sets the root url of the subversion repository. Defaults to
     * <code>svn+ssh://svncisd.ethz.ch/repos</code>.
     */
    public void setRepositoryRoot(final String repositoryRoot)
    {
        assert repositoryRoot != null;

        context.setRepositoryRoot(repositoryRoot);
    }

    /**
     * Sets the name of the group that the project belongs to. Defaults to <code>cisd</code>.
     */
    public void setGroup(final String groupName)
    {
        assert groupName != null;

        try
        {
            context.setGroup(groupName);
        } catch (final UserFailureException ex)
        {
            throw new BuildException(ex.getMessage());
        }
    }

    /**
     * Sets the name of the project.
     */
    public void setName(final String projectName)
    {
        assert projectName != null;

        try
        {
            context.setProjectName(projectName);
        } catch (final UserFailureException ex)
        {
            throw new BuildException(ex.getMessage());
        }
    }

    /**
     * Sets the name of the release branch specified for this project.
     */
    public void setReleaseBranch(final String branchName)
    {
        assert context != null;
        assert branchName != null;

        try
        {
            context.setReleaseBranch(branchName);
        } catch (final UserFailureException ex)
        {
            throw new BuildException(ex.getMessage());
        }
    }

    /**
     * Sets the name of the stage branch specified for this project.
     */
    public void setStageBranch(final String branchName)
    {
        assert context != null;
        assert branchName != null;

        try
        {
            context.setStageBranch(branchName);
        } catch (final UserFailureException ex)
        {
            throw new BuildException(ex.getMessage());
        }
    }

    /**
     * Sets the name of the feature branch specified for this project.
     */
    public void setFeatureBranch(final String branchName)
    {
        assert context != null;
        assert branchName != null;

        try
        {
            context.setFeatureBranch(branchName);
        } catch (final UserFailureException ex)
        {
            throw new BuildException(ex.getMessage());
        }
    }

    /**
     * Sets the version to <var>versionName</var>.
     * <p>
     * If <code>versionName == "trunk"</code>, the version will be the trunk. If
     * <var>versionName</var> fits into the release branch schema, it will be interpreted as a
     * release branch, if it fits into a release tag schema, it will be interpreted as a release
     * tag. If it fits into a sprint tag, it will be interpreted as a sprint tag. In all other cases
     * the version will be interpreted as a feature branch.
     */
    public void setVersion(final String versionName)
    {
        if (SVNUtilities.DEFAULT_VERSION.equals(versionName))
        {
            context.setTrunkVersion();
        } else if (context.isReleaseBranch(versionName))
        {
            setReleaseBranch(versionName);
        } else if (context.isReleaseTag(versionName))
        {
            setReleaseTag(versionName);
        } else if (context.isStageBranch(versionName))
        {
            setStageBranch(versionName);
        } else if (context.isSprintBranch(versionName))
        {
            setSprintBranch(versionName);
        } else if (context.isSprintTag(versionName))
        {
            setSprintTag(versionName);
        } else
        {
            setFeatureBranch(versionName);
        }
    }

    /**
     * Sets the name of the release tag specified for this project.
     */
    public void setReleaseTag(final String tagName)
    {
        assert context != null;
        assert tagName != null;

        try
        {
            context.setReleaseTag(tagName);
        } catch (final UserFailureException ex)
        {
            throw new BuildException(ex.getMessage());
        }
    }

    /**
     * Sets the name of the sprint branch specified for this project.
     */
    public void setSprintBranch(String branchName)
    {
        assert context != null;
        assert branchName != null;

        try
        {
            context.setSprintBranch(branchName);
        } catch (UserFailureException ex)
        {
            throw new BuildException(ex.getMessage());
        }
    }

    /**
     * Sets the name of the sprint tag specified for this project.
     */
    public void setSprintTag(String tagName)
    {
        assert context != null;
        assert tagName != null;

        try
        {
            context.setSprintTag(tagName);
        } catch (UserFailureException ex)
        {
            throw new BuildException(ex.getMessage());
        }
    }

    /**
     * Sets the revision to check out. Defaults to <code>HEAD</code>.
     */
    public void setRevision(final String revision)
    {
        assert context != null;
        assert revision != null;

        try
        {
            context.setRevision(revision);
        } catch (final UserFailureException ex)
        {
            throw new BuildException(ex.getMessage());
        }
    }

}
