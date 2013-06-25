/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.BooleanStatus;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.time.TimingParameters;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.FilteredHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.IHierarchicalContentNodeFilter;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.ZipBasedHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.server.AbstractDataSetPackager;
import ch.systemsx.cisd.openbis.dss.generic.server.ZipDataSetPackager;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.IdentifierAttributeMappingManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DataSetExistenceChecker;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

import de.schlichtherle.io.rof.SimpleReadOnlyFile;
import de.schlichtherle.util.zip.BasicZipFile;
import de.schlichtherle.util.zip.ZipEntry;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class DistributedPackagingDataSetFileOperationsManager implements IDataSetFileOperationsManager
{
    static final String MAPPING_FILE_KEY = "mapping-file";
    
    static final String CREATE_ARCHIVES_KEY = MAPPING_FILE_KEY + ".create-archives";

    static final String DEFAULT_DESTINATION_KEY = "default-archive-folder";
    
    static final String WITH_SHARDING_KEY = "with-sharding";

    static final String COMPRESS_KEY = "compressing";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, DistributedPackagingDataSetFileOperationsManager.class);
    
    private static final IHierarchicalContentNodeFilter FILTER = new IHierarchicalContentNodeFilter()
        {
            @Override
            public boolean accept(IHierarchicalContentNode node)
            {
                return AbstractDataSetPackager.META_DATA_FILE_NAME.equals(node.getRelativePath()) == false;
            }
        };
        
    private boolean compress;
    
    private File defaultFolder;

    private boolean withSharding;

    private String mappingFilePathOrNull;
    
    private boolean createArchives;
    
    private transient IEncapsulatedOpenBISService service;
    
    private transient IHierarchicalContentProvider contentProvider;
    
    private transient IDataSetDirectoryProvider directoryProvider;

    private transient IdentifierAttributeMappingManager archiveFolderMapping;

    public DistributedPackagingDataSetFileOperationsManager(Properties properties)
    {
        this(properties, null, null, null);
    }
    
    DistributedPackagingDataSetFileOperationsManager(Properties properties, 
            IEncapsulatedOpenBISService service, IHierarchicalContentProvider contentProvider, 
            IDataSetDirectoryProvider directoryProvider)
    {
        this.service = service;
        this.contentProvider = contentProvider;
        this.directoryProvider = directoryProvider;
        compress = PropertyUtils.getBoolean(properties, COMPRESS_KEY, true);
        withSharding = PropertyUtils.getBoolean(properties, WITH_SHARDING_KEY, false);
        defaultFolder = new File(PropertyUtils.getMandatoryProperty(properties, DEFAULT_DESTINATION_KEY));
        if (defaultFolder.isDirectory() == false)
        {
            throw new ConfigurationFailureException("Default archive folder '" + defaultFolder.getPath() 
                    + "' doesn't exist or is not a folder.");
        }
        mappingFilePathOrNull = properties.getProperty(MAPPING_FILE_KEY);
        createArchives = PropertyUtils.getBoolean(properties, CREATE_ARCHIVES_KEY, false);
        getArchiveFolderMapping(); // Loads and validates mapping file
    }

    @Override
    public Status copyToDestination(File originalData, DatasetDescription datasetDescription)
    {
        AbstractExternalData dataSet = getDataSetWithAllMetaData(datasetDescription);
        IShareIdManager shareIdManager = getDirectoryProvider().getShareIdManager();
        DataSetExistenceChecker dataSetExistenceChecker =
                new DataSetExistenceChecker(getDirectoryProvider(), TimingParameters.create(new Properties()));
        Status status = Status.OK;
        String dataSetCode = datasetDescription.getDataSetCode();
        File file = getArchiveFile(datasetDescription);
        shareIdManager.lock(dataSetCode);
        AbstractDataSetPackager dataSetPackager = null;
        try
        {
            dataSetPackager = createPackager(file, dataSetExistenceChecker);
            dataSetPackager.addDataSetTo("", dataSet);
            operationLog.info("Data set '" + dataSetCode + "' archived: " + file);
        } catch (Exception ex)
        {
            status = Status.createError(ex.toString());
            operationLog.error("Couldn't create package file: " + file, ex);
        } finally
        {
            if (dataSetPackager != null)
            {
                try
                {
                    dataSetPackager.close();
                } catch (Exception ex)
                {
                    status = Status.createError("Couldn't close package file: " + file + ": " + ex);
                }
            }
            shareIdManager.releaseLock(dataSetCode);
        }
        return status;
    }

    private AbstractDataSetPackager createPackager(File file, DataSetExistenceChecker dataSetExistenceChecker)
    {
        return new ZipDataSetPackager(file, compress, getContentProvider(), dataSetExistenceChecker);
    }

    @Override
    public Status retrieveFromDestination(File originalData, DatasetDescription datasetDescription)
    {
        File file = getArchiveFile(datasetDescription);
        BasicZipFile zipFile = null;
        FileOutputStream fileOutputStream = null;
        try
        {
            zipFile = new BasicZipFile(new SimpleReadOnlyFile(file), "UTF-8", true, false);
            @SuppressWarnings("unchecked")
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements())
            {
                ZipEntry entry = entries.nextElement();
                File outputFile = new File(originalData, entry.getName());
                if (entry.isDirectory() == false                        )
                { 
                    if (AbstractDataSetPackager.META_DATA_FILE_NAME.equals(entry.getName()) == false)
                    {
                        outputFile.getParentFile().mkdirs();
                        InputStream inputStream = new BufferedInputStream(zipFile.getInputStream(entry));
                        fileOutputStream = new FileOutputStream(outputFile);
                        BufferedOutputStream outputStream = new BufferedOutputStream(fileOutputStream);
                        try
                        {
                            IOUtils.copyLarge(inputStream, outputStream);
                        } finally
                        {
                            IOUtils.closeQuietly(inputStream);
                            IOUtils.closeQuietly(outputStream);
                        }
                    }
                } else
                {
                    if (outputFile.isFile())
                    {
                        throw new EnvironmentFailureException("Could not extract directory '" + outputFile 
                                + "' because it exists already as a plain file.");
                    }
                    outputFile.mkdirs();
                }
            }
            operationLog.info("Data set '" + datasetDescription.getDataSetCode() + "' unzipped from archive '"
                    + file.getPath() + "' to '" + originalData + "'.");
            return Status.OK;
        } catch (Exception ex)
        {
            return Status.createError(ex.toString());
        } finally
        {
            if (zipFile != null)
            {
                try
                {
                    zipFile.close();
                } catch (IOException ex)
                {
                    throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                }
            } 
            IOUtils.closeQuietly(fileOutputStream);
        }
    }

    @Override
    public Status deleteFromDestination(IDatasetLocation dataset)
    {
        File archiveFile = tryFindArchiveFile(dataset);
        if (archiveFile == null)
        {
            operationLog.warn("Archive file for data set '" + dataset.getDataSetCode() + "' no konger exists.");
            return Status.OK;
        }
        boolean success = archiveFile.delete();
        return success ? Status.OK : Status.createError("Couldn't delete archive file '" + archiveFile + "'.");
    }
    
    private File tryFindArchiveFile(IDatasetLocation datasetLocation)
    {
        File archiveFile = getArchiveFile(defaultFolder, datasetLocation, false);
        if (archiveFile.isFile())
        {
            return archiveFile;
        }
        Collection<File> folders = getArchiveFolderMapping().getAllFolders();
        for (File folder : folders)
        {
            archiveFile = getArchiveFile(folder, datasetLocation, false);
            if (archiveFile.isFile())
            {
                return archiveFile;
            }
        }
        return null;
    }

    @Override
    public Status markAsDeleted(IDatasetLocation dataset)
    {
        File archiveFile = tryFindArchiveFile(dataset);
        if (archiveFile == null)
        {
            operationLog.warn("Archive file for data set '" + dataset.getDataSetCode() + "' no konger exists.");
            return Status.OK;
        }
        String relPath = (withSharding ? dataset.getDataSetLocation() + "/" : "") + createPackageFileName(dataset);
        String path = archiveFile.getPath();
        int index = path.lastIndexOf(relPath);
        File archiveFolder = new File(path.substring(0, index));
        File folderOfMarkers = new File(archiveFolder, DataSetFileOperationsManager.FOLDER_OF_AS_DELETED_MARKED_DATA_SETS);
        folderOfMarkers.mkdirs();
        File markerFile = new File(folderOfMarkers, dataset.getDataSetCode());
        try
        {
            if (markerFile.createNewFile() == false)
            {
                throw new IOException("Marker file already exists.");
            }
            return Status.OK;
        } catch (IOException ex)
        {
            String message = "Couldn't create marker file '" + markerFile + "': " + ex;
            operationLog.error(message, ex);
            return Status.createError(message);
        }
    }

    @Override
    public BooleanStatus isSynchronizedWithDestination(File originalData, DatasetDescription datasetDescription)
    {
        return BooleanStatus.createFalse();
    }

    @Override
    public BooleanStatus isPresentInDestination(DatasetDescription datasetDescription)
    {
        return BooleanStatus.createFalse();
    }

    @Override
    public boolean isHosted()
    {
        return false;
    }

    @Override
    public IHierarchicalContent getAsHierarchicalContent(DatasetDescription dataset)
    {
        return new FilteredHierarchicalContent(new ZipBasedHierarchicalContent(getArchiveFile(dataset)), FILTER);
    }

    private AbstractExternalData getDataSetWithAllMetaData(DatasetDescription datasetDescription)
    {
        AbstractExternalData dataSet = getService().tryGetDataSet(datasetDescription.getDataSetCode());
        String experimentIdentifier = datasetDescription.getExperimentIdentifier();
        dataSet.setExperiment(getService().tryGetExperiment(ExperimentIdentifierFactory.parse(experimentIdentifier)));
        String sampleIdentifier = datasetDescription.getSampleIdentifier();
        if (sampleIdentifier != null)
        {
            dataSet.setSample(getService().tryGetSampleWithExperiment(SampleIdentifierFactory.parse(sampleIdentifier)));
        }
        return dataSet;
    }
    
    private File getArchiveFile(DatasetDescription datasetDescription)
    {
        File folder = getArchiveFolderMapping().getArchiveFolder(datasetDescription, defaultFolder);
        return getArchiveFile(folder, datasetDescription, true);
    }

    private File getArchiveFile(File baseFolder, IDatasetLocation datasetLocation, boolean forWriting)
    {
        File folder = getArchiveFolder(baseFolder, datasetLocation, forWriting);
        return new File(folder, createPackageFileName(datasetLocation));
    }

    private String createPackageFileName(IDatasetLocation datasetLocation)
    {
        return datasetLocation.getDataSetCode() + ".zip";
    }

    private File getArchiveFolder(File baseFolder, IDatasetLocation datasetLocation, boolean forWriting)
    {
        File folder = baseFolder;
        if (withSharding)
        {
            folder = new File(folder, datasetLocation.getDataSetLocation());
            if (forWriting && folder.exists() == false)
            {
                folder.mkdirs();
            }
        }
        return folder;
    }

    private IEncapsulatedOpenBISService getService()
    {
        if (service == null)
        {
            service = ServiceProvider.getOpenBISService();
        }
        return service;
    }
    
    private IHierarchicalContentProvider getContentProvider()
    {
        if (contentProvider == null)
        {
            contentProvider = ServiceProvider.getHierarchicalContentProvider();
        }
        return contentProvider;
    }
    
    private IDataSetDirectoryProvider getDirectoryProvider()
    {
        if (directoryProvider == null)
        {
            directoryProvider = ServiceProvider.getDataStoreService().getDataSetDirectoryProvider();
        }
        return directoryProvider;
    }
    
    private IdentifierAttributeMappingManager getArchiveFolderMapping()
    {
        if (archiveFolderMapping == null)
        {
            archiveFolderMapping = new IdentifierAttributeMappingManager(mappingFilePathOrNull, createArchives);
        }
        return archiveFolderMapping;
    }
    
}
