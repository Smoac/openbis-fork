/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.client.application.utils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Image;

import ch.systemsx.cisd.cifex.client.application.ICIFEXImageBundle;

/**
 * A static class to access the images.
 * <p>
 * On purpose no cache is applied here: everytime you need an image you must create a new instance
 * of <code>Image</code> and not reuse it.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class ImageUtils
{
    private final static String IMG_DIR = "images/";

    public static final ICIFEXImageBundle ICONS = GWT.create(ICIFEXImageBundle.class);

    private ImageUtils()
    {
        // Can not be instantiated
    }

    private static final Image createImage(final String fileName)
    {
        return new Image(IMG_DIR + fileName);
    }

    public final static Image getCIFEXSymbolImage()
    {
        return createImage("cifex_symbol.png");
    }
    
    public final static Image getCIFEXLogoImage()
    {
        return createImage("cifex.png");
    }

    public final static Image getCIFEXLogoImageSmall()
    {
        return createImage("cifex_small.png");
    }

}
