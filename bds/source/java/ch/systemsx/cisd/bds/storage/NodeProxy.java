/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.bds.storage;

import java.io.File;

/**
 * A {@link INode} implementation which delegates all the interface method calls to the encapsulated
 * object.
 * 
 * @author Christian Ribeaud
 */
public class NodeProxy implements INode
{
    final INode delegate;

    public NodeProxy(final INode delegate)
    {
        this.delegate = delegate;
        assert delegate instanceof NodeProxy == false;
    }

    //
    // INode
    //

    public void extractTo(final File directory)
    {
        delegate.extractTo(directory);
    }

    public String getName()
    {
        return delegate.getName();
    }

    public String getPath()
    {
        return delegate.getPath();
    }

    public boolean isValid()
    {
        return delegate.isValid();
    }

    public void moveTo(final File directory)
    {
        delegate.moveTo(directory);
    }

    public IDirectory tryAsDirectory()
    {
        return delegate.tryAsDirectory();
    }

    public IFile tryAsFile()
    {
        return delegate.tryAsFile();
    }

    public IDirectory tryGetParent()
    {
        return delegate.tryGetParent();
    }
}
