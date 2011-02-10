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

package ch.systemsx.cisd.openbis.dss.screening.shared.api.authorization.internal;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.authorization.internal.DssSessionAuthorizationHolder;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.authorization.internal.IAuthorizationGuardPredicate;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.v1.IDssServiceRpcScreening;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IDatasetIdentifier;

/**
 * Predicate that checks if the user has access to a collection of data set identifiers.
 * <p>
 * <i>This is an internal class. Do not use it as a user of the API.</i>
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DatasetIdentifierPredicate implements
        IAuthorizationGuardPredicate<IDssServiceRpcScreening, List<? extends IDatasetIdentifier>>
{

    static protected final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DatasetIdentifierPredicate.class);

    public Status evaluate(IDssServiceRpcScreening receiver, String sessionToken,
            List<? extends IDatasetIdentifier> datasetIdentifiers) throws UserFailureException
    {
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format(
                    "Check access to the data sets '%s' on openBIS server.", datasetIdentifiers));
        }

        return DssSessionAuthorizationHolder.getAuthorizer().checkDatasetAccess(
                sessionToken, getDatasetCodes(datasetIdentifiers));
    }

    private List<String> getDatasetCodes(List<? extends IDatasetIdentifier> datasetIdentifiers)
    {
        final List<String> result = new ArrayList<String>();
        for (IDatasetIdentifier id : datasetIdentifiers)
        {
            result.add(id.getDatasetCode());
        }
        return result;
    }

}
