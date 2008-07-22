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

package ch.systemsx.cisd.datamover.filesystem;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.common.utilities.ISelfTestable;

/**
 * {@link ISelfTestable}s to be tested during self test.
 *
 * @author Bernd Rinn
 */
public class FileStoreSelfTestables
{
    private static final List<ISelfTestable> selfTestables = new ArrayList<ISelfTestable>();

    /**
     * Add a self testable to the list.
     */
    static synchronized void addSelfTestable(ISelfTestable selfTestable)
    {
        selfTestables.add(selfTestable);
    }
    
    /**
     * Get the array of self testables to test in self test.
     */
    public static synchronized ISelfTestable[] getSelfTestables()
    {
        return selfTestables.toArray(new ISelfTestable[selfTestables.size()]); 
    }
}
