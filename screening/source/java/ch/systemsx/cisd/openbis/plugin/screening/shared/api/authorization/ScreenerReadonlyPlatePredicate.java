/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.shared.api.authorization;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.ShouldFlattenCollections;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.DelegatedPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.SampleOwnerIdentifierCollectionPredicate;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleOwnerIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;

/**
 * @author Tomasz Pylak
 */
@ShouldFlattenCollections(value = false)
public class ScreenerReadonlyPlatePredicate extends
        DelegatedPredicate<List<SampleOwnerIdentifier>, List<PlateIdentifier>>
{

    public ScreenerReadonlyPlatePredicate()
    {
        super(new SampleOwnerIdentifierCollectionPredicate(true));
    }

    @Override
    public List<SampleOwnerIdentifier> convert(List<PlateIdentifier> values)
    {
        ArrayList<SampleOwnerIdentifier> soIds = new ArrayList<SampleOwnerIdentifier>();
        for (PlateIdentifier value : values)
        {
            soIds.add(new SampleOwnerIdentifier(new SpaceIdentifier(DatabaseInstanceIdentifier
                    .createHome(), value.tryGetSpaceCode())));
        }
        return soIds;
    }

    @Override
    public String getCandidateDescription()
    {
        return "plate";
    }

}