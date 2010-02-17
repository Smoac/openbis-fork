/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores information describing an expression with parameters (e.g. custom filter).
 * 
 * @author Izabela Adamczyk
 */
public class GridCustomFilter extends AbstractExpression
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private List<String> allParameters;

    // need to use list here because set doesn't provide fixed order
    private List<String> parameters;

    public GridCustomFilter()
    {
    }

    public List<String> getParameters()
    {
        return parameters;
    }

    private void setParameters(List<String> parameters)
    {
        this.parameters = parameters;

    }

    public List<String> getAllParameters()
    {
        return allParameters;
    }

    private void setAllParameters(List<String> allParameters)
    {
        this.allParameters = allParameters;
    }

    public void setupParameters(List<String> allParameters)
    {
        setAllParameters(allParameters);
        setParameters(createDistinctParametersList(allParameters));
    }

    private static List<String> createDistinctParametersList(List<String> allParameters)
    {
        List<String> result = new ArrayList<String>();
        for (String parameter : allParameters)
        {
            if (result.contains(parameter) == false)
            {
                result.add(parameter);
            }
        }
        return result;
    }
}
