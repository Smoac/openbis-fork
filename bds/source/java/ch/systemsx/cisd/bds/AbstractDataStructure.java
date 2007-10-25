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

import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IStorage;

/**
 * Abstract superclass of classes implementing {@link IDataStructure}.
 *
 * @author Franz-Josef Elmer
 */
abstract class AbstractDataStructure implements IDataStructure
{
    protected final IStorage storage;
    
    protected IDirectory root;

    AbstractDataStructure(IStorage storage)
    {
        assert storage != null: "Unspecified storage.";
        this.storage = storage;
    }

    private void mountStorage()
    {
        storage.mount();
        root = storage.getRoot();
    }
    
    /**
     * Asserts that this instance is already opened or created otherwise a {@link IllegalStateException} is thrown.
     */
    protected void assertOpenOrCreated()
    {
        if (root == null)
        {
            throw new IllegalStateException("Data structure should first be opened or created.");
        }
    }
    
    /**
     * Validates this data structure and throws {@link DataStructureException} if invalid. 
     */
    protected abstract void assertValid(); 
    
    /**
     * Performs opening specific for the concrete data structure. Will be invoked after the common part of
     * {@link #open()} but before validation with {@link #assertValid()}. 
     */
    protected abstract void performOpening();
    
    /**
     * Performs closing specific for the concrete data structure. Will be invoked before validation with
     * {@link #assertValid()}.
     */
    protected abstract void performClosing();
    
    //
    // IDataStructure
    //
    
    public final void create()
    {
        mountStorage();
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
