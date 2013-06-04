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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.BooleanStatus;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.time.TimingParameters;
import ch.systemsx.cisd.openbis.dss.generic.server.AbstractDataSetPackager;
import ch.systemsx.cisd.openbis.dss.generic.server.ZipDataSetPackager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ArchiverTaskContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DataSetExistenceChecker;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class DistributedArchiver extends AbstractArchiverProcessingPlugin
{
    private static final long serialVersionUID = 1L;
    
    private final boolean compress;
    
    private File destination;
    
    public DistributedArchiver(Properties properties, File storeRoot)
    {
        super(properties, storeRoot, null, null);
        destination = new File(properties.getProperty("destination"));
        compress = PropertyUtils.getBoolean(properties, "compress", true);
        destination.mkdirs();
    }
    
    @Override
    protected DatasetProcessingStatuses doArchive(List<DatasetDescription> datasets, ArchiverTaskContext context)
    {
        List<AbstractExternalData> dataSets = getDataSetMetaData(datasets);
        IHierarchicalContentProvider contentProvider = context.getHierarchicalContentProvider();
        IDataSetDirectoryProvider directoryProvider = context.getDirectoryProvider();
        IShareIdManager shareIdManager = directoryProvider.getShareIdManager();
        DataSetExistenceChecker dataSetExistenceChecker =
                new DataSetExistenceChecker(directoryProvider, TimingParameters.create(new Properties()));
        DatasetProcessingStatuses statuses = new DatasetProcessingStatuses();
        for (AbstractExternalData dataSet : dataSets)
        {
            Status status = Status.OK;
            String dataSetCode = dataSet.getCode();
            File file = new File(getArchive(dataSet), dataSetCode + ".zip");
            shareIdManager.lock(dataSetCode);
            AbstractDataSetPackager dataSetPackager = null;
            try
            {
                dataSetPackager = createPackager(file, contentProvider, dataSetExistenceChecker);
                dataSetPackager.addDataSetTo("", dataSet);
            } catch (Exception ex)
            {
                status = Status.createError(ex.toString());
                operationLog.error("Couldn't create package file: " + file, ex);
            } finally
            {
                if (dataSetPackager != null)
                {
                    try
                    {
                        dataSetPackager.close();
                    } catch (Exception ex)
                    {
                        status = Status.createError("Couldn't close package file: " + file + ": " + ex);
                    }
                }
                shareIdManager.releaseLock(dataSetCode);
                operationLog.info("Data set " + dataSetCode + " archived: " + file);
            }
            statuses.addResult(dataSetCode, status, Operation.ARCHIVE);
        }
        return statuses;
    }

    private AbstractDataSetPackager createPackager(File file, IHierarchicalContentProvider contentProvider,
            DataSetExistenceChecker dataSetExistenceChecker)
    {
        return new ZipDataSetPackager(file, compress, contentProvider, dataSetExistenceChecker);
    }

    private List<AbstractExternalData> getDataSetMetaData(List<DatasetDescription> datasets)
    {
        IEncapsulatedOpenBISService service = getService();
        List<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        for (DatasetDescription datasetDescription : datasets)
        {
            AbstractExternalData dataSet = service.tryGetDataSet(datasetDescription.getDataSetCode());
            String experimentIdentifier = datasetDescription.getExperimentIdentifier();
            dataSet.setExperiment(service.tryGetExperiment(ExperimentIdentifierFactory.parse(experimentIdentifier)));
            String sampleIdentifier = datasetDescription.getSampleIdentifier();
            if (sampleIdentifier != null)
            {
                dataSet.setSample(service.tryGetSampleWithExperiment(SampleIdentifierFactory.parse(sampleIdentifier)));
            }
            dataSets.add(dataSet);
        }
        return dataSets;
    }
    
    private File getArchive(AbstractExternalData dataSet)
    {
        return destination;
    }

    @Override
    protected DatasetProcessingStatuses doUnarchive(List<DatasetDescription> datasets, ArchiverTaskContext context)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected DatasetProcessingStatuses doDeleteFromArchive(List<? extends IDatasetLocation> datasets)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected BooleanStatus isDataSetSynchronizedWithArchive(DatasetDescription dataset, ArchiverTaskContext context)
    {
        return BooleanStatus.createFalse();
    }

    @Override
    protected BooleanStatus isDataSetPresentInArchive(DatasetDescription dataset)
    {
        return BooleanStatus.createFalse();
    }
    
}
