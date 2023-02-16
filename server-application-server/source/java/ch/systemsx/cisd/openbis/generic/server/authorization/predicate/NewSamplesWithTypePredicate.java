/*
 * Copyright ETH 2009 - 2023 Zürich, Scientific IT Services
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

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * An <code>IPredicate</code> implementation for {@link NewSamplesWithTypes}.
 * 
 * @author Izabela Adamczyk
 */
public final class NewSamplesWithTypePredicate extends AbstractPredicate<NewSamplesWithTypes>
{

    private NewSampleCollectionPredicate newSampleCollectionPredicate;

    public NewSamplesWithTypePredicate()
    {
        newSampleCollectionPredicate = new NewSampleCollectionPredicate();
    }

    @Override
    public void init(IAuthorizationDataProvider provider)
    {
        newSampleCollectionPredicate.init(provider);
    }

    @Override
    protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles, NewSamplesWithTypes value)
    {
        return newSampleCollectionPredicate.doEvaluation(person, allowedRoles, value.getNewEntities());
    }

    @Override
    public String getCandidateDescription()
    {
        return "new samples with type";
    }

}
