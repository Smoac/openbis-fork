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
import java.util.Comparator;

/**
 * This class contains <code>Comparator</code> implementation suitable for <code>File</code>.
 * 
 * @author Christian Ribeaud
 */
public final class FileComparator
{

    private FileComparator()
    {
        // This class can not be instantiated.
    }

    /**
     * A {@link File} <code>Comparator</code> implementation that considers value returned by
     * {@link File#lastModified()} to sort the files.
     * 
     * @author Christian Ribeaud
     */
    public final static Comparator<File> BY_LAST_MODIFIED = new Comparator<File>()
        {
            //
            // Comparator
            //

            public final int compare(final File o1, final File o2)
            {
                assert o1 != null && o2 != null;
                return (int) (o1.lastModified() - o2.lastModified());
            }
        };

    /**
     * A {@link File} <code>Comparator</code> implementation that considers value returned by {@link File#getName()}
     * to sort the files.
     * 
     * @author Christian Ribeaud
     */
    public final static Comparator<File> BY_NAME = new Comparator<File>()
        {
            //
            // Comparator
            //

            public final int compare(final File o1, final File o2)
            {
                assert o1 != null && o2 != null;
                return o1.getName().compareTo(o2.getName());
            }
        };

    /**
     * A {@link File} <code>Comparator</code> implementation that sorts by type (file or directory): first the files
     * are listed then come the directories.
     * 
     * @author Christian Ribeaud
     */
    public final static Comparator<File> BY_TYPE = new Comparator<File>()
        {
            //
            // Comparator
            //

            public final int compare(final File o1, final File o2)
            {
                assert o1 != null && o2 != null;
                return o1.isFile() ? o2.isFile() ? 0 : -1 : +1;
            }
        };
}