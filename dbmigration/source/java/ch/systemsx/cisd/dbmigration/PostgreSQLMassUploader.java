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

package ch.systemsx.cisd.dbmigration;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.postgresql.PGConnection;
import org.postgresql.copy.CopyManager;

import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * A {@link IMassUploader} for the PostgreSQL database.
 * 
 * @author Bernd Rinn
 */
public class PostgreSQLMassUploader implements IMassUploader
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, PostgreSQLMassUploader.class);

    private final DataSource dataSource;

    public PostgreSQLMassUploader(DataSource dataSource) throws SQLException
    {
        this.dataSource = dataSource;
    }

    public void performMassUpload(File massUploadFile)
    {
        try
        {
            final CopyManager copyManager = getPGConnection().getCopyAPI();
            final String[] splitName = StringUtils.split(massUploadFile.getName(), "=");
            assert splitName.length == 2;
            final String tableNameWithExtension = splitName[1];
            assert tableNameWithExtension.endsWith(".csv");
            final String tableName =
                    tableNameWithExtension.substring(0, tableNameWithExtension.length() - ".csv".length());
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Perform mass upload of file '" + massUploadFile + "' to table '" + tableName + "'.");
            }
            final InputStream is = new FileInputStream(massUploadFile);
            try
            {
                copyManager.copyInQuery("COPY " + tableName + " FROM STDIN WITH CSV HEADER", is);
            } finally
            {
                IOUtils.closeQuietly(is);
            }
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private PGConnection getPGConnection() throws SQLException, NoSuchFieldException, IllegalAccessException
    {
        return getPGConnection(dataSource.getConnection());
    }

    private PGConnection getPGConnection(Connection conn) throws SQLException, NoSuchFieldException,
            IllegalAccessException
    {
        if (conn instanceof PGConnection)
        {
            return (PGConnection) conn;
        }
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Found connection of type '" + conn.getClass().getCanonicalName() + "'.");
        }
        Field delegateField = getField(conn.getClass(), "_conn");
        if (delegateField == null)
        {
            throw new RuntimeException("No PostgreSQL driver found - cannot perform mass upload.");
        }
        delegateField.setAccessible(true);
        return getPGConnection((Connection) delegateField.get(conn));
    }

    private static Field getField(Class<?> clazz, String fieldName)
    {
        assert fieldName != null;
        if (clazz == null)
        {
            return null;
        }

        for (Field field : clazz.getDeclaredFields())
        {
            if (fieldName.equals(field.getName()))
            {
                return field;
            }
        }
        return getField(clazz.getSuperclass(), fieldName);
    }

}
