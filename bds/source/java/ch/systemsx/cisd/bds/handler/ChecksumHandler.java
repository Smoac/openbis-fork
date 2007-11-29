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

package ch.systemsx.cisd.bds.handler;

import ch.systemsx.cisd.bds.DataStructureException;
import ch.systemsx.cisd.bds.DataStructureV1_0;
import ch.systemsx.cisd.bds.IDataStructureHandler;
import ch.systemsx.cisd.bds.storage.IDirectory;

/**
 * A <code>IDataStructureHandler</code> implementation for the <code>md5sum</code> directory.
 * 
 * @author Christian Ribeaud
 */
public final class ChecksumHandler implements IDataStructureHandler
{

    private final ChecksumBuilder checksumBuilder = new ChecksumBuilder(new MD5ChecksumCalculator());

    private final IDirectory checksumDirectory;

    private final IDirectory dataDirectory;

    public static final String CHECKSUM_DIRECTORY = "md5sum";

    public ChecksumHandler(final IDirectory checksumDirectory, final IDirectory dataDirectory)
    {
        this.checksumDirectory = checksumDirectory;
        this.dataDirectory = dataDirectory;
    }

    //
    // IDataStructureHandler
    //

    public final void assertValid() throws DataStructureException
    {
        // TODO 2007-11-29, Christian Ribeaud: validation of loaded checksums.
    }

    public final void performClosing()
    {
        final String checksumsOfOriginal = checksumBuilder.buildChecksumsForAllFilesIn(dataDirectory);
        checksumDirectory.addKeyValuePair(DataStructureV1_0.DIR_ORIGINAL, checksumsOfOriginal);
    }

    public final void performOpening()
    {
    }

}
