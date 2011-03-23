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

package ch.systemsx.cisd.etlserver.api.v1;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.io.ConcatenatedFileOutputStreamWriter;
import ch.systemsx.cisd.etlserver.DefaultStorageProcessor;
import ch.systemsx.cisd.etlserver.ITopLevelDataSetRegistrator;
import ch.systemsx.cisd.etlserver.ITopLevelDataSetRegistratorDelegate;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwner;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * A helper class for carrying out the put command for creating data sets.
 * <p>
 * It is a data set handler that allows overriding information obtained from the extractors.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class PutDataSetTopLevelDataSetHandler
{
    private static class DataSetRegistratorDelegate implements ITopLevelDataSetRegistratorDelegate
    {
        private final ArrayList<DataSetInformation> registeredDataSets =
                new ArrayList<DataSetInformation>();

        public void didRegisterDataSets(List<DataSetInformation> dataSetInformations)
        {
            registeredDataSets.addAll(dataSetInformations);
        }

    }

    // General State
    private final PutDataSetService service;

    // Command Context State
    private final ITopLevelDataSetRegistrator registrator;

    private final String sessionToken;

    private final NewDataSetDTO newDataSet;

    private final InputStream inputStream;

    private final File temporaryIncomingDir;

    private final File dataSetDir;

    PutDataSetTopLevelDataSetHandler(PutDataSetService service,
            ITopLevelDataSetRegistrator registrator, String sessionToken, NewDataSetDTO newDataSet,
            InputStream inputStream)
    {
        this.service = service;
        this.registrator = registrator;
        this.sessionToken = sessionToken;
        this.newDataSet = newDataSet;
        this.inputStream = inputStream;
        this.temporaryIncomingDir = service.createTemporaryIncomingDir();
        this.dataSetDir = new File(temporaryIncomingDir, newDataSet.getDataSetFolderName());
        if (dataSetDir.exists())
        {
            deleteDataSetDir();
        }
        if (false == this.dataSetDir.mkdir())
        {
            throw new EnvironmentFailureException("Could not create directory for data set "
                    + newDataSet.getDataSetFolderName());
        }

    }

    /**
     * Run the put command; does *not* close the input stream &mdash; clients of the executor are
     * expected to close the input stream when appropriate.
     * 
     * @throws IOException
     */
    public List<DataSetInformation> execute() throws UserFailureException, IOException
    {
        // Check that the session owner has at least user access to the space the new data
        // set should belongs to
        SpaceIdentifier spaceId = getSpaceIdentifierForNewDataSet();
        getOpenBisService().checkSpaceAccess(sessionToken, spaceId);

        writeDataSetToTempDirectory();

        // Register the data set
        try
        {
            DataSetRegistratorDelegate delegate = new DataSetRegistratorDelegate();
            registrator.handle(dataSetDir, getCallerDataSetInformation(), delegate);
            return delegate.registeredDataSets;
        } finally
        {
            deleteDataSetDir();
        }

    }

    public SessionContextDTO getSessionContext()
    {
        return getOpenBisService().tryGetSession(sessionToken);
    }

    public File getFileForExternalData(ExternalData externalData, String shareId)
    {
        File share = new File(service.getStoreRootDirectory(), shareId);
        File dataSetFile = new File(share, externalData.getLocation());
        return DefaultStorageProcessor.getOriginalDirectory(dataSetFile);
    }

    public DataSetOwner getDataSetOwner()
    {
        DataSetOwner owner = newDataSet.getDataSetOwner();
        return owner;
    }

    public DataSetInformation getCallerDataSetInformation()
    {
        DataSetInformation dataSetInfo = new DataSetInformation();
        DataSetOwner owner = getDataSetOwner();
        switch (owner.getType())
        {
            case EXPERIMENT:
                dataSetInfo.setExperimentIdentifier(tryExperimentIdentifier());
                break;
            case SAMPLE:
                SampleIdentifier sampleId = trySampleIdentifier();

                dataSetInfo.setSampleCode(sampleId.getSampleCode());
                dataSetInfo.setSpaceCode(sampleId.getSpaceLevel().getSpaceCode());
                dataSetInfo.setInstanceCode(sampleId.getSpaceLevel().getDatabaseInstanceCode());
                break;
        }
        String typeCode = newDataSet.tryDataSetType();
        if (null != typeCode)
        {
            dataSetInfo.setDataSetType(new DataSetType(typeCode));
        }

        Map<String, String> primitiveProps = newDataSet.getProperties();
        if (false == primitiveProps.isEmpty())
        {
            ArrayList<NewProperty> properties = new ArrayList<NewProperty>();
            for (String key : primitiveProps.keySet())
            {
                properties.add(new NewProperty(key, primitiveProps.get(key)));
            }
            dataSetInfo.setDataSetProperties(properties);
        }

        return dataSetInfo;
    }

    private void writeDataSetToTempDirectory() throws IOException
    {
        ConcatenatedFileOutputStreamWriter imagesWriter =
                new ConcatenatedFileOutputStreamWriter(inputStream);
        for (FileInfoDssDTO fileInfo : newDataSet.getFileInfos())
        {
            if (fileInfo.isDirectory())
            {
                // Just make the directory
                File file = new File(dataSetDir, fileInfo.getPathInDataSet());
                file.mkdir();
            } else
            {
                // Download the file -- the directory should have already been made
                OutputStream output = getOutputStream(fileInfo);
                imagesWriter.writeNextBlock(output);
                output.flush();
                output.close();
            }
        }
    }

    private OutputStream getOutputStream(FileInfoDssDTO fileInfo)
    {
        File file = new File(dataSetDir, fileInfo.getPathInDataSet());

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
        deleteDir(temporaryIncomingDir);
    }

    private void deleteDir(File dirToDelete)
    {
        try
        {
            FileUtils.deleteDirectory(dirToDelete);
        } catch (IOException ex)
        {
            getOperationLog().error("Could not delete data set directory " + dirToDelete, ex);
            ex.printStackTrace();
        }
    }

    private SpaceIdentifier getSpaceIdentifierForNewDataSet()
    {
        SpaceIdentifier spaceId = null;
        DataSetOwner owner = getDataSetOwner();
        switch (owner.getType())
        {
            case EXPERIMENT:
            {
                ExperimentIdentifier experimentId = tryExperimentIdentifier();
                spaceId =
                        new SpaceIdentifier(experimentId.getDatabaseInstanceCode(),
                                experimentId.getSpaceCode());
                break;
            }
            case SAMPLE:
            {
                SampleIdentifier sampleId = trySampleIdentifier();
                spaceId = sampleId.getSpaceLevel();
                break;
            }
        }
        return spaceId;
    }

    private ExperimentIdentifier tryExperimentIdentifier()
    {
        DataSetOwner owner = getDataSetOwner();
        switch (owner.getType())
        {
            case EXPERIMENT:
            {
                return new ExperimentIdentifierFactory(owner.getIdentifier()).createIdentifier();
            }
            case SAMPLE:
            {
                return null;
            }
        }

        return null;
    }

    private SampleIdentifier trySampleIdentifier()
    {
        DataSetOwner owner = getDataSetOwner();
        switch (owner.getType())
        {
            case EXPERIMENT:
            {
                return null;
            }
            case SAMPLE:
            {
                return new SampleIdentifierFactory(owner.getIdentifier()).createIdentifier();

            }
        }

        return null;
    }

    private IEncapsulatedOpenBISService getOpenBisService()
    {
        return service.getOpenBisService();
    }

    private Logger getOperationLog()
    {
        return service.getOperationLog();
    }
}
