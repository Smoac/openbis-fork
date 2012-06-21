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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
        // create a list of codes
        List<String> dataSetCodes = new LinkedList<String>();
        for (IDataSetImmutable dataSet : datasets)
        {
            dataSetCodes.add(dataSet.getDataSetCode());
        }

        // call service - to filter the codes
        List<String> filteredCodes = openBisService.filterToVisibleDataSets(user, dataSetCodes);
        // put filtered codes to the set
        Set<String> filteredSet = new HashSet<String>(filteredCodes);

        // filter original values to those returned by the service call
        List<IDataSetImmutable> resultList = new LinkedList<IDataSetImmutable>();
        for (IDataSetImmutable dataSet : datasets)
        {
            if (filteredSet.contains(dataSet.getDataSetCode()))
            {
                resultList.add(dataSet);
            }
        }
        return resultList;
    }

    @Override
    public List<IExperimentImmutable> filterToVisibleExperiments(String user,
            List<IExperimentImmutable> experiments)
    {
        // create a list of codes
        List<String> experimentIds = new LinkedList<String>();
        for (IExperimentImmutable exp : experiments)
        {
            experimentIds.add(exp.getExperimentIdentifier());
        }

        // call service - to filter the codes
        List<String> filteredCodes = openBisService.filterToVisibleExperiments(user, experimentIds);
        // put filtered codes to the set
        Set<String> filteredSet = new HashSet<String>(filteredCodes);

        // filter original values to those returned by the service call
        List<IExperimentImmutable> resultList = new LinkedList<IExperimentImmutable>();
        for (IExperimentImmutable exp : experiments)
        {
            if (filteredSet.contains(exp.getExperimentIdentifier()))
            {
                resultList.add(exp);
            }
        }
        return resultList;
    }

    @Override
    public List<ISampleImmutable> filterToVisibleSamples(String user, List<ISampleImmutable> samples)
    {
        // create a list of codes
        List<String> sampleIds = new LinkedList<String>();
        for (ISampleImmutable sample : samples)
        {
            sampleIds.add(sample.getSampleIdentifier());
        }

        // call service - to filter the codes
        List<String> filteredCodes = openBisService.filterToVisibleSamples(user, sampleIds);
        // put filtered codes to the set
        Set<String> filteredSet = new HashSet<String>(filteredCodes);

        // filter original values to those returned by the service call
        List<ISampleImmutable> resultList = new LinkedList<ISampleImmutable>();
        for (ISampleImmutable sample : samples)
        {
            if (filteredSet.contains(sample.getSampleIdentifier()))
            {
                resultList.add(sample);
            }
        }
        return resultList;
    }
}
