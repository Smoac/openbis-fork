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

package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.ShouldFlattenCollections;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetAccessPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentAccessPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleAccessPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleOwnerIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * @author Pawel Glyzewski
 */
@ShouldFlattenCollections(value = false)
public class DeletionTechIdCollectionPredicate extends AbstractSpacePredicate<List<TechId>>
{
    private SampleOwnerIdentifierCollectionPredicate sampleOwnerIdentifierCollectionPredicate;

    public DeletionTechIdCollectionPredicate()
    {
        this.sampleOwnerIdentifierCollectionPredicate =
                new SampleOwnerIdentifierCollectionPredicate(true);
    }

    @Override
    public final void init(IAuthorizationDataProvider provider)
    {
        super.init(provider);
        sampleOwnerIdentifierCollectionPredicate.init(provider);
    }

    @Override
    public String getCandidateDescription()
    {
        return "deletion technical id";
    }

    @Override
    protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles,
            List<TechId> value)
    {
        Set<ExperimentAccessPE> experiments =
                authorizationDataProvider.getDeletedExperimentCollectionAccessData(value);

        for (ExperimentAccessPE accessDatum : experiments)
        {
            String dbInstanceUUID = accessDatum.getDatabaseInstanceUuid();
            String dbInstanceCode = accessDatum.getDatabaseInstanceCode();
            String spaceCode = accessDatum.getSpaceCode();
            Status result =
                    evaluate(person, allowedRoles, dbInstanceUUID, dbInstanceCode, spaceCode);
            if (result != Status.OK)
            {
                return result;
            }
        }

        Set<SampleAccessPE> samples =
                authorizationDataProvider.getDeletedSampleCollectionAccessData(value);
        ArrayList<SampleOwnerIdentifier> ownerIds = new ArrayList<SampleOwnerIdentifier>();

        for (SampleAccessPE accessDatum : samples)
        {
            String ownerCode = accessDatum.getOwnerCode();
            switch (accessDatum.getOwnerType())
            {
                case SPACE:
                    ownerIds.add(new SampleOwnerIdentifier(new SpaceIdentifier(
                            DatabaseInstanceIdentifier.createHome(), ownerCode)));
                    break;
                case DATABASE_INSTANCE:
                    ownerIds.add(new SampleOwnerIdentifier(
                            new DatabaseInstanceIdentifier(ownerCode)));
                    break;
            }
        }

        Status status =
                sampleOwnerIdentifierCollectionPredicate.evaluate(person, allowedRoles, ownerIds);
        if (status != Status.OK)
        {
            return status;
        }

        Set<DataSetAccessPE> datasets =
                authorizationDataProvider.getDeletedDatasetCollectionAccessData(value);

        for (DataSetAccessPE accessDatum : datasets)
        {
            String dbInstanceUUID = accessDatum.getDatabaseInstanceUuid();
            String dbInstanceCode = accessDatum.getDatabaseInstanceCode();
            String spaceCode = accessDatum.getSpaceCode();
            Status result =
                    evaluate(person, allowedRoles, dbInstanceUUID, dbInstanceCode, spaceCode);
            if (result != Status.OK)
            {
                return result;
            }
        }

        return Status.OK;
    }
}
