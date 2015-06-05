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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.tar.Untar;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.TarBasedHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.dss.archiveverifier.batch.VerificationError;
import ch.systemsx.cisd.openbis.dss.generic.server.AbstractDataSetPackager;
import ch.systemsx.cisd.openbis.dss.generic.server.TarDataSetPackager;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DataSetExistenceChecker;

/**
 * @author pkupczyk
 */
public class TarPackageManager extends AbstractPackageManager
{
    private static final String BUFFER_SIZE_KEY = "buffer-size";

    private static final int DEFAULT_BUFFER_SIZE = (int) (10 * FileUtils.ONE_MB);
    
    private final File tempFolder;

    private final int bufferSize;
    
    private final ISimpleLogger ioSpeedLogger;

    public TarPackageManager(Properties properties, ISimpleLogger ioSpeedLogger)
    {
        this.tempFolder = PropertyUtils.getDirectory(properties, RsyncArchiver.TEMP_FOLDER, null);
        bufferSize = PropertyUtils.getInt(properties, BUFFER_SIZE_KEY, DEFAULT_BUFFER_SIZE);
        this.ioSpeedLogger = ioSpeedLogger;
    }

    @Override
    public String getName(String dataSetCode)
    {
        return dataSetCode + ".tar";
    }

    @Override
    protected AbstractDataSetPackager createPackager(File packageFile, DataSetExistenceChecker existenceChecker)
    {
        return new TarDataSetPackager(packageFile, getContentProvider(), existenceChecker, bufferSize);
    }
    
    @Override
    public List<VerificationError> verify(File packageFile)
    {
        return Collections.emptyList();
    }

    @Override
    public Status extract(File packageFile, File toDirectory)
    {
        Untar untar = null;
        try
        {
            untar = new Untar(packageFile);
            untar.extract(toDirectory);

            File metadataFile = new File(toDirectory, AbstractDataSetPackager.META_DATA_FILE_NAME);
            if (metadataFile.exists() && metadataFile.isFile())
            {
                FileUtilities.delete(metadataFile);
            }

            return Status.OK;
        } catch (Exception ex)
        {
            return Status.createError(ex.toString());
        } finally
        {
            if (untar != null)
            {
                try
                {
                    untar.close();
                } catch (IOException ex)
                {
                    throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                }
            }
        }
    }

    @Override
    public IHierarchicalContent asHierarchialContent(File packageFile)
    {
        return new TarBasedHierarchicalContent(packageFile, tempFolder, bufferSize, ioSpeedLogger);
    }

}
