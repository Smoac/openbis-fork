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

package ch.systemsx.cisd.imagereaders.bioformats;

import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;

import javax.media.jai.widget.ScrollingImagePanel;

import ch.systemsx.cisd.imagereaders.IImageReader;
import ch.systemsx.cisd.imagereaders.TiffReadParams;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class BioFormatsImageViewer
{
    public static void main(String[] args)
    {
        if (args.length == 0)
        {
            FileDialog fileDialog = new FileDialog((Frame) null);
            fileDialog.setMode(FileDialog.LOAD);
            fileDialog.setVisible(true);
            String dir = fileDialog.getDirectory();
            args = new String[] {dir + "/" + fileDialog.getFile()};
        }
        for (String fileName : args)
        {
            IImageReader reader = new BioFormatsReaderLibrary().tryGetReaderForFile(fileName);
            
            System.out.println("=========== File: " + fileName);
            System.out.println("Reader: " + reader);
            File file = new File(fileName);
            TiffReadParams readParams = new TiffReadParams(0);
            readParams.setIntensityRescalingChannel(0);
            final BufferedImage image = reader.readImage(file, readParams);
            showImage(image, fileName);
            
        }
    }

    private static void showImage(final RenderedImage image, String fileName)
    {
        final Frame frame = new Frame("Image: " + fileName);
        final ScrollingImagePanel panel =
                new ScrollingImagePanel(image, image.getWidth() + 10, image.getHeight() + 10);
        frame.add(panel);
        frame.setLocationByPlatform(true);
        frame.pack();
        frame.setVisible(true);
    }
}
