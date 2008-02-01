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

package ch.systemsx.cisd.cifex.client.application;

import ch.systemsx.cisd.cifex.client.application.utils.TableMap;
import ch.systemsx.cisd.cifex.client.dto.File;

/**
 * A <code>TableMap</code> extension suitable for {@link File}.
 * 
 * @author Christian Ribeaud
 */
public final class FileTableMap extends TableMap
{

    public FileTableMap(final File[] files)
    {
        super(files, new IKeyExtractor()
            {
                //
                // IKeyExtractor
                //

                public final Object getKey(final Object e)
                {
                    return new Long(((File) e).getID());
                }
            });
    }
}
