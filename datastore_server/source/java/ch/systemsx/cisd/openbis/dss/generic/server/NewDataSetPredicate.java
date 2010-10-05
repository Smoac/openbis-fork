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

package ch.systemsx.cisd.openbis.dss.generic.server;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.authorization.IAuthorizationGuardPredicate;
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
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class NewDataSetPredicate implements
        IAuthorizationGuardPredicate<IDssServiceRpcGenericInternal, NewDataSetDTO>
{
    public Status evaluate(IDssServiceRpcGenericInternal receiver, String sessionToken,
            NewDataSetDTO newDataSet) throws UserFailureException
    {
        SpaceIdentifier spaceId = getSpaceIdentifier(newDataSet);
        if (receiver.isSpaceWriteable(sessionToken, spaceId))
        {
            return Status.OK;
        } else
        {
            return Status.createError("Space (" + spaceId + ") is not writeable.");
        }
    }

    private SpaceIdentifier getSpaceIdentifier(NewDataSetDTO newDataSet)
    {
        SpaceIdentifier spaceId = null;
        DataSetOwner owner = newDataSet.getDataSetOwner();
        switch (owner.getType())
        {
            case EXPERIMENT:
            {
                ExperimentIdentifier experimentId = tryExperimentIdentifier(newDataSet);
                spaceId =
                        new SpaceIdentifier(experimentId.getDatabaseInstanceCode(),
                                experimentId.getSpaceCode());
                break;
            }
            case SAMPLE:
            {
                SampleIdentifier sampleId = trySampleIdentifier(newDataSet);
                spaceId = sampleId.getSpaceLevel();
                break;
            }
        }
        return spaceId;
    }

    private ExperimentIdentifier tryExperimentIdentifier(NewDataSetDTO newDataSet)
    {
        DataSetOwner owner = newDataSet.getDataSetOwner();
        switch (owner.getType())
        {
            case EXPERIMENT:
            {
                return new ExperimentIdentifierFactory(owner.getIdentifier()).createIdentifier();
            }
            case SAMPLE:
            {
                return null;
            }
        }

        return null;
    }

    private SampleIdentifier trySampleIdentifier(NewDataSetDTO newDataSet)
    {
        DataSetOwner owner = newDataSet.getDataSetOwner();
        switch (owner.getType())
        {
            case EXPERIMENT:
            {
                return null;
            }
            case SAMPLE:
            {
                return new SampleIdentifierFactory(owner.getIdentifier()).createIdentifier();

            }
        }

        return null;
    }

}
