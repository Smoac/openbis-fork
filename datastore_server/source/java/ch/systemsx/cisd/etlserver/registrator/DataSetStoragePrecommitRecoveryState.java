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

import ch.systemsx.cisd.etlserver.DssRegistrationLogger;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.RollbackStack;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * @author jakubs
 */
public class DataSetStoragePrecommitRecoveryState<T extends DataSetInformation> implements
        Serializable
{
    private static final long serialVersionUID = 1L;

    private final List<DataSetStoragePrecommitRecoveryAlgorithm<T>> dataSetStorageAlgorithms;

    private final File dssRegistrationLogFile;

    private final File[] rollbackStackBackingFiles;

    public DataSetStoragePrecommitRecoveryState(
            List<DataSetStorageAlgorithm<T>> dataSetStorageAlgorithms,
            DssRegistrationLogger logger, IRollbackStack rollbackStack)
    {
        this.dataSetStorageAlgorithms =
                new ArrayList<DataSetStoragePrecommitRecoveryAlgorithm<T>>();
        for (DataSetStorageAlgorithm<T> algorithm : dataSetStorageAlgorithms)
        {
            this.dataSetStorageAlgorithms.add(algorithm.getPrecommitRecoveryAlgorithm());
        }
        dssRegistrationLogFile = logger.getFile();
        this.rollbackStackBackingFiles = ((RollbackStack) rollbackStack).getBackingFiles();
    }

    public List<DataSetStoragePrecommitRecoveryAlgorithm<T>> getDataSetStorageAlgorithms()
    {
        return dataSetStorageAlgorithms;
    }

    public File getDssRegistrationLogFile()
    {
        return dssRegistrationLogFile;
    }

    public File[] getRollbackStackBackingFiles()
    {
        return rollbackStackBackingFiles;
    }

}
