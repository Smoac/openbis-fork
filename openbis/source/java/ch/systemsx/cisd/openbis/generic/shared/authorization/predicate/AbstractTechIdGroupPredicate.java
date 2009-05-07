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

package ch.systemsx.cisd.openbis.generic.shared.authorization.predicate;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.shared.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationDataProvider.EntityWithGroupKind;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.exception.UndefinedGroupException;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.util.GroupCodeHelper;

/**
 * An <code>IPredicate</code> abstract implementation based on {@link TechId} and
 * {@link EntityWithGroupKind}
 * 
 * @author Piotr Buczek
 */
public abstract class AbstractTechIdGroupPredicate extends AbstractGroupPredicate<TechId>
{
    private EntityWithGroupKind entityKind;

    public AbstractTechIdGroupPredicate(EntityWithGroupKind entityKind)
    {
        this.entityKind = entityKind;
    }

    public static AbstractTechIdGroupPredicate create(EntityWithGroupKind entityKind)
    {
        switch (entityKind)
        {
            case EXPERIMENT:
                return new ExperimentTechIdGroupPredicate();
            case PROJECT:
                return new ProjectTechIdGroupPredicate();
            case GROUP:
                return new GroupTechIdGroupPredicate();
        }
        return null;
    }

    public static class ExperimentTechIdGroupPredicate extends AbstractTechIdGroupPredicate
    {
        ExperimentTechIdGroupPredicate()
        {
            super(EntityWithGroupKind.EXPERIMENT);
        }
    }

    public static class GroupTechIdGroupPredicate extends AbstractTechIdGroupPredicate
    {
        GroupTechIdGroupPredicate()
        {
            super(EntityWithGroupKind.GROUP);
        }
    }

    public static class ProjectTechIdGroupPredicate extends AbstractTechIdGroupPredicate
    {
        ProjectTechIdGroupPredicate()
        {
            super(EntityWithGroupKind.PROJECT);
        }
    }

    //
    // AbstractDatabaseInstancePredicate
    //

    @Override
    public final String getCandidateDescription()
    {
        return "group identifier";
    }

    @Override
    Status doEvaluation(final PersonPE person, final List<RoleWithIdentifier> allowedRoles,
            final TechId techId)
    {
        assert inited : "Predicate has not been initialized";

        if (techId == null)
        {
            return Status.OK;
        }
        GroupPE groupOrNull = authorizationDataProvider.tryToGetGroup(entityKind, techId);
        if (groupOrNull == null)
        {
            throw new UndefinedGroupException();
        }

        final String groupCode = GroupCodeHelper.getGroupCode(person, groupOrNull);
        final DatabaseInstancePE databaseInstance = groupOrNull.getDatabaseInstance();

        final boolean matching = evaluate(allowedRoles, databaseInstance, groupCode);
        if (matching)
        {
            return Status.OK;
        }
        return Status.createError(String.format(
                "User '%s' does not have enough privileges to access data in the group '%s'.",
                person.getUserId(), new GroupIdentifier(databaseInstance.getCode(), groupCode)));
    }
}
