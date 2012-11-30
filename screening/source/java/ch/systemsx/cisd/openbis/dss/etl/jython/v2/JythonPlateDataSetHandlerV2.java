/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.etl.jython.v2;

import java.io.File;

import ch.systemsx.cisd.common.action.IDelegatedActionWithResult;
import ch.systemsx.cisd.common.jython.PythonInterpreter;
import ch.systemsx.cisd.etlserver.ITopLevelDataSetRegistratorDelegate;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.DataSetFile;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSetRegistrationTransaction;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSetRegistrationTransaction;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetRegistrationTransactionV2;
import ch.systemsx.cisd.etlserver.registrator.api.v2.JythonDataSetRegistrationServiceV2;
import ch.systemsx.cisd.etlserver.registrator.api.v2.JythonTopLevelDataSetHandlerV2;
import ch.systemsx.cisd.etlserver.registrator.recovery.AutoRecoverySettings;
import ch.systemsx.cisd.etlserver.registrator.v1.DataSetRegistrationService;
import ch.systemsx.cisd.etlserver.registrator.v1.IDataSetRegistrationDetailsFactory;
import ch.systemsx.cisd.openbis.dss.etl.jython.ImagingDataSetRegistrationTransaction;
import ch.systemsx.cisd.openbis.dss.etl.jython.JythonPlateDataSetHandlerUtils;
import ch.systemsx.cisd.openbis.dss.etl.jython.JythonPlateDatasetFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * @author jakubs
 */
public class JythonPlateDataSetHandlerV2 extends JythonTopLevelDataSetHandlerV2<DataSetInformation>
{
    private final String originalDirName;

    /**
     * @param globalState
     */
    public JythonPlateDataSetHandlerV2(TopLevelDataSetRegistratorGlobalState globalState)
    {
        super(globalState);
        originalDirName =
                JythonPlateDataSetHandlerUtils.parseOriginalDir(globalState.getThreadParameters()
                        .getThreadProperties());
    }

    /**
     * Create a screening specific factory available to the python script.
     */
    @Override
    public IDataSetRegistrationDetailsFactory<DataSetInformation> createObjectFactory(
            DataSetInformation userProvidedDataSetInformationOrNull)
    {
        return new JythonPlateDatasetFactory(getRegistratorState(),
                userProvidedDataSetInformationOrNull);
    }

    @Override
    protected DataSetRegistrationService<DataSetInformation> createDataSetRegistrationService(
            DataSetFile incomingDataSetFile, DataSetInformation callerDataSetInformationOrNull,
            IDelegatedActionWithResult<Boolean> cleanAfterwardsAction,
            ITopLevelDataSetRegistratorDelegate delegate)
    {
        return new JythonDataSetRegistrationServiceV2<DataSetInformation>(this,
                incomingDataSetFile, callerDataSetInformationOrNull, cleanAfterwardsAction,
                delegate, PythonInterpreter.createIsolatedPythonInterpreter(), getGlobalState())
            {
                @SuppressWarnings("unchecked")
                @Override
                protected DataSetRegistrationTransaction<DataSetInformation> createV2DatasetRegistrationTransaction(
                        File rollBackStackParentFolder,
                        File workingDirectory,
                        File stagingDirectory,
                        IDataSetRegistrationDetailsFactory<DataSetInformation> registrationDetailsFactory)
                {
                    return new ImagingDataSetRegistrationTransaction(rollBackStackParentFolder,
                            workingDirectory, stagingDirectory, this, registrationDetailsFactory,
                            originalDirName, AutoRecoverySettings.USE_AUTO_RECOVERY);
                }
            };
    }

    @Override
    protected IDataSetRegistrationTransactionV2 wrapTransaction(
            IDataSetRegistrationTransaction transaction)
    {
        return new ImagingDataSetRegistrationTransactionV2Delegate(
                (ImagingDataSetRegistrationTransaction) transaction);
    }
}
