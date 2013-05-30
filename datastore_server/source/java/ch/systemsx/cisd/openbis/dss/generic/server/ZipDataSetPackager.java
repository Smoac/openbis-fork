/*
 * Copyright 2013 ETH Zuerich, CISD
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

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DataSetExistenceChecker;

import de.schlichtherle.util.zip.ZipEntry;
import de.schlichtherle.util.zip.ZipOutputStream;

/**
 * Packager based on a {@link java.util.zip.ZipOutputStream}.
 *
 * @author Franz-Josef Elmer
 */
public class ZipDataSetPackager extends AbstractDataSetPackager
{
    private final ZipOutputStream zipOutputStream;
    private final boolean compress;

    protected ZipDataSetPackager(ZipOutputStream zipOutputStream, boolean compress, ISimpleLogger logger, 
            IHierarchicalContentProvider contentProvider, DataSetExistenceChecker dataSetExistenceChecker)
    {
        super(logger, contentProvider, dataSetExistenceChecker);
        this.zipOutputStream = zipOutputStream;
        this.compress = compress;
    }

    @Override
    public void addEntry(String entryPath, long lastModified, InputStream in)
    {
        try
        {
            ZipEntry zipEntry = new ZipEntry(entryPath.replace('\\', '/'));
            zipEntry.setTime(lastModified);
            zipEntry.setMethod(compress ? ZipEntry.DEFLATED : ZipEntry.STORED);
            zipOutputStream.putNextEntry(zipEntry);
            int len;
            byte[] buffer = new byte[1024];
            while ((len = in.read(buffer)) > 0)
            {
                zipOutputStream.write(buffer, 0, len);
            }
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            IOUtils.closeQuietly(in);
            try
            {
                zipOutputStream.closeEntry();
            } catch (IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }
    }

}
