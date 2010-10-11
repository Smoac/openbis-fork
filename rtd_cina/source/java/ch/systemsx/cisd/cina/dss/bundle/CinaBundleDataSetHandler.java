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
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.cina.dss.bundle.BundleDataSetHelper.BundleRegistrationGlobalState;
import ch.systemsx.cisd.cina.shared.constants.CinaConstants;
import ch.systemsx.cisd.etlserver.IDataSetHandler;
import ch.systemsx.cisd.etlserver.IDataSetHandlerRpc;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypeWithVocabularyTerms;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class CinaBundleDataSetHandler implements IDataSetHandler
{
    private final IDataSetHandler delegator;

    private final BundleRegistrationGlobalState bundleRegistrationGlobalState;

    public CinaBundleDataSetHandler(Properties parentProperties, IDataSetHandler delegator,
            IEncapsulatedOpenBISService openbisService)
    {
        this.delegator = delegator;
        this.bundleRegistrationGlobalState =
                createBundleRegistrationGlobalState(delegator, openbisService);
    }

    public List<DataSetInformation> handleDataSet(File dataSet)
    {
        BundleDataSetHelper helper;
        if (delegator instanceof IDataSetHandlerRpc)
        {
            helper = new BundleDataSetHelperRpc(bundleRegistrationGlobalState, dataSet);
        } else
        {
            helper = new BundleDataSetHelper(bundleRegistrationGlobalState, dataSet);
        }

        helper.process();
        return helper.getDataSetInformation();
    }

    private static BundleRegistrationGlobalState createBundleRegistrationGlobalState(
            IDataSetHandler delegator, IEncapsulatedOpenBISService openbisService)
    {
        SampleType replicaSampleType =
                openbisService.getSampleType(CinaConstants.REPLICA_SAMPLE_TYPE_CODE);
        DataSetTypeWithVocabularyTerms imageDataSetType =
                openbisService.getDataSetType(CinaConstants.IMAGE_DATA_SET_TYPE_CODE);
        return new BundleRegistrationGlobalState(delegator, openbisService, replicaSampleType,
                imageDataSetType);
    }
}
