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

package ch.systemsx.cisd.openbis.dss.generic.server;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.authorization.IAuthorizationGuardPredicate;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.DataSetFileDTO;

/**
 * Predicate for checking that the current user has access to a data set specified by a
 * DataSetFileDTO
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetFileDTOPredicate implements
        IAuthorizationGuardPredicate<IDssServiceRpcGenericInternal, DataSetFileDTO>
{
    public Status evaluate(IDssServiceRpcGenericInternal receiver, String sessionToken,
            DataSetFileDTO dataSetFile) throws UserFailureException
    {
        if (receiver.isDatasetAccessible(sessionToken, dataSetFile.getDataSetCode()))
        {
            return Status.OK;
        } else
        {
            return Status.createError("Data set (" + dataSetFile.getDataSetCode()
                    + ") does not exist.");
        }
    }

}
