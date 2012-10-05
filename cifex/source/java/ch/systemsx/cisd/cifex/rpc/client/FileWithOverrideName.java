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

import ch.systemsx.cisd.cifex.rpc.client.encryption.OpenPGPSymmetricKeyEncryption;

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

    public File getOriginalFile()
    {
        return file;
    }

    public String tryGetOverrideName()
    {
        return overrideNameOrNull;
    }

    public File getEncryptedFile()
    {
        final File encryptedFile;
        if (overrideNameOrNull == null)
        {
            encryptedFile =
                    new File(file.getPath() + OpenPGPSymmetricKeyEncryption.PGP_FILE_EXTENSION);
        } else
        {
            encryptedFile = new File(file.getAbsoluteFile().getParent(), overrideNameOrNull);
        }
        return encryptedFile;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((file == null) ? 0 : file.hashCode());
        result =
                prime * result + ((overrideNameOrNull == null) ? 0 : overrideNameOrNull.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        FileWithOverrideName other = (FileWithOverrideName) obj;
        if (file == null)
        {
            if (other.file != null)
            {
                return false;
            }
        } else if (file.equals(other.file) == false)
        {
            return false;
        }
        if (overrideNameOrNull == null)
        {
            if (other.overrideNameOrNull != null)
            {
                return false;
            }
        } else if (!overrideNameOrNull.equals(other.overrideNameOrNull))
        {
            return false;
        }
        return true;
    }

}
