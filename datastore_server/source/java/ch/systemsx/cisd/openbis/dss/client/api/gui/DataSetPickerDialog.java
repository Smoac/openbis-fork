/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.client.api.gui;

import java.util.List;

import javax.swing.JFrame;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeNode;

import ch.systemsx.cisd.openbis.dss.client.api.gui.model.DataSetUploadClientModel;
import ch.systemsx.cisd.openbis.dss.client.api.gui.model.Identifier;
import ch.systemsx.cisd.openbis.dss.client.api.gui.model.SamplesDataSets;
import ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwnerType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;

/**
 * Dialog for browsing and picking a data set.
 * 
 * @author Pawel Glyzewski
 * @author Franz-Josef Elmer
 */
public class DataSetPickerDialog extends AbstractTreeEntityPickerDialog
{
    private static final long serialVersionUID = 1L;

    public DataSetPickerDialog(JFrame mainWindow, DataSetUploadClientModel clientModel)
    {
        super(mainWindow, "Pick a data set", DataSetOwnerType.DATA_SET, clientModel);
        setDialogData();
    }

    @Override
    public void treeWillExpand(final TreeExpansionEvent event) throws ExpandVetoException
    {
        final DefaultMutableTreeNode node =
                (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
        // if top level, then finish
        if (((TreeNode) node).getParent() == null)
        {
            return;
        }
        Object userObject = node.getUserObject();
        if (userObject instanceof Identifier == false)
        {
            return;
        }
        Identifier identifier = (Identifier) userObject;
        expandNode(node, identifier);
    }

    @Override
    protected void expandNode(final DefaultMutableTreeNode node, Identifier identifier)
    {
        if (identifier.getOwnerType() != DataSetOwnerType.DATA_SET)
        {
            clientModel.listSamplesDataSets(identifier,
                    new AsyncNodeAction<SamplesDataSets>(tree, node, scheduler)
                        {
                            @Override
                            public void handleData(SamplesDataSets samplesDataSets)
                            {
                                List<DataSet> dataSets = samplesDataSets.getDataSets();

                                node.removeAllChildren();
                                if (dataSets.size() > 0)
                                {
                                    DefaultMutableTreeNode dataSetsNode =
                                            new DefaultMutableTreeNode("Data Sets");
                                    node.add(dataSetsNode);
                                    for (DataSet dataSet : dataSets)
                                    {
                                        dataSetsNode.add(new DefaultMutableTreeNode(Identifier
                                                .create(dataSet)));
                                    }
                                }

                                for (Sample s : samplesDataSets.getSamples())
                                {
                                    DefaultMutableTreeNode sampleNode =
                                            new DefaultMutableTreeNode(Identifier.create(s));
                                    sampleNode.add(new DefaultMutableTreeNode(
                                            UiUtilities.WAITING_NODE_LABEL));
                                    node.add(sampleNode);
                                }
                            }
                        });
        }
    }

}
