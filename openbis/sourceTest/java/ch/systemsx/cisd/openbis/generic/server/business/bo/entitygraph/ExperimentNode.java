/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph;

import java.util.ArrayList;
import java.util.List;

public final class ExperimentNode extends EntityNode
{
    private final List<SampleNode> samples = new ArrayList<SampleNode>();

    private final List<DataSetNode> dataSets = new ArrayList<DataSetNode>();

    ExperimentNode(long id)
    {
        super("E", id);
    }
    
    public List<SampleNode> getSamples()
    {
        return samples;
    }
    
    public List<DataSetNode> getDataSets()
    {
        return dataSets;
    }
    
    void has(SampleNode... someSamples)
    {
        for (SampleNode sample : someSamples)
        {
            sample.experiment = this;
            samples.add(sample);
        }
    }

    void has(DataSetNode... someDataSets)
    {
        for (DataSetNode dataSet : someDataSets)
        {
            dataSet.experiment = this;
            dataSets.add(dataSet);
        }
    }
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(super.toString());
        Utils.appendTo(builder, "samples", samples);
        Utils.appendTo(builder, "data sets", dataSets);
        return builder.toString();
    }
}