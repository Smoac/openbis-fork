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

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

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

    public DownloadStatusTableCellRenderer(DataSetUploadTableModel tableModel)
    {
        super();
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
        completedDownloadLabel.setFont(completedDownloadLabel.getFont().deriveFont(Font.PLAIN));
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
        final Color backgroundColor =
                (isSelected) ? table.getSelectionBackground() : table.getBackground();

        final JPanel panel;
        switch (0)
        {
            case 0:
                panel = downloadPanel;
                break;
            case 1:
                panel = completedPanel;
                break;
            case 2:
            case 3:
                panel = progressPanel;
                progressBar.setValue(0);
                progressLabel.setText("progress...");
                break;
            case 4:
                panel = retryPanel;
                break;
            default:
                throw new RuntimeException("Unknown status ");
        }

        panel.setBackground(backgroundColor);
        return panel;
    }

}
