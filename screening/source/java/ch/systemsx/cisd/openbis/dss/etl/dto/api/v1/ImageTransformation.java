/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.etl.dto.api.v1;

import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeNormalizer;

/**
 * Describes image transformation, contains user friendly label and description.
 * 
 * @author Tomasz Pylak
 */
public class ImageTransformation
{
    private final String code;

    private final String label;

    // can be null
    private final String description;

    private final boolean isEditable;

    private final IImageTransformerFactory imageTransformerFactory;

    public ImageTransformation(String code, String label, String description,
            IImageTransformerFactory imageTransformerFactory)
    {
        assert code != null : "code is null";
        assert label != null : " label is null";
        assert imageTransformerFactory != null : "imageTransformerFactory is null";

        this.code = CodeNormalizer.normalize(code);
        this.label = label;
        this.description = description;
        this.isEditable = false; // will be used later for ImageViewer transformations
        this.imageTransformerFactory = imageTransformerFactory;
    }

    public String getCode()
    {
        return code;
    }

    public String getLabel()
    {
        return label;
    }

    public String getDescription()
    {
        return description;
    }

    public boolean isEditable()
    {
        return isEditable;
    }

    public IImageTransformerFactory getImageTransformerFactory()
    {
        return imageTransformerFactory;
    }

}
