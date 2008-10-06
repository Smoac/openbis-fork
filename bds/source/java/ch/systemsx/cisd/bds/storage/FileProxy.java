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

package ch.systemsx.cisd.bds.storage;

import java.io.InputStream;
import java.util.List;

/**
 * A {@link IFile} implementation which delegates all the interface method calls to the encapsulated
 * object.
 * 
 * @author Christian Ribeaud
 */
public class FileProxy extends NodeProxy implements IFile
{
    public FileProxy(final IFile delegate)
    {
        super(delegate);
        assert delegate instanceof FileProxy == false;
    }

    protected final IFile getFile()
    {
        return (IFile) delegate;
    }

    //
    // IFile
    //

    public byte[] getBinaryContent()
    {
        return getFile().getBinaryContent();
    }

    public String getExactStringContent()
    {
        return getFile().getExactStringContent();
    }

    public InputStream getInputStream()
    {
        return getFile().getInputStream();
    }

    public String getStringContent()
    {
        return getFile().getStringContent();
    }

    public List<String> getStringContentList()
    {
        return getFile().getStringContentList();
    }
}
