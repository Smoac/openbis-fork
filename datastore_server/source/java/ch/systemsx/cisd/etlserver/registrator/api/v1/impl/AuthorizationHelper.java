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

import ch.systemsx.cisd.common.action.IMapper;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;

/**
 * @author Jakub Straszewski
 */
public class AuthorizationHelper
{
    public static <T> List<T> filterToVisibleDatasets(IEncapsulatedOpenBISService openBisService,
            String user, List<T> datasets,
            IMapper<T, String> codeMapper)
    {
        // create a list of codes
        List<String> dataSetCodes = new LinkedList<String>();
        for (T dataSet : datasets)
        {
            dataSetCodes.add(codeMapper.map(dataSet));
        }

        // call service - to filter the codes
        List<String> filteredCodes = openBisService.filterToVisibleDataSets(user, dataSetCodes);
        // put filtered codes to the set
        Set<String> filteredSet = new HashSet<String>(filteredCodes);

        // filter original values to those returned by the service call
        List<T> resultList = new LinkedList<T>();
        for (T dataSet : datasets)
        {
            if (filteredSet.contains(codeMapper.map(dataSet)))
            {
                resultList.add(dataSet);
            }
        }
        return resultList;
    }

    public static <T> List<T> filterToVisibleExperiments(
            IEncapsulatedOpenBISService openBisService, String user, List<T> experiments,
            IMapper<T, String> identifierMapper)
    {
        // create a list of codes
        List<String> experimentIds = new LinkedList<String>();
        for (T exp : experiments)
        {
            experimentIds.add(identifierMapper.map(exp));
        }

        // call service - to filter the codes
        List<String> filteredCodes = openBisService.filterToVisibleExperiments(user, experimentIds);
        // put filtered codes to the set
        Set<String> filteredSet = new HashSet<String>(filteredCodes);

        // filter original values to those returned by the service call
        List<T> resultList = new LinkedList<T>();
        for (T exp : experiments)
        {
            if (filteredSet.contains(identifierMapper.map(exp)))
            {
                resultList.add(exp);
            }
        }
        return resultList;
    }

    public static <T> List<T> filterToVisibleSamples(IEncapsulatedOpenBISService openBisService,
            String user, List<T> samples, IMapper<T, String> identifierMapper)
    {
        // create a list of codes
        List<String> sampleIds = new LinkedList<String>();
        for (T sample : samples)
        {
            sampleIds.add(identifierMapper.map(sample));
        }

        // call service - to filter the codes
        List<String> filteredCodes = openBisService.filterToVisibleSamples(user, sampleIds);
        // put filtered codes to the set
        Set<String> filteredSet = new HashSet<String>(filteredCodes);

        // filter original values to those returned by the service call
        List<T> resultList = new LinkedList<T>();
        for (T sample : samples)
        {
            if (filteredSet.contains(identifierMapper.map(sample)))
            {
                resultList.add(sample);
            }
        }
        return resultList;
    }

}
