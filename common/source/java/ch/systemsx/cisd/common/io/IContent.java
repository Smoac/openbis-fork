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

import java.io.InputStream;

/**
 * Abstract of streamable binary content with name and known size.
 * 
 * @author Franz-Josef Elmer
 */
public interface IContent
{
    /**
     * Returns name of the content or null.
     */
    public String tryGetName();

    /**
     * Returns number of bytes of the content.
     */
    public long getSize();

    /**
     * Returns <code>true</code> if the content exists.
     */
    public boolean exists();

    /**
     * Returns a new instance of an input stream over the complete content. Note that the returned
     * {@link InputStream} is expected to have {@link InputStream#markSupported()}
     * <code>==true</code>.
     */
    public InputStream getInputStream();

}
