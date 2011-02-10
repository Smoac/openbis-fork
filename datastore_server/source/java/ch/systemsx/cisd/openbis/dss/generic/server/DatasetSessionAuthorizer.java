/*
 * Copyright 2011 ETH Zuerich, CISD
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

import java.util.List;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.authorization.internal.IDssSessionAuthorizer;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * Implementation of {@link IDssSessionAuthorizer} that asks the openBIS application server to
 * check the data set codes.
 * 
 * @author Bernd Rinn
 */
public class DatasetSessionAuthorizer implements IDssSessionAuthorizer
{

    public Status checkDatasetAccess(String sessionToken, List<String> datasetCodes)
    {
        final IEncapsulatedOpenBISService openBISService = ServiceProvider.getOpenBISService();

        try
        {
            openBISService.checkDataSetCollectionAccess(sessionToken, datasetCodes);
            return Status.OK;
        } catch (UserFailureException ex)
        {
            return Status.createError(ex.getMessage());
        }
    }

    public Status checkDatasetAccess(String sessionToken, String datasetCode)
    {
        final IEncapsulatedOpenBISService openBISService = ServiceProvider.getOpenBISService();

        try
        {
            openBISService.checkDataSetAccess(sessionToken, datasetCode);
            return Status.OK;
        } catch (UserFailureException ex)
        {
            return Status.createError(ex.getMessage());
        }
    }

    public Status checkSpaceWriteable(String sessionToken, SpaceIdentifier spaceId)
    {
        final IEncapsulatedOpenBISService openBISService = ServiceProvider.getOpenBISService();

        try
        {
            openBISService.checkSpaceAccess(sessionToken, spaceId);
            return Status.OK;
        } catch (UserFailureException ex)
        {
            return Status.createError(ex.getMessage());
        }
    }

    public Status checkInstanceAdminAuthorization(String sessionToken)
    {
        final IEncapsulatedOpenBISService openBISService = ServiceProvider.getOpenBISService();

        try
        {
            openBISService.checkInstanceAdminAuthorization(sessionToken);
            return Status.OK;
        } catch (UserFailureException ex)
        {
            return Status.createError(ex.getMessage());
        }
    }

}
