/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.sample;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

public class ParentGeneratedFromSampleColDef extends AbstractParentSampleColDef
{
    private static final String IDENTIFIER = "generatedFromParent";

    private static final int MAX_PARENTS = 4;

    // GWT only
    public ParentGeneratedFromSampleColDef()
    {
        this(null);
    }

    public ParentGeneratedFromSampleColDef(String headerText)
    {
        super(headerText);
    }

    public String getIdentifier()
    {
        return IDENTIFIER;
    }

    @Override
    public Sample tryGetParent(Sample sample)
    {
        if (sample.getParents().size() == 1)
        {
            return sample.getGeneratedFrom();
        } else
        {
            return null;
        }
    }

    @Override
    protected String tryGetValue(Sample sample)
    {
        int parentsSize = sample.getParents().size();
        if (parentsSize == 0)
        {
            return null;
        } else if (parentsSize == 1)
        {
            return super.tryGetValue(sample);
        } else
        {
            StringBuilder sb = new StringBuilder();
            int counter = 0;
            for (Sample parent : sample.getParents())
            {
                if (counter == MAX_PARENTS)
                {
                    sb.append("... (").append(parentsSize - MAX_PARENTS).append(" more)");
                    break;
                }
                sb.append(getAsValue(parent)).append("\n");
                counter++;
            }
            return sb.toString();
        }
    }

}
