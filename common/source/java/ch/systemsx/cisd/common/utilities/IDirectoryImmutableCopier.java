/*
 * Copyright 2008 ETH Zuerich, CISD
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
 * A role which can perform an immutable copy of a direcoty. <i>Immutable</i> here means, that none
 * of the copied file must be changed or else the corresponding original file may be changed, tool
 * 
 * @author Bernd Rinn
 */
public interface IDirectoryImmutableCopier
{
    /**
     * Creates a copy of the directory <code>file</code> (which may be a file or a directory) in
     * <code>destinationDirectory</code>, which must not be modified later.
     * <p>
     * Note that this method don't do any checks about whether paths are directories and whether
     * they exist or not. Use methods like
     * {@link FileUtilities#checkDirectoryFullyAccessible(File, String)} for checking prior to
     * calling this method where appropriate.
     * </p>
     * <p>
     * <i>Can use hard links if available.</i>
     * </p>
     * 
     * @param sourceDirectory The source directory. Really has to be a directory. Can not be
     *            <code>null</code> and needs to exists.
     * @param destinationDirectory The directory to copy <var>sourceDirectory</var> to. Can not be
     *            <code>null</code> and must be an existing directory.
     * @param targetNameOrNull The target name of the source directory. If <code>null</code>, the
     *            name of the source directory itself is used.
     * @return <code>true</code> if the operation was successful, <code>false</code> otherwise.
     */
    boolean copyDirectoryImmutably(final File sourceDirectory, final File destinationDirectory,
            String targetNameOrNull);

}
