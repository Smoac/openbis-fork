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
 * Content based on a {@link IContentProvider}.
 *
 * @author Franz-Josef Elmer
 */
public class ContentProviderBasedContent implements IContent
{
    private final IContentProvider contentProvider;
    private IContent content;

    /**
     * Creates an instance for specified content provider.
     */
    public ContentProviderBasedContent(IContentProvider contentProvider)
    {
        this.contentProvider = contentProvider;
    }

    public String tryGetName()
    {
        return getContent().tryGetName();
    }

    public long getSize()
    {
        return getContent().getSize();
    }

    public boolean exists()
    {
        return getContent().exists();
    }

    public InputStream getInputStream()
    {
        return getContent().getInputStream();
    }
    
    private IContent getContent()
    {
        if (content == null)
        {
            content = contentProvider.getContent();
        }
        return content;
    }
    
}
