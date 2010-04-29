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
import ch.systemsx.cisd.openbis.generic.shared.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.ShouldFlattenCollections;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetAccessPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * A {@link IPredicate} based on a list of data set codes.
 * 
 * @author Franz-Josef Elmer
 */
@ShouldFlattenCollections(value = false)
public class DataSetCodeCollectionPredicate extends AbstractGroupPredicate<List<String>>
{
    @Override
    public String getCandidateDescription()
    {
        return "data set code";
    }

    @Override
    Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles,
            List<String> dataSetCodes)
    {
        assert initialized : "Predicate has not been initialized";

        List<DataSetAccessPE> accessData =
                authorizationDataProvider.tryGetDatasetCollectionAccessData(dataSetCodes);

        if (accessData == null)
        {
            return Status.OK;
        }

        if (accessData.size() < dataSetCodes.size())
        {
            return Status.createError("Could not get access data for all datasets");
        }

        for (DataSetAccessPE accessDatum : accessData)
        {
            String dbInstanceUUID = accessDatum.getDatabaseInstanceUuid();
            String dbInstanceCode = accessDatum.getDatabaseInstanceCode();
            String groupCode = accessDatum.getGroupCode();
            Status result =
                    evaluate(person, allowedRoles, dbInstanceUUID, dbInstanceCode, groupCode);
            if (result != Status.OK)
            {
                return result;
            }
        }

        return Status.OK;
    }
}
