/*
 * Copyright 2012 ETH Zuerich, CISD
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;

/**
 * Chooser for files in a data set.
 *
 * @author Franz-Josef Elmer
 */
public class FileChooser extends JPanel
{
    private static final class FileNode implements TreeNode
    {
        private final FileInfoDssDTO fileInfo;
        private final String fileName;
        private final List<FileNode> children;
        
        private FileNode parent;

        FileNode(FileInfoDssDTO fileInfo)
        {
            this.fileInfo = fileInfo;
            children = fileInfo.isDirectory() ? new ArrayList<FileNode>() : null;
            fileName = extractFileName(fileInfo);
        }

        @Override
        public boolean getAllowsChildren()
        {
            return fileInfo.isDirectory();
        }
        
        @Override
        public boolean isLeaf()
        {
            return fileInfo.isDirectory() == false;
        }
        
        @Override
        public Enumeration<FileNode> children()
        {
            return new Enumeration<FileNode>()
                {
                    Iterator<FileNode> iterator = children.iterator();
                    @Override
                    public boolean hasMoreElements()
                    {
                        return iterator.hasNext();
                    }

                    @Override
                    public FileNode nextElement()
                    {
                        return iterator.next();
                    }
                };
        }

        @Override
        public TreeNode getChildAt(int index)
        {
            return children.get(index);
        }

        @Override
        public int getChildCount()
        {
            return children == null ? 0 : children.size();
        }

        @Override
        public int getIndex(TreeNode node)
        {
            return children.indexOf(node);
        }

        @Override
        public TreeNode getParent()
        {
            return parent;
        }
        
        void addChild(FileNode fileNode)
        {
            if (isLeaf() == false)
            {
                children.add(fileNode);
                fileNode.parent = this;
            }
        }
        
        FileInfoDssDTO getFileInfo()
        {
            return fileInfo;
        }
        
        @Override
        public String toString()
        {
            return fileName;
        }
    }

    private static final long serialVersionUID = 1L;
    
    private static String extractParentFileName(FileInfoDssDTO fileInfo)
    {
        String path = fileInfo.getPathInDataSet();
        int lastIndexOfPathSeparator = path.lastIndexOf('/');
        return lastIndexOfPathSeparator < 0 ? null : path.substring(0, lastIndexOfPathSeparator);
    }
    
    private static String extractFileName(FileInfoDssDTO fileInfo)
    {
        String path = fileInfo.getPathInDataSet();
        int lastIndexOfPathSeparator = path.lastIndexOf('/');
        return lastIndexOfPathSeparator < 0 ? path : path.substring(lastIndexOfPathSeparator + 1);
    }

    private JTree tree;

    FileChooser(String dataSetCode, FileInfoDssDTO[] fileInfos)
    {
        super(new BorderLayout());
        add(new JLabel("Choose a single file:"), BorderLayout.NORTH);
        tree = new JTree(new DefaultTreeModel(createTree(dataSetCode, fileInfos), true));
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mousePressed(MouseEvent e)
                {
                    if (e.getClickCount() < 2)
                    {
                        return;
                    }
                    TreePath path = tree.getClosestPathForLocation(e.getX(), e.getY());
                    if (path == null)
                    {
                        return;
                    }
                    FileInfoDssDTO fileInfo = ((FileNode) path.getLastPathComponent()).getFileInfo();
                    if (fileInfo.isDirectory())
                    {
                        return;
                    }
                    Window window = SwingUtilities.getWindowAncestor(tree);
                    if (window == null)
                    {
                        return;
                    }
                    JOptionPane op = (JOptionPane) SwingUtilities.getAncestorOfClass(JOptionPane.class, tree);
                    if (op != null)
                    {
                        op.setValue(JOptionPane.OK_OPTION);
                    }
                    window.setVisible(false);
                }
            });
        add(new JScrollPane(tree), BorderLayout.CENTER);
        setPreferredSize(new Dimension(500, 700));
    }
    
    private FileNode createTree(String dataSetCode, FileInfoDssDTO[] fileInfos)
    {
        Arrays.sort(fileInfos, new Comparator<FileInfoDssDTO>()
            {
                @Override
                public int compare(FileInfoDssDTO i1, FileInfoDssDTO i2)
                {
                    return i1.getPathInDataSet().compareTo(i2.getPathInDataSet());
                }
            });
        FileNode root = new FileNode(new FileInfoDssDTO(dataSetCode, null, true, -1));
        Map<String, FileNode> directories = new HashMap<String, FileChooser.FileNode>();
        for (FileInfoDssDTO fileInfo : fileInfos)
        {
            String parentFileName = extractParentFileName(fileInfo);
            FileNode fileNode = new FileNode(fileInfo);
            (parentFileName == null ? root : directories.get(parentFileName)).addChild(fileNode);
            if (fileInfo.isDirectory())
            {
                directories.put(fileInfo.getPathInDataSet(), fileNode);
            }
        }
        return root;
    }
 
    FileInfoDssDTO getSelectedFileInfoOrNull()
    {
        TreePath selectionPath = tree.getSelectionPath();
        return selectionPath == null ? null : ((FileNode) selectionPath.getLastPathComponent())
                .getFileInfo();
    }
}
