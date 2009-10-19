/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ImageUtil
{
    public static BufferedImage loadImage(File file)
    {
        if (file.exists() == false)
        {
            throw new IllegalArgumentException("File does not exist: " + file.getAbsolutePath());
        }
        try
        {
            PlanarImage planarImage = JAI.create("fileload", file.getAbsolutePath());
            return planarImage.getAsBufferedImage();
        } catch (RuntimeException ex)
        {
            throw new IllegalArgumentException("Isn't a valid image file: " + file.getAbsolutePath());
        }
    }
    
    public static BufferedImage createThumbnail(BufferedImage image, int maxWidth, int maxHeight)
    {
        int width = image.getWidth();
        int height = image.getHeight();
        double widthScale = maxWidth / (double) width;
        double heightScale = maxHeight / (double) height;
        double scale = Math.min(1, Math.min(widthScale, heightScale));
        int thumbnailWidth = (int) (scale * width + 0.5);
        int thumbnailHeight = (int) (scale * height + 0.5);
        
        BufferedImage thumbnail =
                new BufferedImage(thumbnailWidth, thumbnailHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = thumbnail.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.drawImage(image, 0, 0, thumbnailWidth, thumbnailHeight, null);
        return thumbnail;
    }
}
