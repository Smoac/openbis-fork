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

package ch.systemsx.cisd.openbis.dss.screening.server;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.authorization.IAuthorizationGuardPredicate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IDatasetIdentifier;

/**
 * Predicate that checks if the user has access to a collection of data set identifiers.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DatasetIdentifierPredicate
        implements
        IAuthorizationGuardPredicate<IDssServiceRpcScreeningInternal, List<? extends IDatasetIdentifier>>
{

    public Status evaluate(IDssServiceRpcScreeningInternal receiver, String sessionToken,
            List<? extends IDatasetIdentifier> featureDatasets) throws UserFailureException
    {
        try
        {
            receiver.checkDatasetsAuthorizationForIDatasetIdentifier(sessionToken, featureDatasets);
        } catch (IllegalArgumentException e)
        {
            return Status.createError(e.getMessage());
        }
        return Status.OK;
    }

}
