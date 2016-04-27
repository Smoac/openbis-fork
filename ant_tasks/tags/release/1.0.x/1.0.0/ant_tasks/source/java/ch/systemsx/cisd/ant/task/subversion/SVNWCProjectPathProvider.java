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

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * A {@link ISVNProjectPathProvider} that works on a subversion working directory.
 *
 * @author Bernd Rinn
 */
class SVNWCProjectPathProvider implements ISVNProjectPathProvider
{

    private final String baseDirectory;
    private final String projectName;
    
    /**
     * @param projectDirectory The directory of the working copy. Is expected to exist and to be a directory.
     */
    public SVNWCProjectPathProvider(File projectDirectory) throws UserFailureException
    {
        assert projectDirectory != null;
        assert projectDirectory.isDirectory();
        
        final String baseDirectoryPath = projectDirectory.getParentFile().getAbsolutePath();
        final String thisProjectName = projectDirectory.getName();
        if (baseDirectoryPath.endsWith("/.")) // Corresponds to baseDirectory = new File(".")
        {
            this.baseDirectory = baseDirectoryPath.substring(0, baseDirectoryPath.length() - 2);
        } else
        {
            this.baseDirectory = baseDirectoryPath;
        }
        this.projectName = thisProjectName;
    }
    
    public String getPath()
    {
        return getPath(projectName);
    }

    public String getPath(String subProjectName) throws UserFailureException
    {
        assert subProjectName != null;

        SVNUtilities.checkProjectName(subProjectName);
        
        return new File(baseDirectory, subProjectName).getAbsolutePath();
    }

    public String getPath(String subProjectName, String entityPath) throws UserFailureException
    {
        assert subProjectName != null;
        assert entityPath != null;
        
        if (entityPath.length() == 0)
        {
            throw new UserFailureException("Entity path must not be empty.");
        }

        final String normalizedEntityPath = normalize(entityPath);
        return new File(getPath(subProjectName), normalizedEntityPath).getAbsolutePath();
    }

    private String normalize(String entityPath)
    {
        if ('/' == File.separatorChar)
        {
            return entityPath.replace('\\', '/');
        } else {
            return entityPath.replace('/', '\\');
        }
    }

    public String getProjectName()
    {
        return projectName;
    }

    /**
     * @return "BASE"
     */
    public String getRevision()
    {
        return "BASE";
    }

    public boolean isRepositoryPath()
    {
        return false;
    }
    
}
