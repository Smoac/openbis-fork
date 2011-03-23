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

package ch.systemsx.cisd.etlserver.registrator.api.v1.impl;

import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.common.types.BooleanOrUnknown;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewProject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSpace;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * Utility methods to convert transaction-layer DTOs to objects accepted by openBIS.
 * 
 * @author Kaloyan Enimanev
 */
public class ConversionUtils
{

    public static NewExperiment convertToNewExperiment(Experiment apiExperiment)
    {
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment experiment =
                apiExperiment.getExperiment();
        NewExperiment newExperiment = new NewExperiment();
        newExperiment.setIdentifier(experiment.getIdentifier());
        newExperiment.setPermID(experiment.getPermId());
        if (experiment.getExperimentType() != null)
        {
            newExperiment.setExperimentTypeCode(experiment.getExperimentType().getCode());
        }
        IEntityProperty[] properties = experiment.getProperties().toArray(new IEntityProperty[0]);
        newExperiment.setProperties(properties);

        return newExperiment;
    }

    public static NewSample convertToNewSample(Sample apiSample)
    {
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample sample = apiSample.getSample();
        NewSample newSample = new NewSample();
        newSample.setIdentifier(sample.getIdentifier());
        newSample.setPermID(sample.getPermId());
        if (sample.getExperiment() != null)
        {
            newSample.setExperimentIdentifier(sample.getExperiment().getIdentifier());
        }
        newSample.setSampleType(sample.getSampleType());

        IEntityProperty[] properties = sample.getProperties().toArray(new IEntityProperty[0]);
        newSample.setProperties(properties);

        return newSample;
    }

    public static SampleUpdatesDTO convertToSampleUpdateDTO(Sample apiSample)
    {
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample sample = apiSample.getSample();

        List<NewAttachment> attachments = Collections.emptyList();
        SampleUpdatesDTO sampleUpdate = new SampleUpdatesDTO(TechId.create(sample), // db id
                sample.getProperties(), // List<IEntityProperty>
                ExperimentIdentifierFactory.parse(sample.getExperiment().getIdentifier()), // ExperimentIdentifier
                attachments, // Collection<NewAttachment>
                sample.getModificationDate(), // Sample version
                SampleIdentifierFactory.parse(sample.getIdentifier()), // Sample Identifier
                sample.getContainer().getIdentifier(), // Container Identifier
                null // Parent Identifiers
                );
        return sampleUpdate;
    }

    public static NewExternalData convertToNewExternalData(
            DataSetRegistrationDetails<?> registrationDetails, String dataStoreCode,
            StorageFormat storageFormat, String dataFileRelativePath)
    {
        DataSetInformation dataSetInformation = registrationDetails.getDataSetInformation();
        final NewExternalData data = new NewExternalData();
        data.setUserId(dataSetInformation.getUploadingUserIdOrNull());
        data.setUserEMail(dataSetInformation.tryGetUploadingUserEmail());
        data.setExtractableData(dataSetInformation.getExtractableData());
        data.setDataSetType(registrationDetails.getDataSetType());
        data.setFileFormatType(registrationDetails.getFileFormatType());
        data.setMeasured(registrationDetails.isMeasuredData());
        data.setDataStoreCode(dataStoreCode);
        data.setExperimentIdentifierOrNull(dataSetInformation.getExperimentIdentifier());
        data.setSampleIdentifierOrNull(dataSetInformation.getSampleIdentifier());

        final BooleanOrUnknown isCompleteFlag = dataSetInformation.getIsCompleteFlag();

        data.setComplete(isCompleteFlag);
        data.setLocatorType(registrationDetails.getLocatorType());
        data.setShareId(dataSetInformation.getShareId());
        data.setLocation(dataFileRelativePath.substring(data.getShareId().length() + 1));
        data.setStorageFormat(storageFormat);

        List<NewProperty> newProperties =
                dataSetInformation.getExtractableData().getDataSetProperties();
        data.getExtractableData().setDataSetProperties(newProperties);

        return data;
    }

    public static NewSpace convertToNewSpace(Space apiSpace)
    {
        return new NewSpace(apiSpace.getSpaceCode(), apiSpace.getDescription(),
                apiSpace.getSpaceAdminUserId());
    }

    public static NewProject convertToNewProject(Project apiProject)
    {
        return new NewProject(apiProject.getProjectIdentifier(), apiProject.getDescription(),
                apiProject.getProjectLeaderId());
    }

}
