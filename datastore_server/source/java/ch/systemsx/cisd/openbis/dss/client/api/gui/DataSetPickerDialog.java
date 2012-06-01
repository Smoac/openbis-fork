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

import javax.swing.JDialog;
import javax.swing.JFrame;
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

import ch.systemsx.cisd.openbis.dss.client.api.gui.tree.FilterableMutableTreeNode;
import ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;

/**
 * @author Pawel Glyzewski
 */
public class DataSetPickerDialog extends AbstractEntityPickerDialog implements
        TreeWillExpandListener
{
    private static final long serialVersionUID = 1L;

    private final JTree tree;

    private final JTextField filterField;

    private final JOptionPane optionPane;

    public DataSetPickerDialog(JFrame mainWindow, DataSetUploadClientModel clientModel)
    {
        super(mainWindow, "Pick a data set", clientModel);

        tree = new JTree();
        tree.addTreeWillExpandListener(this);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        filterField = createFilterField();

        JPanel northPanel = createFilterAndRefreshButtonPanel(filterField, refreshButton);
        optionPane = createOptionPane(northPanel, tree, this);

        addTreeSelectionListener();
        setDialogData();

        this.setContentPane(optionPane);
    }

    @Override
    protected void setDialogData()
    {
        FilterableMutableTreeNode top = new FilterableMutableTreeNode("Experiments");

        final List<String> projectIdentifiers = clientModel.getProjectIdentifiers();
        List<Experiment> experiments =
                clientModel.getOpenBISService().listExperimentsHavingDataSetsForProjects(
                        projectIdentifiers);
        createNodes(top, experiments);

        DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
        treeModel.setRoot(top);
        updateTreeSelection();
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

    private static JOptionPane createOptionPane(final JPanel northPanel, final JTree tree,
            final JDialog parent)
    {
        final JScrollPane scrollPane = new JScrollPane(tree);

        Object[] objects = new Object[]
            { "Filter experiments: ", northPanel, "Select data set:", scrollPane };
        final JOptionPane optionPane =
                new JOptionPane(objects, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        optionPane.addPropertyChangeListener(new PropertyChangeListener()
            {
                @Override
                public void propertyChange(PropertyChangeEvent evt)
                {
                    if (evt.getPropertyName().equals(JOptionPane.VALUE_PROPERTY)
                            && evt.getNewValue() != null)
                    {
                        if (((Integer) evt.getNewValue()).intValue() == JOptionPane.OK_OPTION
                                && tree.getSelectionPath() == null)
                        {
                            JOptionPane.showMessageDialog(parent, "Data set needs to be selected!",
                                    "No data set selected!", JOptionPane.WARNING_MESSAGE);
                            optionPane.setValue(optionPane.getInitialValue());
                        } else if (((Integer) evt.getNewValue()).intValue() == JOptionPane.OK_OPTION
                                && tree.getSelectionPath().getPath().length < 3)
                        {
                            JOptionPane.showMessageDialog(parent,
                                    "Data set should be selected, not experiment!",
                                    "No data set selected!", JOptionPane.WARNING_MESSAGE);
                            optionPane.setValue(optionPane.getInitialValue());
                        } else
                        {
                            parent.setVisible(false);
                        }
                    }
                }
            });

        return optionPane;
    }

    private static void createNodes(FilterableMutableTreeNode top, List<Experiment> experiments)
    {
        UploadClientSortingUtils.sortExperimentsByIdentifier(experiments);
        for (Experiment experiment : experiments)
        {
            DefaultMutableTreeNode category =
                    new DefaultMutableTreeNode(experiment.getIdentifier(), true);
            category.add(new DefaultMutableTreeNode("dummy child"));
            top.add(category);
        }
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

    private void updateTreeSelection()
    {
        DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
        FilterableMutableTreeNode rootNode = (FilterableMutableTreeNode) treeModel.getRoot();
        rootNode.filter(filterField.getText());
        treeModel.reload();
    }

    public String pickDataSet()
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
        if (isDataSetPicked(value))
        {
            return tree.getSelectionPath().getLastPathComponent().toString();
        } else
        {
            return null;
        }
    }

    private boolean isDataSetPicked(Object optionPaneValue)
    {
        if (optionPaneValue == null
                || ((Integer) optionPaneValue).intValue() == JOptionPane.CANCEL_OPTION)
        {
            return false;
        }
        if (tree.getSelectionPath() == null)
        {
            return false;
        }
        return true;
    }

    @Override
    public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException
    {
        final DefaultMutableTreeNode node =
                (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
        if (node.toString().equals("Data Sets"))
        {
            return;
        }
        node.removeAllChildren();

        if (node.getPath().length == 3) // get datasets for samples
        {
            List<DataSet> dataSets = listSampleDataSets(event);
            for (DataSet dataSet : dataSets)
            {
                node.add(new DefaultMutableTreeNode(dataSet.getCode()));
            }
        } else
        // get datasets and samples for experiment
        {
            // if top level, then finish
            if (((TreeNode) event.getPath().getLastPathComponent()).getParent() == null)
            {
                return;
            }

            List<Sample> samples = listExperimentSamples(event);

            List<DataSet> dataSets = listExperimentDataSets(event);

            if (dataSets.size() > 0)
            {
                DefaultMutableTreeNode dataSetsNode = new DefaultMutableTreeNode("Data Sets");
                node.add(dataSetsNode);
                for (DataSet dataSet : dataSets)
                {
                    dataSetsNode.add(new DefaultMutableTreeNode(dataSet.getCode()));
                }
            }

            for (Sample s : samples)
            {
                DefaultMutableTreeNode sampleNode = new DefaultMutableTreeNode(s.getIdentifier());
                sampleNode.add(new DefaultMutableTreeNode("dummy child"));
                node.add(sampleNode);
            }
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

    protected List<DataSet> listExperimentDataSets(TreeExpansionEvent event)
    {
        final List<String> experimentId =
                Collections.singletonList(event.getPath().getLastPathComponent().toString());
        List<DataSet> dataSets =
                clientModel.getOpenBISService().listDataSetsForExperiments(experimentId);
        UploadClientSortingUtils.sortDataSetsByCode(dataSets);
        return dataSets;
    }

    protected List<Sample> listExperimentSamples(TreeExpansionEvent event)
    {
        final List<String> experimentId =
                Collections.singletonList(event.getPath().getLastPathComponent().toString());
        List<Sample> samples =
                clientModel.getOpenBISService().listSamplesForExperiments(experimentId);
        UploadClientSortingUtils.sortSamplesByIdentifier(samples);
        return samples;
    }

    protected List<DataSet> listSampleDataSets(TreeExpansionEvent event)
    {
        final List<String> sampleId = Collections.singletonList(event.getPath()
                .getLastPathComponent().toString());
        List<DataSet> dataSets = clientModel.getOpenBISService().listDataSetsForSamples(sampleId);
        UploadClientSortingUtils.sortDataSetsByCode(dataSets);
        return dataSets;
    }

    @Override
    public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException
    {
        DefaultMutableTreeNode node =
                (DefaultMutableTreeNode) event.getPath().getLastPathComponent();

        node.removeAllChildren();
        node.add(new DefaultMutableTreeNode("dummy child"));
    }
}
