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

package ch.systemsx.cisd.openbis.dss.client.api.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.util.Formatter;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import ch.systemsx.cisd.openbis.dss.client.api.gui.DataSetUploadClientModel.NewDataSetInfo;

/**
 * Shows file name and file size in a table cell
 * 
 * @author Chandrasekhar Ramakrishnan
 */

public class FileDetailsTableCellRenderer implements TableCellRenderer
{
    // Constants for computing the size of a file in the right units
    private static final long KB = 1024L;

    private static final long MB = 1024L * KB;

    private final JLabel fileName = new JLabel();

    private final JLabel fileSize = new JLabel();

    private final JPanel panel = new JPanel();

    public FileDetailsTableCellRenderer()
    {
        fileSize.setFont(fileName.getFont().deriveFont(Font.PLAIN));
        panel.setLayout(new GridLayout(2, 0));
        panel.add(fileName);
        panel.add(fileSize);
        panel.setOpaque(true);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column)
    {
        NewDataSetInfo newDataSetInfo = (NewDataSetInfo) value;
        Color backgroundColor =
                (isSelected) ? table.getSelectionBackground() : table.getBackground();
        panel.setBackground(backgroundColor);

        if (null == newDataSetInfo)
        {
            return panel;
        }
        File file = newDataSetInfo.getNewDataSetBuilder().getFile();
        String fileString = (null != file) ? file.getName() : "";
        fileName.setText(fileString);
        fileSize.setText(numberOfBytesToDisplayString(newDataSetInfo.getTotalFileSize()));
        return panel;
    }

    static String numberOfBytesToDisplayString(long numBytes)
    {
        final Formatter f = new Formatter();
        float numKBytes = (float) numBytes / KB;
        if (numKBytes < 1.f)
        {
            f.format("%d bytes", numBytes);
            return f.toString();
        }

        if (numKBytes < 1000.f)
        {
            f.format("%.2f kB", numKBytes);
            return f.toString();
        }

        float numMBytes = (float) numBytes / MB;
        if (numMBytes < 1000.f)
        {
            f.format("%.2f MB", numMBytes);
            return f.toString();
        }

        float numGBytes = numMBytes / KB;
        f.format("%.2f GB", numGBytes);
        return f.toString();
    }
}
