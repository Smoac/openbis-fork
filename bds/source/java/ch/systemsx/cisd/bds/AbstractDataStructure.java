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

package ch.systemsx.cisd.bds;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.bds.exception.DataStructureException;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IStorage;

/**
 * Abstract superclass of classes implementing {@link IDataStructure}.
 * 
 * @author Franz-Josef Elmer
 */
abstract class AbstractDataStructure implements IDataStructure, IDataStructureHandler
{
    protected final IStorage storage;

    protected IDirectory root;

    private final List<IDataStructureHandler> handlers;

    AbstractDataStructure(final IStorage storage)
    {
        assert storage != null : "Unspecified storage.";
        this.storage = storage;
        handlers = new ArrayList<IDataStructureHandler>();
    }

    private void mountStorage()
    {
        storage.mount();
        root = storage.getRoot();
    }

    protected final void registerHandler(final IDataStructureHandler handler)
    {
        assert handler != null : "Given handler can not be null.";
        handlers.add(handler);
    }

    /**
     * Asserts that this instance is already opened or created otherwise a
     * {@link IllegalStateException} is thrown.
     */
    protected final void assertOpenOrCreated()
    {
        if (root == null)
        {
            throw new IllegalStateException("Data structure should first be opened or created.");
        }
    }

    /**
     * After-creation jobs that should be done. Kind of initialization for subclasses when they
     * create a new data structure.
     * <p>
     * By default this method does nothing.
     * </p>
     */
    protected void afterCreation()
    {
    }

    //
    // IDataStructureHandler
    //

    public void assertValid()
    {
        for (final IDataStructureHandler handler : handlers)
        {
            handler.assertValid();
        }
    }

    public void performOpening()
    {
        for (final IDataStructureHandler handler : handlers)
        {
            handler.performOpening();
        }
    }

    public void performClosing()
    {
        for (final IDataStructureHandler handler : handlers)
        {
            handler.performClosing();
        }
    }

    //
    // IDataStructure
    //

    public final void create()
    {
        mountStorage();
        afterCreation();
    }

    public final void open()
    {
        mountStorage();
        performOpening();
        Version loadedVersion = Version.loadFrom(root);
        if (loadedVersion.isBackwardsCompatibleWith(getVersion()) == false)
        {
            throw new DataStructureException("Version of loaded data structure is " + loadedVersion
                    + " which is not backward compatible with " + getVersion());
        }
        assertValid();
    }

    public final void close()
    {
        assertOpenOrCreated();
        getVersion().saveTo(root);
        performClosing();
        assertValid();
        storage.unmount();
    }
}
