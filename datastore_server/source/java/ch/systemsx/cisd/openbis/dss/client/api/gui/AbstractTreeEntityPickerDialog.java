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

package ch.systemsx.cisd.openbis.dss.client.api.gui;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import ch.systemsx.cisd.openbis.dss.client.api.gui.model.DataSetUploadClientModel;
import ch.systemsx.cisd.openbis.dss.client.api.gui.model.Identifier;
import ch.systemsx.cisd.openbis.dss.client.api.gui.model.UploadClientSortingUtils;
import ch.systemsx.cisd.openbis.dss.client.api.gui.tree.FilterableMutableTreeNode;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwnerType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;

/**
 * Abstract super class of all entity picker dialogs based on {@link JTree}.
 * 
 * @author Franz-Josef Elmer
 */
public abstract class AbstractTreeEntityPickerDialog extends AbstractEntityPickerDialog implements
        TreeWillExpandListener
{
    private static final long serialVersionUID = 1L;

    protected final JTree tree;

    protected final JOptionPane optionPane;

    private final JTextField filterField;

    private final JTextField identifierField;
    
    private final DataSetOwnerType entityKind;

    public AbstractTreeEntityPickerDialog(JFrame mainWindow, String title, DataSetOwnerType entityKind,
            DataSetUploadClientModel clientModel)
    {
        super(mainWindow, title, clientModel);
        this.entityKind = entityKind;
        tree = new JTree();
        tree.setModel(new DefaultTreeModel(null));
        tree.addTreeWillExpandListener(this);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        addTreeSelectionListener();
        filterField = createFilterField();
        identifierField = createIdentifierField();
        JPanel northPanel = createFilterAndRefreshButtonPanel(filterField, refreshButton);
        optionPane = createOptionPane(northPanel, this);
        this.setContentPane(optionPane);
    }

    private JOptionPane createOptionPane(final JPanel northPanel,
            final JDialog parent)
    {
        final JScrollPane scrollPane = new JScrollPane(tree);
        
        List<Object> objectsAsList = new ArrayList<Object>();
        objectsAsList.add("Filter experiments: ");
        objectsAsList.add(northPanel);
        objectsAsList.add("Select " + entityKind.toString().toLowerCase() + ":");
        
        objectsAsList.add(scrollPane);
        
        if(entityKind == DataSetOwnerType.SAMPLE) {
            objectsAsList.add("Any " + entityKind.toString().toLowerCase() + " identifier:");
            objectsAsList.add(identifierField);
        }
        
        final JOptionPane theOptionPane =
                new JOptionPane(objectsAsList.toArray(), JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        
        theOptionPane.addPropertyChangeListener(new PropertyChangeListener()
            {
                @Override
                public void propertyChange(PropertyChangeEvent evt)
                {
                    if (false == evt.getPropertyName().equals(JOptionPane.VALUE_PROPERTY)
                            || evt.getNewValue() == null)
                    {
                        return;
                    }

                    if (isCancelClicked(evt))
                    {
                        parent.setVisible(false);
                        return;
                    }

                    if (isOkClicked(evt))
                    {
                        acceptCurrentSelectionOrNotify();
                    }

                }

                private boolean isOkClicked(PropertyChangeEvent evt)
                {
                    return ((Integer) evt.getNewValue()).intValue() == JOptionPane.OK_OPTION;
                }

                private boolean isCancelClicked(PropertyChangeEvent evt)
                {
                    return false == isOkClicked(evt);
                }
            });

        return theOptionPane;
    }

    private void acceptCurrentSelectionOrNotify()
    {
        String selected = tryGetSelected();
        if (selected != null)
        {
            if(     entityKind != DataSetOwnerType.SAMPLE ||
                    entityKind == DataSetOwnerType.SAMPLE && clientModel.sampleExists(selected)) {
                this.setVisible(false);
                return;
            }
        }
        String label = entityKind.toString();
        JOptionPane.showMessageDialog(this, label + " needs to be selected!",
                "No " + label.toLowerCase() + " selected!", JOptionPane.WARNING_MESSAGE);
        optionPane.setValue(optionPane.getInitialValue());
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
        if (userObject instanceof Identifier)
        {
            expandNode(node, (Identifier) userObject);
        }
    }

    protected abstract void expandNode(final DefaultMutableTreeNode node, Identifier identifier);
    
    @Override
    public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException
    {
        DefaultMutableTreeNode node =
                (DefaultMutableTreeNode) event.getPath().getLastPathComponent();

        if (node.isRoot())
        {
            throw new ExpandVetoException(event);
        }
        node.removeAllChildren();
        node.add(new DefaultMutableTreeNode(UiUtilities.WAITING_NODE_LABEL));
    }

    protected void createNodes(FilterableMutableTreeNode top, List<Experiment> experiments)
    {
        UploadClientSortingUtils.sortExperimentsByIdentifier(experiments);
        for (Experiment experiment : experiments)
        {
            DefaultMutableTreeNode category =
                    new DefaultMutableTreeNode(Identifier.create(experiment), true);
            category.add(new DefaultMutableTreeNode(UiUtilities.WAITING_NODE_LABEL));
            top.add(category);
        }
    }

    @Override
    protected void setDialogData()
    {
        FilterableMutableTreeNode top = new FilterableMutableTreeNode("Experiments");

        final List<String> projectIdentifiers = clientModel.getProjectIdentifiers();
        List<Experiment> experiments =
                clientModel.getOpenBISService().listExperimentsHavingSamplesForProjects(
                        projectIdentifiers);
        createNodes(top, experiments);

        DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
        treeModel.setRoot(top);
        updateTreeSelection();
    }

    protected void updateTreeSelection()
    {
        DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
        FilterableMutableTreeNode rootNode = (FilterableMutableTreeNode) treeModel.getRoot();
        if (rootNode != null)
        {
            rootNode.filter(filterField.getText());
        }
        treeModel.reload();
    }

    public String pickEntity()
    {
        this.pack();

        int height = this.getHeight() > 500 ? 500 : this.getHeight();
        int width = this.getWidth() > 600 ? 600 : this.getWidth();
        this.setSize(width, height);

        Point mwLocation = mainWindow.getLocationOnScreen();
        int x = mwLocation.x + (mainWindow.getWidth() / 2) - (this.getWidth() / 2);
        int y = mwLocation.y + (mainWindow.getHeight() / 2) - (this.getHeight() / 2);

        this.setLocation(x > 0 ? x : 0, y > 0 ? y : 0);

        this.setVisible(true);

        Object value = optionPane.getValue();
        Object initialValue = optionPane.getInitialValue();
        optionPane.setValue(initialValue);
        if (value == null || ((Integer) value).intValue() == JOptionPane.CANCEL_OPTION)
        {
            return null;
        } else
        {
            return tryGetSelected();
        }
    }

    private String tryGetSelected()
    {
        String anyIdentifier = this.identifierField.getText();
        if (!anyIdentifier.isEmpty())
        {
            return anyIdentifier;
        }
        
        TreePath treePath = tree.getSelectionPath();
        if (treePath == null)
        {
            return null;
        }
        Object lastPathComponent = ((DefaultMutableTreeNode) treePath.getLastPathComponent()).getUserObject();
        if (lastPathComponent instanceof Identifier == false)
        {
            return null;
        }
        Identifier identifier = (Identifier) lastPathComponent;
        return identifier.getOwnerType() == entityKind ? identifier.toString() : null;
    }

    private JTextField createIdentifierField() {
        final JTextField textField = new JTextField();
        textField.setEditable(true);
        return textField;
    }
    
    private JTextField createFilterField()
    {
        final JTextField textField = new JTextField();
        textField.setEditable(true);
        textField.getDocument().addDocumentListener(new DocumentListener()
            {
                @Override
                public void removeUpdate(DocumentEvent e)
                {
                    updateTreeSelection();
                }

                @Override
                public void insertUpdate(DocumentEvent e)
                {
                    updateTreeSelection();
                }

                @Override
                public void changedUpdate(DocumentEvent e)
                {
                    updateTreeSelection();
                }

            });

        return textField;
    }

    /**
     * Treat double click and return the same as clicking the ok button.
     */
    private void addTreeSelectionListener()
    {
        tree.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mousePressed(MouseEvent e)
                {
                    if (e.getClickCount() > 1)
                    {
                        optionPane.setValue(JOptionPane.OK_OPTION);
                    }
                }
            });
    }

}
