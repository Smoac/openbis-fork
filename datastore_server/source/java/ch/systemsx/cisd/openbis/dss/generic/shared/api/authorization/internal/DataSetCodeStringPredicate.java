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

package ch.systemsx.cisd.openbis.dss.generic.shared.api.authorization.internal;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * Predicate for checking that the current user has access to a data set specified by code.
 * <p>
 * <i>This is an internal class. Do not use it as a user of the API.</i>
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetCodeStringPredicate implements
        IAuthorizationGuardPredicate<IDssServiceRpcGenericInternal, String>
{
    static protected final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DataSetCodeStringPredicate.class);

    public Status evaluate(IDssServiceRpcGenericInternal receiver, String sessionToken,
            String datasetCode) throws UserFailureException
    {
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("Check access to the data set '%s' on openBIS server.",
                    datasetCode));
        }

        return DssSessionAuthorizationHolder.getAuthorizer().checkDatasetAccess(sessionToken,
                datasetCode);
    }

}
