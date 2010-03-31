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

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.rpc.shared.IDataSetDss;
import ch.systemsx.cisd.openbis.dss.rpc.shared.IDssServiceRpcV1;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class DssServiceRpcV1 implements IDssServiceRpcV1
{
    // private static final Logger notificationLog =
    // LogFactory.getLogger(LogCategory.NOTIFY, DssServiceRpcV1.class);

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, DssServiceRpcV1.class);

    private final IEncapsulatedOpenBISService openBISService;

    public DssServiceRpcV1(IEncapsulatedOpenBISService openBISService)
    {
        this.openBISService = openBISService;
        operationLog.info("Started RPC V1 service.");
    }

    public IDataSetDss tryDataSet(String sessionToken, String code)
    {
        if (isDatasetAccessible(sessionToken, code) == false)
            return null;
        return null;
    }

    public int getMinClientVersion()
    {
        return 1;
    }

    public int getVersion()
    {
        return 1;
    }

    private boolean isDatasetAccessible(String sessionIdOrNull, String dataSetCode)
    {
        boolean access;
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("Check access to the data set '%s' at openBIS server.",
                    dataSetCode));
        }

        try
        {
            openBISService.checkDataSetAccess(sessionIdOrNull, dataSetCode);
            access = true;
        } catch (UserFailureException ex)
        {
            access = false;
        }

        return access;
    }

}
