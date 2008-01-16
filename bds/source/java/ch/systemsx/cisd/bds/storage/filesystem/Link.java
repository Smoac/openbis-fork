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

package ch.systemsx.cisd.bds.storage.filesystem;

import java.io.File;

import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IFile;
import ch.systemsx.cisd.bds.storage.ILink;
import ch.systemsx.cisd.bds.storage.INode;

/**
 * An <code>ILink</code> implementation.
 * 
 * @author Franz-Josef Elmer
 */
final class Link implements ILink
{
    private final String name;

    private final INode reference;

    private IDirectory parent;

    Link(final String name, final INode reference)
    {
        assert name != null : "A name must be specified.";
        assert reference != null : "Reference can not be null.";
        assert reference instanceof IFile : "Given reference must be of type IFile: " + reference.getClass().getName();
        assert reference.isValid() : "Given reference must be valid: " + reference;
        this.name = name;
        this.reference = reference;
    }

    /** Sets the parent of this {@link INode}. */
    final void setParent(final IDirectory parentOrNull)
    {
        parent = parentOrNull;
    }

    //
    // ILink
    //

    public final String getName()
    {
        return name;
    }

    public final IDirectory tryToGetParent()
    {
        return parent;
    }

    public final INode getReference()
    {
        return reference;
    }

    public final void extractTo(final File directory)
    {
        reference.extractTo(directory);
    }

    public final void moveTo(final File directory)
    {
        reference.moveTo(directory);
    }

    public final boolean isValid()
    {
        if (reference.isValid() == false)
        {
            return false;
        }
        if (parent != null)
        {
            final INode node = parent.tryGetNode(name);
            return node != null && node.isValid();
        }
        return true;
    }
}
