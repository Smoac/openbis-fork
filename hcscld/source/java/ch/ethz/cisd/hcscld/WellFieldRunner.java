/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.ethz.cisd.hcscld;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A runner for P@link {@link IWellFieldRunnable}.
 * 
 * @author Bernd Rinn
 */
class WellFieldRunner
{
    static void run(final WellFieldGeometry geometry, final IWellFieldRunnable runnable,
            final Object stateOrNull)
    {
        for (int row = 0; row < geometry.getNumberOfRows(); ++row)
        {
            for (int col = 0; col < geometry.getNumberOfColumns(); ++col)
            {
                for (int field = 0; field < geometry.getNumberOfFields(); ++field)
                {
                    runnable.run(new WellFieldId(row, col, field), stateOrNull);
                }
            }
        }
    }

    interface IExistChecker
    {
        boolean exists(WellFieldId id);
    }

    static Iterator<WellFieldId> iterator(final WellFieldGeometry geometry,
            final IExistChecker checkerOrNull)
    {
        return new Iterator<WellFieldId>()
            {
                int row = 0;

                int col = 0;

                int field = 0;

                boolean hasNext = true;

                WellFieldId next = null;

                WellFieldId tryInternalNext()
                {
                    if (hasNext == false)
                    {
                        return null;
                    }
                    final WellFieldId id = new WellFieldId(row, col, field);
                    if (++field == geometry.getNumberOfFields())
                    {
                        field = 0;
                        if (++col == geometry.getNumberOfColumns())
                        {
                            col = 0;
                            if (++row == geometry.getNumberOfRows())
                            {
                                hasNext = false;
                            }
                        }
                    }
                    return id;
                }

                WellFieldId tryFindNext()
                {
                    WellFieldId id = tryInternalNext();
                    while (id != null && checkerOrNull != null && checkerOrNull.exists(id) == false)
                    {
                        id = tryInternalNext();
                    }
                    return id;
                }

                public boolean hasNext()
                {
                    if (next == null)
                    {
                        next = tryFindNext();
                    }
                    return (next != null);
                }

                public WellFieldId next()
                {
                    try
                    {
                        if (hasNext() == false)
                        {
                            throw new NoSuchElementException();
                        }
                        return next;
                    } finally
                    {
                        next = null;
                    }
                }

                public void remove()
                {
                    throw new UnsupportedOperationException();
                }

            };
    }
}
