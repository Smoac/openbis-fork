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

package ch.systemsx.cisd.openbis.knime.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwnerType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;

/**
 * Helper class showing a dialog for choosing a data set owner. 
 *
 * @author Franz-Josef Elmer
 */
public class EntityChooser
{
    private static final class DecoratingTreeCellRenderer implements TreeCellRenderer
    {
        private static final class DecoratedIcon implements Icon
        {
            private final Icon icon;
            private final String text;
            
            DecoratedIcon(Icon icon, ChooserTreeNodeType type)
            {
                this.icon = icon;
                this.text = type.getLabel();
            }
            
            @Override
            public int getIconHeight()
            {
                return icon.getIconHeight();
            }
            
            @Override
            public int getIconWidth()
            {
                return icon.getIconWidth();
            }
            
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y)
            {
                icon.paintIcon(c, g, x, y);
                Color color = g.getColor();
                Font font = g.getFont();
                g.setColor(Color.BLACK);
                g.setFont(new Font("SansSerif", Font.BOLD, 10));
                FontMetrics fontMetrics = g.getFontMetrics();
                int stringWidth = fontMetrics.stringWidth(text);
                g.drawString(text, x + getIconWidth() - stringWidth, y + getIconHeight());
                g.setColor(color);
                g.setFont(font);
            }
        }
        
        private final TreeCellRenderer cellRenderer;

        private DecoratingTreeCellRenderer(TreeCellRenderer cellRenderer)
        {
            this.cellRenderer = cellRenderer;
        }

        @Override
        public Component getTreeCellRendererComponent(JTree t, Object value,
                boolean selected, boolean expanded,
                boolean leaf, int row, boolean hasFocus)
        {
            Component component =
                    cellRenderer.getTreeCellRendererComponent(t, value, selected, expanded, leaf,
                            row, hasFocus);
            Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
            if (component instanceof JLabel && userObject instanceof IChooserTreeNode)
            {
                JLabel label = (JLabel) component;
                Icon icon = label.getIcon();
                ChooserTreeNodeType nodeType = ((IChooserTreeNode<?>) userObject).getNodeType();
                label.setIcon(new DecoratedIcon(icon, nodeType));
            }
            return component;
        }
    }

    
    private final DataSetOwnerType entityType;
    private final boolean ownerEntity;
    private final IGeneralInformationService service;
    private final Component component;
    private final String sessionToken;
    
    private String entityIdentifier;

    public EntityChooser(Component component, DataSetOwnerType entityType, boolean ownerEntity, String sessionToken,
            IGeneralInformationService service)
    {
        this.component = component;
        this.entityType = entityType;
        this.ownerEntity = ownerEntity;
        this.sessionToken = sessionToken;
        this.service = service;
    }
    
    public String getOwnerOrNull()
    {
        final JTree tree = new JTree();
        final ChooserTreeModel chooserTreeModel = new ChooserTreeModel(entityType, ownerEntity, sessionToken, service);
        tree.setModel(chooserTreeModel);
        tree.setRootVisible(false);
        tree.setEditable(false);
        tree.setShowsRootHandles(true);
        tree.setCellRenderer(new DecoratingTreeCellRenderer(tree.getCellRenderer()));
        tree.addTreeExpansionListener(createTreeExpansionListener(tree, chooserTreeModel));
        JPanel treePanel = new JPanel(new BorderLayout());
        treePanel.add(new JScrollPane(tree), BorderLayout.CENTER);
        final JOptionPane optionPane = new JOptionPane(treePanel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        optionPane.setSize(600, 800);
        tree.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mousePressed(MouseEvent e)
                {
                    if (e.getClickCount() > 1)
                    {
                        TreePath closestPathForLocation =
                                tree.getClosestPathForLocation(e.getX(), e.getY());
                        if (chooserTreeModel.isSelectable(closestPathForLocation))
                        {
                            Window window = SwingUtilities.getWindowAncestor(tree);
                            if (window != null)
                            {
                                optionPane.setValue(JOptionPane.OK_OPTION);
                                window.setVisible(false);
                            }
                        }
                    }
                }
            });
        String title = StringUtils.capitalize(entityType.toString().toLowerCase()) + " Chooser";
        final JDialog dialog = optionPane.createDialog(component, title);
        optionPane.addPropertyChangeListener(new PropertyChangeListener()
            {
                @Override
                public void propertyChange(PropertyChangeEvent evt)
                {
                    if (evt.getSource() == optionPane 
                            && JOptionPane.VALUE_PROPERTY.equals(evt.getPropertyName()) 
                            && new Integer(JOptionPane.OK_OPTION).equals(evt.getNewValue()))
                    {
                        TreePath selectionPath = tree.getSelectionPath();
                        if (selectionPath != null && chooserTreeModel.isSelectable(selectionPath) == false)
                        {
                            dialog.setVisible(true);
                        }
                    }
                }
            });
        dialog.setVisible(true);
        Object value = optionPane.getValue();
        if (new Integer(JOptionPane.OK_OPTION).equals(value))
        {
            setEntityIdentifier(tree);
        }
        return entityIdentifier;
    }

    private TreeExpansionListener createTreeExpansionListener(final JTree tree,
            final ChooserTreeModel chooserTreeModel)
    {
        return new TreeExpansionListener()
            {
                @Override
                public void treeExpanded(TreeExpansionEvent event)
                {
                    chooserTreeModel.expandNode(event.getPath(), new IAsyncNodeAction()
                        {
                            @Override
                            public void handleException(Throwable throwable)
                            {
                                throwable.printStackTrace();
                                JOptionPane.showMessageDialog(tree, throwable.toString());
                            }
                            
                            @Override
                            public void execute(Runnable runnable)
                            {
                                EventQueue.invokeLater(runnable);
                            }
                        });
                }
                
                @Override
                public void treeCollapsed(TreeExpansionEvent event)
                {
                    chooserTreeModel.collapsNode(event.getPath());
                }
            };
    }
    
    private void setEntityIdentifier(JTree tree)
    {
        TreePath selectionPath = tree.getSelectionPath();
        if (selectionPath == null)
        {
            return;
        }
        Object selectedItem =
                ((DefaultMutableTreeNode) selectionPath.getLastPathComponent()).getUserObject();
        if (selectedItem instanceof IChooserTreeNode == false)
        {
            return;
        }
        entityIdentifier = selectedItem.toString();
        Object nodeObject = ((IChooserTreeNode<?>) selectedItem).getNodeObject();
        if (nodeObject instanceof Experiment)
        {
            entityIdentifier = ((Experiment) nodeObject).getIdentifier();
        }
    }
    
}
