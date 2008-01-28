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

package ch.systemsx.cisd.cifex.server.business.dto;

/**
 * A class that encapsulates general and basic information related to a file.
 * 
 * @author Christian Ribeaud
 */
public final class BasicFileDTO
{

    /** Name of file. */
    private String name;

    /**
     * Size of the file (in bytes).
     * <p>
     * If <code>null</code>, then it has not been set.
     * </p>
     */
    private Long size;

    /** The content type of the file. */
    private String contentType;

    public final String getName()
    {
        return name;
    }

    public final void setName(final String name)
    {
        this.name = name;
    }

    public final Long getSize()
    {
        return size;
    }

    public final void setSize(final Long size)
    {
        this.size = size;
    }

    public final String getContentType()
    {
        return contentType;
    }

    public final void setContentType(final String contentType)
    {
        this.contentType = contentType;
    }
}
