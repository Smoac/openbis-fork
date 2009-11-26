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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
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
            user.setPermanent(rs.getBoolean("is_permanent"));
            user.setActive(rs.getBoolean("is_active"));
            user.setRegistrationDate(DBUtils.tryToTranslateTimestampToDate(rs
                    .getTimestamp("registration_timestamp")));
            user.setExpirationDate(DBUtils.tryToTranslateTimestampToDate(rs
                    .getTimestamp("expiration_timestamp")));
            registrator.setID(rs.getLong("user_id_registrator"));
            user.setRegistrator(registrator);
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
                user.setFileRetention(fileRetention);
                user.setCustomFileRetention(true);
            }
            final int userRetention = rs.getInt("user_retention");
            if (rs.wasNull() == false)
            {
                user.setUserRetention(userRetention);
                user.setCustomUserRetention(true);
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
            user.setFileRetention(rs.getInt("file_retention"));
            if (rs.wasNull())
            {
                user.setFileRetention(null);
            }
            user.setUserRetention(rs.getInt("user_retention"));
            if (rs.wasNull())
            {
                user.setUserRetention(null);
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

    UserDAO(final DataSource dataSource, final ISequencerHandler sequencerHandler)
    {
        super(dataSource, sequencerHandler);
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

    public List<UserDTO> listUsers() throws DataAccessException
    {
        final SimpleJdbcTemplate template = getSimpleJdbcTemplate();
        final List<UserDTO> list =
                template.query(
                        "select * from users u join quota_groups q on q.id = u.quota_group_id",
                        new UserRowMapperWithQuotaInfo());
        fillInRegistrators(list);
        return list;
    }

    public List<UserDTO> listUsersRegisteredBy(final String userCode) throws DataAccessException
    {
        assert userCode != null;
        final UserDTO registrator = tryFindUserByCode(userCode);
        if (registrator == null)
        {

            throw new DataRetrievalFailureException("User '" + userCode + "' does not exist.");
        }
        final SimpleJdbcTemplate template = getSimpleJdbcTemplate();
        final List<UserDTO> list =
                template.query(
                        "select * from users u join quota_groups q on q.id = u.quota_group_id"
                                + " where user_id_registrator ="
                                + " (select id from users where user_code=?)",
                        new UserRowMapperWithQuotaInfo(), userCode);

        for (final UserDTO user : list)
        {
            user.setRegistrator(registrator);
        }
        return list;
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
            final UserDTO registratorDTO = idToUserMap.get(user.getRegistrator().getID());
            if (registratorDTO != null)
            {
                user.setRegistrator(registratorDTO);
            }
        }
    }

    private void fillInRegistrator(final UserDTO user)
    {
        assert user != null : "User can not be null";
        assert user.getRegistrator() != null : "Registrator can not be null";
        if (StringUtils.isNotBlank(user.getRegistrator().getUserCode()))
        {
            UserDTO registrator = this.tryFindUserByCode(user.getRegistrator().getUserCode());
            if (registrator != null)
            {
                user.setRegistrator(registrator);
            }
        }
    }

    private void fillInRegistrators(final List<UserDTO> users)
    {
        final Map<Long, UserDTO> idToUserMap = new HashMap<Long, UserDTO>();
        for (final UserDTO user : users)
        {
            idToUserMap.put(user.getID(), user);
        }
        for (final UserDTO user : users)
        {
            final UserDTO registratorDTO = idToUserMap.get(user.getRegistrator().getID());
            if (registratorDTO != null)
            {
                user.setRegistrator(registratorDTO);
            }
        }
    }

    public UserDTO tryFindUserByCode(final String userCode) throws DataAccessException
    {
        assert StringUtils.isNotBlank(userCode) : "No code specified!";

        final SimpleJdbcTemplate template = getSimpleJdbcTemplate();
        try
        {
            final UserDTO user =
                    template.queryForObject(
                            "select * from users u join quota_groups q on q.id = u.quota_group_id"
                                    + " where user_code = ?", new UserRowMapperWithQuotaInfo(),
                            userCode);
            fillInRegistrator(user);
            return user;
        } catch (final EmptyResultDataAccessException e)
        {
            return null;
        }
    }

    public UserDTO getUserById(final long id) throws DataAccessException
    {
        final SimpleJdbcTemplate template = getSimpleJdbcTemplate();
        final UserDTO user =
                template.queryForObject(
                        "select * from users u join quota_groups q on q.id = u.quota_group_id"
                                + " where u.id = ?", new UserRowMapperWithQuotaInfo(), id);
        fillInRegistrator(user);
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

    public List<UserDTO> tryFindUserByEmail(final String email) throws DataAccessException
    {
        assert StringUtils.isNotBlank(email) : "No email specified!";

        final SimpleJdbcTemplate template = getSimpleJdbcTemplate();
        try
        {
            final List<UserDTO> list =
                    template.query(
                            "select * from users u join quota_groups q on q.id = u.quota_group_id"
                                    + " where email = ?", new UserRowMapperWithQuotaInfo(), email);
            fillInRegistrators(list, this.listUsers());
            return list;
        } catch (final EmptyResultDataAccessException e)
        {
            return null;
        }
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

    public void createUser(final UserDTO user) throws DataAccessException
    {
        assert user != null : "Given user can not be null.";

        final Long id = createID();

        final Long registratorIdOrNull = tryGetRegistratorId(user);
        final SimpleJdbcTemplate template = getSimpleJdbcTemplate();
        if (Password.isEmpty(user.getPassword()))
        {
            template.update("insert into users (id, user_code, email, full_name,"
                    + "is_externally_authenticated, is_admin,"
                    + "is_permanent, is_active, user_id_registrator, quota_group_id,"
                    + "expiration_timestamp) " + "values (?,?,?,?,?,?,?,?,?,?,?)", id, user
                    .getUserCode(), user.getEmail(), user.getUserFullName(), user
                    .isExternallyAuthenticated(), user.isAdmin(), user.isPermanent(), user
                    .isActive(), registratorIdOrNull, user.getQuotaGroupId(), user
                    .getExpirationDate());
        } else
        {
            template.update("insert into users (id, user_code, email, full_name, password_hash,"
                    + "is_externally_authenticated, is_admin,"
                    + "is_permanent, is_active, user_id_registrator, quota_group_id,"
                    + "expiration_timestamp) " + "values (?,?,?,?,?,?,?,?,?,?,?,?)", id, user
                    .getUserCode(), user.getEmail(), user.getUserFullName(), user.getPassword()
                    .createPasswordHash(), user.isExternallyAuthenticated(), user.isAdmin(), user
                    .isPermanent(), user.isActive(), registratorIdOrNull, user.getQuotaGroupId(),
                    user.getExpirationDate());
        }
        if (user.getFileRetention() != null || user.getUserRetention() != null)
        {
            template.update("update quota_groups set file_retention = ?, user_retention = ? "
                    + "where id = (select quota_group_id from users where id = ?)", user
                    .getFileRetention(), user.getUserRetention(), id);
        }
        user.setID(id);
    }

    private Long tryGetRegistratorId(final UserDTO user)
    {
        assert user != null;

        UserDTO registrator = user.getRegistrator();
        if (registrator == null)
        {
            return null;
        } else if (registrator.getUserCode() == null)
        {
            return null;
        } else
        {
            Long registratorId = registrator.getID();
            if (registratorId == null)
            {
                registrator = tryFindUserByCode(registrator.getUserCode());
                if (registrator != null)
                {
                    registratorId = registrator.getID();
                }
            }
            return registratorId;
        }
    }

    public boolean deleteUser(final long userId)
    {
        final SimpleJdbcTemplate template = getSimpleJdbcTemplate();
        final int affectedRows = template.update("delete from users where id = ?", userId);
        return affectedRows > 0;
    }

    public void updateUser(final UserDTO user)
    {
        assert user.getID() != null : "User needs an ID, otherwise it can't be updated";
        final SimpleJdbcTemplate template = getSimpleJdbcTemplate();

        if (Password.isEmpty(user.getPassword()))
        {
            template.update(
                    "update users set email = ?, user_code = ?, full_name = ?, "
                            + "is_externally_authenticated = ?, is_admin = ?, "
                            + "is_permanent = ?, is_active = ?, "
                            + "expiration_timestamp = ? where id = ?", user.getEmail(), user
                            .getUserCode(), user.getUserFullName(), user
                            .isExternallyAuthenticated(), user.isAdmin(), user.isPermanent(), user
                            .isActive(), user.getExpirationDate(), user.getID());
        } else
        {
            template.update(
                    "update users set email = ?, user_code = ?, full_name = ?, "
                            + "password_hash = ?, is_externally_authenticated = ?, is_admin = ?, "
                            + "is_permanent = ?, is_active = ?, "
                            + "expiration_timestamp = ? where id = ?", user.getEmail(), user
                            .getUserCode(), user.getUserFullName(), user.getPassword()
                            .createPasswordHash(), user.isExternallyAuthenticated(),
                    user.isAdmin(), user.isPermanent(), user.isActive(), user.getExpirationDate(),
                    user.getID());
        }
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
        if (user.isCustomFileRetention())
        {
            fileRetention = (user.getFileRetention() == null) ? 0 : user.getFileRetention();
        }
        Integer userRetention = null;
        if (user.isCustomUserRetention())
        {
            userRetention = (user.getUserRetention() == null) ? 0 : user.getUserRetention();
        }
        template.update("update quota_groups set quota_file_count = ?, quota_file_size = ?, "
                + "file_retention = ?, user_retention = ? "
                + "where id = (select quota_group_id from users where id = ?)",
                maxFileCountPerQuotaGroup, maxFileSizePerQuotaGroupInMB, fileRetention,
                userRetention, user.getID());
    }

    public void changeUserCode(final String before, final String after)
    {
        getSimpleJdbcTemplate().update("update users set user_code = ? where user_code = ? ",
                after, before);

    }

}
