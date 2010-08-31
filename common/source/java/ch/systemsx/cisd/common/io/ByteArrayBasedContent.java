/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.io;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Content based on an array of bytes.
 * 
 * @author Franz-Josef Elmer
 */
public class ByteArrayBasedContent implements IContent
{
    private final byte[] byteArray;

    private final String nameOrNull;

    /**
     * Creates an instance for the specified byte array.
     * 
     * @param nameOrNull Name of the content or null
     */
    public ByteArrayBasedContent(byte[] byteArray, String nameOrNull)
    {
        this.byteArray = byteArray;
        this.nameOrNull = nameOrNull;
    }

    public String tryGetName()
    {
        return nameOrNull;
    }

    /**
     * Returns the number of bytes in the array.
     */
    public long getSize()
    {
        return byteArray.length;
    }

    /**
     * Returns always <code>true</code>.
     */
    public boolean exists()
    {
        return true;
    }

    /**
     * Returns an instance of {@link ByteArrayInputStream}.
     */
    public InputStream getInputStream()
    {
        return new ByteArrayInputStream(byteArray);
    }
}
