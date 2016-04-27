/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.knime.query;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * @author Franz-Josef Elmer
 */
public class QueryNodeFactory extends NodeFactory<QueryNodeModel>
{

    @Override
    protected NodeDialogPane createNodeDialogPane()
    {
        return new QueryNodeDialog();
    }

    @Override
    public QueryNodeModel createNodeModel()
    {
        return new QueryNodeModel();
    }

    @Override
    public NodeView<QueryNodeModel> createNodeView(int viewIndex, QueryNodeModel model)
    {
        return null;
    }

    @Override
    protected int getNrNodeViews()
    {
        return 0;
    }

    @Override
    protected boolean hasDialog()
    {
        return true;
    }

}
