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
import ch.systemsx.cisd.cina.shared.metadata.CollectionMetadataExtractor;
import ch.systemsx.cisd.cina.shared.metadata.ImageMetadataExtractor;
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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * Registers/Updates a collection, its metadata, the raw images and the annotated images.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class CollectionRegistrator extends BundleDataSetHelper
{
    private static final String COLLECTIONS_FOLDER_NAME =
            BundleStructureConstants.COLLECTIONS_FOLDER_NAME;

    private static final String RAW_IMAGES_FOLDER_NAME =
            BundleStructureConstants.RAW_IMAGES_FOLDER_NAME;

    private final CollectionMetadataExtractor collectionMetadataExtractor;

    private final Sample gridPrepSample;

    private final SampleIdentifier gridPrepSampleId;

    private final DataSetInformation bundleMetadataDataSetInformation;

    // Processing State (gets set during the execution of registration)
    private SampleIdentifier collectionSampleId;

    private Sample collectionSample;

    private boolean didCreateSample = false;

    CollectionRegistrator(BundleRegistrationState globalState,
            CollectionMetadataExtractor replicaMetadataExtractor, Sample gridPrepSample,
            SampleIdentifier gridPrepSampleId, DataSetInformation bundleMetadataDataSetInformation,
            File dataSet)
    {
        super(globalState, dataSet);

        this.collectionMetadataExtractor = replicaMetadataExtractor;
        this.gridPrepSample = gridPrepSample;
        this.gridPrepSampleId = gridPrepSampleId;
        this.bundleMetadataDataSetInformation = bundleMetadataDataSetInformation;
    }

    /**
     * Create the replica sample (if necessary). Register the original images (if the replica sample did not exist), register the metadata dataset,
     * and update the sample metadata, register the annotated images.
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
        String sampleCode = collectionMetadataExtractor.tryReplicaSampleCode();
        collectionSampleId = new SampleIdentifier(gridPrepSampleId.getSpaceLevel(), sampleCode);
        collectionSample = getOpenbisService().tryGetSampleWithExperiment(collectionSampleId);
        didCreateSample = false;

        // Get the relevant metadata from the file
        String sampleDescriptionOrNull = collectionMetadataExtractor.tryReplicaSampleDescription();
        String sampleCreatorOrNull = collectionMetadataExtractor.tryReplicaSampleCreatorName();

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

        if (collectionSample == null)
        {
            // Sample doesn't exist, create it

            // Add the collection name as metadata
            addCollectionNamePoperty(properties);

            NewSample newSample =
                    NewSample.createWithParent(collectionSampleId.toString(),
                            globalState.getReplicaSampleType(), null, gridPrepSampleId.toString());
            newSample.setExperimentIdentifier(gridPrepSample.getExperiment().getIdentifier());
            newSample.setProperties(properties.toArray(IEntityProperty.EMPTY_ARRAY));

            String userId = getSessionContext().getUserName();
            getOpenbisService().registerSample(newSample, userId);
            didCreateSample = true;
        } else
        {
            ExperimentIdentifier experimentId =
                    new ExperimentIdentifier(collectionSample.getExperiment());
            // Sample does exist, update the metadata
            SampleUpdatesDTO sampleUpdate =
                    new SampleUpdatesDTO(TechId.create(collectionSample), properties, experimentId, null, 
                            new ArrayList<NewAttachment>(), collectionSample.getVersion(),
                            collectionSampleId, null, null);
            getOpenbisService().updateSample(sampleUpdate);
            didCreateSample = false;
        }

        collectionSample = getOpenbisService().tryGetSampleWithExperiment(collectionSampleId);

        assert collectionSample != null;
    }

    private void addCollectionNamePoperty(ArrayList<IEntityProperty> properties)
    {
        // see if the collection name property is assigned
        boolean isCollectionNameAssigned = false;
        for (SampleTypePropertyType assignedPropertyType : globalState.getReplicaSampleType()
                .getAssignedPropertyTypes())
        {
            if (CinaConstants.COLLECTION_NAME_PROPERTY.equals(assignedPropertyType
                    .getPropertyType().getCode()))
            {
                isCollectionNameAssigned = true;
                break;
            }
        }
        if (false == isCollectionNameAssigned)
        {
            return;
        }
        String collectionName = collectionMetadataExtractor.getCollectionName();
        EntityProperty collectionNameProperty =
                createProperty(CinaConstants.COLLECTION_NAME_PROPERTY);
        collectionNameProperty.setValue(collectionName);
        properties.add(collectionNameProperty);
    }

    private void registerRawImages()
    {
        String collectionName = collectionMetadataExtractor.getCollectionName();

        System.out.println("COllectionName: " + collectionName);

        File collectionOriginalImages =
                new File(new File(new File(dataSet, COLLECTIONS_FOLDER_NAME), collectionName), RAW_IMAGES_FOLDER_NAME);

        CollectionRawImagesRegistrator registrator =
                new CollectionRawImagesRegistrator(globalState, collectionMetadataExtractor,
                        collectionSample, collectionSampleId, collectionOriginalImages);
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
        CollectionMetadataRegistrator registrator =
                new CollectionMetadataRegistrator(globalState, collectionMetadataExtractor,
                        collectionSample, collectionSampleId, bundleMetadataDataSetInformation);
        List<DataSetInformation> registeredDataSetInfos = registrator.register();
        getDataSetInformation().addAll(registeredDataSetInfos);
        return registrator.getMetadataDataSetFile();
    }

    private void registerAnnotatedImages(File registeredMetadataFile)
    {
        // Create a metadata extractor on the data set in the store (the ivar is a replica metadata
        // extractor on the data set in the incoming directory, so the paths it has are not
        // persistent)
        CollectionMetadataExtractor storeReplicaMetadataExtractor =
                new CollectionMetadataExtractor(registeredMetadataFile);
        storeReplicaMetadataExtractor.prepare();
        for (ImageMetadataExtractor imageMetadataExtractor : storeReplicaMetadataExtractor
                .getImageMetadataExtractors())
        {
            CollectionAnnotatedImagesRegistrator registrator =
                    new CollectionAnnotatedImagesRegistrator(globalState, imageMetadataExtractor,
                            collectionSample, collectionSampleId, bundleMetadataDataSetInformation);
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
