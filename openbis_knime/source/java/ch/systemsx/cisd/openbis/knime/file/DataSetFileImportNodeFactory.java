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

package ch.systemsx.cisd.openbis.knime.file;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * Factory for {@link DataSetFileImportNodeDialog} and {@link DataSetFileImportNodeModel}.
 * 
 * @author Franz-Josef Elmer
 */
public class DataSetFileImportNodeFactory extends NodeFactory<DataSetFileImportNodeModel>
{
    private final IDataSetProvider dataSetProvider = new DataSetProvider();

    @Override
    protected NodeDialogPane createNodeDialogPane()
    {
        return new DataSetFileImportNodeDialog(dataSetProvider);
    }

    @Override
    public DataSetFileImportNodeModel createNodeModel()
    {
        return new DataSetFileImportNodeModel(dataSetProvider);
    }

    @Override
    public NodeView<DataSetFileImportNodeModel> createNodeView(int viewIndex,
            DataSetFileImportNodeModel model)
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
