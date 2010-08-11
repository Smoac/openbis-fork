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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.ShouldFlattenCollections;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleAccessPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleOwnerIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * An <code>IPredicate</code> implementation based on a list of sample {@link TechId}s.
 * 
 * @author Piotr Buczek
 */
@ShouldFlattenCollections(value = false)
public class SampleTechIdCollectionPredicate extends
        DelegatedPredicate<List<SampleOwnerIdentifier>, List<TechId>>
{

    public SampleTechIdCollectionPredicate()
    {
        super(new SampleOwnerIdentifierCollectionPredicate(true));
    }

    @Override
    public List<SampleOwnerIdentifier> tryConvert(List<TechId> techIds)
    {
        ArrayList<SampleOwnerIdentifier> ownerIds = new ArrayList<SampleOwnerIdentifier>();

        Set<SampleAccessPE> accessData =
                authorizationDataProvider.getSampleCollectionAccessData(techIds);

        for (SampleAccessPE accessDatum : accessData)
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

        return ownerIds;
    }

    @Override
    public final String getCandidateDescription()
    {
        return "sample technical ids";
    }

}
