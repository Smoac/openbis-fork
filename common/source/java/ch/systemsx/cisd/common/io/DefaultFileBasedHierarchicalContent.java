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

package ch.systemsx.cisd.common.io;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link IHierarchicalContent} implementation for normal {@link java.io.File}.
 * 
 * @author Piotr Buczek
 */
class DefaultFileBasedHierarchicalContent implements IHierarchicalContent
{
    private final HierarchicalContentFactory hierarchicalContentFactory;

    private final File root;

    DefaultFileBasedHierarchicalContent(HierarchicalContentFactory hierarchicalContentFactory,
            File file)
    {
        assert hierarchicalContentFactory != null;
        this.hierarchicalContentFactory = hierarchicalContentFactory;

        if (file.exists() == false)
        {
            throw new IllegalArgumentException(file.getAbsolutePath() + " doesn't exist");
        }
        if (file.isDirectory() == false)
        {
            throw new IllegalArgumentException(file.getAbsolutePath() + " is not a directory");
        }
        this.root = file;

    }

    public IHierarchicalContentNode getRootNode()
    {
        return asNode(root);
    }

    public IHierarchicalContentNode getNode(String relativePath)
    {
        return asNode(new File(root, relativePath));
    }

    private IHierarchicalContentNode asNode(File file)
    {
        return hierarchicalContentFactory.asHierarchicalContentNode(this, file);
    }

    public List<IHierarchicalContentNode> listMatchingNodes(final String pattern)
    {
        File[] files = root.listFiles(new FilenameFilter()
            {
                public boolean accept(File dir, String name)
                {
                    return name.matches(pattern);
                }
            });

        List<IHierarchicalContentNode> nodes = new ArrayList<IHierarchicalContentNode>();
        for (File file : files)
        {
            nodes.add(hierarchicalContentFactory.asHierarchicalContentNode(this, file));
        }
        return nodes;
    }

    //
    // Object
    //

    @Override
    public String toString()
    {
        return "DefaultFileBasedHierarchicalContent [root=" + root + "]";
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((root == null) ? 0 : root.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (!(obj instanceof DefaultFileBasedHierarchicalContent))
        {
            return false;
        }
        DefaultFileBasedHierarchicalContent other = (DefaultFileBasedHierarchicalContent) obj;
        if (root == null)
        {
            if (other.root != null)
            {
                return false;
            }
        } else if (!root.equals(other.root))
        {
            return false;
        }
        return true;
    }

}
