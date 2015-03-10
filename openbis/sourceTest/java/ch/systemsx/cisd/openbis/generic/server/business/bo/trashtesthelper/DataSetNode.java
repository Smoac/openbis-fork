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


public final class DataSetNode extends EntityNode
{
    private boolean deletable = true;
    
    ExperimentNode experiment;

    SampleNode sample;

    private final List<DataSetNode> components = new ArrayList<DataSetNode>();

    private final List<DataSetNode> containers = new ArrayList<DataSetNode>();

    private final List<DataSetNode> children = new ArrayList<DataSetNode>();

    private final List<DataSetNode> parents = new ArrayList<DataSetNode>();

    DataSetNode(long id)
    {
        super("DS", id);
    }
    
    public boolean isDeletable()
    {
        return deletable;
    }

    public DataSetNode nonDeletable()
    {
        deletable = false;
        return this;
    }
    
    ExperimentNode getExperiment()
    {
        return experiment;
    }
    
    SampleNode getSample()
    {
        return sample;
    }

    public void hasComponents(DataSetNode... someComponentDataSets)
    {
        for (DataSetNode componentDataSet : someComponentDataSets)
        {
            components.add(componentDataSet);
            componentDataSet.containers.add(this);
        }
    }

    public void hasChildren(DataSetNode... someChildDataSets)
    {
        for (DataSetNode childDataSet : someChildDataSets)
        {
            children.add(childDataSet);
            childDataSet.parents.add(this);
        }
    }
    
    List<DataSetNode> getChildren()
    {
        return children;
    }

    List<DataSetNode> getParents()
    {
        return parents;
    }
    
    List<DataSetNode> getComponents()
    {
        return components;
    }
    
    List<DataSetNode> getContainers()
    {
        return containers;
    }
    
    
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(getCode());
        if (deletable == false)
        {
            builder.append(" (non deletable)");
        }
        Utils.appendTo(builder, "experiment", experiment);
        Utils.appendTo(builder, "sample", sample);
        Utils.appendTo(builder, "children", children);
        Utils.appendTo(builder, "parents", parents);
        Utils.appendTo(builder, "components", components);
        Utils.appendTo(builder, "containers", containers);
        return builder.toString();
    }
}