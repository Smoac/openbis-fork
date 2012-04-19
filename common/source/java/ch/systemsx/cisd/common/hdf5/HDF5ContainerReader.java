/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.hdf5;

import java.io.File;
import java.io.OutputStream;
import java.util.List;

import ch.systemsx.cisd.hdf5.h5ar.ArchiveEntry;
import ch.systemsx.cisd.hdf5.h5ar.HDF5ArchiverFactory;
import ch.systemsx.cisd.hdf5.h5ar.IHDF5ArchiveReader;
import ch.systemsx.cisd.hdf5.h5ar.ListParameters;

/**
 * An implementation of {@link IHDF5ContainerReader}.
 * 
 * @author Bernd Rinn
 */
final class HDF5ContainerReader implements IHDF5ContainerReader
{
    private final IHDF5ArchiveReader archiveReader;

    HDF5ContainerReader(final File hdf5Container)
    {
        this.archiveReader = HDF5ArchiverFactory.openForReading(hdf5Container);
    }

    public void close()
    {
        archiveReader.close();
    }

    public boolean exists(String objectPath)
    {
        return archiveReader.exists(objectPath);
    }

    public boolean isGroup(String objectPath)
    {
        return archiveReader.isDirectory(objectPath);
    }

    public List<ArchiveEntry> getGroupMembers(String groupPath)
    {
        return archiveReader.list(groupPath, ListParameters.build().nonRecursive()
                .noReadLinkTarget().get());
    }

    public void readFromHDF5Container(String objectPath, OutputStream ostream)
    {
        archiveReader.extractFile(objectPath, ostream);
  }
}