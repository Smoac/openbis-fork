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

package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.common.exception.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.ShouldFlattenCollections;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewDataSetsWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * A {@link IPredicate} based on a {@link NewDataSetsWithTypes}.
 * 
 * @author Izabela Adamczyk
 */
@ShouldFlattenCollections(value = false)
public class NewDataSetsWithTypePredicate extends AbstractPredicate<NewDataSetsWithTypes>
{
    private final DataSetCodeCollectionPredicate dataSetCodeCollectionPredicate;

    public NewDataSetsWithTypePredicate()
    {
        dataSetCodeCollectionPredicate = new DataSetCodeCollectionPredicate();
    }

    @Override
    public String getCandidateDescription()
    {
        return "data set code";
    }

    @Override
    protected
    Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles,
            NewDataSetsWithTypes dataSets)
    {
        List<String> codes = new ArrayList<String>();
        for (NewDataSet ds : dataSets.getNewDataSets())
        {
            codes.add(ds.getCode());
        }
        return dataSetCodeCollectionPredicate.doEvaluation(person, allowedRoles, codes);

    }

    @Override
    public void init(IAuthorizationDataProvider provider)
    {
        dataSetCodeCollectionPredicate.init(provider);
    }
}
