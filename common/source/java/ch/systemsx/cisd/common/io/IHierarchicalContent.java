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

    /**
     * Returns node with specified <var>relativePath</var> starting from root node. If the path is
     * <code>null<code> or an empty string then the root node is returned.
     * 
     * @throws IllegalArgumentException if resource with given <var>relativePath</var> doesn't exist
     */
    IHierarchicalContentNode getNode(String relativePath) throws IllegalArgumentException;

    /**
     * Returns list of all file nodes in this hierarchy with relative paths matching given
     * <var>relativePathPattern</var>.
     * <p>
     * NOTE: this operation may be expensive for huge hierarchies. If prefix of relative path is
     * constant use {@link #listMatchingNodes(String, String)} instead with pattern for filename.
     */
    List<IHierarchicalContentNode> listMatchingNodes(String relativePathPattern);

    /**
     * Returns list of all file nodes in this hierarchy starting from <var>startingPath</var> with
     * file names matching given <var>fileNamePattern</var>.
     * 
     * @param startingPath Relative path from which the search should start. Use empty string to
     *            start in root.
     */
    List<IHierarchicalContentNode> listMatchingNodes(String startingPath, String fileNamePattern);

    /** Cleans resources acquired to access this hierarchical content. */
    void close();
}
