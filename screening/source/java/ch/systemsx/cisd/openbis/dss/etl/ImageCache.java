/*
 * Copyright 2015 ETH Zuerich, SIS
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

import java.awt.image.BufferedImage;
import java.util.Map;

import org.apache.commons.collections.map.ReferenceMap;

import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageLibraryInfo;

/**
 * An image cache based on {@link ReferenceMap}. 
 * All entries with soft value references are removed when free heap space is needed.
 *
 * @author Franz-Josef Elmer
 */
public class ImageCache
{
    private final Map<String, BufferedImage> images = new ReferenceMap<String, BufferedImage>();

    /**
     * Returns or loads the image specified by file name, identifier and loading library.
     */
    public synchronized BufferedImage getImage(IHierarchicalContentNode contentNode, 
            String imageIdOrNull, ImageLibraryInfo imageLibraryOrNull)
    {
        String key = contentNode.getName() + ":" + imageIdOrNull + " [" + imageLibraryOrNull + "]";
        BufferedImage image = images.get(key);
        if (image == null)
        {
            image = Utils.loadUnchangedImage(contentNode, imageIdOrNull, imageLibraryOrNull);
            images.put(key, image);
        }
        return image;
    }
    
    public int size()
    {
        return images.size();
    }
}
