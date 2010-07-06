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

package ch.systemsx.cisd.cifex.server.business.dataaccess.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import ch.systemsx.cisd.cifex.server.business.dataaccess.IUserDAO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.cifex.server.common.Password;
import ch.systemsx.cisd.common.db.DBUtils;
import ch.systemsx.cisd.common.db.ISequencerHandler;

/**
 * Performs database manipulation on <code>users</code> table.
 * 
 * @author Basil Neff
 */
final class UserDAO extends AbstractDAO implements IUserDAO
{

    private static final String SELECT_USERS_WITH_QUOTA_INFO =
            "select u.*,q.file_count,q.file_size,q.quota_file_count,q.quota_file_size,q.file_retention,q.user_retention"
                    + " from users u join quota_groups q on q.id = u.quota_group_id";

    public static class UserRowMapper implements ParameterizedRowMapper<UserDTO>
    {

        public UserDTO mapRow(final ResultSet rs, final int rowNum) throws SQLException
        {
            final UserDTO user = fillUserFromResultSet(rs);
            return user;
        }

        protected UserDTO fillUserFromResultSet(final ResultSet rs) throws SQLException
        {
            final UserDTO user = new UserDTO();
            final UserDTO registrator = new UserDTO();
            user.setID(rs.getLong("id"));
            user.setUserCode(rs.getString("user_code"));
            user.setEmail(rs.getString("email"));
            user.setUserFullName(rs.getString("full_name"));
            user.setPasswordHash(rs.getString("password_hash"));
            user.setExternallyAuthenticated(rs.getBoolean("is_externally_authenticated"));
            user.setAdmin(rs.getBoolean("is_admin"));
            user.setActive(rs.getBoolean("is_active"));
            user.setRegistrationDate(DBUtils.tryToTranslateTimestampToDate(rs
                    .getTimestamp("registration_timestamp")));
            user.setExpirationDate(DBUtils.tryToTranslateTimestampToDate(rs
                    .getTimestamp("expiration_timestamp")));
            registrator.setID(rs.getLong("user_id_registrator"));
            if (rs.wasNull() == false)
            {
                user.setRegistrator(registrator);
            }
            user.setQuotaGroupId(rs.getLong("quota_group_id"));

            return user;
        }

    }

    public static final class UserRowMapperWithQuotaInfo extends UserRowMapper
    {

        @Override
        public UserDTO mapRow(final ResultSet rs, final int rowNum) throws SQLException
        {
            final UserDTO user = fillUserFromResultSet(rs);
            return user;
        }

        @Override
        protected UserDTO fillUserFromResultSet(final ResultSet rs) throws SQLException
        {
            final UserDTO user = super.fillUserFromResultSet(rs);
            final int fileRetention = rs.getInt("file_retention");
            if (rs.wasNull() == false)
            {
                user.setMaxFileRetention(fileRetention);
                user.setCustomMaxFileRetention(true);
            }
            final int userRetention = rs.getInt("user_retention");
            if (rs.wasNull() == false)
            {
                user.setMaxUserRetention(userRetention);
                user.setCustomMaxUserRetention(true);
            }
            user.setCurrentFileCount(rs.getInt("file_count"));
            user.setCurrentFileSize(rs.getLong("file_size"));
            int maxFileCountPerQuotaGroup = rs.getInt("quota_file_count");
            if (rs.wasNull() == false)
            {
                user.setMaxFileCountPerQuotaGroup(maxFileCountPerQuotaGroup);
            }
            user.setCustomMaxFileCountPerQuotaGroup(true);
            long maxFileSizePerQuotaGroupInMB = rs.getLong("quota_file_size");
            if (rs.wasNull() == false)
            {
                user.setMaxFileSizePerQuotaGroupInMB(maxFileSizePerQuotaGroupInMB);
            }
            user.setCustomMaxFileSizePerQuotaGroup(true);

            return user;
        }

    }

    private final static class QuotaRowMapper implements ParameterizedRowMapper<UserDTO>
    {
        private final UserDTO user;

        QuotaRowMapper(UserDTO user)
        {
            this.user = user;
        }

        public UserDTO mapRow(ResultSet rs, int rowNum) throws SQLException
        {
            user.setMaxFileRetention(rs.getInt("file_retention"));
            if (rs.wasNull())
            {
                user.setMaxFileRetention(null);
            }
            user.setMaxUserRetention(rs.getInt("user_retention"));
            if (rs.wasNull())
            {
                user.setMaxUserRetention(null);
            }
            user.setCurrentFileCount(rs.getInt("file_count"));
            user.setCurrentFileSize(rs.getLong("file_size"));
            user.setMaxFileCountPerQuotaGroup(rs.getInt("quota_file_count"));
            if (rs.wasNull())
            {
                user.setMaxFileCountPerQuotaGroup(null);
            }
            user.setMaxFileSizePerQuotaGroupInMB(rs.getLong("quota_file_size"));
            if (rs.wasNull())
            {
                user.setMaxFileSizePerQuotaGroupInMB(null);
            }
            return user;
        }
    }

    UserDAO(final DataSource dataSource, final ISequencerHandler sequencerHandler,
            final boolean supportsAnyOperator)
    {
        super(dataSource, sequencerHandler, supportsAnyOperator);
    }

    private long createID()
    {
        return getNextValueOf("USER_ID_SEQ");
    }

    public int getNumberOfUsers() throws DataAccessException
    {
        final JdbcTemplate template = getJdbcTemplate();
        return template.queryForInt("select count(*) from users");
    }

    public boolean isMainUserOfQuotaGroup(UserDTO user) throws DataAccessException
    {
        final long id = user.getID();
        final long quotaGroupId = user.getQuotaGroupId();
        return getSimpleJdbcTemplate().queryForInt(
                "select count(*) from users where quota_group_id = ? and id != ? and "
                        + "(user_id_registrator != ? or user_id_registrator is null)",
                quotaGroupId, id, id) == 0;
    }

    public List<UserDTO> listUsers() throws DataAccessException
    {
        final SimpleJdbcTemplate template = getSimpleJdbcTemplate();
        final List<UserDTO> list =
                template.query(SELECT_USERS_WITH_QUOTA_INFO, new UserRowMapperWithQuotaInfo());
        fillInRegistrators(list);
        return list;
    }

    public List<UserDTO> listUsersById(long... userIds) throws DataAccessException
    {
        if (supportsAnyOperator)
        {
            final List<UserDTO> list =
                    query(SELECT_USERS_WITH_QUOTA_INFO + " where u.id = any(?)", new Object[]
                        { new SimpleSQLLongArray(userIds) }, new int[]
                        { Types.ARRAY }, new UserRowMapperWithQuotaInfo());
            return list;
        } else
        {
            final Set<Long> uniqueIdSet = new HashSet<Long>();
            final List<UserDTO> list = new ArrayList<UserDTO>(userIds.length);
            for (final long id : userIds)
            {
                if (uniqueIdSet.contains(id) == false)
                {
                    uniqueIdSet.add(id);
                    list.addAll(getSimpleJdbcTemplate().query(
                            SELECT_USERS_WITH_QUOTA_INFO + " where u.id = ?",
                            new UserRowMapperWithQuotaInfo(), id));
                }
            }
            return list;
        }
    }

    public List<UserDTO> listUsersByCode(String... userCodes) throws DataAccessException
    {
        if (supportsAnyOperator)
        {
            final List<UserDTO> list =
                    query(SELECT_USERS_WITH_QUOTA_INFO + " where u.user_code = any(?)", new Object[]
                        { new SimpleSQLStringArray(userCodes) }, new int[]
                        { Types.ARRAY }, new UserRowMapperWithQuotaInfo());
            return list;
        } else
        {
            final Set<String> uniqueIdSet = new HashSet<String>();
            final List<UserDTO> list = new ArrayList<UserDTO>(userCodes.length);
            for (final String code : userCodes)
            {
                if (uniqueIdSet.contains(code) == false)
                {
                    uniqueIdSet.add(code);
                    list.addAll(getSimpleJdbcTemplate().query(
                            SELECT_USERS_WITH_QUOTA_INFO + " where u.user_code = ?",
                            new UserRowMapperWithQuotaInfo(), code));
                }
            }
            return list;
        }
    }

    public List<UserDTO> listUsersByEmail(String... emailAddresses) throws DataAccessException
    {
        if (supportsAnyOperator)
        {
            final List<UserDTO> list =
                    query(SELECT_USERS_WITH_QUOTA_INFO + " where u.email = any(?)", new Object[]
                        { new SimpleSQLStringArray(emailAddresses) }, new int[]
                        { Types.ARRAY }, new UserRowMapperWithQuotaInfo());
            return list;
        } else
        {
            final Set<String> uniqueIdSet = new HashSet<String>();
            final List<UserDTO> list = new ArrayList<UserDTO>(emailAddresses.length);
            for (final String email : emailAddresses)
            {
                if (uniqueIdSet.contains(email) == false)
                {
                    uniqueIdSet.add(email);
                    list.addAll(getSimpleJdbcTemplate().query(
                            SELECT_USERS_WITH_QUOTA_INFO + " where u.email = ?",
                            new UserRowMapperWithQuotaInfo(), email));
                }
            }
            return list;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> query(String sql, Object[] args, int[] argTypes,
            ParameterizedRowMapper<T> rowMapper) throws DataAccessException
    {
        return getSimpleJdbcTemplate().getJdbcOperations().query(sql, args, argTypes, rowMapper);
    }

    public List<UserDTO> listUsersRegisteredBy(final long userId) throws DataAccessException
    {
        final UserDTO registrator = getUserById(userId);
        final SimpleJdbcTemplate template = getSimpleJdbcTemplate();
        final List<UserDTO> list =
                template.query(SELECT_USERS_WITH_QUOTA_INFO + " where user_id_registrator = ?",
                        new UserRowMapperWithQuotaInfo(), userId);

        for (final UserDTO user : list)
        {
            user.setRegistrator(registrator);
        }
        return list;
    }

    private void fillInRegistrator(final UserDTO user)
    {
        assert user != null : "User can not be null";
        final Long registratorIdOrNull = tryGetRegistratorId(user);
        if (registratorIdOrNull != null)
        {
            user.setRegistrator(primGetUserById(registratorIdOrNull));
        }
    }

    private void fillInRegistrators(final List<UserDTO> usersToFill, final List<UserDTO> allUsers)
    {
        final Map<Long, UserDTO> idToUserMap = new HashMap<Long, UserDTO>();
        for (final UserDTO user : allUsers)
        {
            idToUserMap.put(user.getID(), user);
        }
        for (final UserDTO user : usersToFill)
        {
            final UserDTO registratorOrNull = user.getRegistrator();
            if (registratorOrNull != null)
            {
                user.setRegistrator(idToUserMap.get(registratorOrNull.getID()));
            }
        }
    }

    public void fillInRegistrators(final List<UserDTO> users)
    {
        fillInRegistrators(users, users);
    }

    public UserDTO tryFindUserByCode(final String userCode) throws DataAccessException
    {
        assert StringUtils.isNotBlank(userCode) : "No code specified!";

        final SimpleJdbcTemplate template = getSimpleJdbcTemplate();
        try
        {
            final UserDTO user =
                    template.queryForObject(SELECT_USERS_WITH_QUOTA_INFO + " where user_code = ?",
                            new UserRowMapperWithQuotaInfo(), userCode);
            fillInRegistrator(user);
            return user;
        } catch (final EmptyResultDataAccessException e)
        {
            return null;
        }
    }

    public UserDTO getUserById(final long id) throws DataAccessException
    {
        final UserDTO user = primGetUserById(id);
        fillInRegistrator(user);
        return user;
    }

    private UserDTO primGetUserById(final long id)
    {
        final SimpleJdbcTemplate template = getSimpleJdbcTemplate();
        final UserDTO user =
                template.queryForObject(SELECT_USERS_WITH_QUOTA_INFO + " where u.id = ?",
                        new UserRowMapperWithQuotaInfo(), id);
        return user;
    }

    public String tryFindUserCodeById(final long id) throws DataAccessException
    {
        final SimpleJdbcTemplate template = getSimpleJdbcTemplate();
        try
        {
            final String userCode =
                    template.queryForObject("select user_code from users where id = ?",
                            String.class, id);
            return userCode;
        } catch (final EmptyResultDataAccessException e)
        {
            return null;
        }
    }

    public boolean hasUserCode(final String code) throws DataAccessException
    {
        final SimpleJdbcTemplate template = getSimpleJdbcTemplate();
        final int count =
                template
                        .queryForInt("select count(*) from users where user_code = ? limit 1", code);
        return count > 0;
    }

    public List<UserDTO> findUserByEmail(final String email) throws DataAccessException
    {
        assert StringUtils.isNotBlank(email) : "No email specified!";

        final SimpleJdbcTemplate template = getSimpleJdbcTemplate();
        final List<UserDTO> list =
                template.query(SELECT_USERS_WITH_QUOTA_INFO + " where email = ?",
                        new UserRowMapperWithQuotaInfo(), email);
        fillInRegistrators(list, this.listUsers());
        return list;
    }

    /**
     * Doesn't fill registrator and quota fields in UserDTO.
     */
    public List<UserDTO> listExpiredUsers() throws DataAccessException
    {
        final SimpleJdbcTemplate template = getSimpleJdbcTemplate();
        final List<UserDTO> list =
                template.query("select * from users where expiration_timestamp < now() ",
                        new UserRowMapper());
        return list;
    }

    /**
     * Doesn't fill registrator and quota fields in UserDTO.
     */
    public List<UserDTO> listUsersFileSharedWith(final long fileId) throws DataAccessException
    {

        final SimpleJdbcTemplate template = getSimpleJdbcTemplate();
        final List<UserDTO> list =
                template
                        .query("select u.* from file_shares fs, users u "
                                + "where u.id = fs.user_id and fs.file_id = ?",
                                new UserRowMapper(), fileId);
        fillInRegistrators(list, this.listUsers());
        return list;
    }

    public void refreshQuotaInformation(UserDTO user) throws DataAccessException
    {
        getSimpleJdbcTemplate().query("select * from quota_groups where id = ?",
                new QuotaRowMapper(user), user.getQuotaGroupId());
    }

    public boolean hasUserFilesForDownload(long id)
    {
        return getSimpleJdbcTemplate().queryForList(
                "select id from file_shares where user_id = ? limit 1", id).isEmpty() == false;
    }

    public void createUser(final UserDTO user) throws DataAccessException
    {
        assert user != null : "Given user can not be null.";

        final long id = createID();

        final Long registratorIdOrNull = tryGetRegistratorId(user);
        final SimpleJdbcTemplate template = getSimpleJdbcTemplate();
        if (Password.isEmpty(user.getPassword()))
        {
            template.update("insert into users (id, user_code, email, full_name,"
                    + "is_externally_authenticated, is_admin,"
                    + "is_active, user_id_registrator, quota_group_id," + "expiration_timestamp) "
                    + "values (?,?,?,?,?,?,?,?,?,?)", id, user.getUserCode(), user.getEmail(), user
                    .getUserFullName(), user.isExternallyAuthenticated(), user.isAdmin(), user
                    .isActive(), registratorIdOrNull, user.getQuotaGroupId(), user
                    .getExpirationDate());
        } else
        {
            template.update("insert into users (id, user_code, email, full_name, password_hash,"
                    + "is_externally_authenticated, is_admin,"
                    + "is_active, user_id_registrator, quota_group_id," + "expiration_timestamp) "
                    + "values (?,?,?,?,?,?,?,?,?,?,?)", id, user.getUserCode(), user.getEmail(),
                    user.getUserFullName(), user.getPassword().createPasswordHash(), user
                            .isExternallyAuthenticated(), user.isAdmin(), user.isActive(),
                    registratorIdOrNull, user.getQuotaGroupId(), user.getExpirationDate());
        }
        user.setID(id);
        updateCustomQuotaInformation(user, template);
    }

    private Long tryGetRegistratorId(final UserDTO user)
    {
        assert user != null;

        UserDTO registratorOrNull = user.getRegistrator();
        if (registratorOrNull == null)
        {
            return null;
        } else
        {
            Long registratorId = registratorOrNull.getID();
            if (registratorId == null)
            {
                registratorOrNull = tryFindUserByCode(registratorOrNull.getUserCode());
                if (registratorOrNull != null)
                {
                    registratorId = registratorOrNull.getID();
                }
            }
            return registratorId;
        }
    }

    public boolean deleteUser(final UserDTO user, final Long requestUserIdOrNull)
    {
        assert user != null;

        final Long registratorIdOrNull = tryGetRegistratorId(user);
        final Long newOwnerIdOrNull =
                (registratorIdOrNull == null) ? requestUserIdOrNull : registratorIdOrNull;
        final SimpleJdbcTemplate template = getSimpleJdbcTemplate();
        if (newOwnerIdOrNull != null)
        {
            // Set new owner for owned users and files.
            template.update(
                    "update users set user_id_registrator = ? where user_id_registrator = ?",
                    newOwnerIdOrNull, user.getID());
            template.update("update files set user_id = ? where user_id = ?", newOwnerIdOrNull,
                    user.getID());
        }
        final int affectedRows = template.update("delete from users where id = ?", user.getID());
        return affectedRows > 0;
    }

    public void updateUser(final UserDTO user)
    {
        assert user.getID() != null : "User needs an ID, otherwise it can't be updated";
        final SimpleJdbcTemplate template = getSimpleJdbcTemplate();

        template.update("update users set email = ?, user_code = ?, full_name = ?, "
                + "is_externally_authenticated = ?, is_admin = ?, "
                + "is_active = ?, quota_group_id = ?, expiration_timestamp = ? where id = ?", user
                .getEmail(), user.getUserCode(), user.getUserFullName(), user
                .isExternallyAuthenticated(), user.isAdmin(), user.isActive(), user
                .getQuotaGroupId(), user.getExpirationDate(), user.getID());
        if (Password.isEmpty(user.getPassword()) == false)
        {
            template.update("update users set password_hash = ? where id = ?", user.getPassword()
                    .createPasswordHash(), user.getID());
        }
        // If a new registrator has been set, update.
        if (user.getRegistrator() != null && user.getRegistrator().getID() != null)
        {
            template.update("update users set user_id_registrator = ? where id = ?", user
                    .getRegistrator().getID(), user.getID());

        }
        updateCustomQuotaInformation(user, template);
    }

    private void updateCustomQuotaInformation(final UserDTO user, final SimpleJdbcTemplate template)
    {
        // Custom quota update
        Integer maxFileCountPerQuotaGroup = null;
        if (user.isCustomMaxFileCountPerQuotaGroup())
        {
            maxFileCountPerQuotaGroup =
                    (user.getMaxFileCountPerQuotaGroup() == null) ? 0 : user
                            .getMaxFileCountPerQuotaGroup();
        }
        Long maxFileSizePerQuotaGroupInMB = null;
        if (user.isCustomMaxFileSizePerQuotaGroup())
        {
            maxFileSizePerQuotaGroupInMB =
                    (user.getMaxFileSizePerQuotaGroupInMB() == null) ? 0 : user
                            .getMaxFileSizePerQuotaGroupInMB();
        }
        Integer fileRetention = null;
        if (user.isCustomMaxFileRetention())
        {
            fileRetention = (user.getMaxFileRetention() == null) ? 0 : user.getMaxFileRetention();
        }
        Integer userRetention = null;
        if (user.isCustomMaxUserRetention())
        {
            userRetention = (user.getMaxUserRetention() == null) ? 0 : user.getMaxUserRetention();
        }
        // If quotaGroupId had been set to null, it will now have been set to the id of the new
        // quota group.
        final long quotaGroupId =
                (user.getQuotaGroupId() == null) ? template.queryForInt(
                        "select quota_group_id from users where id = ?", user.getID()) : user
                        .getQuotaGroupId();
        template.update("update quota_groups set quota_file_count = ?, quota_file_size = ?, "
                + "file_retention = ?, user_retention = ? where id = ?", maxFileCountPerQuotaGroup,
                maxFileSizePerQuotaGroupInMB, fileRetention, userRetention, quotaGroupId);
        user.setQuotaGroupId(quotaGroupId);
    }

    public void changeUserCode(final String before, final String after)
    {
        getSimpleJdbcTemplate().update("update users set user_code = ? where user_code = ? ",
                after, before);

    }

}
