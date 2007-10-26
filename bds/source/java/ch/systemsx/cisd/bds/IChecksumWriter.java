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
import java.io.Writer;

/**
 * Implementations write computed checksums to some specified {@link Writer}.
 * 
 * @author Christian Ribeaud
 */
public interface IChecksumWriter
{

    /**
     * Appends computed checksum for given <var>file</var> to given <var>writer</var>.
     * <p>
     * If given <var>file</var> is a directory, then recursively calls this method for each file composing the
     * directory.
     * </p>
     * 
     * @param file can not be <code>null</code> and must exist.
     * @param writer can not be <code>null</code>.
     */
    public void writeChecksum(final File file, final Writer writer);

}