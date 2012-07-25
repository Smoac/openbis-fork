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

package ch.systemsx.cisd.openbis.dss.screening.server.plugins.jython;

import java.io.File;
import java.util.Properties;

import ch.systemsx.cisd.common.utilities.IDelegatedActionWithResult;
import ch.systemsx.cisd.etlserver.registrator.AbstractOmniscientTopLevelDataSetRegistrator.NoOpDelegate;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSetRegistrationTransaction;
import ch.systemsx.cisd.etlserver.registrator.recovery.AutoRecoverySettings;
import ch.systemsx.cisd.etlserver.registrator.DataSetFile;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationService;
import ch.systemsx.cisd.etlserver.registrator.IDataSetRegistrationDetailsFactory;
import ch.systemsx.cisd.openbis.dss.etl.jython.ImagingDataSetRegistrationTransaction;
import ch.systemsx.cisd.openbis.dss.etl.jython.JythonPlateDataSetHandlerUtils;
import ch.systemsx.cisd.openbis.dss.etl.jython.JythonPlateDatasetFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython.IPluginScriptRunnerFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython.JythonBasedDbModifyingAggregationServiceReportingPlugin;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * A version of the {@link JythonBasedDbModifyingAggregationServiceReportingPlugin} with extra
 * support for screening.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class ScreeningJythonBasedDbModifyingAggregationServiceReportingPlugin extends
        JythonBasedDbModifyingAggregationServiceReportingPlugin
{

    private static final long serialVersionUID = 1L;

    /**
     * Public constructor.
     */
    public ScreeningJythonBasedDbModifyingAggregationServiceReportingPlugin(Properties properties,
            File storeRoot)
    {
        this(properties, storeRoot, new ScreeningPluginScriptRunnerFactory(
                getScriptPathProperty(properties)));
    }

    /**
     * Constructor used in tests.
     */
    protected ScreeningJythonBasedDbModifyingAggregationServiceReportingPlugin(
            Properties properties, File storeRoot, IPluginScriptRunnerFactory scriptRunnerFactory)
    {
        super(properties, storeRoot, scriptRunnerFactory);
    }

    @Override
    protected DataSetRegistrationService<DataSetInformation> createRegistrationService(
            DataSetFile incoming, IDelegatedActionWithResult<Boolean> cleanUpAction,
            NoOpDelegate delegate)
    {
        IDataSetRegistrationDetailsFactory<DataSetInformation> registrationDetailsFactory =
                (IDataSetRegistrationDetailsFactory<DataSetInformation>) new JythonPlateDatasetFactory(
                        getRegistratorState(), null);

        DataSetRegistrationService<DataSetInformation> service =
                new DataSetRegistrationService<DataSetInformation>(this, incoming,
                        registrationDetailsFactory, cleanUpAction, delegate)
                    {
                        @SuppressWarnings("unchecked")
                        @Override
                        protected DataSetRegistrationTransaction<DataSetInformation> createTransaction(
                                File rollBackStackParentFolder,
                                File workingDirectory,
                                File stagingDirectory,
                                IDataSetRegistrationDetailsFactory<DataSetInformation> factory)
                        {
                            return new ImagingDataSetRegistrationTransaction(
                                    rollBackStackParentFolder, workingDirectory, stagingDirectory,
                                    this, factory,
                                    JythonPlateDataSetHandlerUtils.parseOriginalDir(properties),
                                    AutoRecoverySettings.DO_NOT_USE_AUTO_RECOVERY);
                        }
                    };
        return service;
    }
}
