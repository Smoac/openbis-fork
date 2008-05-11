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

package ch.systemsx.cisd.common.utilities;

import java.io.File;

/**
 * Represents one entry (file or directory) in a file store
 * 
 * @author Tomasz Pylak
 */
public class StoreItem
{
    public static final StoreItem[] EMPTY_ARRAY = new StoreItem[0];

    private final String name;

    public StoreItem(final String name)
    {
        this.name = name;
    }

    /** Should not be used for logging. Use toString() instead. */
    public String getName()
    {
        return name;
    }

    public final static File asFile(final File parentDirectory, final StoreItem item)
    {
        return new File(parentDirectory, item.getName());
    }

    //
    // Object
    //

    @Override
    public final String toString()
    {
        return name;
    }

    @Override
    public final boolean equals(final Object obj)
    {
        return obj != null && obj instanceof StoreItem && name.equals(((StoreItem) obj).name);
    }

    @Override
    public final int hashCode()
    {
        return name.hashCode();
    }
}
