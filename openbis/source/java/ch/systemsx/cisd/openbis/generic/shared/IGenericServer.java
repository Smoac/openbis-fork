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

package ch.systemsx.cisd.openbis.generic.shared;

import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.ReturnValueFilter;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RoleSet;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.GroupIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.validator.GroupValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleOwnerIdentifier;

/**
 * Definition of the client-server interface.
 * 
 * @author Franz-Josef Elmer
 */
public interface IGenericServer extends IServer
{
    /**
     * Returns all groups which belong to the specified database instance.
     */
    @Transactional
    @RolesAllowed(RoleSet.OBSERVER)
    @ReturnValueFilter(validatorClass = GroupValidator.class)
    public List<GroupPE> listGroups(String sessionToken, DatabaseInstanceIdentifier identifier);

    /**
     * Registers a new group with specified code and optional description and group leader ID.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    public void registerGroup(String sessionToken, String groupCode, String descriptionOrNull,
            String groupLeaderOrNull);

    /**
     * Returns all persons from current instance.
     */
    @Transactional
    @RolesAllowed(RoleSet.OBSERVER)
    public List<PersonPE> listPersons(String sessionToken);

    /**
     * Registers a new person.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    public void registerPerson(String sessionToken, String userID);

    /**
     * Returns a list of all roles.
     */
    @Transactional
    @RolesAllowed(RoleSet.GROUP_ADMIN)
    public List<RoleAssignmentPE> listRoles(String sessionToken);

    /**
     * Registers a new group role.
     */
    @Transactional
    @RolesAllowed(RoleSet.GROUP_ADMIN)
    public void registerGroupRole(String sessionToken, RoleCode roleCode,
            @AuthorizationGuard(guardClass = GroupIdentifierPredicate.class)
            GroupIdentifier identifier, String person);

    /**
     * Registers a new instance role.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    public void registerInstanceRole(String sessionToken, RoleCode roleCode, String person);

    /**
     * Deletes role described by given role code, group identifier and user id.
     */
    @Transactional
    @RolesAllowed(RoleSet.GROUP_ADMIN)
    public void deleteGroupRole(String sessionToken, RoleCode roleCode,
            @AuthorizationGuard(guardClass = GroupIdentifierPredicate.class)
            GroupIdentifier groupIdentifier, String person);

    /**
     * Deletes role described by given role code and user id.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    public void deleteInstanceRole(String sessionToken, RoleCode roleCode, String person);

    /**
     * Lists sample types which are appropriate for listing.
     */
    @Transactional
    @RolesAllowed(RoleSet.OBSERVER)
    public List<SampleTypePE> listSampleTypes(String sessionToken);

    /**
     * Lists samples using given configuration.No properties are loaded.
     */
    @Transactional
    @RolesAllowed(RoleSet.OBSERVER)
    public List<SamplePE> listSamples(String sessionToken,
            List<SampleOwnerIdentifier> ownerIdentifiers, SampleTypePE sampleType);

    /**
     * Lists chosen properties for given samples.
     */
    @Transactional
    @RolesAllowed(RoleSet.OBSERVER)
    public Map<SampleIdentifier, List<SamplePropertyPE>> listSamplesProperties(String sessionToken,
            List<SampleIdentifier> sampleIdentifiers, List<PropertyTypePE> list);

    /**
     * For given {@link SampleIdentifier} returns the corresponding {@link SamplePE}.
     */
    @Transactional
    @RolesAllowed(RoleSet.OBSERVER)
    public SamplePE getSampleInfo(final String sessionToken, final SampleIdentifier identifier);
}
