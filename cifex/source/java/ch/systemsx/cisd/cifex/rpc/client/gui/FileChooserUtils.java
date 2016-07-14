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

package ch.systemsx.cisd.cifex.rpc.client.gui;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;

import javax.swing.JFileChooser;

import ch.systemsx.cisd.base.utilities.OSUtilities;

/**
 * Utilities for choosing files and directories in the GUI.
 * 
 * @author Bernd Rinn
 */
public class FileChooserUtils
{

    public enum FileChooserMode
    {
        File, Files, Directory
    }

    /**
     * Let the user choose a file (<code>chooseDirectories=false</code>) or directory ( <code>chooseDirectories=true</code>). Start the selection
     * process in <var>initialDirectory</var>. The windows will be shown relative to <var>parentFrame</var>.
     * 
     * @return The new file or directory if the user approved the selection or <code>null</code> if the user cancelled the selection.
     */
    public static File[] tryChooseFile(Frame parentFrame, File initialDirectory,
            FileChooserMode mode)
    {
        if (OSUtilities.isMacOS())
        {
            if (mode == FileChooserMode.Directory)
            {
                System.setProperty("apple.awt.fileDialogForDirectories", "true");
            }
            final FileDialog fileChooser = new FileDialog(parentFrame, getTitle(mode));
            if (mode == FileChooserMode.Files)
            {
                fileChooser.setMultipleMode(true);
            }
            fileChooser.setModal(true);
            fileChooser.setMode(FileDialog.LOAD);
            fileChooser.setDirectory(initialDirectory.getAbsolutePath());
            fileChooser.setVisible(true);
            final File[] newFiles = fileChooser.getFiles();
            if (mode == FileChooserMode.Directory)
            {
                System.setProperty("apple.awt.fileDialogForDirectories", "false");
            }
            return newFiles;
        } else
        {
            final JFileChooser fileChooser = new JFileChooser(initialDirectory);
            if (mode == FileChooserMode.Files)
            {
                fileChooser.setMultiSelectionEnabled(true);
            }
            fileChooser.setFileSelectionMode(mode == FileChooserMode.Directory ? JFileChooser.DIRECTORIES_ONLY
                    : JFileChooser.FILES_ONLY);
            fileChooser.setDialogTitle(getTitle(mode));
            final int returnVal = fileChooser.showOpenDialog(parentFrame);
            if (returnVal == JFileChooser.APPROVE_OPTION)
            {
                return fileChooser.getSelectedFiles();
            } else
            {
                return null;
            }
        }

    }

    private static String getTitle(FileChooserMode mode)
    {
        switch (mode)
        {
            case File:
                return "Select a file";
            case Files:
                return "Select one or more files";
            case Directory:
                return "Select a directory";
            default:
                return null;
        }
    }
}
