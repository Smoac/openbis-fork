/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.filesystem.tar.Tar;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DataSetExistenceChecker;

/**
 * @author pkupczyk
 */
public class TarDataSetPackager extends AbstractDataSetPackager
{

    private final Tar tar;

    public TarDataSetPackager(File tarFile, IHierarchicalContentProvider contentProvider, DataSetExistenceChecker dataSetExistenceChecker)
    {
        super(contentProvider, dataSetExistenceChecker);
        try
        {
            tar = new Tar(tarFile);
        } catch (FileNotFoundException e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        }
    }

    @Override
    protected boolean isChecksumNeeded()
    {
        return false;
    }

    @Override
    public void addEntry(String entryPath, long lastModified, long size, long checksum, InputStream in)
    {
        TarArchiveEntry entry = new TarArchiveEntry(entryPath.replace('\\', '/'));
        entry.setSize(size);
        try
        {
            tar.add(entry, in);
        } catch (IOException e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        }
    }

    @Override
    public void addDirectoryEntry(String entryPath)
    {
        String path = entryPath.replace('\\', '/');
        if (path.endsWith("/") == false)
        {
            path += "/";
        }
        try
        {
            tar.add(path);
        } catch (IOException e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        }
    }

    @Override
    public void close()
    {
        try
        {
            tar.close();
        } catch (IOException e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        }
    }
}
