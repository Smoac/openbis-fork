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

package ch.systemsx.cisd.cifex.rpc.client.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import ch.systemsx.cisd.cifex.rpc.client.gui.FileDownloadClientModel.FileDownloadInfo;
import ch.systemsx.cisd.common.utilities.DateTimeUtils;

/**
 * Displays a button or download status, depending on whether the file has been downloaded or not.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DownloadStatusTableCellRenderer implements TableCellRenderer
{
    private final JButton downloadButton = new JButton("Download");

    private final JPanel downloadPanel = new JPanel();

    private final JProgressBar progressBar = new JProgressBar();

    private final JLabel progressLabel = new JLabel();

    private final JPanel progressPanel = new JPanel();

    private final JButton retryButton = new JButton("Retry");

    private final JLabel retryLabel = new JLabel("Could not download. Please retry.");

    private final JPanel retryPanel = new JPanel();

    private final JLabel completedLabel = new JLabel("Finished.");

    private final JPanel completedPanel = new JPanel();

    public DownloadStatusTableCellRenderer(FileDownloadClientModel tableModel)
    {
        super();
        createDownloadPanel();
        createProgressPanel();
        createRetryPanel();
        createCompletedPanel();
    }

    private void createCompletedPanel()
    {
        completedPanel.setLayout(new GridLayout(1, 0));
        completedLabel.setFont(completedLabel.getFont().deriveFont(Font.PLAIN));
        completedPanel.add(completedLabel);
        completedPanel.setOpaque(true);
    }

    private void createRetryPanel()
    {
        retryPanel.setLayout(new GridLayout(2, 0));
        retryPanel.add(retryButton);
        retryLabel.setFont(retryLabel.getFont().deriveFont(Font.PLAIN));
        retryPanel.add(retryLabel);
        retryPanel.setOpaque(true);
    }

    private void createProgressPanel()
    {
        progressPanel.setLayout(new GridLayout(2, 0));
        progressPanel.add(progressBar);
        progressLabel.setFont(progressLabel.getFont().deriveFont(Font.PLAIN));
        progressPanel.add(progressLabel);
        progressPanel.setOpaque(true);
    }

    private void createDownloadPanel()
    {
        downloadPanel.setLayout(new GridLayout(1, 0));
        downloadPanel.add(downloadButton);
        downloadPanel.setOpaque(true);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column)
    {
        FileDownloadInfo fileInfo = (FileDownloadInfo) value;
        if (null == fileInfo)
            return null;

        Color backgroundColor =
                (isSelected) ? table.getSelectionBackground() : table.getBackground();

        JPanel panel = null;
        switch (fileInfo.getStatus())
        {
            case TO_DOWNLOAD:
                panel = downloadPanel;
                break;
            case COMPLETED:
                panel = completedPanel;
                break;
            // Queued and Downloading have the same logic
            case QUEUED:
            case DOWNLOADING:
                panel = progressPanel;
                progressBar.setValue(fileInfo.getPercentageDownloaded());
                // progressBar.setStringPainted(true);
                // progressBar.setString(getAmountDownloadedStringForFileInfo(fileInfo));
                progressLabel.setText(getAmountDownloadedStringForFileInfo(fileInfo));
                break;
            case FAILED:
                panel = retryPanel;
                break;
        }

        panel.setBackground(backgroundColor);
        return panel;
    }

    String getAmountDownloadedStringForFileInfo(FileDownloadInfo fileInfo)
    {
        StringBuffer sb = new StringBuffer();
        // sb.append(FileDetailsTableCellRenderer.numberOfBytesToDisplayString(fileInfo
        // .getNumberOfBytesDownloaded()));
        // sb.append(" of ");
        // sb.append(FileDetailsTableCellRenderer.numberOfBytesToDisplayString(fileInfo
        // .getFileInfoDTO().getSize()));
        String eta = DateTimeUtils.renderDuration(fileInfo.getEstimatedTimeOfArrival());
        sb.append(fileInfo.getPercentageDownloaded());
        sb.append("% (ETA: ");
        sb.append(eta);
        sb.append(")");
        return sb.toString();
    }

}
