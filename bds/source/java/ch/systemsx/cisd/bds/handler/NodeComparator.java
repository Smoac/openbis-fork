/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.bds.handler;

import java.util.Comparator;

import ch.systemsx.cisd.bds.storage.INode;

/**
 * Contains <code>Comparator</code> implementations for {@link INode}.
 * 
 * @author Christian Ribeaud
 */
public final class NodeComparator
{

    /**
     * A <code>Comparator</code> for <code>INode</code> which sorts nodes by name ({@link INode#getName()}).
     */
    public final static Comparator<INode> BY_NAME_IGNORE_CASE = new Comparator<INode>()
        {

            //
            // Comparator
            //

            public final int compare(final INode n1, final INode n2)
            {
                return n1.getName().compareToIgnoreCase(n2.getName());
            }
        };
}
