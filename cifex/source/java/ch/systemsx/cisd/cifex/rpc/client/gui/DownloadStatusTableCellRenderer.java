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

import ch.systemsx.cisd.cifex.rpc.client.gui.FileDownloadClient.IDecryptionChecker;
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

    private final JLabel decryptingLabel = new JLabel("Decrypting\u2026");

    private final JPanel decryptingPanel = new JPanel();

    private final JButton retryButton = new JButton("Retry Download");

    private final JLabel retryLabel = new JLabel("<html><i>Download Failed</i></html>");

    private final JPanel retryPanel = new JPanel();

    private final JLabel completedLabel = new JLabel("Finished");

    private final JPanel completedPanel = new JPanel();

    private final JButton decryptButton = new JButton("Decrypt");

    private final JLabel completedDownloadLabel = new JLabel("Finished Download");

    private final JPanel completedDownloadPanel = new JPanel();

    private final IDecryptionChecker decryptionChecker;

    public DownloadStatusTableCellRenderer(FileDownloadClientModel tableModel)
    {
        super();
        this.decryptionChecker = tableModel.getDecryptionChecker();
        createDownloadPanel();
        createProgressPanel();
        createRetryPanel();
        createDecryptingPanel();
        createCompletedPanel();
        createCompletedNotDecryptedPanel();
    }

    private void createCompletedPanel()
    {
        completedPanel.setLayout(new GridLayout(1, 0));
        completedLabel.setFont(completedLabel.getFont().deriveFont(Font.PLAIN));
        completedPanel.add(completedLabel);
        completedPanel.setOpaque(true);
    }

    private void createCompletedNotDecryptedPanel()
    {
        completedDownloadPanel.setLayout(new GridLayout(2, 0));
        completedDownloadLabel.setFont(completedDownloadLabel.getFont()
                .deriveFont(Font.PLAIN));
        completedDownloadPanel.add(completedDownloadLabel);
        completedDownloadPanel.add(decryptButton);
        completedDownloadPanel.setOpaque(true);
    }

    private void createDecryptingPanel()
    {
        decryptingPanel.setLayout(new GridLayout(1, 0));
        decryptingPanel.setFont(decryptingLabel.getFont().deriveFont(Font.PLAIN));
        decryptingPanel.add(decryptingLabel);
        decryptingPanel.setOpaque(true);
    }

    private void createRetryPanel()
    {
        retryPanel.setLayout(new GridLayout(2, 0));
        retryLabel.setFont(retryLabel.getFont().deriveFont(Font.PLAIN));
        retryPanel.add(retryLabel);
        retryPanel.add(retryButton);
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
        {
            return null;
        }

        final Color backgroundColor =
                (isSelected) ? table.getSelectionBackground() : table.getBackground();

        final JPanel panel;
        switch (fileInfo.getStatus())
        {
            case TO_DOWNLOAD:
                panel = downloadPanel;
                break;
            // Queued_for_Decryption and Decrypting have the same logic
            case QUEUED_FOR_DECRYPTION:
            case DECRYPTING:
                panel = decryptingPanel;
                break;
            case COMPLETED_DOWNLOAD_AND_DECRYPTION:
                panel = completedPanel;
                break;
            case COMPLETED_DOWNLOAD:
                panel =
                        decryptionChecker.willDecrypt() ? completedDownloadPanel
                                : completedPanel;
                break;
            // Queued_for_Download and Downloading have the same logic
            case QUEUED_FOR_DOWNLOAD:
            case DOWNLOADING:
                panel = progressPanel;
                progressBar.setValue(fileInfo.getPercentageDownloaded());
                progressLabel.setText(getAmountDownloadedStringForFileInfo(fileInfo, false));
                break;
            case STALLED:
                panel = progressPanel;
                progressBar.setValue(fileInfo.getPercentageDownloaded());
                progressLabel.setText(getAmountDownloadedStringForFileInfo(fileInfo, true));
                break;
            case FAILED:
                panel = retryPanel;
                break;
            default:
                throw new RuntimeException("Unknown status: " + fileInfo.getStatus());
        }

        panel.setBackground(backgroundColor);
        return panel;
    }

    String getAmountDownloadedStringForFileInfo(FileDownloadInfo fileInfo, boolean stalled)
    {
        StringBuffer sb = new StringBuffer();
        String eta = DateTimeUtils.renderDuration(fileInfo.getEstimatedTimeOfArrival());
        sb.append(fileInfo.getPercentageDownloaded());
        if (stalled)
        {
            sb.append("% (stalled)");
        } else
        {
            sb.append("% (remaining: ");
            sb.append(eta);
            sb.append(")");
        }
        return sb.toString();
    }

}
