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
import java.util.Iterator;
import java.util.List;

/**
 * A {@link IDirectory} implementation which getDirectory()s all the interface method calls to the
 * encapsulated object.
 * 
 * @author Christian Ribeaud
 */
public class DirectoryProxy extends NodeProxy implements IDirectory
{
    public DirectoryProxy(final IDirectory delegate)
    {
        super(delegate);
        assert delegate instanceof DirectoryProxy == false;
    }

    protected final IDirectory getDirectory()
    {
        return (IDirectory) delegate;
    }

    //
    // IDirectory
    //

    public INode addFile(final File file, final String nameOrNull, final boolean move)
    {
        return getDirectory().addFile(file, nameOrNull, move);
    }

    public IFile addKeyValuePair(final String key, final String value)
    {
        return getDirectory().addKeyValuePair(key, value);
    }

    public Iterator<INode> iterator()
    {
        return getDirectory().iterator();
    }

    public List<IDirectory> listDirectories(final boolean recursive)
    {
        return getDirectory().listDirectories(recursive);
    }

    public List<IFile> listFiles(final String[] extensionsOrNull, final boolean recursive)
    {
        return getDirectory().listFiles(extensionsOrNull, recursive);
    }

    public IDirectory makeDirectory(final String name)
    {
        return getDirectory().makeDirectory(name);
    }

    public void removeNode(final INode node)
    {
        getDirectory().removeNode(node);
    }

    public ILink tryAddLink(final String name, final INode node)
    {
        return getDirectory().tryAddLink(name, node);
    }

    public INode tryGetNode(final String name)
    {
        return getDirectory().tryGetNode(name);
    }

}