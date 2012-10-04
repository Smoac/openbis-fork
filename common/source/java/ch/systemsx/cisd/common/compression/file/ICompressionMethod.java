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

package ch.systemsx.cisd.common.compression.file;

import java.io.File;
import java.io.FileFilter;

import ch.systemsx.cisd.common.exception.Status;
import ch.systemsx.cisd.common.fileconverter.IFileConversionMethod;

/**
 * A role that compresses a file. A compression method may only be suitable for some files, thus it
 * is also a {@link FileFilter}.
 * 
 * @deprecated Use {@link IFileConversionMethod} instead.
 * @author Bernd Rinn
 */
@Deprecated
public interface ICompressionMethod extends FileFilter
{

    /**
     * Compress the <var>fileToCompress</var>
     * 
     * @return {@link Status#OK} if the operation was successful, a status indicating the kind of
     *         problem otherwise.
     */
    public Status compress(File fileToCompress);

}
