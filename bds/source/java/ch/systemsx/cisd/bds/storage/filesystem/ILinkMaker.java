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

package ch.systemsx.cisd.bds.storage.filesystem;

import java.io.File;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;

/**
 * Kind of strategy how to 'serialize' a <code>ILink</code> implementation.
 * 
 * @author Christian Ribeaud
 */
public interface ILinkMaker
{

    /**
     * An default <code>ILinkMaker</code> implementation which always returns <code>null</code> without trying to do
     * anything.
     */
    public final static ILinkMaker DEFAULT = new ILinkMaker()
        {

            //
            // ILinkMaker
            //

            public final File tryCreateLink(final File file, final File destDir, final String nameOrNull)
                    throws EnvironmentFailureException
            {
                return null;
            }
        };

    /**
     * Tries to create a link to given <var>file</var> in given <var>destDir</var>.
     * 
     * @param nameOrNull the link name in the destination directory.
     * @return might returns <code>null</code> if the application was not able to create the link.
     */
    public File tryCreateLink(final File file, final File destDir, final String nameOrNull)
            throws EnvironmentFailureException;
}
