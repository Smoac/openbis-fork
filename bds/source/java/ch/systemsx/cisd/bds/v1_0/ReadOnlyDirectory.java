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

package ch.systemsx.cisd.bds.v1_0;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ch.systemsx.cisd.bds.storage.DirectoryProxy;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IFile;
import ch.systemsx.cisd.bds.storage.ILink;
import ch.systemsx.cisd.bds.storage.INode;

/**
 * An {@link IDirectory} implementation which denies access to write operations.
 * 
 * @author Christian Ribeaud
 */
final class ReadOnlyDirectory extends DirectoryProxy
{
    ReadOnlyDirectory(final IDirectory delegate)
    {
        super(delegate);
    }

    final static ReadOnlyDirectory tryCreateReadOnlyDirectory(final IDirectory directory)
    {
        if (directory == null)
        {
            return null;
        }
        return new ReadOnlyDirectory(directory);
    }

    //
    // DirectoryProxy
    //

    // Access denied

    @Override
    public final INode addFile(final File file, final String nameOrNull, final boolean move)
    {
        ReadOnlyNode.denyAccess();
        return null;
    }

    @Override
    public final IFile addKeyValuePair(final String key, final String value)
    {
        ReadOnlyNode.denyAccess();
        return null;
    }

    @Override
    public final IDirectory makeDirectory(final String name)
    {
        ReadOnlyNode.denyAccess();
        return null;
    }

    @Override
    public final void moveTo(final File directory)
    {
        ReadOnlyNode.denyAccess();
    }

    @Override
    public final void removeNode(final INode node)
    {
        ReadOnlyNode.denyAccess();
    }

    @Override
    public final ILink tryAddLink(final String name, final INode node)
    {
        ReadOnlyNode.denyAccess();
        return null;
    }

    // Read-only return value

    @Override
    public final Iterator<INode> iterator()
    {
        return new ImmutableIterator(super.iterator());
    }

    @Override
    public final List<IDirectory> listDirectories(final boolean recursive)
    {
        final List<IDirectory> directories = super.listDirectories(recursive);
        final List<IDirectory> list = new ArrayList<IDirectory>(directories.size());
        for (final IDirectory directory : directories)
        {
            list.add(tryCreateReadOnlyDirectory(directory));
        }
        return Collections.unmodifiableList(list);
    }

    @Override
    public final List<IFile> listFiles(final String[] extensionsOrNull, final boolean recursive)
    {
        final List<IFile> files = super.listFiles(extensionsOrNull, recursive);
        final List<IFile> list = new ArrayList<IFile>(files.size());
        for (final IFile file : files)
        {
            list.add(ReadOnlyFile.tryCreateReadOnlyFile(file));
        }
        return Collections.unmodifiableList(list);
    }

    @Override
    public final IDirectory tryAsDirectory()
    {
        return tryCreateReadOnlyDirectory(super.tryAsDirectory());
    }

    @Override
    public final IFile tryAsFile()
    {
        return ReadOnlyFile.tryCreateReadOnlyFile(super.tryAsFile());
    }

    @Override
    public final IDirectory tryGetParent()
    {
        return tryCreateReadOnlyDirectory(super.tryGetParent());
    }

    //
    // Helper classes
    //

    private final static class ImmutableIterator implements Iterator<INode>
    {

        private final Iterator<INode> delegate;

        ImmutableIterator(final Iterator<INode> delegate)
        {
            this.delegate = delegate;
        }

        //
        // Iterator
        //

        public final boolean hasNext()
        {
            return delegate.hasNext();
        }

        public final INode next()
        {
            return ReadOnlyNode.tryCreateReadOnlyNode(delegate.next());
        }

        public final void remove()
        {
            ReadOnlyNode.denyAccess();
        }
    }
}
