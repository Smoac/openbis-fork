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

package ch.systemsx.cisd.yeastx.db.migration;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.dbmigration.java.MigrationStepAdapter;

/**
 * @author Franz-Josef Elmer
 */
public class MigrationStepFrom004To005 extends MigrationStepAdapter
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, MigrationStepFrom004To005.class);

    @Override
    public void performPostMigration(SimpleJdbcTemplate simpleJdbcTemplate, DataSource dataSource)
            throws DataAccessException
    {
        operationLog.warn("!!!!!!!! METABOL DATABASE MIGRATED WITH OUT MIGRATING TABLE MZ_PEAKS. "
                + "THIS TABLE IS STILL KEPT. !!!!!!!! "
                + "Please, delete MZ_PEAKS manually if it no longer needed.");
    }

}
