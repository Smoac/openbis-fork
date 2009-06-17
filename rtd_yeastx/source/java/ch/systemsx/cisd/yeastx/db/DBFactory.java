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

import java.sql.Connection;
import java.sql.SQLException;

import net.lemnik.eodsql.QueryTool;

import ch.systemsx.cisd.dbmigration.DBMigrationEngine;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;

/**
 * Factory for database connections.
 * 
 * @author Bernd Rinn
 */
public class DBFactory
{
    /** Current version of the database. */
    public static final String DATABASE_VERSION = "001";

    static
    {
        QueryTool.getTypeMap().put(float[].class, new FloatArrayMapper());
    }

    private final DatabaseConfigurationContext context;

    public DBFactory(DatabaseConfigurationContext context)
    {
        this.context = context;
        DBMigrationEngine.createOrMigrateDatabaseAndGetScriptProvider(context, DATABASE_VERSION);
    }

    public Connection getConnection() throws SQLException
    {
        final Connection conn = context.getDataSource().getConnection();
        conn.setAutoCommit(false);
        return conn;
    }

    public static DatabaseConfigurationContext createDefaultDBContext()
    {
        final DatabaseConfigurationContext context = new DatabaseConfigurationContext();
        context.setDatabaseEngineCode("postgresql");
        context.setBasicDatabaseName("metabol");
        context.setReadOnlyGroup("metabol_readonly");
        context.setReadWriteGroup("metabol_readwrite");
        // TODO 2009-06-03, Tomasz Pylak: move to external file. Change for productive usage
        // context.setDatabaseKind("productive");
        // context.setScriptFolder("."); // or "sql" ???

        context.setDatabaseKind("dev");
        context.setScriptFolder("source/sql");
        return context;
    }

    /**
     * Returns the data access object for the generic tables of the data mart.
     */
    public static IGenericDAO getDAO(Connection conn)
    {
        return QueryTool.getQuery(conn, IGenericDAO.class);
    }

    /**
     * Creates the data set based on the information given in <var>dataSet</var>. The sample and
     * experiment of the data set may already exist in the database. If they don't, they are created
     * as well.
     */
    public static void createDataSet(IGenericDAO dao, DMDataSetDTO dataSet)
    {
        DMSampleDTO sample = dao.getSample(dataSet.getSample().getPermId());
        if (sample == null)
        {
            DMExperimentDTO experiment = dao.getExperiment(dataSet.getExperiment().getPermId());
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
            sample.setExperiment(dao.getExperiment(sample.getExperimentId()));
        }
        long dataSetId = dao.addDataSet(dataSet);
        dataSet.setId(dataSetId);
    }

}
