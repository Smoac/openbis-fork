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

package ch.systemsx.cisd.bds.storage;

import java.io.InputStream;
import java.util.List;

/**
 * Node representing a file with some content.
 * 
 * @author Franz-Josef Elmer
 */
public interface IFile extends INode
{
    /**
     * Returns the content of this file node as a byte array.
     * 
     * @return never <code>null</code> but could return an empty byte array.
     */
    public byte[] getBinaryContent();

    /**
     * Returns the content of this file node as an input stream.
     */
    public InputStream getInputStream();

    /**
     * Returns the content of this file node as a string.
     * 
     * @return never <code>null</code> but could return an empty string.
     */
    public String getStringContent();

    /**
     * Returns the content of this file node as a string. Doesn't change line terminating characters.
     * 
     * @return never <code>null</code> but could return an empty string.
     */
    public String getExactStringContent();

    /**
     * Returns the content of this file node as a list of <code>String</code> objects.
     * <p>
     * This is useful when you know that the file content is composed of several lines.
     * </p>
     * 
     * @return never <code>null</code> but could return an empty list.
     */
    public List<String> getStringContentList();

}
