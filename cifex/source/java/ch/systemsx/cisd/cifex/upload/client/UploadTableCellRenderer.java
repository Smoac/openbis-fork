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

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import ch.systemsx.cisd.common.utilities.DateTimeUtils;

/**
 * @author Franz-Josef Elmer
 */
final class UploadTableCellRenderer implements TableCellRenderer
{
    private JProgressBar progressBar = new JProgressBar();

    private JLabel cell = new JLabel();

    public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
            boolean hasFocus, int row, int columnIndex)
    {
        if (value instanceof FileItem)
        {
            FileItem fileItem = (FileItem) value;
            FileItemStatus status = fileItem.getStatus();
            switch (status)
            {
                case NOT_STARTED:
                    return renderText("");
                case UPLOADING:
                    return renderProgressBar(fileItem);
                case FINISHED:
                    return renderText("Sucessfully uploaded");
                case ABORTED:
                    return renderText("Uploading aborted");
            }
        }
        return renderText(String.valueOf(value));
    }

    private Component renderProgressBar(FileItem fileItem)
    {
        long percentage =
                100 * fileItem.getNumberOfBytesUploaded() / Math.max(1, fileItem.getLength());
        progressBar.setValue((int) percentage);
        progressBar.setStringPainted(true);
        String eta = DateTimeUtils.renderDuration(fileItem.getEstimatedTimeOfArrival());
        progressBar.setString(percentage + "% (ETA: " + eta + ")");
        return progressBar;
    }

    private Component renderText(String text)
    {
        cell.setText(text);
        return cell;
    }
}