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

import ch.systemsx.cisd.cina.shared.constants.BundleStructureConstants;
import ch.systemsx.cisd.cina.shared.constants.CinaConstants;
import ch.systemsx.cisd.cina.shared.metadata.ImageMetadataExtractor;
import ch.systemsx.cisd.cina.shared.metadata.ReplicaMetadataExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * Registers/Updates a replica, its metadata, the raw images and the annotated images.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class ReplicaRegistrator extends BundleDataSetHelper
{
    private static final String RAW_IMAGES_FOLDER_NAME =
            BundleStructureConstants.RAW_IMAGES_FOLDER_NAME;

    private final ReplicaMetadataExtractor replicaMetadataExtractor;

    private final Sample gridPrepSample;

    private final SampleIdentifier gridPrepSampleId;

    private final DataSetInformation bundleMetadataDataSetInformation;

    // Processing State (gets set during the execution of registration)
    private SampleIdentifier replicaSampleId;

    private Sample replicaSample;

    private boolean didCreateSample = false;

    ReplicaRegistrator(BundleRegistrationState globalState,
            ReplicaMetadataExtractor replicaMetadataExtractor, Sample gridPrepSample,
            SampleIdentifier gridPrepSampleId, DataSetInformation bundleMetadataDataSetInformation,
            File dataSet)
    {
        super(globalState, dataSet);

        this.replicaMetadataExtractor = replicaMetadataExtractor;
        this.gridPrepSample = gridPrepSample;
        this.gridPrepSampleId = gridPrepSampleId;
        this.bundleMetadataDataSetInformation = bundleMetadataDataSetInformation;
    }

    /**
     * Create the replica sample (if necessary). Register the original images (if the replica sample
     * did not exist), register the metadata dataset, and update the sample metadata, register the
     * annotated images.
     * 
     * @return DataSetInformation objects for each of the data sets registered
     */
    public List<DataSetInformation> register()
    {
        retrieveOrCreateReplicaSample();
        if (didCreateSample)
        {
            registerRawImages();
        }
        File registeredDataSetFile = registerMetadata();
        registerAnnotatedImages(registeredDataSetFile);

        return getDataSetInformation();
    }

    private void retrieveOrCreateReplicaSample()
    {
        String sampleCode = replicaMetadataExtractor.tryReplicaSampleCode();
        replicaSampleId = new SampleIdentifier(gridPrepSampleId.getSpaceLevel(), sampleCode);
        replicaSample = getOpenbisService().tryGetSampleWithExperiment(replicaSampleId);
        didCreateSample = false;

        // Get the relevant metadata from the file
        String sampleDescriptionOrNull = replicaMetadataExtractor.tryReplicaSampleDescription();
        String sampleCreatorOrNull = replicaMetadataExtractor.tryReplicaSampleCreatorName();

        ArrayList<IEntityProperty> properties = new ArrayList<IEntityProperty>();
        if (null != sampleDescriptionOrNull)
        {
            EntityProperty prop = createProperty(CinaConstants.DESCRIPTION_PROPERTY_CODE);
            prop.setValue(sampleDescriptionOrNull);
            properties.add(prop);
        }

        if (null != sampleCreatorOrNull)
        {
            EntityProperty prop = createProperty(CinaConstants.CREATOR_EMAIL_PROPERTY_CODE);
            prop.setValue(sampleCreatorOrNull);
            properties.add(prop);
        }

        if (replicaSample == null)
        {
            // Sample doesn't exist, create it
            NewSample newSample =
                    NewSample.createWithParent(replicaSampleId.toString(),
                            globalState.getReplicaSampleType(), null, gridPrepSampleId.toString());
            newSample.setExperimentIdentifier(gridPrepSample.getExperiment().getIdentifier());
            newSample.setProperties(properties.toArray(IEntityProperty.EMPTY_ARRAY));

            String userId = getSessionContext().getUserName();
            getOpenbisService().registerSample(newSample, userId);
            didCreateSample = true;
        } else
        {
            ExperimentIdentifier experimentId =
                    new ExperimentIdentifier(replicaSample.getExperiment());
            experimentId.setDatabaseInstanceCode(replicaSample.getExperiment().getProject()
                    .getSpace().getInstance().getCode());
            // Sample does exist, update the metadata
            SampleUpdatesDTO sampleUpdate =
                    new SampleUpdatesDTO(TechId.create(replicaSample), properties, experimentId,
                            new ArrayList<NewAttachment>(), replicaSample.getModificationDate(),
                            replicaSampleId, null, null);
            getOpenbisService().updateSample(sampleUpdate);
            didCreateSample = false;
        }

        replicaSample = getOpenbisService().tryGetSampleWithExperiment(replicaSampleId);

        assert replicaSample != null;
    }

    private void registerRawImages()
    {
        String replicaName = replicaMetadataExtractor.getFolder().getName();
        File replicaOriginalImages =
                new File(new File(dataSet, RAW_IMAGES_FOLDER_NAME), replicaName);
        ReplicaRawImagesRegistrator registrator =
                new ReplicaRawImagesRegistrator(globalState, replicaMetadataExtractor,
                        replicaSample, replicaSampleId, replicaOriginalImages);
        List<DataSetInformation> registeredDataSetInfos = registrator.register();
        getDataSetInformation().addAll(registeredDataSetInfos);
    }

    /**
     * Register the metadata data set and return the File object for the registered data set.
     * 
     * @return The File object for the registered data set
     */
    private File registerMetadata()
    {
        ReplicaMetadataRegistrator registrator =
                new ReplicaMetadataRegistrator(globalState, replicaMetadataExtractor,
                        replicaSample, replicaSampleId, bundleMetadataDataSetInformation);
        List<DataSetInformation> registeredDataSetInfos = registrator.register();
        getDataSetInformation().addAll(registeredDataSetInfos);
        return registrator.getMetadataDataSetFile();
    }

    private void registerAnnotatedImages(File registeredMetadataFile)
    {
        // Create a metadata extractor on the data set in the store (the ivar is a replica metadata
        // extractor on the data set in the incoming directory, so the paths it has are not
        // persistent)
        ReplicaMetadataExtractor storeReplicaMetadataExtractor =
                new ReplicaMetadataExtractor(registeredMetadataFile);
        storeReplicaMetadataExtractor.prepare();
        for (ImageMetadataExtractor imageMetadataExtractor : storeReplicaMetadataExtractor
                .getImageMetadataExtractors())
        {
            ReplicaAnnotatedImagesRegistrator registrator =
                    new ReplicaAnnotatedImagesRegistrator(globalState, imageMetadataExtractor,
                            replicaSample, replicaSampleId, bundleMetadataDataSetInformation);
            List<DataSetInformation> registeredDataSetInfos = registrator.register();
            getDataSetInformation().addAll(registeredDataSetInfos);
        }
    }

    private EntityProperty createProperty(String propertyTypeCode)
    {
        PropertyType propertyType = new PropertyType();
        DataType dataType = new DataType();
        dataType.setCode(DataTypeCode.VARCHAR);
        propertyType.setCode(propertyTypeCode);
        propertyType.setDataType(dataType);

        EntityProperty property = new EntityProperty();
        property.setPropertyType(propertyType);
        return property;
    }
}
