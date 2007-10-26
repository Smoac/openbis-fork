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

package ch.systemsx.cisd.bds;

import java.io.File;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;

/**
 * Implementations know how to compute a <a href="http://en.wikipedia.org/wiki/Checksum">checksum</a> from a file.
 * 
 * @author Christian Ribeaud
 */
public interface IChecksum
{

    /**
     * Returns the checksum for given <var>file</var>.
     * 
     * @param file could not be <code>null</code> and must exist.
     */
    public String getChecksum(final File file) throws EnvironmentFailureException;
}
