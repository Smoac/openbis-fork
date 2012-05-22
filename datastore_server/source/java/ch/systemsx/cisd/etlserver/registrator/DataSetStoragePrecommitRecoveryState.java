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

package ch.systemsx.cisd.etlserver.registrator;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.etlserver.DssRegistrationLogDirectoryHelper;
import ch.systemsx.cisd.etlserver.DssRegistrationLogger;
import ch.systemsx.cisd.etlserver.registrator.AbstractOmniscientTopLevelDataSetRegistrator.OmniscientTopLevelDataSetRegistratorState;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.RollbackStack;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;

/**
 * @author jakubs
 */
public class DataSetStoragePrecommitRecoveryState<T extends DataSetInformation> implements
        Serializable
{
    private static final long serialVersionUID = 1L;

    private final TechId registrationId;

    private final List<DataSetStoragePrecommitRecoveryAlgorithm<T>> dataSetRecoveryStorageAlgorithms;

    private final File dssRegistrationLogFile;

    private final File[] rollbackStackBackingFiles;

    private final DataSetFile incomingDataSetFile;

    private final DataSetRegistrationContext persistentMap;

    public DataSetStoragePrecommitRecoveryState(TechId registrationId,
            List<DataSetStorageAlgorithm<T>> dataSetStorageAlgorithms,
            DssRegistrationLogger logger, IRollbackStack rollbackStack,
            DataSetFile incomingDataSetFile, DataSetRegistrationContext persistentMap)
    {
        this.registrationId = registrationId;
        this.dataSetRecoveryStorageAlgorithms =
                new ArrayList<DataSetStoragePrecommitRecoveryAlgorithm<T>>();
        for (DataSetStorageAlgorithm<T> algorithm : dataSetStorageAlgorithms)
        {
            this.dataSetRecoveryStorageAlgorithms.add(algorithm.getPrecommitRecoveryAlgorithm());
        }
        dssRegistrationLogFile = logger.getFile();
        this.rollbackStackBackingFiles = ((RollbackStack) rollbackStack).getBackingFiles();

        this.incomingDataSetFile = incomingDataSetFile;

        this.persistentMap = persistentMap;
    }

    public TechId getRegistrationId()
    {
        return registrationId;
    }

    public DataSetFile getIncomingDataSetFile()
    {
        return incomingDataSetFile;
    }

    public List<String> getDataSetCodes()
    {
        List<String> dataSetCodes = new ArrayList<String>();
        for (DataSetStoragePrecommitRecoveryAlgorithm<T> recoveryAlgorithm : this.dataSetRecoveryStorageAlgorithms)
        {
            dataSetCodes.add(recoveryAlgorithm.getDataSetCode());
        }
        return dataSetCodes;
    }

    public ArrayList<DataSetStorageAlgorithm<T>> getDataSetStorageAlgorithms(
            OmniscientTopLevelDataSetRegistratorState state)
    {
        ArrayList<DataSetStorageAlgorithm<T>> algorithms =
                new ArrayList<DataSetStorageAlgorithm<T>>();

        for (DataSetStoragePrecommitRecoveryAlgorithm<T> recoveryAlgorithm : this.dataSetRecoveryStorageAlgorithms)
        {
            algorithms.add(recoveryAlgorithm.recoverDataSetStorageAlgorithm(state));
        }
        return algorithms;
    }

    public DssRegistrationLogger getRegistrationLogger(
            OmniscientTopLevelDataSetRegistratorState state)
    {
        DssRegistrationLogDirectoryHelper helper =
                new DssRegistrationLogDirectoryHelper(state.getGlobalState()
                        .getDssRegistrationLogDir());

        DssRegistrationLogger logger =
                new DssRegistrationLogger(dssRegistrationLogFile, helper, state.getFileOperations());
        return logger;
    }

    public RollbackStack getRollbackStack()
    {
        return new RollbackStack(rollbackStackBackingFiles[0], rollbackStackBackingFiles[1]);
    }

    public DataSetRegistrationContext getPersistentMap()
    {
        return persistentMap;
    }
}
