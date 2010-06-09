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

package ch.systemsx.cisd.cina.dss.bundle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.etlserver.IDataSetHandler;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypeWithVocabularyTerms;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * Helper to help registering data sets.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class BundleDataSetHelper
{

    static class BundleRegistrationGlobalState
    {
        private final IDataSetHandler delegator;

        private final IEncapsulatedOpenBISService openbisService;

        private final SampleType replicaSampleType;

        private final DataSetTypeWithVocabularyTerms imageDataSetType;

        BundleRegistrationGlobalState(IDataSetHandler delegator,
                IEncapsulatedOpenBISService openbisService, SampleType replicaSampleType,
                DataSetTypeWithVocabularyTerms imageDataSetType)
        {
            this.delegator = delegator;
            this.openbisService = openbisService;
            this.replicaSampleType = replicaSampleType;
            this.imageDataSetType = imageDataSetType;
        }

        IDataSetHandler getDelegator()
        {
            return delegator;
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

    protected final BundleRegistrationGlobalState globalState;

    protected final File dataSet;

    protected final ArrayList<DataSetInformation> dataSetInformation;

    BundleDataSetHelper(BundleRegistrationGlobalState globalState, File dataSet)
    {
        this.globalState = globalState;
        this.dataSet = dataSet;
        this.dataSetInformation = new ArrayList<DataSetInformation>();
    }

    public void process()
    {
        // Register the bundle as one data set
        List<DataSetInformation> bigDataSet = getDelegator().handleDataSet(dataSet);
        dataSetInformation.addAll(bigDataSet);
    }

    protected IDataSetHandler getDelegator()
    {
        return globalState.getDelegator();
    }

    protected IEncapsulatedOpenBISService getOpenbisService()
    {
        return globalState.getOpenbisService();
    }

    /**
     * Get all the data set information that has been created as a result of {@link #process}. Only
     * makes sense to invoke after process has been called
     */
    public ArrayList<DataSetInformation> getDataSetInformation()
    {
        return dataSetInformation;
    }

}
