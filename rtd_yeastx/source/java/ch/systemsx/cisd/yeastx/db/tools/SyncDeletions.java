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

package ch.systemsx.cisd.yeastx.db.tools;

import java.sql.Connection;
import java.sql.SQLException;

import net.lemnik.eodsql.DataSet;
import net.lemnik.eodsql.QueryTool;

import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.dbmigration.DatabaseEngine;
import ch.systemsx.cisd.yeastx.db.DMDataSetDTO;
import ch.systemsx.cisd.yeastx.db.IGenericDAO;

/**
 * Synchronize the deletion events of data sets done in openBIS to the metabol database.
 * 
 * @author Bernd Rinn
 */
public class SyncDeletions
{

    public static void main(String[] args) throws SQLException
    {
        final DatabaseConfigurationContext bisContext = new DatabaseConfigurationContext();
        bisContext.setDatabaseEngineCode(DatabaseEngine.POSTGRESQL.getCode());
        bisContext.setBasicDatabaseName("openbis");
        bisContext.setDatabaseKind("productive");
        final DatabaseConfigurationContext metabolContext = new DatabaseConfigurationContext();
        metabolContext.setDatabaseEngineCode(DatabaseEngine.POSTGRESQL.getCode());
        metabolContext.setBasicDatabaseName("metabol");
        metabolContext.setDatabaseKind("productive");
        final Connection bisConnection = bisContext.getDataSource().getConnection();
        final Connection metabolConnection = metabolContext.getDataSource().getConnection();
        final IGenericDAO dao = QueryTool.getQuery(metabolConnection, IGenericDAO.class);
        final DataSet<DMDataSetDTO> dataSets =
                QueryTool.select(bisConnection, DMDataSetDTO.class,
                        "select identifier as perm_id from events"
                                + " where event_type='DELETION' and entity_type='DATASET'");
        System.out.printf("Found %d data sets to delete in metabol.\n", dataSets.size());
        dao.deleteDataSets(dataSets);
        dao.commit();
        bisConnection.close();
        metabolConnection.close();
    }

}
