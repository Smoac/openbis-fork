/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.registrator.api.v1.impl;

import java.util.List;

import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization.IAuthorizationService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IDataSetImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IExperimentImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.ISampleImmutable;

/**
 * @author Jakub Straszewski
 */
public class AuthorizationService implements IAuthorizationService
{
    private IEncapsulatedOpenBISService openBisService;

    public AuthorizationService(IEncapsulatedOpenBISService openBisService)
    {
        this.openBisService = openBisService;
    }

    @Override
    public boolean doesUserHaveRole(String user, String role, String spaceOrNull)
    {
        return openBisService.doesUserHaveRole(user, role, spaceOrNull);
    }

    @Override
    public List<IDataSetImmutable> filterToVisibleDatasets(String user,
            List<IDataSetImmutable> datasets)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<IExperimentImmutable> filterToVisibleExperiments(String user,
            List<IExperimentImmutable> experiments)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ISampleImmutable> filterToVisibleSamples(String user, List<ISampleImmutable> samples)
    {
        // TODO Auto-generated method stub
        return null;
    }
}
