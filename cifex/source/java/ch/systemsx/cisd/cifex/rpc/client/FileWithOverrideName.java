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

package ch.systemsx.cisd.cifex.rpc.client;

import java.io.File;

/**
 * A file with an optional override name. The override name, if provided, is the name reported to
 * the server.
 * 
 * @author Bernd Rinn
 */
public class FileWithOverrideName
{
    private final File file;

    private final String overrideNameOrNull;
    
    public FileWithOverrideName(File file, String overrideNameOrNull)
    {
        this.file = file;
        this.overrideNameOrNull = overrideNameOrNull;
    }

    public File getFile()
    {
        return file;
    }

    public String tryGetOverrideName()
    {
        return overrideNameOrNull;
    }

}
