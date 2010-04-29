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
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetAccessPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * A {@link IPredicate} based on a list of data set codes.
 * 
 * @author Franz-Josef Elmer
 */
public class DataSetCodePredicate extends AbstractGroupPredicate<String>
{
    @Override
    public String getCandidateDescription()
    {
        return "data set code";
    }

    @Override
    Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles, String dataSetCode)
    {
        assert initialized : "Predicate has not been initialized";

        DataSetAccessPE accessData = authorizationDataProvider.tryGetDatasetAccessData(dataSetCode);

        if (accessData != null)
        {
            String dbInstanceUUID = accessData.getDatabaseInstanceUuid();
            String dbInstanceCode = accessData.getDatabaseInstanceCode();
            String groupCode = accessData.getGroupCode();
            Status result =
                    evaluate(person, allowedRoles, dbInstanceUUID, dbInstanceCode, groupCode);
            return result;
        }
        return Status.OK;
    }
}
