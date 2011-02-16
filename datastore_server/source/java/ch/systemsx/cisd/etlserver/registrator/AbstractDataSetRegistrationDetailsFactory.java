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

package ch.systemsx.cisd.etlserver.registrator;

import java.io.File;

import ch.systemsx.cisd.etlserver.registrator.AbstractOmniscientTopLevelDataSetRegistrator.OmniscientTopLevelDataSetRegistratorState;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSet;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.dto.types.DataSetTypeCode;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public abstract class AbstractDataSetRegistrationDetailsFactory<T extends DataSetInformation>
        implements IDataSetRegistrationDetailsFactory<T>
{
    protected final OmniscientTopLevelDataSetRegistratorState registratorState;

    public AbstractDataSetRegistrationDetailsFactory(
            OmniscientTopLevelDataSetRegistratorState registratorState)
    {
        this.registratorState = registratorState;
    }

    /**
     * Factory method that creates a new registration details object.
     */
    public DataSetRegistrationDetails<T> createDataSetRegistrationDetails()
    {
        DataSetRegistrationDetails<T> registrationDetails = new DataSetRegistrationDetails<T>();
        T dataSetInfo = createDataSetInformation();
        if (null == dataSetInfo.getDataSetType())
        {
            dataSetInfo.setDataSetType(new DataSetType(DataSetTypeCode.UNKNOWN.getCode()));
        }
        setDatabaseInstance(dataSetInfo);
        registrationDetails.setDataSetInformation(dataSetInfo);
        return registrationDetails;
    }

    protected final void setDatabaseInstance(DataSetInformation dataSetInfo)
    {
        dataSetInfo.setInstanceCode(registratorState.getHomeDatabaseInstance().getCode());
        dataSetInfo.setInstanceUUID(registratorState.getHomeDatabaseInstance().getUuid());
    }

    public DataSet<T> createDataSet(DataSetRegistrationDetails<T> registrationDetails,
            File stagingFile)
    {
        return new DataSet<T>(registrationDetails, stagingFile);
    }

    /**
     * Factory method that creates a new data set information object.
     */
    protected abstract T createDataSetInformation();

}
