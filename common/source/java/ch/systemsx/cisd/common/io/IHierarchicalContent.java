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

import java.util.List;

/**
 * Abstraction of a tree-like hierarchical content on the filesystem.
 * 
 * @author Chandrasekhar Ramakrishnan
 * @author Piotr Buczek
 */
public interface IHierarchicalContent
{
    /** Returns root node. */
    IHierarchicalContentNode getRootNode();

    /** Returns node with specified relative path starting from root node. */
    IHierarchicalContentNode getNode(String relativePath);

    /**
     * Returns list of all file nodes in this hierarchy with names matching given
     * <code>pattern</code>.
     * <p>
     * NOTE: this operation may be expensive for huge hierarchies
     */
    List<IHierarchicalContentNode> listMatchingNodes(String pattern);

    /**
     * Like {@link #listMatchingNodes(String)} but search starts from specified relative path.
     * <p>
     * NOTE: this operation may be expensive for huge hierarchies
     */
    List<IHierarchicalContentNode> listMatchingNodes(String startingPath, String pattern);

    /** Cleans resources acquired to access this hierarchical content. */
    void close();
}
