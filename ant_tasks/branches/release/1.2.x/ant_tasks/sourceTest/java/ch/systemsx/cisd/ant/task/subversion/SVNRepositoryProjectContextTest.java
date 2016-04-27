package ch.systemsx.cisd.ant.task.subversion;

import static ch.systemsx.cisd.ant.task.subversion.SVNProjectVersionType.FEATURE_BRANCH;
import static ch.systemsx.cisd.ant.task.subversion.SVNProjectVersionType.RELEASE_BRANCH;
import static ch.systemsx.cisd.ant.task.subversion.SVNProjectVersionType.RELEASE_TAG;
import static ch.systemsx.cisd.ant.task.subversion.SVNProjectVersionType.TRUNK;
import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;

import org.testng.annotations.Test;

import ch.systemsx.cisd.ant.common.StringUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

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

/**
 * Test cases for {@link SVNRepositoryProjectContext}.
 * 
 * @author Bernd Rinn
 */
public class SVNRepositoryProjectContextTest
{

    @Test
    public void testVersionTypeTrunk()
    {
        final SVNRepositoryProjectContext def = new SVNRepositoryProjectContext();
        assert TRUNK == def.getVersionType();
    }

    @Test
    public void testVersionTypeReleaseBranch()
    {
        final SVNRepositoryProjectContext def = new SVNRepositoryProjectContext();
        def.setReleaseBranch("2.3.x");
        assert RELEASE_BRANCH == def.getVersionType();
        assertEquals("2.3.x", def.getVersion());
        def.setReleaseBranch("0.0.x");
        assert RELEASE_BRANCH == def.getVersionType();
        assertEquals("0.0.x", def.getVersion());
    }

    @Test
    public void testVersionTypeTag()
    {
        final SVNRepositoryProjectContext def = new SVNRepositoryProjectContext();
        def.setReleaseTag("2.3.0");
        assert RELEASE_TAG == def.getVersionType();
        assertEquals("2.3.0", def.getVersion());
        def.setReleaseTag("1.18.100");
        assert RELEASE_TAG == def.getVersionType();
        assertEquals("1.18.100", def.getVersion());
    }

    @Test
    public void testVersionTypeFeatureBranch()
    {
        final SVNRepositoryProjectContext def = new SVNRepositoryProjectContext();
        def.setFeatureBranch("test5");
        assert FEATURE_BRANCH == def.getVersionType();
        assertEquals("test5", def.getVersion());
    }

    @Test
    public void testCreateTrunkUrl()
    {
        final SVNRepositoryProjectContext def = new SVNRepositoryProjectContext();
        final String name = "someProject";
        def.setProjectName(name);
        final String repositoryUrl =
            SVNUtilities.DEFAULT_REPOSITORY_ROOT + "/" + SVNUtilities.DEFAULT_GROUP;
        final String projectPath = StringUtils.join(Arrays.asList(repositoryUrl, name, "trunk"), "/");
        assertEquals(repositoryUrl, def.getRepositoryUrl());
        assertEquals(projectPath, def.getPathProvider().getPath());
    }

    @Test
    public void testCreateTrunkUrlForSubproject()
    {
        final SVNRepositoryProjectContext def = new SVNRepositoryProjectContext();
        final String name = "someProject";
        final String subName = "someSubProject";
        def.setProjectName(name);
        final String repositoryUrl =
            SVNUtilities.DEFAULT_REPOSITORY_ROOT + "/" + SVNUtilities.DEFAULT_GROUP;
        final String projectPath = StringUtils.join(Arrays.asList(repositoryUrl, subName, "trunk"), "/");
        assertEquals(projectPath, def.getPathProvider().getPath(subName));
    }

    @Test
    public void testCreateTrunkUrlForPathEntry()
    {
        final SVNRepositoryProjectContext def = new SVNRepositoryProjectContext();
        final String name = "someProject";
        final String subName = "someSubProject";
        final String pathEntry = ".classpath";
        def.setProjectName(name);
        final String repositoryUrl =
            SVNUtilities.DEFAULT_REPOSITORY_ROOT + "/" + SVNUtilities.DEFAULT_GROUP;
        final String projectPath =
                StringUtils.join(Arrays.asList(repositoryUrl, subName, "trunk", pathEntry), "/");
        assertEquals(projectPath, def.getPathProvider().getPath(subName, pathEntry));
    }

    @Test
    public void testCreateReleaseBranchUrl()
    {
        final SVNRepositoryProjectContext def = new SVNRepositoryProjectContext();
        final String name = "someProject";
        final String branchName = "0.9.x";
        def.setProjectName(name);
        def.setReleaseBranch(branchName);
        final String branchUrl =
            SVNUtilities.DEFAULT_REPOSITORY_ROOT + "/" + SVNUtilities.DEFAULT_GROUP
                        + "/" + name + "/branches/release/" + branchName;
        final String projectPath = StringUtils.join(Arrays.asList(branchUrl, name), "/");
        assertEquals(branchUrl, def.getRepositoryUrl());
        assertEquals(projectPath, def.getPathProvider().getPath());
    }

    @Test
    public void testCreateReleaseBranchUrlForSubProject()
    {
        final SVNRepositoryProjectContext def = new SVNRepositoryProjectContext();
        final String name = "someProject";
        final String subName = "someSubProject";
        final String branchName = "0.9.x";
        def.setProjectName(name);
        def.setReleaseBranch(branchName);
        final String branchUrl =
            SVNUtilities.DEFAULT_REPOSITORY_ROOT + "/" + SVNUtilities.DEFAULT_GROUP
                        + "/" + name + "/branches/release/" + branchName;
        final String projectPath = StringUtils.join(Arrays.asList(branchUrl, subName), "/");
        assertEquals(projectPath, def.getPathProvider().getPath(subName));
    }

    @Test
    public void testCreateReleaseBranchUrlForPathEntry()
    {
        final SVNRepositoryProjectContext def = new SVNRepositoryProjectContext();
        final String name = "someProject";
        final String subName = "someSubProject";
        final String branchName = "0.9.x";
        final String pathEntry = ".classpath";
        def.setProjectName(name);
        def.setReleaseBranch(branchName);
        final String branchUrl =
            SVNUtilities.DEFAULT_REPOSITORY_ROOT + "/" + SVNUtilities.DEFAULT_GROUP
                        + "/" + name + "/branches/release/" + branchName;
        final String projectPath = StringUtils.join(Arrays.asList(branchUrl, subName, pathEntry), "/");
        assertEquals(projectPath, def.getPathProvider().getPath(subName, pathEntry));
    }

    @Test
    public void testCreateFeatureBranchUrl()
    {
        final SVNRepositoryProjectContext def = new SVNRepositoryProjectContext();
        final String name = "someProject";
        final String branchName = "test5";
        def.setProjectName(name);
        def.setFeatureBranch(branchName);
        final String branchUrl =
            SVNUtilities.DEFAULT_REPOSITORY_ROOT + "/" + SVNUtilities.DEFAULT_GROUP
                        + "/" + name + "/branches/feature/" + branchName;
        final String projectPath = StringUtils.join(Arrays.asList(branchUrl, name), "/");
        assertEquals(branchUrl, def.getRepositoryUrl());
        assertEquals(projectPath, def.getPathProvider().getPath());
    }

    @Test
    public void testCreateFeatureBranchUrlForSubProject()
    {
        final SVNRepositoryProjectContext def = new SVNRepositoryProjectContext();
        final String name = "someProject";
        final String subName = "someSubProject";
        final String branchName = "test5";
        def.setProjectName(name);
        def.setFeatureBranch(branchName);
        final String branchUrl =
            SVNUtilities.DEFAULT_REPOSITORY_ROOT + "/" + SVNUtilities.DEFAULT_GROUP
                        + "/" + name + "/branches/feature/" + branchName;
        final String projectPath = StringUtils.join(Arrays.asList(branchUrl, subName), "/");
        assertEquals(projectPath, def.getPathProvider().getPath(subName));
    }

    @Test
    public void testCreateReleaseTagUrl()
    {
        final SVNRepositoryProjectContext def = new SVNRepositoryProjectContext();
        final String name = "someProject";
        final String branchName = "0.9.x";
        final String tagName = "0.9.18";
        def.setProjectName(name);
        def.setReleaseTag(tagName);
        final String tagUrl =
            SVNUtilities.DEFAULT_REPOSITORY_ROOT + "/" + SVNUtilities.DEFAULT_GROUP
                        + "/" + name + "/tags/release/" + branchName + "/" + tagName;
        final String projectPath = StringUtils.join(Arrays.asList(tagUrl, name), "/");
        assertEquals(tagUrl, def.getRepositoryUrl());
        assertEquals(projectPath, def.getPathProvider().getPath());
    }

    @Test
    public void testCreateReleaseTagUrlForSubProject()
    {
        final SVNRepositoryProjectContext def = new SVNRepositoryProjectContext();
        final String name = "someProject";
        final String subName = "someSubProject";
        final String branchName = "0.9.x";
        final String tagName = "0.9.18";
        def.setProjectName(name);
        def.setReleaseTag(tagName);
        final String tagUrl =
            SVNUtilities.DEFAULT_REPOSITORY_ROOT + "/" + SVNUtilities.DEFAULT_GROUP
                        + "/" + name + "/tags/release/" + branchName + "/" + tagName;
        final String projectPath = StringUtils.join(Arrays.asList(tagUrl, subName), "/");
        assertEquals(tagUrl, def.getRepositoryUrl());
        assertEquals(projectPath, def.getPathProvider().getPath(subName));
    }

    @Test
    public void testIllegalFeatureBranch()
    {
        final SVNRepositoryProjectContext def = new SVNRepositoryProjectContext();
        def.setFeatureBranch("a/b");
    }

    @Test(expectedExceptions =
        { UserFailureException.class })
    public void testIllegalTag1()
    {
        final SVNRepositoryProjectContext def = new SVNRepositoryProjectContext();
        def.setReleaseTag("1");
    }

    @Test(expectedExceptions =
        { UserFailureException.class })
    public void testIllegalTag2()
    {
        final SVNRepositoryProjectContext def = new SVNRepositoryProjectContext();
        def.setReleaseTag("1.0");
    }

    @Test(expectedExceptions =
        { UserFailureException.class })
    public void testIllegalTag3()
    {
        final SVNRepositoryProjectContext def = new SVNRepositoryProjectContext();
        def.setReleaseTag("1.0.");
    }

    @Test(expectedExceptions =
        { UserFailureException.class })
    public void testIllegalTag4()
    {
        final SVNRepositoryProjectContext def = new SVNRepositoryProjectContext();
        def.setReleaseTag(".1.0.0");
    }

    @Test(expectedExceptions =
        { UserFailureException.class })
    public void testIllegalTag5()
    {
        final SVNRepositoryProjectContext def = new SVNRepositoryProjectContext();
        def.setReleaseTag("1.0.0a");
    }

    @Test(expectedExceptions =
        { UserFailureException.class })
    public void testIllegalTag6()
    {
        final SVNRepositoryProjectContext def = new SVNRepositoryProjectContext();
        def.setReleaseTag("1.test.1");
    }

    @Test(expectedExceptions =
        { UserFailureException.class })
    public void testIllegalTag7()
    {
        final SVNRepositoryProjectContext def = new SVNRepositoryProjectContext();
        def.setReleaseTag("1.1.x");
    }

    @Test(expectedExceptions =
        { UserFailureException.class })
    public void testIllegalBranch1()
    {
        final SVNRepositoryProjectContext def = new SVNRepositoryProjectContext();
        def.setReleaseBranch("1");
    }

    @Test(expectedExceptions =
        { UserFailureException.class })
    public void testIllegalBranch2()
    {
        final SVNRepositoryProjectContext def = new SVNRepositoryProjectContext();
        def.setReleaseBranch("1.0");
    }

    @Test(expectedExceptions =
        { UserFailureException.class })
    public void testIllegalBranch3()
    {
        final SVNRepositoryProjectContext def = new SVNRepositoryProjectContext();
        def.setReleaseBranch("1.0.");
    }

    @Test(expectedExceptions =
        { UserFailureException.class })
    public void testIllegalBranch4()
    {
        final SVNRepositoryProjectContext def = new SVNRepositoryProjectContext();
        def.setReleaseBranch(".1.0.0");
    }

    @Test(expectedExceptions =
        { UserFailureException.class })
    public void testIllegalBranch5()
    {
        final SVNRepositoryProjectContext def = new SVNRepositoryProjectContext();
        def.setReleaseBranch("1.0.xa");
    }

    @Test(expectedExceptions =
        { UserFailureException.class })
    public void testIllegalBranch6()
    {
        final SVNRepositoryProjectContext def = new SVNRepositoryProjectContext();
        def.setReleaseBranch("1.test.1");
    }

    @Test(expectedExceptions =
        { UserFailureException.class })
    public void testIllegalBranch7()
    {
        final SVNRepositoryProjectContext def = new SVNRepositoryProjectContext();
        def.setReleaseBranch("1.1.0");
    }

    @Test
    public void testMissingName()
    {
        final SVNRepositoryProjectContext def = new SVNRepositoryProjectContext();
        assert null == def.getProjectName();
    }

    @Test(expectedExceptions =
        { UserFailureException.class })
    public void testMissingNameInGetUrl()
    {
        final SVNRepositoryProjectContext def = new SVNRepositoryProjectContext();
        def.getPathProvider();
    }

}
