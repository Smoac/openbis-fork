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

package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.SpaceOwnerKind;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.ShouldFlattenCollections;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * Predicates for list of {@link TechId} and all {@link SpaceOwnerKind}s.
 * 
 * @author Franz-Josef Elmer
 */
@ShouldFlattenCollections(value = false)
public class AbstractTechIdCollectionPredicate extends AbstractSpacePredicate<List<TechId>>
{
    private final SpaceOwnerKind entityKind;

    private AbstractTechIdCollectionPredicate(SpaceOwnerKind entityKind)
    {
        this.entityKind = entityKind;
    }

    public static class DataSetTechIdCollectionPredicate extends AbstractTechIdCollectionPredicate
    {
        public DataSetTechIdCollectionPredicate()
        {
            super(SpaceOwnerKind.DATASET);
        }
    }

    public static class ExperimentTechIdCollectionPredicate extends
            AbstractTechIdCollectionPredicate
    {
        public ExperimentTechIdCollectionPredicate()
        {
            super(SpaceOwnerKind.EXPERIMENT);
        }
    }

    public static class SpaceTechIdCollectionPredicate extends AbstractTechIdCollectionPredicate
    {
        public SpaceTechIdCollectionPredicate()
        {
            super(SpaceOwnerKind.SPACE);
        }
    }

    public static class ProjectTechIdCollectionPredicate extends AbstractTechIdCollectionPredicate
    {
        public ProjectTechIdCollectionPredicate()
        {
            super(SpaceOwnerKind.PROJECT);
        }
    }

    @Override
    public String getCandidateDescription()
    {
        return entityKind + " technical id collection";
    }

    @Override
    protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles,
            List<TechId> techIds)
    {
        assert initialized : "Predicate has not been initialized";

        Set<SpacePE> entitySpaces =
                authorizationDataProvider.getDistinctSpacesByEntityIds(entityKind, techIds);
        for (SpacePE space : entitySpaces)
        {
            Status result = evaluateSpace(person, allowedRoles, space);
            if (result.isOK() == false)
            {
                return result;
            }
        }
        return Status.OK;
    }

}
