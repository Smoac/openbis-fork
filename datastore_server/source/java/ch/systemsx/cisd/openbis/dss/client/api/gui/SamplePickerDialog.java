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

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
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

import ch.systemsx.cisd.openbis.dss.client.api.gui.tree.FilterableMutableTreeNode;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;

/**
 * @author Pawel Glyzewski
 */
public class SamplePickerDialog extends AbstractEntityPickerDialogWithServerConnection implements
        TreeWillExpandListener
{
    private static final long serialVersionUID = 1L;

    private final JTree tree;

    private final JTextField filterField;

    private final JOptionPane optionPane;

    /**
     * @param mainWindow
     * @param experiments
     * @param openbisService
     */
    public SamplePickerDialog(JFrame mainWindow, List<Experiment> experiments,
            final IOpenbisServiceFacade openbisService)
    {
        super(mainWindow, "Pick a sample", openbisService);

        FilterableMutableTreeNode top = new FilterableMutableTreeNode("Experiments");
        createNodes(top, experiments);
        tree = new JTree(top);
        tree.addTreeWillExpandListener(this);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        filterField = createFilterField(top, tree);

        optionPane = createOptionPane(tree, filterField, this);

        addTreeSelectionListener();

        this.setContentPane(optionPane);
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

    private static JOptionPane createOptionPane(final JTree tree, final JTextField filterField,
            final SamplePickerDialog parent)
    {
        final JScrollPane scrollPane = new JScrollPane(tree);

        Object[] objects = new Object[]
            { "Filter experiments: ", filterField, "Select Sample:", scrollPane };
        final JOptionPane theOptionPane =
                new JOptionPane(objects, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);

        theOptionPane.addPropertyChangeListener(new PropertyChangeListener()
            {
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
                        parent.acceptCurrentSelectionOrNotify();
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
        if (tree.getSelectionPath() == null)
        {
            JOptionPane.showMessageDialog(this, "Sample needs to be selected!",
                    "No sample selected!", JOptionPane.WARNING_MESSAGE);
            optionPane.setValue(optionPane.getInitialValue());
        } else if (tree.getSelectionPath().getPath().length < 3)
        {
            JOptionPane.showMessageDialog(this, "Sample should be selected, not experiment!",
                    "No sample selected!", JOptionPane.WARNING_MESSAGE);
            optionPane.setValue(optionPane.getInitialValue());
        } else
        {
            this.setVisible(false);
        }
    }

    private static void createNodes(FilterableMutableTreeNode top, List<Experiment> experiments)
    {
        for (Experiment experiment : experiments)
        {
            DefaultMutableTreeNode category =
                    new DefaultMutableTreeNode(experiment.getIdentifier(), true);
            category.add(new DefaultMutableTreeNode("dummy child"));
            top.add(category);
        }
    }

    private static JTextField createFilterField(final FilterableMutableTreeNode treeNode,
            final JTree tree)
    {
        final JTextField filterField = new JTextField();
        filterField.setEditable(true);
        filterField.getDocument().addDocumentListener(new DocumentListener()
            {
                public void removeUpdate(DocumentEvent e)
                {
                    treeNode.filter(filterField.getText());
                    ((DefaultTreeModel) tree.getModel()).reload();
                }

                public void insertUpdate(DocumentEvent e)
                {
                    treeNode.filter(filterField.getText());
                    ((DefaultTreeModel) tree.getModel()).reload();
                }

                public void changedUpdate(DocumentEvent e)
                {
                    treeNode.filter(filterField.getText());
                    ((DefaultTreeModel) tree.getModel()).reload();
                }
            });

        return filterField;
    }

    public String pickSample()
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
        optionPane.setValue(optionPane.getInitialValue());
        if (value == null || ((Integer) value).intValue() == JOptionPane.CANCEL_OPTION)
        {
            return null;
        } else
        {
            return tree.getSelectionPath().getLastPathComponent().toString();
        }
    }

    public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException
    {
        // if top level, then finish
        if (((TreeNode) event.getPath().getLastPathComponent()).getParent() == null)
        {
            return;
        }

        List<Sample> samples =
                openbisService.listSamplesForExperiments(Collections.singletonList(event.getPath()
                        .getLastPathComponent().toString()));

        final DefaultMutableTreeNode node =
                (DefaultMutableTreeNode) event.getPath().getLastPathComponent();

        node.removeAllChildren();
        for (Sample s : samples)
        {
            node.add(new DefaultMutableTreeNode(s.getIdentifier()));
        }

        if (node.getChildCount() == 0)
        {
            scheduler.schedule(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        tree.collapsePath(new TreePath(node.getPath()));
                    }
                }, 1500l);
        }
    }

    public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException
    {
        DefaultMutableTreeNode node =
                (DefaultMutableTreeNode) event.getPath().getLastPathComponent();

        node.removeAllChildren();
        node.add(new DefaultMutableTreeNode("dummy child"));
    }
}
