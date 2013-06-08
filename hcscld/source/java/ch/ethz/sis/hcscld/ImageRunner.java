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

package ch.ethz.sis.hcscld;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A runner for {@link IImageRunnable}.
 * 
 * @author Bernd Rinn
 */
class ImageRunner
{
    static void run(final ImageQuantityStructure geometry, final IImageRunnable runnable,
            final Object stateOrNull)
    {
        for (int row = 0; row < geometry.getNumberOfRows(); ++row)
        {
            for (int col = 0; col < geometry.getNumberOfColumns(); ++col)
            {
                for (int field = 0; field < geometry.getNumberOfFields(); ++field)
                {
                    for (int seqIdx = 0; seqIdx < geometry.getSequenceLength(); ++seqIdx)
                    {
                        runnable.run(new ImageId(row, col, field, seqIdx), stateOrNull);
                    }
                }
            }
        }
    }

    interface IExistChecker
    {
        boolean exists(ImageId id);
    }

    static Iterator<ImageId> iterator(final ImageQuantityStructure geometry,
            final IExistChecker checkerOrNull)
    {
        return new Iterator<ImageId>()
            {
                int row = 0;

                int col = 0;

                int field = 0;
                
                int sequenceIdx = 0;

                boolean hasNext = true;

                ImageId next = null;

                ImageId tryInternalNext()
                {
                    if (hasNext == false)
                    {
                        return null;
                    }
                    final ImageId id = new ImageId(row, col, field, sequenceIdx);
                    if (++sequenceIdx == geometry.getSequenceLength())
                    {
                        sequenceIdx = 0;
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
                    }
                    return id;
                }

                ImageId tryFindNext()
                {
                    ImageId id = tryInternalNext();
                    while (id != null && checkerOrNull != null && checkerOrNull.exists(id) == false)
                    {
                        id = tryInternalNext();
                    }
                    return id;
                }

                @Override
                public boolean hasNext()
                {
                    if (next == null)
                    {
                        next = tryFindNext();
                    }
                    return (next != null);
                }

                @Override
                public ImageId next()
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

                @Override
                public void remove()
                {
                    throw new UnsupportedOperationException();
                }

            };
    }
}
