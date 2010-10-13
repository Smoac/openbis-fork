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

import ch.systemsx.cisd.cina.shared.constants.CinaConstants;
import ch.systemsx.cisd.etlserver.IDataSetHandlerRpc;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypeWithVocabularyTerms;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

public class BundleRegistrationState
{
    private final IDataSetHandlerRpc delegator;

    private final SessionContextDTO sessionContext;

    private final IEncapsulatedOpenBISService openbisService;

    private final SampleType gridPrepSampleType;

    private final SampleType replicaSampleType;

    private final DataSetTypeWithVocabularyTerms rawImagesDataSetType;

    private final DataSetTypeWithVocabularyTerms metadataDataSetType;

    private final DataSetTypeWithVocabularyTerms imageDataSetType;

    public BundleRegistrationState(IDataSetHandlerRpc delegator,
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