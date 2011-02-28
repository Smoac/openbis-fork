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

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwner;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * Predicate for checking that the new data set can be registered (i.e., user has access to the
 * space for the new data set).
 * <p>
 * <i>This is an internal class. Do not use it as a user of the API.</i>
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class NewDataSetPredicate implements
        IAuthorizationGuardPredicate<IDssServiceRpcGeneric, NewDataSetDTO>
{
    public Status evaluate(IDssServiceRpcGeneric receiver, String sessionToken,
            NewDataSetDTO newDataSet) throws UserFailureException
    {
        SpaceIdentifier spaceId = getSpaceIdentifier(newDataSet);
        return DssSessionAuthorizationHolder.getAuthorizer().checkSpaceWriteable(sessionToken,
                spaceId);
    }

    private SpaceIdentifier getSpaceIdentifier(NewDataSetDTO newDataSet)
    {
        SpaceIdentifier spaceId = null;
        DataSetOwner owner = newDataSet.getDataSetOwner();
        String ownerIdentifier = owner.getIdentifier();
        switch (owner.getType())
        {
            case EXPERIMENT:
            {
                ExperimentIdentifier experimentId =
                        new ExperimentIdentifierFactory(ownerIdentifier).createIdentifier();
                spaceId =
                        new SpaceIdentifier(experimentId.getDatabaseInstanceCode(),
                                experimentId.getSpaceCode());
                break;
            }
            case SAMPLE:
            {
                SampleIdentifier sampleId =
                        new SampleIdentifierFactory(ownerIdentifier).createIdentifier();
                spaceId = sampleId.getSpaceLevel();
                break;
            }
        }
        return spaceId;
    }

}
