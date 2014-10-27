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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess;

import net.lemnik.eodsql.Select;
import net.lemnik.eodsql.TransactionQuery;
import net.lemnik.eodsql.Update;

/**
 * @author Jakub Straszewski
 */
public interface IMultiDataSetArchiverQueryDAO extends TransactionQuery, IMultiDataSetArchiverReadonlyQueryDAO
{
    /*
     * UPDATES / INSERTS
     */

    final static String INSERT_CONTAINER = "INSERT INTO containers "
            + "(code, path, location) "
            + "values "
            + "(?{1.code}, ?{1.path}, ?{1.location}) "
            + "RETURNING ID";

    @Select(sql = INSERT_CONTAINER)
    public long addContainer(MultiDataSetArchiverContainerDTO container);

    // updates

    final static String UPDATE_CONTAINER = "UPDATE containers "
            + "SET "
            + "code = ?{1.code}, "
            + "path = ?{1.path}, "
            + "location = ?{1.location}, "
            + "where ID = ?{1.id}";

    @Update(UPDATE_CONTAINER)
    public void updateContainer(MultiDataSetArchiverContainerDTO container);

    final static String INSERT_DATA_SET = "INSERT INTO data_sets "
            + "(code, ctnr_id, size_in_bytes) "
            + "VALUES "
            + "(?{1.code}, ?{1.containerId}, ?{1.sizeInBytes}) "
            + "RETURNING ID";

    @Select(sql = INSERT_DATA_SET)
    public long addDataSet(MultiDataSetArchiverDataSetDTO dataSet);
}
