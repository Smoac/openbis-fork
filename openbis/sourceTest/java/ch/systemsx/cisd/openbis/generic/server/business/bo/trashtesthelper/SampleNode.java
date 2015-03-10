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

package ch.systemsx.cisd.openbis.generic.server.business.bo.trashtesthelper;

import java.util.ArrayList;
import java.util.List;

public final class SampleNode extends EntityNode
{
    ExperimentNode experiment;
    private SampleNode container;
    private final List<SampleNode> components = new ArrayList<SampleNode>();
    private final List<SampleNode> parents = new ArrayList<SampleNode>();
    private final List<SampleNode> children = new ArrayList<SampleNode>();
    private final List<DataSetNode> dataSets = new ArrayList<DataSetNode>();

    SampleNode(long id)
    {
        super("S", id);
    }
    
    ExperimentNode getExperiment()
    {
        return experiment;
    }

    SampleNode getContainer()
    {
        return container;
    }

    List<SampleNode> getComponents()
    {
        return components;
    }

    public void hasComponents(SampleNode... someComponentSamples)
    {
        for (SampleNode componentSample : someComponentSamples)
        {
            components.add(componentSample);
            componentSample.container = this;
        }
    }
    
    List<SampleNode> getParents()
    {
        return parents;
    }

    List<SampleNode> getChildren()
    {
        return children;
    }

    void hasChildren(SampleNode... someChildSamples)
    {
        for (SampleNode childSample : someChildSamples)
        {
            children.add(childSample);
            childSample.parents.add(this);
        }
    }
    
    List<DataSetNode> getDataSets()
    {
        return dataSets;
    }

    public void has(DataSetNode... someDataSets)
    {
        for (DataSetNode dataSet : someDataSets)
        {
            dataSet.sample = this;
            if (experiment != null)
            {
                experiment.has(dataSet);
            }
            dataSets.add(dataSet);
        }
    }
    
    @Override
    public String toString()
    {

        StringBuilder builder = new StringBuilder(getCode());
        Utils.appendTo(builder, "experiment", experiment);
        Utils.appendTo(builder, "children", children);
        Utils.appendTo(builder, "parents", parents);
        Utils.appendTo(builder, "container", container);
        Utils.appendTo(builder, "components", components);
        Utils.appendTo(builder, "data sets", dataSets);
        return builder.toString();
    }
}