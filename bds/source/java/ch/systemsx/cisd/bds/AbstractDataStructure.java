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
public abstract class AbstractDataStructure implements IDataStructure, IDataStructureHandler
{
    protected final IStorage storage;

    protected IDirectory root;

    private final List<IDataStructureHandler> handlers;

    private Mode mode;

    protected FormatParameters formatParameters = new FormatParameters();

    protected AbstractDataStructure(final IStorage storage)
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

    //
    // IDataStructureHandler
    //

    public void performCreating()
    {
        for (final IDataStructureHandler handler : handlers)
        {
            handler.performCreating();
        }
    }

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

    public final boolean isOpenOrCreated()
    {
        return root != null;
    }

    public final void create(List<FormatParameter> parameters)
    {
        this.mode = Mode.READ_WRITE;
        addFormatParameters(parameters);
        mountStorage();
        performCreating();
    }

    private void addFormatParameters(List<FormatParameter> parameters)
    {
        for (FormatParameter param : parameters)
        {
            this.formatParameters.addParameter(param);
        }
    }

    public final void open(final Mode thatMode)
    {
        open(thatMode, true);
    }

    public final void open(final Mode thatMode, boolean validate)
    {
        assert thatMode != null : "Unspecified mode";
        mode = thatMode;
        mountStorage();
        performOpening();
        final Version loadedVersion = Version.loadFrom(root);
        if (getVersion().isBackwardsCompatibleWith(loadedVersion) == false)
        {
            throw new DataStructureException("Version of loaded data structure is " + loadedVersion
                    + " which is not backward compatible with " + getVersion());
        }
        if (validate)
        {
            assertValid();
        }
    }

    public final void close()
    {
        if (mode == Mode.READ_WRITE)
        {
            performClosing();
            getVersion().saveTo(root);
            // TODO 2008-07-03, Bernd Rinn: make this optional
            // assertValid();
        }
        mode = null;
        storage.unmount();
        root = null;
    }
}
