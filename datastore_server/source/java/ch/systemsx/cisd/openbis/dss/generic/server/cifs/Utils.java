/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.dss.generic.server.cifs;

import java.util.List;
import java.util.Map;

import org.alfresco.jlan.server.filesys.FileAttribute;
import org.alfresco.jlan.server.filesys.FileInfo;
import org.apache.ftpserver.ftplet.FtpFile;
import org.springframework.extensions.config.ConfigElement;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class Utils
{
    public static String render(ConfigElement element)
    {
        StringBuilder builder = new StringBuilder();
        render("", builder, element);
        return builder.toString();
    }
    
    private static void render(String indentation, StringBuilder builder, ConfigElement element)
    {
        builder.append(indentation).append(element.getName()).append(" = ").append(element.getValue());
        Map<String, String> attributes = element.getAttributes();
        if (attributes != null)
        {
            builder.append(" ").append(attributes);
        }
        List<ConfigElement> children = element.getChildren();
        if (children != null)
        {
            for (ConfigElement child : children)
            {
                builder.append('\n');
                render(indentation + "  ", builder, child);
            }
        }
    }

    static void populateFileInfo(FileInfo fileInfo, FtpFile file)
    {
        fileInfo.setFileName(file.getName());
        fileInfo.setModifyDateTime(file.getLastModified());
        fileInfo.setSize(file.getSize());
        int attr = 0;
        if (file.isDirectory())
        {
            attr |= FileAttribute.Directory;
        }
        if (file.isHidden())
        {
            attr |= FileAttribute.Hidden;
        }
        if (file.isWritable() == false)
        {
            attr |= FileAttribute.ReadOnly;
        }
        fileInfo.setFileAttributes(attr);
        fileInfo.setFileId(file.getAbsolutePath().hashCode());
    }


}
