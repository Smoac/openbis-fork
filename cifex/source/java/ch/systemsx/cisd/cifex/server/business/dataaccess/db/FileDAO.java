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
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import ch.systemsx.cisd.cifex.server.business.dataaccess.IFileDAO;
import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.common.db.ISequencerHandler;

/**
 * Performs database manipulation on <code>files</code> table
 * 
 * @author Izabela Adamczyk
 */
final public class FileDAO extends AbstractDAO implements IFileDAO
{
    private static final ParameterizedRowMapper<FileDTO> FILE_ROW_MAPPER = new FileRowMapper();

    private static final ParameterizedRowMapper<UserDTO> SHARING_USER_ROW_MAPPER = new SharingUserRowMapper();

    private static final String SELECT =
            "select f.ID as f_id, f.NAME as f_name, f.PATH as f_path, f.USER_ID_REGISTERER as f_user_id_registerer, "
                    + "f.REGISTRATION_TIMESTAMP as f_registration_timestamp, "
                    + "f.EXPIRATION_TIMESTAMP as f_expiration_timestamp";

    private static final String FILES_JOIN_USERS =
            SELECT + ", u.* " + " from files as f " + " join users as u on f.user_id_registerer = u.id where f.id = ?";

    FileDAO(DataSource dataSource, ISequencerHandler sequencerHandler)
    {
        super(dataSource, sequencerHandler);
    }

    public final void createFile(FileDTO file) throws DataAccessException
    {// FIXME 2008-01-24, Izabela: This method should be transactional (check if other are)
        assert file != null : "Given file cannot be null.";
        final long id = createID();
        final long registererId = file.getRegisterer().getID();
        getSimpleJdbcTemplate().update(
                "insert into files (ID, NAME, PATH, USER_ID_REGISTERER, REGISTRATION_TIMESTAMP, EXPIRATION_TIMESTAMP) "
                        + "values (?,?,?,?,?,?)", id, file.getName(), file.getPath(), registererId,
                file.getRegistrationDate(), file.getExpirationDate());
        List<UserDTO> sharingUsers = file.getSharingUsers();
        if (sharingUsers != null)
        {
            for (UserDTO sharingUser : sharingUsers)
            {
                getSimpleJdbcTemplate().update("insert into file_shares (file_id, user_id) " + "values (?,?)", id,
                        sharingUser.getID());
            }
        }
        file.setID(id);
    }

    private final long createID()
    {
        return getNextValueOf("FILE_ID_SEQ");
    }

    public void deleteFile(Long id) throws DataAccessException
    {
        assert id != null : "Given file id can not be null!";
        // getSimpleJdbcTemplate().update("delete from file_shares where file_id = ?", id);
        getSimpleJdbcTemplate().update("delete from files where id = ?", id);
    }

    public List<FileDTO> listFiles() throws DataAccessException
    {
        final List<FileDTO> list = getSimpleJdbcTemplate().query(SELECT + " from files as f", FILE_ROW_MAPPER);
        return list;
    }

    /** Returns file with given id. Fills registerer field and sharing users ids */
    public FileDTO tryGetFile(Long id) throws DataAccessException
    {
        assert id != null : "Given file id can not be null!";
        final SimpleJdbcTemplate template = getSimpleJdbcTemplate();
        final FileDTO file = template.queryForObject(FILES_JOIN_USERS, new FileWithRegistererRowMapper(), id);
        List<UserDTO> sharingUsers = listSharingUsers(id);
        if (sharingUsers.size() > 0)
        {
            file.setSharingUsers(sharingUsers);
        }
        return file;
    }

    private List<UserDTO> listSharingUsers(Long fileId) throws DataAccessException
    {
        assert fileId != null : "File id cannot be null";
        final List<UserDTO> list =
                getSimpleJdbcTemplate().query("select * from file_shares where file_id = ?", SHARING_USER_ROW_MAPPER,
                        fileId);
        return list;
    }

    private static final class FileRowMapper implements ParameterizedRowMapper<FileDTO>
    {
        public final FileDTO mapRow(final ResultSet rs, final int rowNum) throws SQLException
        {
            return fillSimpleFileFromResultSet(rs);
        }

        private static final FileDTO fillSimpleFileFromResultSet(final ResultSet rs) throws SQLException
        {
            final FileDTO file = new FileDTO();
            final Date expDate = new Date(rs.getTimestamp("f_EXPIRATION_TIMESTAMP").getTime());
            file.setExpirationDate(expDate);
            file.setID(rs.getLong("f_ID"));
            file.setName(rs.getString("f_NAME"));
            file.setPath(rs.getString("f_PATH"));

            final UserDTO registerer = new UserDTO();
            registerer.setID(rs.getLong("f_USER_ID_REGISTERER"));
            file.setRegisterer(registerer);

            final Date regDate = new Date(rs.getTimestamp("f_REGISTRATION_TIMESTAMP").getTime());
            file.setRegistrationDate(regDate);
            return file;
        }
    }

    private static final class FileWithRegistererRowMapper implements ParameterizedRowMapper<FileDTO>
    {
        public final FileDTO mapRow(final ResultSet rs, final int rowNum) throws SQLException
        {
            return fillFileWithRegistererFromResultSet(rs);
        }

        private static final FileDTO fillFileWithRegistererFromResultSet(final ResultSet rs) throws SQLException
        {
            FileDTO file = FileRowMapper.fillSimpleFileFromResultSet(rs);
            UserDTO registerer = UserDAO.UserRowMapper.fillUserFromResultSet(rs);
            file.setRegisterer(registerer);
            return file;
        }
    }

    private static final class SharingUserRowMapper implements ParameterizedRowMapper<UserDTO>
    {
        public final UserDTO mapRow(final ResultSet rs, final int rowNum) throws SQLException
        {
            UserDTO sharingUser = new UserDTO();
            sharingUser.setID(rs.getLong("USER_ID"));
            return sharingUser;
        }
    }

}
