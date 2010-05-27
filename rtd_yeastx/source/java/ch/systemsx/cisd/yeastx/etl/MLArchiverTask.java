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

package ch.systemsx.cisd.yeastx.etl;

import java.io.File;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractArchiverProcessingPlugin;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.HighWaterMarkChecker;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.IStatusChecker;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSourceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.yeastx.db.DBUtils;
import ch.systemsx.cisd.yeastx.db.generic.IDMGenericDAO;

/**
 * Archiver that removes/adds data related to given data set from/to metabol database.
 * 
 * @author Izabela Adamczyk
 */
public class MLArchiverTask extends AbstractArchiverProcessingPlugin
{

    private static final long serialVersionUID = 1L;

    // specification of the size of the disc free space in KB which must be available to unarchive
    // one dataset
    private static final String HIGHWATER_MARK_PROPERTY_KEY = "dataset-unarchiving-highwater-mark";

    private final String dataSourceName;

    public MLArchiverTask(Properties properties, File storeRoot)
    {
        super(properties, storeRoot, null, tryCreateUnarchivingStatusChecker(properties, storeRoot));
        dataSourceName = DataSourceProvider.extractDataSourceName(properties);
        // Check if given data source exists
        getDataSource(dataSourceName);
    }

    private static IStatusChecker tryCreateUnarchivingStatusChecker(Properties properties,
            File storeRoot)
    {

        long archiveHighWaterMark =
                PropertyUtils.getLong(properties, HIGHWATER_MARK_PROPERTY_KEY, -1);
        if (archiveHighWaterMark == -1)
        {
            return null;
        }
        // NOTE: we assume that the database (which grows when unarchiving is done) is stored on the
        // same file system as the DSS store
        return new HighWaterMarkChecker(archiveHighWaterMark, storeRoot);
    }

    /**
     * Deletes data related to given data set from metabol database.
     */
    private Status doArchive(DatasetDescription dataset, IDMGenericDAO dao)
            throws UserFailureException
    {
        try
        {
            dao.deleteDataSet(dataset.getDatasetCode());
            dao.commit();
        } catch (Exception ex)
        {
            dao.rollback();
            return createErrorStatus(ex);
        }
        return Status.OK;
    }

    private static DataSource getDataSource(String dataSourceName)
    {
        return ServiceProvider.getDataSourceProvider().getDataSource(dataSourceName);
    }

    /**
     * Adds data related to given data set to metabol database.
     */
    private Status doUnarchive(DatasetDescription dataset, ML2DatabaseUploader databaseUploader)
            throws UserFailureException
    {
        try
        {
            Sample sample = null;
            if (dataset.getSampleCode() != null)
            {
                sample = fetchSample(dataset);
            }
            Experiment experiment = getOrFetchExperiment(dataset, sample);
            databaseUploader.upload(getDataFile(dataset), sample, experiment, dataset
                    .getDatasetCode());
            databaseUploader.commit();
        } catch (Exception ex)
        {
            if (databaseUploader != null)
            {
                databaseUploader.rollback();
            }
            return createErrorStatus(ex);
        }
        return Status.OK;
    }

    private Status createErrorStatus(Exception ex)
    {
        return Status.createError(ex.getMessage() == null ? "unknown reason "
                + ex.getClass().getName() : ex.getMessage());
    }

    private Sample fetchSample(DatasetDescription dataset)
    {
        Sample sample;
        // NOTE: we assume that it is not a shared sample
        SampleIdentifier sampleIdentifier =
                new SampleIdentifier(new SpaceIdentifier(dataset.getDatabaseInstanceCode(), dataset
                        .getGroupCode()), dataset.getSampleCode());
        sample = ServiceProvider.getOpenBISService().tryGetSampleWithExperiment(sampleIdentifier);
        return sample;
    }

    private Experiment getOrFetchExperiment(DatasetDescription dataset, Sample sample)
    {
        if (sample != null && sample.getExperiment() != null)
        {
            return sample.getExperiment();
        }
        ExperimentIdentifier experimentIdentifier =
                new ExperimentIdentifier(dataset.getDatabaseInstanceCode(), dataset.getGroupCode(),
                        dataset.getProjectCode(), dataset.getExperimentCode());
        Experiment experiment =
                ServiceProvider.getOpenBISService().tryToGetExperiment(experimentIdentifier);
        return experiment;
    }

    private File getDataFile(DatasetDescription dataset)
    {
        File datasetDir = getDataSubDir(dataset);
        File[] files = datasetDir.listFiles();
        if (files == null || files.length < 1)
        {
            throw new UserFailureException(
                    "Data set directory contains no files (exactly one expected)");
        } else if (files.length > 1)
        {
            throw new UserFailureException(
                    "Data set directory contains more than one file (exactly one expected)");
        } else
        {
            return files[0];
        }
    }

    @Override
    protected DatasetProcessingStatuses doArchive(List<DatasetDescription> datasets)
            throws UserFailureException
    {
        DataSource dataSource = getDataSource(dataSourceName);
        final IDMGenericDAO dao = DBUtils.getQuery(dataSource, IDMGenericDAO.class);

        try
        {
            int counter = 0;
            DatasetProcessingStatuses statuses = new DatasetProcessingStatuses();
            for (DatasetDescription dataset : datasets)
            {
                Status status = doArchive(dataset, dao);
                statuses.addResult(dataset.getDatasetCode(), status, true);
                counter++;
                if (counter % 100 == 0)
                {
                    operationLog.info("Archiving status: " + counter + "/" + datasets.size());
                }
            }
            return statuses;
        } finally
        {
            dao.close();
        }
    }

    @Override
    protected DatasetProcessingStatuses doUnarchive(List<DatasetDescription> datasets)
            throws UserFailureException
    {
        ML2DatabaseUploader databaseUploader = new ML2DatabaseUploader(properties);

        DatasetProcessingStatuses statuses = new DatasetProcessingStatuses();
        int counter = 0;
        for (DatasetDescription dataset : datasets)
        {
            Status status = doUnarchive(dataset, databaseUploader);
            statuses.addResult(dataset.getDatasetCode(), status, false);
            counter++;
            if (counter % 100 == 0)
            {
                operationLog.info("Unarchiving status: " + counter + "/" + datasets.size());
            }
        }
        return statuses;
    }
}
