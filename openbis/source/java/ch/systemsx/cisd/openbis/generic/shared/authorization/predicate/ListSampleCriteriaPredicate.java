/*
 * Copyright 2009 ETH Zuerich, CISD
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
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.shared.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.AbstractTechIdPredicate.ExperimentTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * An <code>IPredicate</code> implementation for {@link ListSampleCriteria}.
 * 
 * @author Bernd Rinn
 */
public class ListSampleCriteriaPredicate extends AbstractGroupPredicate<ListSampleCriteria>
{

    private final ExperimentTechIdPredicate experimentTechIdPredicate =
            new ExperimentTechIdPredicate();

    private final SampleTechIdPredicate sampleTechIdPredicate = new SampleTechIdPredicate();

    private DatabaseInstancePE homeDatabase;

    @Override
    public String getCandidateDescription()
    {
        return "sample listing criteria";
    }

    @Override
    public final void init(IAuthorizationDataProvider provider)
    {
        super.init(provider);
        homeDatabase = provider.getHomeDatabaseInstance();
        experimentTechIdPredicate.init(provider);
        sampleTechIdPredicate.init(provider);
    }

    @Override
    protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles,
            ListSampleCriteria value)
    {
        Status status = Status.OK;
        if (value.getExperimentId() != null)
        {
            status =
                    experimentTechIdPredicate.doEvaluation(person, allowedRoles, value
                            .getExperimentId());
        } else if (value.getContainerSampleId() != null)
        {
            status =
                    sampleTechIdPredicate.doEvaluation(person, allowedRoles, value
                            .getContainerSampleId());
        } else if (value.getParentSampleId() != null)
        {
            status =
                    sampleTechIdPredicate.doEvaluation(person, allowedRoles, value
                            .getParentSampleId());
        } else if (value.getChildSampleId() != null)
        {
            status =
                    sampleTechIdPredicate.doEvaluation(person, allowedRoles, value
                            .getChildSampleId());
        }
        if (value.isIncludeSpace() && status == Status.OK)
        {
            status = evaluate(person, allowedRoles, homeDatabase, value.getSpaceCode());
        }
        return status;
    }

}
