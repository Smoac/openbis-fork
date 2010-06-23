/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.etl.bdsmigration;

import java.io.File;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import net.lemnik.eodsql.QueryTool;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.etl.AbstractHCSImageFileExtractor;
import ch.systemsx.cisd.openbis.dss.etl.PlateStorageProcessor;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.ColorComponent;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.IImagingUploadDAO;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;

/**
 * @author Franz-Josef Elmer
 */
public class BDSImagingDatabaseMigrator extends AbstractBDSMigrator
{
    private IImagingUploadDAO dao;

    private List<String> channelNames;

    private List<ColorComponent> channelColorComponentsOrNull;

    private static File tryGetOriginalDir(File dataset)
    {
        File orgDir = new File(dataset, DATA_DIR + DIR_SEP + ORIGINAL_DIR);
        if (orgDir.isDirectory() == false)
        {
            return null;
        }
        return orgDir;
    }

    private static String tryGetOriginalDatasetDirName(File dataset)
    {
        File originalDir = tryGetOriginalDir(dataset);
        if (originalDir == null)
        {
            BDSMigrationMaintananceTask.logError(dataset, "Original directory does not exist.");
            return null;
        }
        File[] files = originalDir.listFiles();
        if (files.length != 1)
        {
            BDSMigrationMaintananceTask.logError(dataset, "Original directory '" + originalDir
                    + "' should contain exactly one file, but contains " + files.length + ": "
                    + files);
            return null;
        }
        return files[0].getName();
    }

    public BDSImagingDatabaseMigrator(Properties properties)
    {
        DataSource dataSource = ServiceProvider.getDataSourceProvider().getDataSource(properties);
        dao = QueryTool.getQuery(dataSource, IImagingUploadDAO.class);
        channelNames =
                PropertyUtils.getMandatoryList(properties, PlateStorageProcessor.CHANNEL_NAMES);
        channelColorComponentsOrNull =
                AbstractHCSImageFileExtractor.tryGetChannelComponents(properties);
        if (channelColorComponentsOrNull != null
                && channelColorComponentsOrNull.size() != channelNames.size())
        {
            throw new ConfigurationFailureException(
                    "There should be exactly one color component for each channel name."
                            + " Correct the list of values for the components property.");
        }
    }

    public String getDescription()
    {
        return "uploading data to the imaging database";
    }

    @Override
    protected boolean doMigration(File dataset)
    {
        String originalDatasetDirName = tryGetOriginalDatasetDirName(dataset);
        if (originalDatasetDirName == null)
        {
            return false;
        }
        return new BDSImagingDbUploader(dataset, dao, originalDatasetDirName, channelNames,
                channelColorComponentsOrNull).migrate();
    }

    @Override
    public void close()
    {
        // close the dao
        dao.close();
    }

}
