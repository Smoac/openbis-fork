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

package ch.systemsx.cisd.common.utilities;

import java.io.File;

/**
 * Utility to create copies of files/resources, which should not be modified later.
 * 
 * @author Tomasz Pylak
 */
public interface IPathImmutableCopier
{
    /**
     * Creates a copy of <code>path</code> (which may be a file or a directory) in <code>destinationDirectory</code>,
     * which must not be modified later.
     * <p>
     * <i>Can use hard links if available.</i>
     * </p>
     * 
     * @param path the source path. Can be a file or a directory. Can not be <code>null</code>.
     * @param destinationDirectory the directory where given <var>path</var> should be copied. Can not be
     *            <code>null</code> and must be an existing directory.
     * @param nameOrNull the link name in the destination directory.
     * @return the new path created, or <code>null</code> if the operation fails.
     */
    File tryCopy(final File path, final File destinationDirectory, final String nameOrNull);
}
