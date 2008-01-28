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
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import ch.systemsx.cisd.cifex.server.business.dataaccess.IUserDAO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.common.db.ISequencerHandler;

/**
 * Performs database manipulation on <code>users</code> table.
 * 
 * @author Basil Neff
 */
final class UserDAO extends AbstractDAO implements IUserDAO
{

    public static final class UserRowMapper implements ParameterizedRowMapper<UserDTO>
    {

        public final UserDTO mapRow(final ResultSet rs, final int rowNum) throws SQLException
        {
            final UserDTO user = fillUserFromResultSet(rs);
            return user;
        }

        /**
         * Requires <code>id</code>, <code>email</code>, <code>user_name</code>,
         * <code>encrypted_password</code>, <code>is_externally_authenticated</code>, <code>is_admin</code>,
         * <code>is_permanent</code>, <code>registration_timestamp</code>, <code>expiration_timestamp</code> to
         * be present in the {@link ResultSet} <var>rs</var>.
         */
        final public static UserDTO fillUserFromResultSet(final ResultSet rs) throws SQLException
        {
            final UserDTO user = new UserDTO();
            user.setID(rs.getLong("id"));
            user.setEmail(rs.getString("email"));
            user.setUserName(rs.getString("user_name"));
            user.setEncryptedPassword(rs.getString("encrypted_password"));
            user.setExternallyAuthenticated(rs.getBoolean("is_externally_authenticated"));
            user.setAdmin(rs.getBoolean("is_admin"));
            user.setPermanent(rs.getBoolean("is_permanent"));
            user.setRegistrationDate(tryConvertToDate(rs, "registration_timestamp"));
            user.setExpirationDate(tryConvertToDate(rs, "expiration_timestamp"));
            return user;
        }

        private static Date tryConvertToDate(final ResultSet rs, final String fieldName) throws SQLException
        {
            final Timestamp timestamp = rs.getTimestamp(fieldName);
            if (timestamp != null)
            {
                return new Date(timestamp.getTime());
            }
            return null;
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

    public List<UserDTO> listUsers() throws DataAccessException
    {
        final SimpleJdbcTemplate template = getSimpleJdbcTemplate();
        final List<UserDTO> list = template.query("select * from users", new UserRowMapper());
        return list;
    }

    public void createUser(final UserDTO user) throws DataAccessException
    {
        assert user != null : "Given user can not be null.";

        final Long id = createID();

        final SimpleJdbcTemplate template = getSimpleJdbcTemplate();
        template.update(
                "insert into users (id, email, user_name, encrypted_password, is_externally_authenticated, is_admin,"
                        + "is_permanent, expiration_timestamp) values (?,?,?,?,?,?,?,?)", id, user.getEmail(), user
                        .getUserName(), user.getEncryptedPassword(), user.isExternallyAuthenticated(), user.isAdmin(),
                user.isPermanent(), user.getExpirationDate());

        user.setID(id);
    }

    public UserDTO tryFindUserByEmail(final String email) throws DataAccessException
    {
        assert StringUtils.isNotBlank(email) : "No email specified!";

        final SimpleJdbcTemplate template = getSimpleJdbcTemplate();
        try
        {
            final UserDTO user =
                    template.queryForObject("select * from users where email = ?", new UserRowMapper(), email);
            return user;
        } catch (final EmptyResultDataAccessException e)
        {
            return null;
        }
    }

    public boolean removeUser(final Long userID)
    {
        assert userID != null : "Given userID can not be null!";

        final SimpleJdbcTemplate template = getSimpleJdbcTemplate();
        final int affectedRows = template.update("delete from users where id = ?", userID);

        if (affectedRows > 0)
        {
            return true;
        } else
        {
            return false;
        }
    }

    public List<UserDTO> listExpiredUsers() throws DataAccessException
    {
        final SimpleJdbcTemplate template = getSimpleJdbcTemplate();
        final List<UserDTO> list =
                template.query("select * from users where expiration_timestamp < now() ", new UserRowMapper());
        return list;
    }
}
