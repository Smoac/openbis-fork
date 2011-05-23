/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.client.api.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import ch.systemsx.cisd.base.namedthread.NamingThreadPoolExecutor;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.io.TransmissionSpeedCalculator;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.openbis.dss.client.api.gui.DataSetUploadClientModel.NewDataSetInfo.Status;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDssComponent;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTOBuilder;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetMetadataDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.validation.ValidationError;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationChangingService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ControlledVocabularyPropertyType.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyTypeGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.util.SimplePropertyValidator;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetUploadClientModel
{
    public static interface Observer
    {
        public void update(Vocabulary vocabulary, String code);
    }

    private static ExecutorService executor = new NamingThreadPoolExecutor("Data Set Upload", 1, 1,
            0, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>()).daemonize();

    private final IDssComponent dssComponent;

    private final IGeneralInformationService generalInformationService;

    private final IGeneralInformationChangingService generalInformationChangingService;

    private final ITimeProvider timeProvider;

    private List<DataSetType> dataSetTypes;

    private final ArrayList<NewDataSetInfo> newDataSetInfos = new ArrayList<NewDataSetInfo>();

    // Track which files a user selected to make share the list of files in the selection combo box.
    // The list is reverse-ordered according to selection made by user. There are no duplictes on
    // the list.
    private final LinkedList<File> userSelectedFiles = new LinkedList<File>();

    // Generic validator for property values.
    private final SimplePropertyValidator simplePropertyValidator = new SimplePropertyValidator();

    // References to UI elements that are looking at the client model -- a way of implementing
    // observer.
    private DataSetUploadTableModel tableModel;

    private final List<Observer> observers = new LinkedList<DataSetUploadClientModel.Observer>();

    private HashMap<Vocabulary, List<VocabularyTerm>> vocabularyTerms;

    public DataSetUploadClientModel(DssCommunicationState commState, ITimeProvider timeProvider)
    {
        this.dssComponent = commState.getDssComponent();
        this.generalInformationService = commState.getGeneralInformationService();
        this.generalInformationChangingService = commState.getGeneralInformationChangingService();
        this.timeProvider = timeProvider;
        dataSetTypes = generalInformationService.listDataSetTypes(dssComponent.getSessionToken());
        vocabularyTerms =
                generalInformationService.getVocabularyTermsMap(dssComponent.getSessionToken());
    }

    /**
     * NewDataSetInfo is a mixture of NewDataSetDTO, which encapsulates information about new data
     * sets, and upload progress state.
     * <p>
     * Internally, NewDataSetInfo functions as a state machine with the following state transitions:
     * 
     * <pre>
     *                                              /-> FAILED
     * TO_UPLOAD -> QUEUED_FOR_UPLOAD -> UPLOADING <       ->  COMPLETED_UPLOAD
     *                                         /    \-> STALLED -\
     *                                         \-----------------/
     * </pre>
     * 
     * </p>
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    static class NewDataSetInfo
    {
        static enum Status
        {
            TO_UPLOAD, QUEUED_FOR_UPLOAD, UPLOADING, COMPLETED_UPLOAD, FAILED, STALLED
        }

        private final NewDataSetDTOBuilder newDataSetBuilder;

        private final TransmissionSpeedCalculator transmissionSpeedCalculator;

        private Status status;

        // Computed when the status is set to QUEUED_FOR_UPLOAD
        private long totalFileSize;

        // 0 until status is set to UPLOADING.
        private int percentageUploaded;

        private long numberOfBytesUploaded;

        private final ArrayList<ValidationError> validationErrors =
                new ArrayList<ValidationError>();

        private NewDataSetInfo(NewDataSetDTOBuilder newDataSetBuilder, ITimeProvider timeProvider)
        {
            this.newDataSetBuilder = newDataSetBuilder;
            transmissionSpeedCalculator = new TransmissionSpeedCalculator(timeProvider);
            percentageUploaded = 0;
            numberOfBytesUploaded = 0;
            setStatus(Status.TO_UPLOAD);
        }

        public NewDataSetDTOBuilder getNewDataSetBuilder()
        {
            return newDataSetBuilder;
        }

        public long getTotalFileSize()
        {
            return totalFileSize;
        }

        public int getPercentageDownloaded()
        {
            return percentageUploaded;
        }

        public long getNumberOfBytesDownloaded()
        {
            return numberOfBytesUploaded;
        }

        public long getEstimatedTimeOfArrival()
        {
            float remainingBytes = (totalFileSize - numberOfBytesUploaded);
            float bytesPerMillisecond =
                    transmissionSpeedCalculator.getEstimatedBytesPerMillisecond();
            if (bytesPerMillisecond < 0.001)
            {
                return -1;
            }
            return (long) (remainingBytes / bytesPerMillisecond);
        }

        void updateProgress(int percent, long numberOfBytes)
        {
            setStatus(Status.UPLOADING);
            int transmittedSinceLastUpdate = (int) (numberOfBytes - numberOfBytesUploaded);
            percentageUploaded = percent;
            numberOfBytesUploaded = numberOfBytes;
            transmissionSpeedCalculator
                    .noteTransmittedBytesSinceLastUpdate(transmittedSinceLastUpdate);
        }

        public void setStatus(Status status)
        {
            this.status = status;
            if (Status.QUEUED_FOR_UPLOAD == status)
            {
                long size = 0;
                for (FileInfoDssDTO fileInfo : newDataSetBuilder.getFileInfos())
                {
                    if (fileInfo.getFileSize() > 0)
                    {
                        size += fileInfo.getFileSize();
                    }
                }
                // Initialize some variables
                totalFileSize = size;

                percentageUploaded = 0;
                numberOfBytesUploaded = 0;
            }
        }

        public Status getStatus()
        {
            return status;
        }

        public boolean canBeQueued()
        {
            return status != Status.UPLOADING && status != Status.STALLED
                    && status != Status.COMPLETED_UPLOAD && hasErrors() == false;
        }

        public boolean hasErrors()
        {
            return validationErrors.isEmpty() == false;
        }

        public List<ValidationError> getValidationErrors()
        {
            return validationErrors;
        }

        private void setValidationErrors(List<ValidationError> errors)
        {
            validationErrors.clear();
            validationErrors.addAll(errors);
        }
    }

    /**
     * A list of data set info object managed by this model.
     */
    public List<NewDataSetInfo> getNewDataSetInfos()
    {
        return Collections.unmodifiableList(newDataSetInfos);
    }

    /**
     * Add a new data set info to the list of data set info objects and return it.
     */
    public NewDataSetInfo addNewDataSetInfo(NewDataSetInfo template)
    {
        NewDataSetDTOBuilder newDataSetBuilder = new NewDataSetDTOBuilder();
        if (null == template)
        {
            String defaultDataSetTypeCode = getDefaultDataSetTypeCode();
            newDataSetBuilder.getDataSetMetadata().setDataSetTypeOrNull(defaultDataSetTypeCode);
        } else
        {
            newDataSetBuilder.initializeFromTemplate(template.getNewDataSetBuilder());
        }
        NewDataSetInfo newDataSetInfo = new NewDataSetInfo(newDataSetBuilder, timeProvider);
        newDataSetInfos.add(newDataSetInfo);
        validateNewDataSetInfoAndNotifyObservers(newDataSetInfo);
        return newDataSetInfo;
    }

    private String getDefaultDataSetTypeCode()
    {
        if (dataSetTypes.size() > 0)
        {
            return getDataSetTypes().get(0).getCode();
        } else
        {
            return null;
        }
    }

    /**
     * Remove a data set info.
     */
    public void removeNewDataSetInfo(NewDataSetInfo dataSetInfoToRemove)
    {
        newDataSetInfos.remove(dataSetInfoToRemove);
    }

    public IDssComponent getDssComponent()
    {
        return dssComponent;
    }

    /**
     * Get the data set types that are shown here.
     */
    public List<DataSetType> getDataSetTypes()
    {
        return dataSetTypes;
    }

    public int getIndexOfDataSetType(String dataSetTypeCode)
    {
        if (null == dataSetTypeCode)
        {
            return 0;
        }

        for (int i = 0; i < dataSetTypes.size(); ++i)
        {
            if (dataSetTypes.get(i).getCode().equals(dataSetTypeCode))
            {
                return i;
            }
        }
        return 0;
    }

    public DataSetUploadTableModel getTableModel()
    {
        return tableModel;
    }

    public void setTableModel(DataSetUploadTableModel tableModel)
    {
        this.tableModel = tableModel;
    }

    /**
     * Broadcast changes to observers.
     */
    public void notifyObserversOfChanges(NewDataSetInfo changedInfo)
    {
        tableModel.selectedRowDataChanged();
    }

    /**
     * Clean the <var>newDataSetDTO</var> object. This means removing any properties that are not
     * valid for the data set type.
     */
    public NewDataSetDTO cleanNewDataSetDTO(NewDataSetDTO newDataSetDTO)
    {
        DataSetType dataSetType = tryDataSetType(newDataSetDTO.tryDataSetType());
        if (null == dataSetType)
        {
            throw new UserFailureException("The new data set has no type");
        }

        HashSet<String> allPropertyCodes = new HashSet<String>();
        for (PropertyTypeGroup group : dataSetType.getPropertyTypeGroups())
        {
            for (PropertyType propertyType : group.getPropertyTypes())
            {
                allPropertyCodes.add(propertyType.getCode());
            }
        }

        HashSet<String> keys = new HashSet<String>(newDataSetDTO.getProperties().keySet());
        for (String propertyTypeCode : keys)
        {
            if (false == allPropertyCodes.contains(propertyTypeCode))
            {
                newDataSetDTO.getProperties().remove(propertyTypeCode);
            }
        }

        return newDataSetDTO;
    }

    /**
     * Start a data set upload in a separate thread. Callers need to ensure that queuing makes
     * sense.
     */
    public void queueUploadOfDataSet(NewDataSetInfo newDataSetInfo)
    {
        if (false == newDataSetInfo.canBeQueued())
        {
            return;
        }
        newDataSetInfo.setStatus(Status.QUEUED_FOR_UPLOAD);
        DataSetUploadOperation op = new DataSetUploadOperation(tableModel, this, newDataSetInfo);
        executor.submit(op);
    }

    public void queueUploadOfDataSet(List<NewDataSetInfo> newDataSetInfosToQueue)
    {
        for (NewDataSetInfo newDataSetInfo : newDataSetInfosToQueue)
        {
            queueUploadOfDataSet(newDataSetInfo);
        }

    }

    public void userDidSelectFile(File selectedFile)
    {
        userSelectedFiles.remove(selectedFile);
        userSelectedFiles.addFirst(selectedFile);
    }

    private DataSetType tryDataSetType(String dataSetTypeCode)
    {
        if (null == dataSetTypeCode)
        {
            return null;
        }

        for (int i = 0; i < dataSetTypes.size(); ++i)
        {
            DataSetType type = dataSetTypes.get(i);
            if (type.getCode().equals(dataSetTypeCode))
            {
                return type;
            }
        }
        return null;
    }

    public List<File> getUserSelectedFiles()
    {
        return userSelectedFiles;
    }

    /**
     * Validate a new data set info and update the validation errors.
     */
    public final void validateNewDataSetInfoAndNotifyObservers(NewDataSetInfo newDataSetInfo)
    {
        validateNewDataSetInfo(newDataSetInfo);
        notifyObserversOfChanges(newDataSetInfo);
    }

    /**
     * Validate a new data set info and update the validation errors.
     */
    private final void validateNewDataSetInfo(NewDataSetInfo newDataSetInfo)
    {
        ArrayList<ValidationError> errors = new ArrayList<ValidationError>();
        validateNewDataSetInfo(newDataSetInfo, errors);
        newDataSetInfo.setValidationErrors(errors);
    }

    /**
     * This method actually carries out the validation. Subclasses may override.
     */
    protected void validateNewDataSetInfo(NewDataSetInfo newDataSetInfo,
            ArrayList<ValidationError> errors)
    {
        NewDataSetDTOBuilder builder = newDataSetInfo.getNewDataSetBuilder();
        String identifier = builder.getDataSetOwner().getIdentifier();
        if (identifier == null || identifier.trim().length() < 1)
        {
            errors.add(ValidationError.createOwnerValidationError("An owner must be specified."));
        }

        if (null == builder.getFile())
        {
            errors.add(ValidationError.createFileValidationError("A file must be specified."));
        }

        NewDataSetMetadataDTO builderMetadata = builder.getDataSetMetadata();
        String typeCode = builderMetadata.tryDataSetType();
        if (null == typeCode)
        {
            errors.add(ValidationError
                    .createDataSetTypeValidationError("A data set type must be specified."));
        } else
        {
            DataSetType type = dataSetTypes.get(getIndexOfDataSetType(typeCode));
            Map<String, String> properties = builderMetadata.getProperties();
            for (PropertyTypeGroup ptGroup : type.getPropertyTypeGroups())
            {
                for (PropertyType propertyType : ptGroup.getPropertyTypes())
                {
                    validatePropertyType(propertyType, properties.get(propertyType.getCode()),
                            errors);
                }
            }
        }

        // If we have passed local validation, run the server validation script
        if (errors.isEmpty())
        {
            List<ValidationError> scriptDetectedErrors =
                    dssComponent.validateDataSet(builder.asNewDataSetDTO(), builder.getFile());
            errors.addAll(scriptDetectedErrors);
        }

    }

    protected void validatePropertyType(PropertyType propertyType, String valueOrNull,
            ArrayList<ValidationError> errors)
    {
        if (null == valueOrNull || valueOrNull.trim().length() < 1)
        {
            if (propertyType.isMandatory())
            {
                errors.add(ValidationError.createPropertyValidationError(propertyType.getCode(),
                        "A value must be provided."));
            }

            // Otherwise, we can ignore this.
            return;
        }

        try
        {
            DataTypeCode dataType = propertyType.getDataType();
            if (simplePropertyValidator.canValidate(dataType))
            {
                simplePropertyValidator.validatePropertyValue(dataType, valueOrNull);
            }
        } catch (UserFailureException e)
        {
            errors.add(ValidationError.createPropertyValidationError(propertyType.getCode(),
                    e.getMessage()));
        }
    }

    public void addUnofficialVocabularyTerm(Vocabulary vocabulary, String code, String label,
            String description, Long previousTermOrdinal)
    {
        generalInformationChangingService.addUnofficialVocabularyTerm(
                dssComponent.getSessionToken(), TechId.create(vocabulary), code, label,
                description, previousTermOrdinal);
        dataSetTypes = generalInformationService.listDataSetTypes(dssComponent.getSessionToken());
        vocabularyTerms =
                generalInformationService.getVocabularyTermsMap(dssComponent.getSessionToken());

        notifyObservers(vocabulary, code);
    }

    public void registerObserver(Observer observer)
    {
        observers.add(observer);
    }

    public void notifyObservers(Vocabulary vocabulary, String code)
    {
        for (Observer observer : observers)
        {
            observer.update(vocabulary, code);
        }
    }

    public List<VocabularyTerm> getVocabularyTerms(Vocabulary vocabulary)
    {
        return vocabularyTerms.get(vocabulary);
    }
}
