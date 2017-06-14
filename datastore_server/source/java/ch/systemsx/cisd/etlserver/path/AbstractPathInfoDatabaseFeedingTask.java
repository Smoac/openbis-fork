/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.systemsx.cisd.etlserver.path;

import java.io.File;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.openbis.common.io.hierarchical_content.IHierarchicalContentFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
abstract class AbstractPathInfoDatabaseFeedingTask
{
    static final String COMPUTE_CHECKSUM_KEY = "compute-checksum";
    
    static final String CHECKSUM_TYPE_KEY = "checksum-type";

    protected IDataSetDirectoryProvider directoryProvider;

    protected IPathsInfoDAO dao;

    protected IHierarchicalContentFactory hierarchicalContentFactory; // filesystem based

    protected boolean computeChecksum;
    
    protected String checksumType;

    protected void feedPathInfoDatabase(IDatasetLocation dataSet)
    {
        IShareIdManager shareIdManager = directoryProvider.getShareIdManager();
        String dataSetCode = dataSet.getDataSetCode();
        shareIdManager.lock(dataSetCode);

        try
        {
            File dataSetRoot = directoryProvider.getDataSetDirectory(dataSet);
            if (dataSetRoot.exists() == false)
            {
                getOperationLog().error("Root directory of data set " + dataSetCode
                        + " does not exists: " + dataSetRoot);
                shareIdManager.releaseLocks();
                return;
            }
            DatabaseBasedDataSetPathsInfoFeeder feeder =
                    new DatabaseBasedDataSetPathsInfoFeeder(dao, hierarchicalContentFactory,
                            computeChecksum, checksumType);
            Long id = dao.tryGetDataSetId(dataSetCode);
            if (id == null)
            {
                feeder.addPaths(dataSetCode, dataSet.getDataSetLocation(), dataSetRoot);
                feeder.commit();
                getOperationLog().info("Paths inside data set " + dataSetCode
                        + " successfully added to database.");
            }
        } catch (Exception ex)
        {
            getOperationLog().error("Couldn't feed database with path infos of data set " + dataSetCode, ex);
            dao.rollback();
        } finally
        {
            shareIdManager.releaseLocks();
        }
    }

    protected abstract Logger getOperationLog();

}
