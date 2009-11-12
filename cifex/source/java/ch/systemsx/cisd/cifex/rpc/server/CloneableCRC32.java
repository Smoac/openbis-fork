/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.rpc.server;

import java.util.zip.CRC32;

/**
 * A {@link CRC32} class which can be cloned.
 *
 * @author Bernd Rinn
 */
public class CloneableCRC32 extends CRC32 implements Cloneable
{
    /**
     * Returns the CRC32 as an <code>int</code>.
     */
    public int getIntValue()
    {
        return (int) super.getValue();
    }

    @Override
    public CloneableCRC32 clone()
    {
        try
        {
            return (CloneableCRC32) super.clone();
        } catch (CloneNotSupportedException ex)
        {
            throw new Error("Cloning failed");
        }
    }

}
