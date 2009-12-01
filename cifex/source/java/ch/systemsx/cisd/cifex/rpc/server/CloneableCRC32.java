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

import java.lang.reflect.Field;
import java.util.zip.CRC32;

/**
 * A {@link CRC32} class which can be cloned and initialized with a value.
 * 
 * @author Bernd Rinn
 */
public class CloneableCRC32 extends CRC32 implements Cloneable
{
    public CloneableCRC32()
    {
    }

    /**
     * Initialize the object, setting the CRC32 value initially to <var>initialCRC32Value</var>. Use
     * this for resuming a checksum calculation.
     */
    public CloneableCRC32(int initialCRC32Value)
    {
        // WORKAROUND: this is an awful hack, but the Sun engineers just didn't think of this case
        // when designing the API.
        final Field crcField;
        try
        {
            crcField = CRC32.class.getDeclaredField("crc");
            crcField.setAccessible(true);
            crcField.setInt(this, initialCRC32Value);
        } catch (Exception ex)
        {
            throw new Error("Cannot set crc field: " + ex.getClass().getSimpleName());
        }
    }

    /**
     * Returns the CRC32 value as an <code>int</code>.
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
