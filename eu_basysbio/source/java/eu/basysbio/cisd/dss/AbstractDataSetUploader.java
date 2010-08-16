/*
 * Copyright 2010 ETH Zuerich, CISD
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

package eu.basysbio.cisd.dss;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import net.lemnik.eodsql.QueryTool;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
abstract class AbstractDataSetUploader implements IDataSetUploader
{
    protected final ITimeSeriesDAO dao;

    protected final IEncapsulatedOpenBISService service;

    protected final TimeSeriesDataSetUploaderParameters parameters;

    private Connection connection;

    AbstractDataSetUploader(DataSource dataSource, IEncapsulatedOpenBISService service,
            TimeSeriesDataSetUploaderParameters parameters)
    {
        this.service = service;
        this.parameters = parameters;
        try
        {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            dao = QueryTool.getQuery(connection, ITimeSeriesDAO.class);
        } catch (SQLException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    AbstractDataSetUploader(ITimeSeriesDAO dao, IEncapsulatedOpenBISService service,
            TimeSeriesDataSetUploaderParameters parameters)
    {
        this.dao = dao;
        this.service = service;
        this.parameters = parameters;
    }

    /** the uploader should not be used after calling this method */
    public void commit()
    {
        try
        {
            if (connection != null)
            {
                connection.commit();
            }
            dao.close();
        } catch (SQLException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            connection = null;
        }
    }

    /** the uploader should not be used after calling this method */
    public void rollback()
    {
        try
        {
            if (connection != null)
            {
                connection.rollback();
            }
            dao.close();
        } catch (SQLException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            connection = null;
        }
    }

    public void upload(File originalData, DataSetInformation dataSetInformation)
    {
        ExperimentIdentifier experimentIdentifier = dataSetInformation.getExperimentIdentifier();
        if (experimentIdentifier == null)
        {
            throw new UserFailureException(
                    "Data set should be registered for an experiment and not for a sample.");
        }
        if (originalData.isFile())
        {
            handleTSVFile(originalData, dataSetInformation);
        } else
        {
            File[] tsvFiles = originalData.listFiles(new FilenameFilter()
                {
                    public boolean accept(File dir, String name)
                    {
                        String lowerCaseName = name.toLowerCase();
                        return lowerCaseName.endsWith(".txt") || lowerCaseName.endsWith(".tsv");
                    }
                });
            if (tsvFiles == null || tsvFiles.length == 0)
            {
                throw new UserFailureException("No files of type "
                        + "'.txt', '.TXT', '.tsv', or '.TSV'. found in " + originalData);
            }
            for (File tsvFile : tsvFiles)
            {
                handleTSVFile(tsvFile, dataSetInformation);
            }
        }

    }

    protected abstract void handleTSVFile(File tsvFile, DataSetInformation dataSetInformation);

}
