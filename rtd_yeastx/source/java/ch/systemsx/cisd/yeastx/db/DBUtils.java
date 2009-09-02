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

import net.lemnik.eodsql.QueryTool;
import net.lemnik.eodsql.TransactionQuery;

import ch.systemsx.cisd.dbmigration.DBMigrationEngine;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;

/**
 * Database utilities. Call {@link #init(DatabaseConfigurationContext)} before working with the
 * database.
 * 
 * @author Bernd Rinn
 */
public class DBUtils
{
    /** Current version of the database. */
    public static final String DATABASE_VERSION = "002";

    static
    {
        QueryTool.getTypeMap().put(float[].class, new FloatArrayMapper());
    }

    public static DatabaseConfigurationContext createDefaultDBContext()
    {
        final DatabaseConfigurationContext context = new DatabaseConfigurationContext();
        context.setDatabaseEngineCode("postgresql");
        context.setBasicDatabaseName("metabol");
        context.setReadOnlyGroup("metabol_readonly");
        context.setReadWriteGroup("metabol_readwrite");
        context.setDatabaseKind("dev");
        context.setScriptFolder("source/sql");
        return context;
    }

    /**
     * Checks the database specified by <var>context</var> and migrates it to the current version if
     * necessary.
     */
    public static void init(DatabaseConfigurationContext context)
    {
        DBMigrationEngine.createOrMigrateDatabaseAndGetScriptProvider(context, DATABASE_VERSION);
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
     * as well.
     */
    public static void createDataSet(IGenericDAO dao, DMDataSetDTO dataSet)
    {
        DMSampleDTO sample = dao.getSampleByPermId(dataSet.getSample().getPermId());
        if (sample == null)
        {
            DMExperimentDTO experiment =
                    dao.getExperimentByPermId(dataSet.getExperiment().getPermId());
            if (experiment == null)
            {
                experiment = dataSet.getExperiment();
                final long experimentId = dao.addExperiment(experiment);
                experiment.setId(experimentId);
            }
            sample = dataSet.getSample();
            sample.setExperiment(experiment);
            final long sampleId = dao.addSample(sample);
            sample.setId(sampleId);
            dataSet.setSample(sample); // make sure all the ids are set correctly.
        } else
        {
            dataSet.setSample(sample);
            sample.setExperiment(dao.getExperimentById(sample.getExperimentId()));
        }
        long dataSetId = dao.addDataSet(dataSet);
        dataSet.setId(dataSetId);
    }

}
