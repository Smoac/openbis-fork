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

    private final class IdRowMapper implements ParameterizedRowMapper<Long>
    {
        public Long mapRow(ResultSet rs, int rowNum) throws SQLException
        {
            return rs.getLong("id");
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

    private final boolean isPostgreSQL;

    public MigrationStepFrom009To010(DatabaseConfigurationContext context)
    {
        isPostgreSQL = DatabaseEngine.POSTGRESQL.getCode().equals(context.getDatabaseEngineCode());
    }

    public void performPreMigration(SimpleJdbcTemplate simpleJdbcTemplate)
            throws DataAccessException
    {
    }

    public void performPostMigration(SimpleJdbcTemplate simpleJdbcTemplate)
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

        final List<Long> independentUsers =
                simpleJdbcTemplate.query("select id from users u1 where is_permanent or"
                        + " user_id_registrator is null or"
                        + " (select is_admin from users u2 where u1.user_id_registrator = u2.id)",
                        new IdRowMapper());
        for (long userId : independentUsers)
        {
            final long quotaGroupId =
                    simpleJdbcTemplate.queryForLong("select nextval('quota_group_id_seq')");
            simpleJdbcTemplate
                    .update("insert into quota_groups (id) values (?)", quotaGroupId);
            simpleJdbcTemplate.update("update users set quota_group_id = ? where id = ?",
                    quotaGroupId, userId);
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

        // Create index on USERS.QUOTA_GROUP_ID
        simpleJdbcTemplate.update("create index user_quota_group_fk_i on users (quota_group_id)");

        // Compute the current resource usage for all quota groups.

        simpleJdbcTemplate.update("update quota_groups q set file_count ="
                + " (select count(*) from files f join users u on f.user_id = u.id"
                + " where u.quota_group_id = q.id)");
        simpleJdbcTemplate.update("update quota_groups q set file_size ="
                + " (select coalesce(sum(f.complete_size), 0) from files f join users u "
                + "on f.user_id = u.id where u.quota_group_id = q.id)");
    }

}
