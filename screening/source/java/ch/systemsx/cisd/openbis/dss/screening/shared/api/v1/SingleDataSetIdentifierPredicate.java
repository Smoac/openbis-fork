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

package ch.systemsx.cisd.openbis.dss.screening.shared.api.v1;

import java.util.Collections;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.authorization.IAuthorizationGuardPredicate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IDatasetIdentifier;

/**
 * A predicate for testing a single data set identifier.
 *
 * @author Franz-Josef Elmer
 */
public class SingleDataSetIdentifierPredicate implements
        IAuthorizationGuardPredicate<IDssServiceRpcScreening, IDatasetIdentifier>

{
    private static final DatasetIdentifierPredicate PREDICATE = new DatasetIdentifierPredicate();

    public Status evaluate(IDssServiceRpcScreening receiver, String sessionToken,
            IDatasetIdentifier datasetIdentifier) throws UserFailureException
    {
        return PREDICATE.evaluate(receiver, sessionToken,
                Collections.singletonList(datasetIdentifier));
    }

}
