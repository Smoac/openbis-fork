/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.dbmigration.postgresql;

import static ch.systemsx.cisd.dbmigration.MassUploadFileType.CSV;
import static ch.systemsx.cisd.dbmigration.MassUploadFileType.TSV;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.postgresql.PGConnection;
import org.postgresql.copy.CopyManager;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;

import ch.systemsx.cisd.common.db.ISequenceNameMapper;
import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.dbmigration.IMassUploader;

/**
 * A {@link IMassUploader} for the PostgreSQL database.
 * 
 * @author Bernd Rinn
 */
public class PostgreSQLMassUploader extends SimpleJdbcDaoSupport implements IMassUploader
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, PostgreSQLMassUploader.class);

    private final DataSource dataSource;

    private CopyManager copyManager;

    private final ISequenceNameMapper sequenceNameMapper;

    /**
     * Creates an instance for the specified data source and sequence mapper.
     */
    public PostgreSQLMassUploader(final DataSource dataSource,
            final ISequenceNameMapper sequenceNameMapper) throws SQLException
    {
        this.dataSource = dataSource;
        this.sequenceNameMapper = sequenceNameMapper;
        setDataSource(dataSource);
    }

    private final CopyManager getCopyManager() throws SQLException, NoSuchFieldException,
            IllegalAccessException
    {
        if (copyManager == null)
        {
            copyManager = getPGConnection().getCopyAPI();
        }
        return copyManager;
    }

    public final void performMassUpload(final File[] massUploadFiles)
    {
        final Set<String> tables = new LinkedHashSet<String>();
        for (final File file : massUploadFiles)
        {
            performMassUpload(file, tables);
        }
        for (final String name : tables)
        {
            fixSequence(name);
        }
    }

    private final void performMassUpload(final File massUploadFile, final Set<String> tables)
    {
        try
        {
            final String[] splitName = StringUtils.split(massUploadFile.getName(), "=");
            assert splitName.length == 2 : "Missing '=' in name of file '"
                    + massUploadFile.getName() + "'.";
            final String tableNameWithExtension = splitName[1];
            final boolean csvFileType = CSV.isOfType(tableNameWithExtension);
            final boolean tsvFileType = TSV.isOfType(tableNameWithExtension);
            assert tsvFileType || csvFileType : "Non of expected file types [" + TSV.getFileType()
                    + ", " + CSV.getFileType() + "]: " + massUploadFile.getName();
            final String tableName =
                    tableNameWithExtension.substring(0, tableNameWithExtension.lastIndexOf('.'));
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Perform mass upload of file '" + massUploadFile + "' to table '"
                        + tableName + "'.");
            }
            final InputStream is = new FileInputStream(massUploadFile);
            try
            {
                if (tsvFileType)
                {
                    getCopyManager().copyIn(tableName, is);
                } else
                {
                    getCopyManager().copyInQuery(
                            "COPY " + tableName + " FROM STDIN WITH CSV HEADER", is);
                    tables.add(tableName);
                }
            } finally
            {
                IOUtils.closeQuietly(is);
            }
        } catch (final Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private final void fixSequence(final String tableName)
    {
        final String sequenceName = sequenceNameMapper.getSequencerForTable(tableName);
        if (sequenceName == null)
        {
            return;
        }
        try
        {
            // The result returned by setval is just the value of its second argument.
            final long newSequenceValue =
                    getSimpleJdbcTemplate().queryForLong(
                            String.format("select setval('%s', max(id)) from %s", sequenceName,
                                    tableName));
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Updating sequence " + sequenceName + " for table " + tableName
                        + " to value " + newSequenceValue);
            }
        } catch (final DataAccessException ex)
        {
            operationLog.error("Failed to set new value for sequence '" + sequenceName
                    + "' of table '" + tableName + "'.", ex);
        }
    }

    private final PGConnection getPGConnection() throws SQLException, NoSuchFieldException,
            IllegalAccessException
    {
        return getPGConnection(dataSource.getConnection());
    }

    private final PGConnection getPGConnection(final Connection conn) throws SQLException,
            NoSuchFieldException, IllegalAccessException
    {
        if (conn instanceof PGConnection)
        {
            return (PGConnection) conn;
        }
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Found connection of type '" + conn.getClass().getCanonicalName()
                    + "'.");
        }
        final Field delegateField = getField(conn.getClass(), "_conn");
        if (delegateField == null)
        {
            throw new RuntimeException("No PostgreSQL driver found - cannot perform mass upload.");
        }
        delegateField.setAccessible(true);
        return getPGConnection((Connection) delegateField.get(conn));
    }

    private final static Field getField(final Class<?> clazz, final String fieldName)
    {
        assert fieldName != null;
        if (clazz == null)
        {
            return null;
        }

        for (final Field field : clazz.getDeclaredFields())
        {
            if (fieldName.equals(field.getName()))
            {
                return field;
            }
        }
        return getField(clazz.getSuperclass(), fieldName);
    }

}
