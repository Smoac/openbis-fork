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

package ch.systemsx.cisd.openbis.dss.generic.server.api.v1;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.io.ConcatenatedFileOutputStreamWriter;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwner;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwnerType;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * A helper class for carrying out the put command for creating data sets.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class PutDataSetExecutor
{
    // General State
    private final IEncapsulatedOpenBISService openBisService;

    // Command Context State
    private final String sessionToken;

    private final NewDataSetDTO newDataSet;

    private final InputStream inputStream;

    private final File dataSetDir;

    PutDataSetExecutor(IEncapsulatedOpenBISService openBisService, File incomingDir,
            String sessionToken, NewDataSetDTO newDataSet, InputStream inputStream)
    {
        this.openBisService = openBisService;
        this.sessionToken = sessionToken;
        this.newDataSet = newDataSet;
        this.inputStream = inputStream;
        this.dataSetDir = new File(incomingDir, newDataSet.getDataSetFolderName());
        if (false == this.dataSetDir.mkdir())
        {
            throw new EnvironmentFailureException("Could not create directory for data set "
                    + newDataSet.getDataSetFolderName());
        }
    }

    /**
     * Run the put command; does *not* close the input stream &mdash; clients are expected to close
     * the input stream when appropriate.
     * 
     * @throws IOException
     */
    public void execute() throws UserFailureException, IOException
    {
        // Check that the session owner has at least user access to the space the new data
        // set should belongs to
        SpaceIdentifier spaceId = getSpaceIdentifierForNewDataSet();
        openBisService.checkSpaceAccess(sessionToken, spaceId);

        writeDataSetToTempDirectory();

        // TODO: When registering, set the registrator to the session owner; only an admin on
        // the space or an ETL server can override.

        // Register data set

        deleteDataSetDir();
    }

    private void writeDataSetToTempDirectory() throws IOException
    {
        ConcatenatedFileOutputStreamWriter imagesWriter =
                new ConcatenatedFileOutputStreamWriter(inputStream);
        for (FileInfoDssDTO fileInfo : newDataSet.getFileInfos())
        {
            OutputStream output = getOutputStream(fileInfo);
            imagesWriter.writeNextBlock(output);
        }
    }

    private OutputStream getOutputStream(FileInfoDssDTO fileInfo)
    {
        File file = new File(dataSetDir, fileInfo.getPathInDataSet());
        System.out.println(file);

        FileOutputStream fos;
        try
        {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException ex)
        {
            // This should not happen since we just created the directory to store the new file in
            throw new EnvironmentFailureException("Could not save file " + file, ex);
        }
        return new BufferedOutputStream(fos);
    }

    private void deleteDataSetDir()
    {
        try
        {
            FileUtils.deleteDirectory(dataSetDir);
        } catch (IOException ex)
        {
            DssServiceRpcGeneric.getOperationLog().error(
                    "Could not delete data set directory " + dataSetDir, ex);
            ex.printStackTrace();
        }
    }

    private SpaceIdentifier getSpaceIdentifierForNewDataSet()
    {
        SpaceIdentifier spaceId;
        DataSetOwner owner = newDataSet.getDataSetOwner();
        if (DataSetOwnerType.EXPERIMENT == owner.getType())
        {
            ExperimentIdentifier experimentId =
                    new ExperimentIdentifierFactory(owner.getIdentifier()).createIdentifier();
            spaceId =
                    new SpaceIdentifier(experimentId.getDatabaseInstanceCode(), experimentId
                            .getSpaceCode());
        } else
        {
            SampleIdentifier sampleId =
                    new SampleIdentifierFactory(owner.getIdentifier()).createIdentifier();
            spaceId = sampleId.getSpaceLevel();
        }
        return spaceId;
    }
}
