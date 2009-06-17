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

package ch.systemsx.cisd.yeastx.etl;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.io.FilenameUtils;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.yeastx.db.DBFactory;
import ch.systemsx.cisd.yeastx.db.DMDataSetDTO;
import ch.systemsx.cisd.yeastx.eicml.EICML2Database;
import ch.systemsx.cisd.yeastx.fiaml.FIAML2Database;

/**
 * Extracts and uploads information from dataset files (like <code>eicML</code> or
 * <code>fiaML</code> files) to the additional database.
 * 
 * @author Tomasz Pylak
 */
public class ML2DatabaseUploader
{
    private final Connection connection;

    public ML2DatabaseUploader()
    {
        this.connection = getDatabaseConnection();
    }

    private static Connection getDatabaseConnection() throws EnvironmentFailureException
    {
        try
        {
            return new DBFactory(DBFactory.createDefaultDBContext()).getConnection();
        } catch (SQLException e)
        {
            throw EnvironmentFailureException.fromTemplate(e,
                    "Cannot connect to the database which stores transformed mzXML files.");
        }
    }

    /** uploads files with recognized extensions to the additional database */
    public void upload(File dataSet, DataSetInformation dataSetInformation)
            throws EnvironmentFailureException
    {
        String datasetPermId = dataSetInformation.getDataSetCode();
        String extension = getExtension(dataSet);
        try
        {
            if (extension.equalsIgnoreCase(ConstantsYeastX.FIAML_EXT))
            {
                translateFIA(dataSet, datasetPermId);
            } else if (extension.equalsIgnoreCase(ConstantsYeastX.EICML_EXT))
            {
                translateEIC(dataSet, datasetPermId);
            } else
            {
                // do nothing
            }
        } catch (SQLException e)
        {
            throw EnvironmentFailureException
                    .fromTemplate(
                            e,
                            "A database error occured while extracting additional information from '%s' file content for '%s' dataset.",
                            dataSet.getPath(), datasetPermId);
        }
    }

    private void translateEIC(File dataSet, String datasetPermId) throws SQLException
    {
        EICML2Database.uploadEicMLFile(connection, dataSet, new DMDataSetDTO(datasetPermId,
                "sample perm id", "sample name", "experiment perm id", "eperiment name"));
    }

    private void translateFIA(File dataSet, String datasetPermId) throws SQLException
    {
        FIAML2Database.uploadFiaMLFile(connection, dataSet, new DMDataSetDTO(datasetPermId,
                "sample perm id", "sample name", "experiment perm id", "experiment name"));
    }

    private static String getExtension(final File incomingDataSetPath)
    {
        return FilenameUtils.getExtension(incomingDataSetPath.getName()).toLowerCase();
    }

}
