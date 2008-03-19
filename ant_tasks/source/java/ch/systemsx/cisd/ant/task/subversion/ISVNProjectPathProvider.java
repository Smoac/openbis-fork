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

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * A provider for subversion project paths, either in a repository or a working copy. The provider is based on a project
 * context, that is the name, the version and the revision of a project.
 * 
 * @author Bernd Rinn
 */
interface ISVNProjectPathProvider
{

    /**
     * @return The absolute path of the project ofthe context of this path provider.
     */
    public String getPath();

    /**
     * @param subProjectName The name of the sub project (interpreted in the context of this path provider). Must not
     *            contain any path delimiter.
     * @return The absolute path of the <var>subProjectName</var> in the context of this path provider.
     * @throws UserFailureException If <var>subProjectName</var> is invalid.
     */
    public String getPath(String subProjectName) throws UserFailureException;

    /**
     * @param subProjectName The name of the sub project (interpreted in the context of this path provider). Must not
     *            contain any path delimiter.
     * @param entityPath The path of the entity relative to the context of this path provider. If it contains path
     *            delimiters, the delimiters will be converted as needed.
     * @return The absolute path of the <var>entityPath</var> for the <var>subProjectName</var> in the context of this
     *         path provider.
     * @throws UserFailureException If either the <var>subProjectName</var> or the <var>entityPath</var> are invalid.
     */
    public String getPath(String subProjectName, String entityPath) throws UserFailureException;

    /**
     * @return The name of the project that is path provider's context is based on.
     */
    public String getProjectName();

    /**
     * @return The revision of the project that is path provider's context is based on.
     */
    public String getRevision();

    /**
     * @return <code>true</code> if this provider represents a subversion repository path (rather than a working copy
     *         path).
     */
    public boolean isRepositoryPath();

}