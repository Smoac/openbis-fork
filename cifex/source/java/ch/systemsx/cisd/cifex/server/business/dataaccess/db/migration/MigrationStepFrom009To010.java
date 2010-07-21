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

package ch.systemsx.cisd.cifex.server.business.dataaccess.db.migration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.dbmigration.DatabaseEngine;
import ch.systemsx.cisd.dbmigration.java.IMigrationStep;

/**
 * Migration step from 9 to 10: Create quota groups, assign them to users and compute the initial
 * usage statistics.
 * 
 * @author Bernd Rinn
 */
public class MigrationStepFrom009To010 implements IMigrationStep
{

    private final class IdQuotaGroupIdHolder
    {
        final long userId;

        final long quotaGroupId;

        IdQuotaGroupIdHolder(long userId, long quotaGroupId)
        {
            this.userId = userId;
            this.quotaGroupId = quotaGroupId;
        }
    }

    private final class IdFileRentionHolder
    {
        final long userId;

        final Long fileRetention;

        IdFileRentionHolder(long userId, Long fileRetention)
        {
            this.userId = userId;
            this.fileRetention = fileRetention;
        }
    }

    private final class IdQuotaGroupIdRowMapper implements
            ParameterizedRowMapper<IdQuotaGroupIdHolder>
    {
        public IdQuotaGroupIdHolder mapRow(ResultSet rs, int rowNum) throws SQLException
        {
            return new IdQuotaGroupIdHolder(rs.getLong("id"), rs.getLong("quota_group_id"));
        }
    }

    private final class IdFileRententionRowMapper implements
            ParameterizedRowMapper<IdFileRentionHolder>
    {
        public IdFileRentionHolder mapRow(ResultSet rs, int rowNum) throws SQLException
        {
            // Conversion from minutes to hours.
            final long fileRetention = Math.max(rs.getLong("file_retention") / 60, 1);
            if (rs.wasNull())
            {
                return new IdFileRentionHolder(rs.getLong("id"), null);
            } else
            {
                return new IdFileRentionHolder(rs.getLong("id"), fileRetention);

            }
        }
    }

    private final boolean isPostgreSQL;

    public MigrationStepFrom009To010(DatabaseConfigurationContext context)
    {
        isPostgreSQL = DatabaseEngine.POSTGRESQL.getCode().equals(context.getDatabaseEngineCode());
    }

    public void performPreMigration(SimpleJdbcTemplate simpleJdbcTemplate, DataSource dataSource)
            throws DataAccessException
    {
    }

    public void performPostMigration(SimpleJdbcTemplate simpleJdbcTemplate, DataSource dataSource)
            throws DataAccessException
    {
        // Rename column USERS.USER_ID to USER_CODE and domain USER_ID to USER_CODE
        simpleJdbcTemplate.update("create domain user_code as varchar(50)");
        if (isPostgreSQL)
        {
            simpleJdbcTemplate.update("alter table users rename column user_id to user_code");
            simpleJdbcTemplate.update("alter table users alter column user_code type user_code");
        } else
        {
            simpleJdbcTemplate.update("alter table users alter column user_id rename to user_code");
        }
        simpleJdbcTemplate.update("drop domain user_id");

        final List<IdFileRentionHolder> independentUsers =
                simpleJdbcTemplate
                        .query(
                                "select id, file_retention from users u1 where is_permanent or"
                                        + " user_id_registrator is null or"
                                        + " (select is_admin from users u2 where u1.user_id_registrator = u2.id)",
                                new IdFileRententionRowMapper());
        for (IdFileRentionHolder userIdFileRention : independentUsers)
        {
            final long quotaGroupId =
                    simpleJdbcTemplate.queryForLong("select nextval('quota_group_id_seq')");
            simpleJdbcTemplate.update(
                    "insert into quota_groups (id, file_retention, user_retention) values (?,?,?)",
                    quotaGroupId, userIdFileRention.fileRetention, userIdFileRention.fileRetention);
            simpleJdbcTemplate.update("update users set quota_group_id = ? where id = ?",
                    quotaGroupId, userIdFileRention.userId);
        }
        final List<IdQuotaGroupIdHolder> dependentUsers =
                simpleJdbcTemplate.query("select u1.id, u2.quota_group_id from users u1 "
                        + "join users u2 on u1.user_id_registrator = u2.id "
                        + "where u1.quota_group_id is null", new IdQuotaGroupIdRowMapper());
        for (IdQuotaGroupIdHolder idHolder : dependentUsers)
        {
            simpleJdbcTemplate.update("update users set quota_group_id = ? where id = ?",
                    idHolder.quotaGroupId, idHolder.userId);
        }

        // Drop column USERS.FILE_RETENTION
        simpleJdbcTemplate.update("alter table users drop column file_retention");

        // Create index on USERS.QUOTA_GROUP_ID
        simpleJdbcTemplate.update("create index user_quota_group_fk_i on users (quota_group_id)");

        // Compute the current resource usage for all quota groups.
        simpleJdbcTemplate.queryForList("select CALC_ACCOUNTING_FOR_ALL_QUOTA_GROUPS()");
    }
}
