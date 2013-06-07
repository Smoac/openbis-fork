/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.knime.file;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

import ch.systemsx.cisd.openbis.knime.common.IOpenbisServiceFacadeFactory;
import ch.systemsx.cisd.openbis.knime.common.OpenbisServiceFacadeFactory;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class DataSetRegistrationViaFlowNodeFactory extends NodeFactory<DataSetRegistrationViaFlowNodeModel>
{
    private final IOpenbisServiceFacadeFactory factory = new OpenbisServiceFacadeFactory();
    
    @Override
    protected NodeDialogPane createNodeDialogPane()
    {
        return new DataSetRegistrationNodeDialog(factory)
            {
                @Override
                protected boolean withFileVariable()
                {
                    return true;
                }
            };
    }

    @Override
    public DataSetRegistrationViaFlowNodeModel createNodeModel()
    {
        return new DataSetRegistrationViaFlowNodeModel(factory);
    }

    @Override
    protected int getNrNodeViews()
    {
        return 0;
    }

    @Override
    public NodeView<DataSetRegistrationViaFlowNodeModel> createNodeView(int viewIndex,
            DataSetRegistrationViaFlowNodeModel nodeModel)
    {
        return null;
    }

    @Override
    protected boolean hasDialog()
    {
        return true;
    }

}
