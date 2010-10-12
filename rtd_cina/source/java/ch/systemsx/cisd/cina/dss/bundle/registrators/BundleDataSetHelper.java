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

import ch.systemsx.cisd.etlserver.IDataSetHandlerRpc;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypeWithVocabularyTerms;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

/**
 * Helper to aid registering bundle data sets.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
abstract class BundleDataSetHelper
{
    static class BundleRegistrationState
    {
        private final IDataSetHandlerRpc delegator;

        private final SessionContextDTO sessionContext;

        private final IEncapsulatedOpenBISService openbisService;

        private final SampleType replicaSampleType;

        private final DataSetTypeWithVocabularyTerms imageDataSetType;

        BundleRegistrationState(IDataSetHandlerRpc delegator,
                IEncapsulatedOpenBISService openbisService, SampleType replicaSampleType,
                DataSetTypeWithVocabularyTerms imageDataSetType)
        {
            this.delegator = delegator;
            sessionContext = delegator.getSessionContext();
            this.openbisService = openbisService;
            this.replicaSampleType = replicaSampleType;
            this.imageDataSetType = imageDataSetType;
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

        SampleType getReplicaSampleType()
        {
            return replicaSampleType;
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
     * Register the provided file as a data set and add it to the dataSetInformation collection.
     * 
     * @param dataSetFile The file to register as a data set
     * @return A collection of data set information objects for each data set just registered
     */
    public List<DataSetInformation> registerDataSet(File dataSetFile)
    {
        // Register the given file
        List<DataSetInformation> bigDataSet = getDelegator().handleDataSet(dataSetFile);
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

    /**
     * Get all the data set information that has been created as a result of
     * {@link #registerDataSet}. Only makes sense to invoke after registerDataSet has been called
     */
    public ArrayList<DataSetInformation> getDataSetInformation()
    {
        return registeredDataSets;
    }

}
