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

package ch.systemsx.cisd.openbis.plugin.screening.shared.authorization;

import java.util.List;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.shared.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.AbstractTechIdPredicate.ExperimentTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.IPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.SpaceIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.SingleExperimentSearchCriteria;

/**
 * @author Tomasz Pylak
 */
public final class WellSearchCriteriaPredicate implements IPredicate<WellSearchCriteria>
{
    private final IPredicate<TechId> experimentPredicate;

    private final SpaceIdentifierPredicate spacePredicate;

    public WellSearchCriteriaPredicate()
    {
        this.experimentPredicate = new ExperimentTechIdPredicate();
        this.spacePredicate = new SpaceIdentifierPredicate();
    }

    public final void init(IAuthorizationDataProvider provider)
    {
        experimentPredicate.init(provider);
        spacePredicate.init(provider);
    }

    public final Status evaluate(final PersonPE person,
            final List<RoleWithIdentifier> allowedRoles, final WellSearchCriteria value)
    {
        assert person != null : "Unspecified person";
        assert allowedRoles != null : "Unspecified allowed roles";
        if (value == null)
        {
            throw UserFailureException.fromTemplate("No well search criteria specified.");
        }
        try
        {
            ExperimentSearchCriteria experimentCriteria = value.getExperimentCriteria();
            SingleExperimentSearchCriteria experiment = experimentCriteria.tryGetExperiment();
            BasicProjectIdentifier project = experimentCriteria.tryGetProjectIdentifier();
            if (experiment != null)
            {
                return experimentPredicate.evaluate(person, allowedRoles,
                        experiment.getExperimentId());
            } else if (project != null)
            {
                SpaceIdentifier space =
                        new SpaceIdentifier(project.getInstanceCode(), project.getSpaceCode());
                return spacePredicate.evaluate(person, allowedRoles, space);
            } else
            {
                return Status.OK;
            }
        } catch (DataAccessException ex)
        {
            throw new UserFailureException(ex.getMessage(), ex);
        }
    }
}
