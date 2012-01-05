/*
 * Copyright 2012 ETH Zuerich, CISD
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

import java.awt.Dimension;
import java.awt.image.BufferedImage;

import ch.systemsx.cisd.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageLibraryInfo;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.Size;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ImageUtil;

/**
 * Collection of helper functions.
 *
 * @author Franz-Josef Elmer
 */
public class Utils
{

    public static BufferedImage loadUnchangedImage(IHierarchicalContentNode contentNode,
            String imageIdOrNull, ImageLibraryInfo imageLibraryOrNull)
    {
        String imageLibraryNameOrNull = null;
        String imageLibraryReaderNameOrNull = null;
        if (imageLibraryOrNull != null)
        {
            imageLibraryNameOrNull = imageLibraryOrNull.getName();
            imageLibraryReaderNameOrNull = imageLibraryOrNull.getReaderName();
        }
        return ImageUtil.loadUnchangedImage(contentNode, imageIdOrNull, imageLibraryNameOrNull,
                imageLibraryReaderNameOrNull, null);
    }

    public static Size loadUnchangedImageSize(IHierarchicalContentNode contentNode,
            String imageIdOrNull, ImageLibraryInfo imageLibraryOrNull)
    {
        String imageLibraryNameOrNull = null;
        String imageLibraryReaderNameOrNull = null;
        if (imageLibraryOrNull != null)
        {
            imageLibraryNameOrNull = imageLibraryOrNull.getName();
            imageLibraryReaderNameOrNull = imageLibraryOrNull.getReaderName();
        }
        Dimension dimension = ImageUtil.loadUnchangedImageDimension(contentNode, imageIdOrNull,
                imageLibraryNameOrNull, imageLibraryReaderNameOrNull);
        return new Size(dimension.width, dimension.height);
    }

}
