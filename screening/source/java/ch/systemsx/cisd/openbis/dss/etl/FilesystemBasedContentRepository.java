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

package ch.systemsx.cisd.openbis.dss.etl;

import java.io.File;

import ch.systemsx.cisd.common.io.FileBasedContent;
import ch.systemsx.cisd.common.io.IContent;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class FilesystemBasedContentRepository implements IContentRepository
{
    private final File rootDirectory;

    public FilesystemBasedContentRepository(File rootDirectory)
    {
        this.rootDirectory = rootDirectory;
    }

    public void close()
    {
    }

    public IContent getContent(String path)
    {
        return new FileBasedContent(new File(rootDirectory, path));
    }

    public void open()
    {
    }

}
