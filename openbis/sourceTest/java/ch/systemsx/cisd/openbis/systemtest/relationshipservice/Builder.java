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

package ch.systemsx.cisd.openbis.systemtest.relationshipservice;

import ch.systemsx.cisd.openbis.generic.server.ICommonServerForInternalUse;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;

/**
 * @author anttil
 */
public abstract class Builder<T>
{
    protected final ICommonServerForInternalUse commonServer;

    protected final IGenericServer genericServer;

    protected final String systemSession;

    public Builder(ICommonServerForInternalUse commonServer, IGenericServer genericServer)
    {
        this.commonServer = commonServer;
        this.genericServer = genericServer;
        this.systemSession = commonServer.tryToAuthenticateAsSystem().getSessionToken();
    }

    public abstract T create();

    protected static SampleIdentifier getSampleIdentifier(Sample sample)
    {
        DatabaseInstanceIdentifier dbin;
        if (sample.getSpace() != null)
        {
            dbin = new DatabaseInstanceIdentifier(sample.getSpace().getInstance().getCode());
            return new SampleIdentifier(new SpaceIdentifier(dbin, sample.getSpace().getCode()),
                    sample.getCode());
        } else
        {
            dbin = new DatabaseInstanceIdentifier(sample.getDatabaseInstance().getCode());
            return new SampleIdentifier(dbin, sample.getCode());
        }

    }
}
