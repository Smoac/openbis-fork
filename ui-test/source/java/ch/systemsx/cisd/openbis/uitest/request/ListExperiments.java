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

package ch.systemsx.cisd.openbis.uitest.request;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import ch.systemsx.cisd.openbis.uitest.rmi.Identifiers;
import ch.systemsx.cisd.openbis.uitest.type.Experiment;

/**
 * @author anttil
 */
public class ListExperiments implements Request<List<Experiment>>
{

    private Collection<String> experimentIds;

    public ListExperiments(Experiment... experiments)
    {
        experimentIds = new HashSet<String>();
        for (Experiment e : experiments)
        {
            experimentIds.add(Identifiers.get(e).toString());
        }
    }

    public ListExperiments(String... experimentIds)
    {
        this.experimentIds = Arrays.asList(experimentIds);
    }

    public Collection<String> getExperimentIds()
    {
        return experimentIds;
    }
}
