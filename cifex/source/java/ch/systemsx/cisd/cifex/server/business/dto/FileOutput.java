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

package ch.systemsx.cisd.cifex.server.business.dto;

import java.io.InputStream;

import ch.systemsx.cisd.cifex.server.business.dto.BasicFileDTO;
import ch.systemsx.cisd.common.utilities.AbstractHashable;

public final class FileOutput extends AbstractHashable
{
    /** Basic information about a file. */
    private final BasicFileDTO basicFile;

    /** To access the file content. */
    private final InputStream inputStream;

    public FileOutput(final BasicFileDTO basicFile, final InputStream inputStream)
    {
        this.basicFile = basicFile;
        this.inputStream = inputStream;
    }

    public final BasicFileDTO getBasicFile()
    {
        return basicFile;
    }

    public final InputStream getInputStream()
    {
        return inputStream;
    }
}