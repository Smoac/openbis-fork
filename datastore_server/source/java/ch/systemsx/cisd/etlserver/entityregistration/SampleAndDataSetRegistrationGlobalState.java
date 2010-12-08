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

package ch.systemsx.cisd.etlserver.entityregistration;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * Global state for the SampleAndDatasetRegistration operation.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class SampleAndDataSetRegistrationGlobalState
{
    enum SampleRegistrationMode
    {
        ACCEPT_ALL, REJECT_EXISTING, REJECT_NONEXISTING
    }

    private final IEncapsulatedOpenBISService openbisService;

    private final SpaceIdentifier spaceIdentifierOrNull;

    private final SampleType sampleTypeOrNull;

    private final SampleRegistrationMode sampleRegistrationMode;

    private final Logger operationLog;

    SampleAndDataSetRegistrationGlobalState(IEncapsulatedOpenBISService openbisService,
            SpaceIdentifier spaceIdentifierOrNull, SampleType sampleTypeOrNull,
            SampleRegistrationMode sampleRegistrationMode, Logger operationLog)
    {
        this.openbisService = openbisService;
        this.spaceIdentifierOrNull = spaceIdentifierOrNull;
        this.sampleTypeOrNull = sampleTypeOrNull;
        this.sampleRegistrationMode = sampleRegistrationMode;
        this.operationLog = operationLog;
    }

    /**
     * The connection to the Application Server.
     */
    public IEncapsulatedOpenBISService getOpenbisService()
    {
        return openbisService;
    }

    /**
     * Return true if the dropbox puts all samples/data sets in the specified space identifier
     */
    public boolean hasGlobalSpaceIdentifier()
    {
        return null != spaceIdentifierOrNull;
    }

    /**
     * Rreturn the space identifier for the drop box (if there is one).
     */
    public SpaceIdentifier trySpaceIdentifier()
    {
        return spaceIdentifierOrNull;
    }

    public SampleType trySampleType()
    {
        return sampleTypeOrNull;
    }

    public SampleRegistrationMode getSampleRegistrationMode()
    {
        return sampleRegistrationMode;
    }

    public Logger getOperationLog()
    {
        return operationLog;
    }
}
