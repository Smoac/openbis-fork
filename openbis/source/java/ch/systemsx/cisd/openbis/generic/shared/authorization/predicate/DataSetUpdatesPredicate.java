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
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.shared.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.AbstractTechIdPredicate.DataSetTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * An <code>IPredicate</code> implementation based on {@link DataSetUpdatesDTO}. Checks that: 1) the
 * user has rights to update the data set 2) if data set is moved to a different sample the user has
 * access to this sample.
 * 
 * @author Piotr Buczek
 */
public class DataSetUpdatesPredicate extends AbstractPredicate<DataSetUpdatesDTO>
{
    private final DataSetTechIdPredicate dataSetTechIdPredicate;

    private final SampleOwnerIdentifierPredicate samplePredicate;

    public DataSetUpdatesPredicate()
    {
        this.dataSetTechIdPredicate = new DataSetTechIdPredicate();
        this.samplePredicate = new SampleOwnerIdentifierPredicate();
    }

    public final void init(IAuthorizationDataProvider provider)
    {
        dataSetTechIdPredicate.init(provider);
        samplePredicate.init(provider);
    }

    @Override
    public final String getCandidateDescription()
    {
        return "data set updates";
    }

    @Override
    protected
    Status doEvaluation(final PersonPE person, final List<RoleWithIdentifier> allowedRoles,
            final DataSetUpdatesDTO updates)
    {
        assert dataSetTechIdPredicate.initialized : "Predicate has not been initialized";
        Status status;
        status = dataSetTechIdPredicate.doEvaluation(person, allowedRoles, updates.getDatasetId());
        if (status.equals(Status.OK) == false)
        {
            return status;
        }
        SampleIdentifier sampleIdentifierOrNull = updates.getSampleIdentifierOrNull();
        if (sampleIdentifierOrNull != null)
        {
            status = samplePredicate.doEvaluation(person, allowedRoles, sampleIdentifierOrNull);
        }
        return status;

    }
}
