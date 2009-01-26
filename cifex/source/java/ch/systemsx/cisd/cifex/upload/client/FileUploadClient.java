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

package ch.systemsx.cisd.cifex.upload.client;

import java.awt.BorderLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;


/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class FileUploadClient
{
    private final String uploadSessionID;
    private final List<File> files;

    FileUploadClient(String uploadSessionID, List<File> files)
    {
        this.uploadSessionID = uploadSessionID;
        this.files = files;
    }
    
    void upload()
    {
        JFrame frame = new JFrame("CIFEX Uploader");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(createGUI(), BorderLayout.CENTER);
        frame.setBounds(200, 200, 800, 600);
        frame.setVisible(true);
    }

    private JPanel createGUI()
    {
        JPanel panel = new JPanel();
        panel.add(new JLabel("upload " + files.size() + " files."));
        return panel;
    }
    
    public static void main(String[] args)
    {
        String uploadSessionID = args[0];
        List<File> files = new ArrayList<File>(args.length - 1);
        for (int i = 1; i < files.size(); i++)
        {
            files.add(new File(args[i]));
        }
        new FileUploadClient(uploadSessionID, files).upload();

    }

}
