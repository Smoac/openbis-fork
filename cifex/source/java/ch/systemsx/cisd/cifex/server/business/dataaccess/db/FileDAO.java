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

import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import ch.systemsx.cisd.cifex.server.business.dataaccess.IFileDAO;
import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.common.db.ISequencerHandler;

/**
 * Performs database manipulation on <code>files</code> table.
 * 
 * @author Izabela Adamczyk
 */
final public class FileDAO extends AbstractDAO implements IFileDAO
{
    private static final int MAX_COMMENT_LENGTH = 1000;

    private static final FileWithOwnerRowMapper FILE_WITH_OWNER_ROW_MAPPER =
            new FileWithOwnerRowMapper();

    private static final ParameterizedRowMapper<FileDTO> FILE_ROW_MAPPER = new FileRowMapper();

    private static final ParameterizedRowMapper<UserDTO> SHARING_USER_ROW_MAPPER =
            new SharingUserRowMapper();

    private static final String SELECT_FILES =
            "select f.ID as f_id, f.NAME as f_name, f.PATH as f_path, f.COMMENT as f_comment, "
                    + "f.USER_ID as f_user_id, f.REGISTRATOR_CODE as f_REGISTRATOR_CODE,  "
                    + "f.REGISTRATION_TIMESTAMP as f_registration_timestamp, "
                    + "f.EXPIRATION_TIMESTAMP as f_expiration_timestamp, "
                    + "f.CONTENT_TYPE as f_content_type, f.SIZE as f_size, "
                    + "f.CRC32_CHECKSUM as f_crc32_checkum, f.COMPLETE_SIZE as f_complete_size";

    private static final String FILES_JOIN_USERS =
            SELECT_FILES + ", u.* " + " from files as f "
                    + " left join users as u on f.user_id = u.id";

    private static final String FILES_JOIN_USERS_WHERE_ID = FILES_JOIN_USERS + " where f.id = ?";

    FileDAO(final DataSource dataSource, final ISequencerHandler sequencerHandler,
            final boolean supportsAnyOperator)
    {
        super(dataSource, sequencerHandler, supportsAnyOperator);
    }

    private final long createID()
    {
        return getNextValueOf("FILE_ID_SEQ");
    }

    private final List<UserDTO> listSharingUsers(final long fileId) throws DataAccessException
    {
        final List<UserDTO> list =
                getSimpleJdbcTemplate().query("select * from file_shares where file_id = ?",
                        SHARING_USER_ROW_MAPPER, fileId);
        return list;
    }

    //
    // IFileDAO
    //

    public void createSharingLink(long fileID, long userID) throws DataAccessException
    {
        getSimpleJdbcTemplate().update(
                "insert into file_shares (id, file_id, user_id) " + "values (?,?,?)",
                getNextValueOf("FILE_SHARE_ID_SEQ"), fileID, userID);

    }

    public boolean deleteSharingLink(long fileID, String userCode) throws DataAccessException
    {
        final int affectedRows =
                getSimpleJdbcTemplate().update(
                        "delete from file_shares where file_id = ? and user_id in "
                                + "(select id from users where user_code = ?)", fileID, userCode);
        return affectedRows > 0;

    }

    public final void createFile(final FileDTO file) throws DataAccessException
    {
        assert file != null : "Given file cannot be null.";

        final long id = createID();
        getSimpleJdbcTemplate().update(
                "insert into files (ID, NAME, PATH, COMMENT, USER_ID, REGISTRATOR_CODE, CONTENT_TYPE, "
                        + "SIZE, CRC32_CHECKSUM, EXPIRATION_TIMESTAMP, COMPLETE_SIZE) "
                        + "values (?,?,?,?,?," + "(select user_code from users u where u.id = ?),"
                        + "?,?,?,?,?)", id, file.getName(), file.getPath(),
                StringUtils.abbreviate(file.getComment(), MAX_COMMENT_LENGTH), file.getOwnerId(),
                file.getOwnerId(), file.getContentType(), file.getSize(), file.getCrc32Value(),
                file.getExpirationDate(), file.getCompleteSize());
        file.setID(id);
    }

    /**
     * Updates all fields from <var>file</var> in the database.
     */
    public void updateFile(final FileDTO file) throws DataAccessException
    {
        assert file != null;
        assert file.getID() != null : "File needs an ID, otherwise it can't be updated";

        final SimpleJdbcTemplate template = getSimpleJdbcTemplate();
        template
                .update(
                        "update files set name = ?, path = ?, comment = ?, expiration_timestamp = ?, "
                                + "user_id = ?, content_type = ?, size = ?, crc32_checksum = ?, complete_size = ? "
                                + "where id = ?", file.getName(), file.getPath(), StringUtils
                                .abbreviate(file.getComment(), MAX_COMMENT_LENGTH), file
                                .getExpirationDate(), file.getOwnerId(), file.getContentType(),
                        file.getSize(), file.getCrc32Value(), file.getCompleteSize(), file.getID());
    }

    /**
     * Updates the <var>name</var>, the <var>comment</var> and the <var>expirationDate</var> of the
     * file with given <var>id</var>.
     */
    public void updateFileUserEdit(final long id, final String name, String commentOrNull,
            final Date expirationDate) throws DataAccessException
    {
        assert name != null;
        assert expirationDate != null;

        final SimpleJdbcTemplate template = getSimpleJdbcTemplate();
        template.update("update files set name = ?, comment = ?, expiration_timestamp = ? "
                + "where id = ?", name, StringUtils.abbreviate(commentOrNull, MAX_COMMENT_LENGTH),
                expirationDate, id);
    }

    /**
     * Updates the <var>file</var> in the database with the current upload progress.
     */
    public void updateFileUploadProgress(final long id, final long size, final int crc32,
            final Date expirationDate) throws DataAccessException
    {
        final SimpleJdbcTemplate template = getSimpleJdbcTemplate();

        template.update("update files set size = ?, crc32_checksum = ?, expiration_timestamp = ?"
                + " where id = ?", size, crc32, expirationDate, id);
    }

    public boolean deleteFile(final long id) throws DataAccessException
    {
        final int affectedRows =
                getSimpleJdbcTemplate().update("delete from files where id = ?", id);
        return affectedRows > 0;
    }

    public List<FileDTO> listFiles() throws DataAccessException
    {
        final List<FileDTO> list =
                getSimpleJdbcTemplate().query(FILES_JOIN_USERS, FILE_WITH_OWNER_ROW_MAPPER);
        for (FileDTO file : list)
        {
            final List<UserDTO> sharingUsers = listSharingUsers(file.getID());
            if (sharingUsers.size() > 0)
            {
                file.setSharingUsers(sharingUsers);
            }

        }
        return list;
    }

    public final FileDTO tryGetFile(final long id) throws DataAccessException
    {
        final SimpleJdbcTemplate template = getSimpleJdbcTemplate();
        try
        {
            final FileDTO file =
                    template.queryForObject(FILES_JOIN_USERS_WHERE_ID, FILE_WITH_OWNER_ROW_MAPPER,
                            id);
            final List<UserDTO> sharingUsers = listSharingUsers(id);
            if (sharingUsers.size() > 0)
            {
                file.setSharingUsers(sharingUsers);
            }
            return file;
        } catch (final EmptyResultDataAccessException e)
        {
            return null;
        }
    }

    public Date getFileRegistrationDate(long fileId) throws DataAccessException
    {
        final SimpleJdbcTemplate template = getSimpleJdbcTemplate();
        return template.queryForObject("select registration_timestamp from files where id = ?",
                Date.class, fileId);
    }

    public final List<FileDTO> getExpiredFiles()
    {
        final List<FileDTO> list =
                getSimpleJdbcTemplate().query(
                        SELECT_FILES + " from files as f where f.expiration_timestamp < now() ",
                        FILE_ROW_MAPPER);
        return list;

    }

    public final List<FileDTO> listDownloadFiles(final long userId) throws DataAccessException
    {
        final List<FileDTO> list =
                getSimpleJdbcTemplate()
                        .query(
                                SELECT_FILES
                                        + ", u.* from files f left join users u "
                                        + "on f.user_id = u.id left join file_shares s on s.file_id = f.id "
                                        + "where f.complete_size = f.size and s.user_id = ?",
                                FILE_WITH_OWNER_ROW_MAPPER, userId);
        return list;
    }

    public final List<FileDTO> listDirectlyAndIndirectlyOwnedFiles(final long userId)
            throws DataAccessException
    {
        final List<FileDTO> list =
                getSimpleJdbcTemplate().query(
                        SELECT_FILES
                                + ", u1.* from files f left join users u1 on f.user_id = u1.id "
                                + "left join users u2 on u1.user_id_registrator = u2.id "
                                + "where u1.id = ? or u2.id = ?", FILE_WITH_OWNER_ROW_MAPPER,
                        userId, userId);
        for (FileDTO file : list)
        {
            final List<UserDTO> sharingUsers = listSharingUsers(file.getID());
            if (sharingUsers.size() > 0)
            {
                file.setSharingUsers(sharingUsers);
            }

        }
        return list;
    }

    /**
     * Returns a partially uploaded file that is a candidate for resuming upload. Candidates are
     * defined by being uploaded by the same user, having the same file name and the same complete
     * size. If there are multiple files which are candidates, a random one is provided. If there is
     * no candidate, <code>null</code> is returned.
     */
    public FileDTO tryGetResumeCandidate(final long userId, final String fileName,
            final long completeSize)
    {
        final List<FileDTO> list =
                getSimpleJdbcTemplate().query(
                        SELECT_FILES + " from files f where f.user_id = ? and f.name = ? and "
                                + "f.complete_size = ? and f.size < f.complete_size "
                                + "order by f.size desc limit 1", FILE_ROW_MAPPER, userId,
                        fileName, completeSize);
        if (list.isEmpty())
        {
            return null;
        } else
        {
            return list.get(0);
        }
    }

    //
    // Helper classes
    //

    private static final class FileRowMapper implements ParameterizedRowMapper<FileDTO>
    {

        private static final FileDTO fillSimpleFileFromResultSet(final ResultSet rs)
                throws SQLException
        {
            final long registererId = rs.getLong("f_USER_ID");
            final FileDTO file = new FileDTO(registererId);
            final Date expDate = new Date(rs.getTimestamp("f_EXPIRATION_TIMESTAMP").getTime());
            file.setExpirationDate(expDate);
            file.setID(rs.getLong("f_ID"));
            file.setName(rs.getString("f_NAME"));
            file.setPath(rs.getString("f_PATH"));
            file.setComment(rs.getString("f_COMMENT"));
            file.setContentType(rs.getString("f_CONTENT_TYPE"));
            file.setCompleteSize(rs.getLong("f_COMPLETE_SIZE"));
            final long size = rs.getLong("f_SIZE");
            if (rs.wasNull() == false)
            {
                file.setSize(size);
            }
            final int crc32Value = rs.getInt("f_crc32_checkum");
            if (rs.wasNull() == false)
            {
                file.setCrc32Value(crc32Value);
            }
            final Date regDate = new Date(rs.getTimestamp("f_REGISTRATION_TIMESTAMP").getTime());
            file.setRegistrationDate(regDate);
            file.setRegistratorCode(rs.getString("f_REGISTRATOR_CODE"));
            return file;
        }

        //
        // ParameterizedRowMapper
        //

        public final FileDTO mapRow(final ResultSet rs, final int rowNum) throws SQLException
        {
            return fillSimpleFileFromResultSet(rs);
        }
    }

    private static final class FileWithOwnerRowMapper implements ParameterizedRowMapper<FileDTO>
    {
        private final static UserDAO.UserRowMapper userRowMapper = new UserDAO.UserRowMapper();

        private static final FileDTO fillFileWithOwnerFromResultSet(final ResultSet rs)
                throws SQLException
        {
            final FileDTO file = FileRowMapper.fillSimpleFileFromResultSet(rs);
            final UserDTO owner = userRowMapper.mapRow(rs, 1);
            file.setOwner(owner);
            return file;
        }

        //
        // ParameterizedRowMapper
        //

        public final FileDTO mapRow(final ResultSet rs, final int rowNum) throws SQLException
        {
            return fillFileWithOwnerFromResultSet(rs);
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
