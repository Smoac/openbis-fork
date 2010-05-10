/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.yeastx.db;

import javax.sql.DataSource;

import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.InvalidDataTypeException;
import net.lemnik.eodsql.InvalidQueryException;
import net.lemnik.eodsql.QueryTool;
import net.lemnik.eodsql.TransactionQuery;

/**
 * Database utilities.
 * 
 * @author Bernd Rinn
 */
public class DBUtils
{

    static
    {
        QueryTool.getTypeMap().put(float[].class, new FloatArrayMapper());
    }

    /**
     * Use this method instead of {@link QueryTool#getQuery(DataSource, Class)}. Only in this way
     * you make sure that the rigth query mappers will be registered.
     */
    public static <T extends BaseQuery> T getQuery(final DataSource dataSource, final Class<T> query)
            throws InvalidDataTypeException, InvalidQueryException
    {
        return QueryTool.getQuery(dataSource, query);
    }

    /**
     * Rolls backs and closes the given <var>transactionOrNull</var>, if it is not <code>null</code>
     * .
     */
    public static void rollbackAndClose(TransactionQuery transactionOrNull)
    {
        if (transactionOrNull != null)
        {
            transactionOrNull.rollback();
            transactionOrNull.close();
        }
    }

    /**
     * Closes the given <var>transactionOrNull</var>, if it is not <code>null</code> .
     */
    public static void close(TransactionQuery transactionOrNull)
    {
        if (transactionOrNull != null)
        {
            transactionOrNull.close();
        }
    }

    /**
     * Creates the data set based on the information given in <var>dataSet</var>. The sample and
     * experiment of the data set may already exist in the database. If they don't, they are created
     * as well.<br>
     * Sets sample, experiment and dataset id fields of the specified dataset.
     * <p>
     * NOTE: Code responsible for trying to get sample and experiment from the DB and creating them
     * if they don't exist is in synchronized block and uses currently opened transaction. Then the
     * transaction is closed and data set is added to the DB in second transaction. If second
     * transaction will be rolled back sample and experiment created in first transaction will stay
     * in the DB.
     */
    public static void createDataSet(IGenericDAO dao, DMDataSetDTO dataSet)
    {
        synchronized (IGenericDAO.class)
        {
            DMExperimentDTO experiment = getOrCreateExperiment(dao, dataSet);
            dataSet.setExperimentId(experiment.getId()); // make sure all the ids are set correctly.

            if (dataSet.getSample() != null)
            {
                String permId = dataSet.getSample().getPermId();
                DMSampleDTO sample;
                // it may have happened that the sample has been created by another thread after
                // we checked that it does not exist

                sample = dao.getSampleByPermId(permId);
                if (sample == null)
                {
                    sample = dataSet.getSample();
                    sample.setExperiment(experiment);
                    sample = createSample(dao, sample, permId);
                }

                sample.setExperiment(experiment);
                dataSet.setSample(sample); // make sure all the ids are set correctly.
            }
            dao.close(true);
        }

        long dataSetId = dao.addDataSet(dataSet);
        dataSet.setId(dataSetId);
    }

    private static DMSampleDTO createSample(IGenericDAO dao, DMSampleDTO sample, String samplePermId)
    {
        final long sampleId = dao.addSample(sample);
        sample.setId(sampleId);
        return sample;
    }

    private static DMExperimentDTO getOrCreateExperiment(IGenericDAO dao, DMDataSetDTO dataSet)
    {
        String permId = dataSet.getExperiment().getPermId();
        // it may have happened that the experiment has been created by another thread after
        // we checked that it does not exist
        synchronized (IGenericDAO.class)
        {
            DMExperimentDTO experiment = dao.getExperimentByPermId(permId);
            if (experiment == null)
            {
                experiment = createExperiment(dao, dataSet, permId);
            }
            return experiment;
        }
    }

    private static DMExperimentDTO createExperiment(IGenericDAO dao, DMDataSetDTO dataSet,
            String permId)
    {
        DMExperimentDTO experiment;
        experiment = dataSet.getExperiment();
        long experimentId = dao.addExperiment(experiment);
        experiment.setId(experimentId);
        return experiment;
    }

}
