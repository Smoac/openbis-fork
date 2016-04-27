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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.*;

import ch.systemsx.cisd.common.utilities.StringUtilities;

/**
 * Test cases for the {@link SVNBranchAndTagTask}.
 * 
 * @author Bernd Rinn
 */
public class SVNBranchAndTagTaskTest
{

    private static final class CopyItem
    {
        final String sourcePath;

        final String sourceRevision;

        final String destinationPath;

        final String logMessage;

        CopyItem(String sourcePath, String sourceRevision, String destinationPath, String logMessage)
        {
            assert sourcePath != null;
            assert sourceRevision != null;
            assert destinationPath != null;
            assert logMessage != null;

            this.sourcePath = sourcePath;
            this.sourceRevision = sourceRevision;
            this.destinationPath = destinationPath;
            this.logMessage = logMessage;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj instanceof CopyItem == false)
            {
                return false;
            }
            CopyItem that = (CopyItem) obj;
            return this.sourcePath.equals(that.sourcePath) && this.sourceRevision.equals(that.sourceRevision)
                    && this.destinationPath.equals(that.destinationPath) && this.logMessage.equals(that.logMessage);
        }

        @Override
        public int hashCode()
        {
            return ((this.sourcePath.hashCode() * 37 + this.sourceRevision.hashCode()) * 37 + this.destinationPath
                    .hashCode())
                    * 37 + this.logMessage.hashCode();
        }

        @Override
        public String toString()
        {
            return String.format("CopyItem: (sourcePath=%s, sourceRevision=%s, destinationPath=%s, logMessage=%s)",
                    sourcePath, sourceRevision, destinationPath, logMessage);
        }
    }

    private static final class MkdirItem
    {
        final String path;

        final String logMessage;

        MkdirItem(String path, String logMessage)
        {
            assert path != null;
            assert logMessage != null;

            this.path = path;
            this.logMessage = logMessage;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj instanceof MkdirItem == false)
            {
                return false;
            }
            final MkdirItem that = (MkdirItem) obj;
            return this.path.equals(that.path) && this.logMessage.equals(that.logMessage);
        }

        @Override
        public int hashCode()
        {
            return path.hashCode() * 37 + logMessage.hashCode();
        }

        @Override
        public String toString()
        {
            return "MkdirItem: (path=" + path + ", logMessage=" + logMessage + ")";
        }
    }

    private static final class MockSVNRepositoryActions implements ISVNActions
    {
        final Map<String, List<String>> listMap;

        final Map<String, String> catMap;

        List<MkdirItem> mkdirList = new ArrayList<MkdirItem>();

        List<CopyItem> copyList = new ArrayList<CopyItem>();

        public MockSVNRepositoryActions(final Map<String, List<String>> listMap, final Map<String, String> catMap)
        {
            super();
            this.listMap = listMap;
            this.catMap = catMap;
        }

        public String cat(String path) throws SVNException
        {
            System.out.println("CAT - " + path);
            final String result = catMap.get(path);
            return result == null ? "" : result;
        }

        public List<String> list(String path) throws SVNException
        {
            System.out.println("LIST - " + path);
            List<String> result = listMap.get(path);
            assert result != null : "Path '" + path + "' does not exist.";
            return result;
        }

        public void mkdir(String path, String logMessage) throws SVNException
        {
            mkdirList.add(new MkdirItem(path, logMessage));
        }

        public void copy(String sourcePath, String sourceRevision, String destinationPath, String logMessage)
                throws SVNException
        {
            copyList.add(new CopyItem(sourcePath, sourceRevision, destinationPath, logMessage));
        }

        public SVNInfoRecord info(String pathOrUrl)
        {
            throw new AssertionError("Unexpected call info()");
        }

        public List<SVNItemStatus> status(String path)
        {
            throw new AssertionError("Unexpected call status()");
        }

        public boolean isMuccAvailable()
        {
            return false;
        }

        public void mucc(String logMessage, String... args) throws SVNException
        {
            throw new AssertionError();
        }
    }

    @Test
    public void testCreateReleaseBranch()
    {
        final String repositoryRoot = "http://host/repos";
        final String groupName = "group";
        final String projectName = "testProject";
        final String branchName = "1.2.x";
        final Map<String, List<String>> listMap = new HashMap<String, List<String>>();
        final String sourceUrl =
                StringUtilities.concatenate(Arrays.asList(repositoryRoot, groupName, projectName, "trunk"), "/");
        final String releaseBranchesUrl =
                StringUtilities.concatenate(Arrays.asList(repositoryRoot, groupName, projectName, "branches/release"),
                        "/");
        listMap.put(sourceUrl, Collections.<String> emptyList());
        listMap.put(releaseBranchesUrl, Collections.<String> emptyList());
        final Map<String, String> catMap = new HashMap<String, String>();
        final MockSVNRepositoryActions svn = new MockSVNRepositoryActions(listMap, catMap);
        final SVNBranchAndTagTask task = new SVNBranchAndTagTask()
            {
                @Override
                ISVNActions createSVNActions()
                {
                    return svn;
                }
            };
        task.setProject(new Project()); // Required for log not to throw a NPE.
        task.setRepositoryRoot(repositoryRoot);
        task.setGroup(groupName);
        task.setName(projectName);
        task.setReleaseBranch(branchName);
        task.execute();
        final String branchUrl =
                StringUtilities.concatenate(Arrays.asList(repositoryRoot, groupName, projectName, "branches/release",
                        branchName), "/");
        final String tagBaseUrl =
            StringUtilities.concatenate(Arrays.asList(repositoryRoot, groupName, projectName, "tags/release",
                    branchName), "/");
        final String logMessage = "Create branch '" + branchName + "'";
        final List<MkdirItem> expectedMkDirList =
                Arrays.asList(new MkdirItem(branchUrl, logMessage), new MkdirItem(tagBaseUrl, logMessage));
        assertEquals(expectedMkDirList, svn.mkdirList);
        final String branchMainUrl = branchUrl + "/" + projectName;
        final String branchBuildResourcesUrl = branchUrl + "/" + SVNUtilities.BUILD_RESOURCES_PROJECT;
        final String sourceBuildResourcesUrl =
                StringUtilities.concatenate(Arrays.asList(repositoryRoot, groupName,
                        SVNUtilities.BUILD_RESOURCES_PROJECT, "trunk"), "/");
        final Set<CopyItem> expectedCopySet =
                new HashSet<CopyItem>(Arrays.asList(new CopyItem(sourceBuildResourcesUrl, SVNUtilities.HEAD_REVISION,
                        branchBuildResourcesUrl, logMessage), new CopyItem(sourceUrl, SVNUtilities.HEAD_REVISION,
                        branchMainUrl, logMessage)));
        assertEquals(expectedCopySet, new HashSet<CopyItem>(svn.copyList));
    }

    @Test
    public void testCreateFeatureBranchWithDependency()
    {
        final String repositoryRoot = "http://host/repos";
        final String groupName = "group";
        final String projectName = "testProject";
        final String dependentProjectName = "someDependentProject";
        final String branchName = "someFeature";
        final Map<String, List<String>> listMap = new HashMap<String, List<String>>();
        final String sourceUrl =
                StringUtilities.concatenate(Arrays.asList(repositoryRoot, groupName, projectName, "trunk"), "/");
        final String sourceUrlDependent =
                StringUtilities.concatenate(Arrays.asList(repositoryRoot, groupName, dependentProjectName, "trunk"),
                        "/");
        final String featureBranchesUrl =
                StringUtilities.concatenate(Arrays.asList(repositoryRoot, groupName, projectName, "branches/feature"),
                        "/");
        listMap.put(sourceUrl, Collections.singletonList(".classpath"));
        listMap.put(sourceUrlDependent, Collections.<String> emptyList());
        listMap.put(featureBranchesUrl, Collections.<String> emptyList());
        final String classPathFileContent =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                        + "<classpath>\n"
                        + String.format(
                                "   <classpathentry combineaccessrules=\"false\" kind=\"src\" path=\"/%s\"/>\n",
                                dependentProjectName) + "</classpath>\n";
        final Map<String, String> catMap = new HashMap<String, String>();
        catMap.put(sourceUrl + "/.classpath", classPathFileContent);
        final MockSVNRepositoryActions svn = new MockSVNRepositoryActions(listMap, catMap);
        final SVNBranchAndTagTask task = new SVNBranchAndTagTask()
            {
                @Override
                ISVNActions createSVNActions()
                {
                    return svn;
                }
            };
        task.setProject(new Project()); // Required for log not to throw a NPE.
        task.setRepositoryRoot(repositoryRoot);
        task.setGroup(groupName);
        task.setName(projectName);
        task.setFeatureBranch(branchName);
        task.execute();
        final String branchUrl =
                StringUtilities.concatenate(Arrays.asList(repositoryRoot, groupName, projectName, "branches/feature",
                        branchName), "/");
        final String logMessage = "Create branch '" + branchName + "'";
        final List<MkdirItem> expectedMkDirList = Arrays.asList(new MkdirItem(branchUrl, logMessage));
        assertEquals(expectedMkDirList, svn.mkdirList);
        final String branchMainUrl = branchUrl + "/" + projectName;
        final String branchBuildResourcesUrl = branchUrl + "/" + SVNUtilities.BUILD_RESOURCES_PROJECT;
        final String branchDependendUrl = branchUrl + "/" + dependentProjectName;
        final String sourceBuildResourcesUrl =
                StringUtilities.concatenate(Arrays.asList(repositoryRoot, groupName,
                        SVNUtilities.BUILD_RESOURCES_PROJECT, "trunk"), "/");
        final Set<CopyItem> expectedCopySet =
                new HashSet<CopyItem>(Arrays.asList(new CopyItem(sourceBuildResourcesUrl, SVNUtilities.HEAD_REVISION,
                        branchBuildResourcesUrl, logMessage), new CopyItem(sourceUrl, SVNUtilities.HEAD_REVISION,
                        branchMainUrl, logMessage), new CopyItem(sourceUrlDependent, SVNUtilities.HEAD_REVISION,
                        branchDependendUrl, logMessage)));
        assertEquals(expectedCopySet, new HashSet<CopyItem>(svn.copyList));
    }

    @Test
    public void testCreateReleaseTag()
    {
        final String repositoryRoot = "http://host/repos";
        final String groupName = "group";
        final String projectName = "testProject";
        final String tagName = "1.2.2";
        final String branchName = "1.2.x";
        final Map<String, List<String>> listMap = new HashMap<String, List<String>>();
        final String releaseBranchesUrl =
                StringUtilities.concatenate(Arrays.asList(repositoryRoot, groupName, projectName, "branches/release"),
                        "/");
        listMap.put(releaseBranchesUrl, Collections.singletonList(branchName + "/"));
        final Map<String, String> catMap = new HashMap<String, String>();
        final MockSVNRepositoryActions svn = new MockSVNRepositoryActions(listMap, catMap);
        final SVNBranchAndTagTask task = new SVNBranchAndTagTask()
            {
                @Override
                ISVNActions createSVNActions()
                {
                    return svn;
                }
            };
        task.setProject(new Project()); // Required for log not to throw a NPE.
        task.setRepositoryRoot(repositoryRoot);
        task.setGroup(groupName);
        task.setName(projectName);
        task.setReleaseTag(tagName);
        task.execute();
        final String tagUrl =
                StringUtilities.concatenate(Arrays.asList(repositoryRoot, groupName, projectName, "tags/release",
                        branchName, tagName), "/");
        final String branchUrl =
                StringUtilities.concatenate(Arrays.asList(repositoryRoot, groupName, projectName, "branches/release",
                        branchName), "/");
        final String logMessage = "Create tag '" + tagName + "'";
        assertEquals(0, svn.mkdirList.size());
        final Set<CopyItem> expectedCopySet =
                new HashSet<CopyItem>(Collections.singletonList(new CopyItem(branchUrl, SVNUtilities.HEAD_REVISION,
                        tagUrl, logMessage)));
        assertEquals(expectedCopySet, new HashSet<CopyItem>(svn.copyList));
    }

    @Test(expectedExceptions =
        { BuildException.class })
    public void testCreateReleaseTagWithBranchMissing()
    {
        final String repositoryRoot = "http://host/repos";
        final String groupName = "group";
        final String projectName = "testProject";
        final String tagName = "1.2.0";
        final Map<String, List<String>> listMap = new HashMap<String, List<String>>();
        final String releaseBranchesUrl =
                StringUtilities.concatenate(Arrays.asList(repositoryRoot, groupName, projectName, "branches/release"),
                        "/");
        listMap.put(releaseBranchesUrl, Collections.<String> emptyList());
        final Map<String, String> catMap = new HashMap<String, String>();
        final MockSVNRepositoryActions svn = new MockSVNRepositoryActions(listMap, catMap);
        final SVNBranchAndTagTask task = new SVNBranchAndTagTask()
            {
                @Override
                ISVNActions createSVNActions()
                {
                    return svn;
                }
            };
        task.setProject(new Project()); // Required for log not to throw a NPE.
        task.setRepositoryRoot(repositoryRoot);
        task.setGroup(groupName);
        task.setName(projectName);
        task.setReleaseTag(tagName);
        task.execute();
    }

    @Test
    public void testCreateReleaseTagWithBranchCreation()
    {
        final String repositoryRoot = "http://host/repos";
        final String groupName = "group";
        final String projectName = "testProject";
        final String tagName = "1.2.0";
        final String branchName = "1.2.x";
        final Map<String, List<String>> listMap = new HashMap<String, List<String>>();
        final String sourceUrl =
                StringUtilities.concatenate(Arrays.asList(repositoryRoot, groupName, projectName, "trunk"), "/");
        final String releaseBranchesUrl =
                StringUtilities.concatenate(Arrays.asList(repositoryRoot, groupName, projectName, "branches/release"),
                        "/");
        listMap.put(releaseBranchesUrl, Collections.<String> emptyList());
        final Map<String, String> catMap = new HashMap<String, String>();
        final MockSVNRepositoryActions svn = new MockSVNRepositoryActions(listMap, catMap);
        final SVNBranchAndTagTask task = new SVNBranchAndTagTask()
            {
                @Override
                ISVNActions createSVNActions()
                {
                    return svn;
                }
            };
        task.setProject(new Project()); // Required for log not to throw a NPE.
        task.setRepositoryRoot(repositoryRoot);
        task.setGroup(groupName);
        task.setName(projectName);
        task.setReleaseTag(tagName);
        task.setBranchIfNecessary(true);
        task.execute();
        final String tagBaseUrl =
            StringUtilities.concatenate(Arrays.asList(repositoryRoot, groupName, projectName, "tags/release",
                    branchName), "/");
        final String tagUrl = tagBaseUrl + "/" + tagName;
        final String branchUrl =
                StringUtilities.concatenate(Arrays.asList(repositoryRoot, groupName, projectName, "branches/release",
                        branchName), "/");
        final String logMessageBranch = "Create branch '" + branchName + "'";
        final String logMessageTag = "Create tag '" + tagName + "'";
        final List<MkdirItem> expectedMkDirList =
            Arrays.asList(new MkdirItem(branchUrl, logMessageBranch), new MkdirItem(tagBaseUrl, logMessageBranch));
        assertEquals(expectedMkDirList, svn.mkdirList);
        final String branchMainUrl = branchUrl + "/" + projectName;
        final String branchBuildResourcesUrl = branchUrl + "/" + SVNUtilities.BUILD_RESOURCES_PROJECT;
        final String sourceBuildResourcesUrl =
                StringUtilities.concatenate(Arrays.asList(repositoryRoot, groupName,
                        SVNUtilities.BUILD_RESOURCES_PROJECT, "trunk"), "/");
        final List<CopyItem> expectedCopyList =
                Arrays.asList(new CopyItem(sourceUrl, SVNUtilities.HEAD_REVISION, branchMainUrl, logMessageBranch),
                        new CopyItem(sourceBuildResourcesUrl, SVNUtilities.HEAD_REVISION, branchBuildResourcesUrl,
                                logMessageBranch), new CopyItem(branchUrl, SVNUtilities.HEAD_REVISION, tagUrl,
                                logMessageTag));
        assertEquals(expectedCopyList, svn.copyList);
    }

    @Test(expectedExceptions = { BuildException.class })
    public void testCreateReleaseTagWithBranchCreationButIllegalTag()
    {
        final String repositoryRoot = "http://host/repos";
        final String groupName = "group";
        final String projectName = "testProject";
        final String tagName = "1.2.1";
        final Map<String, List<String>> listMap = new HashMap<String, List<String>>();
        final String releaseBranchesUrl =
                StringUtilities.concatenate(Arrays.asList(repositoryRoot, groupName, projectName, "branches/release"),
                        "/");
        listMap.put(releaseBranchesUrl, Collections.<String> emptyList());
        final Map<String, String> catMap = new HashMap<String, String>();
        final MockSVNRepositoryActions svn = new MockSVNRepositoryActions(listMap, catMap);
        final SVNBranchAndTagTask task = new SVNBranchAndTagTask()
            {
                @Override
                ISVNActions createSVNActions()
                {
                    return svn;
                }
            };
        task.setProject(new Project()); // Required for log not to throw a NPE.
        task.setRepositoryRoot(repositoryRoot);
        task.setGroup(groupName);
        task.setName(projectName);
        task.setReleaseTag(tagName);
        task.setBranchIfNecessary(true);
        task.execute();
    }
}
