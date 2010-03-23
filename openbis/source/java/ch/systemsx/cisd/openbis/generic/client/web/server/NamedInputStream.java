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

package ch.systemsx.cisd.openbis.generic.client.web.server;

import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author Izabela Adamczyk
 */
public class NamedInputStream
{

    private final InputStream stream;

    private final String originalName;

    private final byte[] bytes;

    public NamedInputStream(InputStream stream, String originalName, byte[] bytes)
    {
        this.stream = stream;
        this.originalName = originalName;
        this.bytes = bytes;
    }

    public InputStream getInputStream()
    {
        return stream;
    }

    public String getOriginalFilename()
    {
        return originalName;
    }

    /**
     * @see MultipartFile#getBytes()
     */
    public byte[] getBytes()
    {
        return bytes;
    }
}
