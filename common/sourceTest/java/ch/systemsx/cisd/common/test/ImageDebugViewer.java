/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.test;

import java.awt.BorderLayout;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.color.ColorSpace;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import ch.systemsx.cisd.common.image.ImageHistogram;


/**
 * Helper class to view {@link BufferedImage} instances when debugging.
 *
 * @author felmer
 */
public class ImageDebugViewer
{
    public static boolean debug = true;
    
    public static void view(String title, final BufferedImage image)
    {
        if (debug == false)
        {
            return;
        }
        if (image == null)
        {
            System.out.println(title + ": no image");
            return;
        }
        printDebugInfo(title, image);
        ImageIcon imageIcon = new ImageIcon(image);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(title), BorderLayout.NORTH);
        panel.add(new JLabel(imageIcon), BorderLayout.CENTER);
        StringBuilder builder = new StringBuilder(image.toString());
        ColorSpace colorSpace = image.getColorModel().getColorSpace();
        int numComponents = colorSpace.getNumComponents();
        for (int i = 0; i < numComponents; i++)
        {
            builder.append('\n').append(colorSpace.getName(i)).append(':');
            builder.append(colorSpace.getMinValue(i)).append('-').append(colorSpace.getMaxValue(i));
        }
        panel.add(new JLabel(builder.toString()), BorderLayout.SOUTH);
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent arg0)
                {
                    FileDialog fileDialog = new FileDialog((Frame) null, "Save image", FileDialog.SAVE);
                    fileDialog.setVisible(true);
                    String fileName = fileDialog.getFile();
                    if (fileName == null)
                    {
                        return;
                    }
                    String directory = fileDialog.getDirectory();
                    File file = new File(directory, fileName);
                    try
                    {
                        boolean success = ImageIO.write(image, "png", file);
                        if (success)
                        {
                            System.out.println("image saved to " + file);
                        } else
                        {
                            throw new IOException("Couldn't save image to file " + file);
                        }
                    } catch (IOException ex)
                    {
                        JOptionPane.showMessageDialog(null, ex.toString());
                    }
                }
            });
        panel.add(saveButton, BorderLayout.EAST);
        JOptionPane.showMessageDialog(null, panel);
    }
    
    private static void printDebugInfo(String title, BufferedImage image)
    {
        System.out.println("/--------- " + title);
        System.out.println(image);
        System.out.println("accelartion priority: " + image.getAccelerationPriority());
        String[] propertyNames = image.getPropertyNames();
        if (propertyNames != null)
        {
            for (String propertyName : propertyNames)
            {
                System.out.println(propertyName + ": " + image.getProperty(propertyName));
            }
        }
        ColorModel colorModel = image.getColorModel();
        System.out.println("color model: " + colorModel);
        ColorSpace colorSpace = colorModel.getColorSpace();
        System.out.println("color space: " + colorSpace);
        for (int i = 0; i < colorSpace.getNumComponents(); i++)
        {
            System.out.println("color space: " + colorSpace.getName(i) 
                    + ": [" + colorSpace.getMinValue(i) + ", " + colorSpace.getMaxValue(i) + "]");
        }
        ImageHistogram histogram = ImageHistogram.calculateHistogram(image);
        System.out.println(histogram);
        System.out.println(Arrays.toString(histogram.getRedHistogram()));
        System.out.println("\\_________ " + title);
    }


}
