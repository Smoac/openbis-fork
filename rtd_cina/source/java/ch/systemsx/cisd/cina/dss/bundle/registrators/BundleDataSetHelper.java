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

package ch.systemsx.cisd.cina.dss.bundle.registrators;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.cina.shared.constants.CinaConstants;
import ch.systemsx.cisd.cina.shared.metadata.IMetadataExtractor;
import ch.systemsx.cisd.etlserver.IDataSetHandlerRpc;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypeWithVocabularyTerms;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyTypeWithVocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

/**
 * Helper to aid registering bundle data sets.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
abstract class BundleDataSetHelper
{
    private static final String MISC_DATA_SET_PROPERTY_CODE = "MISC";

    static class BundleRegistrationState
    {
        private final IDataSetHandlerRpc delegator;

        private final SessionContextDTO sessionContext;

        private final IEncapsulatedOpenBISService openbisService;

        private final SampleType gridPrepSampleType;

        private final SampleType replicaSampleType;

        private final DataSetTypeWithVocabularyTerms rawImagesDataSetType;

        private final DataSetTypeWithVocabularyTerms metadataDataSetType;

        private final DataSetTypeWithVocabularyTerms imageDataSetType;

        BundleRegistrationState(IDataSetHandlerRpc delegator,
                IEncapsulatedOpenBISService openbisService)
        {
            this.delegator = delegator;
            sessionContext = delegator.getSessionContext();
            this.openbisService = openbisService;
            this.gridPrepSampleType =
                    openbisService.getSampleType(CinaConstants.GRID_PREP_SAMPLE_TYPE_CODE);
            this.replicaSampleType =
                    openbisService.getSampleType(CinaConstants.REPLICA_SAMPLE_TYPE_CODE);
            this.rawImagesDataSetType =
                    openbisService.getDataSetType(CinaConstants.RAW_IMAGES_DATA_SET_TYPE_CODE);
            this.metadataDataSetType =
                    openbisService.getDataSetType(CinaConstants.METADATA_DATA_SET_TYPE_CODE);
            this.imageDataSetType =
                    openbisService.getDataSetType(CinaConstants.IMAGE_DATA_SET_TYPE_CODE);
        }

        IDataSetHandlerRpc getDelegator()
        {
            return delegator;
        }

        SessionContextDTO getSessionContext()
        {
            return sessionContext;
        }

        IEncapsulatedOpenBISService getOpenbisService()
        {
            return openbisService;
        }

        SampleType getGridPrepSampleType()
        {
            return gridPrepSampleType;
        }

        SampleType getReplicaSampleType()
        {
            return replicaSampleType;
        }

        DataSetTypeWithVocabularyTerms getRawImagesDataSetType()
        {
            return rawImagesDataSetType;
        }

        DataSetTypeWithVocabularyTerms getMetadataDataSetType()
        {
            return metadataDataSetType;
        }

        DataSetTypeWithVocabularyTerms getImageDataSetType()
        {
            return imageDataSetType;
        }
    }

    protected final BundleRegistrationState globalState;

    protected final File dataSet;

    protected final ArrayList<DataSetInformation> registeredDataSets;

    BundleDataSetHelper(BundleRegistrationState globalState, File dataSet)
    {
        this.globalState = globalState;
        this.dataSet = dataSet;
        this.registeredDataSets = new ArrayList<DataSetInformation>();
    }

    /**
     * Get all the data set information that has been created as a result of
     * {@link #registerDataSet}. Only makes sense to invoke after registerDataSet has been called
     */
    public ArrayList<DataSetInformation> getDataSetInformation()
    {
        return registeredDataSets;
    }

    /**
     * Register the provided file as a data set and add it to the dataSetInformation collection.
     * 
     * @param dataSetFile The file to register as a data set
     * @return A collection of data set information objects for each data set just registered
     */
    protected List<DataSetInformation> registerDataSet(File dataSetFile,
            DataSetInformation dataSetInfo)
    {
        // Register the given file
        List<DataSetInformation> bigDataSet =
                getDelegator().handleDataSet(dataSetFile, dataSetInfo);
        registeredDataSets.addAll(bigDataSet);
        return bigDataSet;
    }

    protected IEncapsulatedOpenBISService getOpenbisService()
    {
        return globalState.getOpenbisService();
    }

    protected IDataSetHandlerRpc getDelegator()
    {
        return globalState.getDelegator();
    }

    protected SessionContextDTO getSessionContext()
    {
        return globalState.getSessionContext();
    }

    protected ArrayList<NewProperty> createDataSetProperties(IMetadataExtractor metadata)
    {
        List<PropertyTypeWithVocabulary> propertyTypes =
                globalState.getImageDataSetType().getPropertyTypes();
        ArrayList<NewProperty> properties = new ArrayList<NewProperty>();
        for (PropertyTypeWithVocabulary propertyType : propertyTypes)
        {
            String value = metadata.getMetadataMap().get(propertyType.getCode().toLowerCase());
            if (null != value)
            {
                NewProperty prop;
                prop = new NewProperty(propertyType.getCode(), value);
                properties.add(prop);
            }
        }

        // Add a property with everything
        properties.add(new NewProperty(MISC_DATA_SET_PROPERTY_CODE, metadata.getMetadataMap()
                .toString()));
        return properties;
    }
}
