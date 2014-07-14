/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.authorization.validator;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetAccessPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentAccessPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleAccessPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * A {@link IValidator} implementation for a {@link Deletion}.
 * 
 * @author Izabela Adamczyk
 */
public final class DeletionValidator extends AbstractValidator<Deletion>
{
    //
    // IValidator
    //

    @Override
    public final boolean doValidation(final PersonPE person, final Deletion value)
    {
        // only creator of deletion and instance admin can see it
        return isRegistrator(person, value) || isInstanceAdmin(person)
                || isSpaceAdmin(person, value);
    }

    private boolean isSpaceAdmin(PersonPE person, Deletion value)
    {
        List<TechId> singletonList = Collections.singletonList(new TechId(value.getId()));

        Set<ExperimentAccessPE> experimentAccessData =
                authorizationDataProvider.getDeletedExperimentCollectionAccessData(singletonList);
        for (ExperimentAccessPE experimentAccessDatum : experimentAccessData)
        {
            if (verifySpace(person, experimentAccessDatum.getSpaceCode()))
            {
                return true;
            }
        }

        Set<SampleAccessPE> sampleAccessData =
                authorizationDataProvider.getDeletedSampleCollectionAccessData(singletonList);
        for (SampleAccessPE sampleAccessDatum : sampleAccessData)
        {
            String ownerCode = sampleAccessDatum.getOwnerCode();
            switch (sampleAccessDatum.getOwnerType())
            {
                case SPACE:
                    SpaceIdentifier si =
                            new SpaceIdentifier(ownerCode);
                    if (verifySpace(person, si.getSpaceCode()))
                    {
                        return true;
                    }

                    break;
                case DATABASE_INSTANCE:
                    if (verifyDBInstance(person, ownerCode))
                    {
                        return true;
                    }
                    break;
            }
        }

        Set<DataSetAccessPE> datasets =
                authorizationDataProvider.getDeletedDatasetCollectionAccessData(singletonList);
        for (DataSetAccessPE datasetAccessDatum : datasets)
        {
            if (verifySpace(person, datasetAccessDatum.getSpaceCode()))
            {
                return true;
            }
        }
        return false;
    }

    private final boolean verifyDBInstance(PersonPE person, final String databaseInstanceCode)
    {
        Set<RoleAssignmentPE> roleAssignments = person.getAllPersonRoles();
        for (final RoleAssignmentPE roleAssignment : roleAssignments)
        {
            final SpacePE space = roleAssignment.getSpace();
            if (space == null && roleAssignment.getRole().equals(RoleCode.ADMIN))
            {
                return true;
            }
        }
        return false;
    }

    private boolean verifySpace(PersonPE person, String spaceCode)
    {
        final Set<RoleAssignmentPE> roleAssignments = person.getAllPersonRoles();
        for (final RoleAssignmentPE roleAssignment : roleAssignments)
        {
            final SpacePE space = roleAssignment.getSpace();
            if (space != null && roleAssignment.getRole().equals(RoleCode.ADMIN))
            {
                if (space.getCode().equals(spaceCode))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isRegistrator(final PersonPE person, final Deletion value)
    {
        Person registrator = value.getRegistrator();
        return person.getUserId().equals(registrator.getUserId());
    }

    private static boolean isInstanceAdmin(final PersonPE person)
    {
        final Set<RoleAssignmentPE> roleAssignments = person.getAllPersonRoles();
        for (final RoleAssignmentPE roleAssignment : roleAssignments)
        {
            if (roleAssignment.getSpace() == null && roleAssignment.getRole().equals(RoleCode.ADMIN))
            {
                return true;
            }
        }
        return false;
    }
}
