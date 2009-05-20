/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.migration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.dbmigration.java.IMigrationStep;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;

/**
 * Finishes migration of the database version 33 to version 34 by setting the <i>PERM_ID</i> of
 * samples and experiments and adding NOT NULL constraint.
 * 
 * @author Izabela Adamczyk
 */
public final class MigrationStepFrom033To034 implements IMigrationStep
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, MigrationStepFrom033To034.class);

    private final static ParameterizedRowMapper<Long> ID_ROW_MAPPER =
            new ParameterizedRowMapper<Long>()
                {
                    public Long mapRow(ResultSet rs, int rowNum) throws SQLException
                    {
                        return rs.getLong("id");
                    }
                };

    public final void performPostMigration(final SimpleJdbcTemplate simpleJdbcTemplate)
            throws DataAccessException
    {
        List<String> tables = Arrays.asList(TableNames.EXPERIMENTS_TABLE, TableNames.SAMPLES_TABLE);
        for (String tableName : tables)
        {
            List<Long> ids =
                    simpleJdbcTemplate.query(String.format("SELECT id FROM %s", tableName),
                            ID_ROW_MAPPER);
            for (Long id : ids)
            {
                int updated =
                        simpleJdbcTemplate.update(String.format(
                                "UPDATE %s SET perm_id = ? WHERE id = ?", tableName),
                                createPermId(getNextPermIdSeq(simpleJdbcTemplate)), id);
                if (updated != 1)
                {
                    throw new IncorrectResultSizeDataAccessException(1, updated);
                }
            }
            simpleJdbcTemplate.getJdbcOperations().execute(
                    String.format("ALTER TABLE %s ALTER COLUMN perm_id SET NOT NULL", tableName));
            simpleJdbcTemplate.getJdbcOperations().execute(
                    String.format("ALTER TABLE %s ADD CONSTRAINT %s_PI_UK UNIQUE(PERM_ID)",
                            tableName, firstFour(tableName)));
            operationLog.info(String
                    .format("Column 'perm_id' updated for %s records in table '%s'.", ids.size(),
                            tableName));
        }
    }

    private String firstFour(String tableName)
    {
        assert tableName != null && tableName.length() >= 4;
        return tableName.substring(0, 4).toUpperCase();
    }

    @Private
    static String createPermId(final long nextPermIdSeq)
    {
        return DateFormatUtils.format(new Date(), "yyyyMMddHHmmssSSS") + "-"
                + Long.toString(nextPermIdSeq);
    }

    private long getNextPermIdSeq(final SimpleJdbcTemplate simpleJdbcTemplate)
    {
        return simpleJdbcTemplate.queryForLong("select nextval('perm_id_seq')");
    }

    public final void performPreMigration(final SimpleJdbcTemplate simpleJdbcTemplate)
            throws DataAccessException
    {
    }

}
