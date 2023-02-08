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

package ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization;

import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.authorization.IAuthorizationGuardPredicate;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwner;

/**
 * Predicate for checking that the new data set can be registered (i.e., user has access to the space for the new data set).
 * <p>
 * <i>This is an internal class. Do not use it as a user of the API.</i>
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class NewDataSetPredicate implements
        IAuthorizationGuardPredicate<IDssServiceRpcGeneric, NewDataSetDTO>
{

    @Override
    public List<String> getDataSetCodes(NewDataSetDTO argument)
    {
        return Arrays.asList();
    }

    @Override
    public Status evaluate(IDssServiceRpcGeneric receiver, String sessionToken,
            NewDataSetDTO newDataSet) throws UserFailureException
    {
        if (newDataSet == null)
        {
            throw new UserFailureException("New data set cannot be null");
        }

        DataSetOwner owner = newDataSet.getDataSetOwner();

        if (owner == null)
        {
            throw new UserFailureException("Owner of a new data set cannot be null");
        }

        String ownerIdentifier = owner.getIdentifier();

        if (ownerIdentifier == null)
        {
            throw new UserFailureException("Owner identifier of a new data set cannot be null");
        }

        switch (owner.getType())
        {
            case EXPERIMENT:
                return DssSessionAuthorizationHolder.getAuthorizer().checkExperimentWriteable(sessionToken, ownerIdentifier);
            case SAMPLE:
                return DssSessionAuthorizationHolder.getAuthorizer().checkSampleWriteable(sessionToken, ownerIdentifier);
            case DATA_SET:
                return DssSessionAuthorizationHolder.getAuthorizer().checkDatasetAccess(sessionToken, ownerIdentifier);
        }

        return null;
    }

}
