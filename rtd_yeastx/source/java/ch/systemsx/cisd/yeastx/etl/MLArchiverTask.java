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
import java.util.Properties;

import javax.sql.DataSource;

import net.lemnik.eodsql.QueryTool;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractArchiverProcessingPlugin;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.yeastx.db.DBUtils;
import ch.systemsx.cisd.yeastx.db.IGenericDAO;

/**
 * Archiver that removes/adds data related to given data set from/to metabol database.
 * 
 * @author Izabela Adamczyk
 */
public class MLArchiverTask extends AbstractArchiverProcessingPlugin
{

    private static final long serialVersionUID = 1L;

    public MLArchiverTask(Properties properties, File storeRoot)
    {
        super(properties, storeRoot, null, null);
    }

    /**
     * Deletes data related to given data set from metabol database.
     */
    @Override
    protected void archive(DatasetDescription dataset) throws UserFailureException
    {
        final IGenericDAO dao = createQuery(properties);
        try
        {
            dao.deleteDataSet(dataset.getDatasetCode());
            dao.commit();
        } catch (Exception ex)
        {
            dao.rollback();
            throw new UserFailureException(ex.getMessage());
        } finally
        {
            dao.close();
        }
    }

    private static IGenericDAO createQuery(Properties properties)
    {
        final DatabaseConfigurationContext dbContext = DBUtils.createAndInitDBContext(properties);
        DataSource dataSource = dbContext.getDataSource();
        return QueryTool.getQuery(dataSource, IGenericDAO.class);
    }

    /**
     * Adds data related to given data set to metabol database.
     */
    @Override
    protected void unarchive(DatasetDescription dataset) throws UserFailureException
    {
        try
        {
            Sample sample = null;
            if (dataset.getSampleCode() != null)
            {
                SampleIdentifier sampleIdentifier =
                        new SampleIdentifier(new SpaceIdentifier(dataset.getDatabaseInstanceCode(),
                                dataset.getGroupCode()), dataset.getSampleCode());
                sample =
                        ServiceProvider.getOpenBISService().tryGetSampleWithExperiment(
                                sampleIdentifier);
            }
            ExperimentIdentifier experimentIdentifier =
                    new ExperimentIdentifier(dataset.getDatabaseInstanceCode(), dataset
                            .getGroupCode(), dataset.getProjectCode(), dataset.getExperimentCode());
            Experiment experiment =
                    ServiceProvider.getOpenBISService().tryToGetExperiment(experimentIdentifier);
            ML2DatabaseUploader databaseUploader = new ML2DatabaseUploader(properties);
            databaseUploader.upload(getDataFile(dataset), sample, experiment, dataset
                    .getDatasetCode());
            databaseUploader.commit();
        } catch (Exception ex)
        {
            throw new UserFailureException(ex.getMessage());
        }
    }

    private File getDataFile(DatasetDescription dataset)
    {
        File datasetDir = getDataSubDir(dataset);
        File[] files = datasetDir.listFiles();
        if (files.length < 1)
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
}
