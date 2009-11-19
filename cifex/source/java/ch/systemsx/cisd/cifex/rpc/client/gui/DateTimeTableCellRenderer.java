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
import java.text.DateFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Show date and time information in a table cell.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DateTimeTableCellRenderer implements TableCellRenderer
{
    private final JLabel dateLabel = new JLabel();

    private final JLabel timeLabel = new JLabel();

    private final JPanel panel = new JPanel();

    DateFormat dateFormatter;

    DateFormat timeFormatter;

    public DateTimeTableCellRenderer()
    {
        super();

        timeLabel.setFont(dateLabel.getFont().deriveFont(Font.PLAIN));
        panel.setLayout(new GridLayout(2, 0));
        panel.add(dateLabel);
        panel.add(timeLabel);
        panel.setOpaque(true);

        dateFormatter = DateFormat.getDateInstance(DateFormat.MEDIUM);
        timeFormatter = DateFormat.getTimeInstance(DateFormat.MEDIUM);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column)
    {
        Color backgroundColor =
                (isSelected) ? table.getSelectionBackground() : table.getBackground();
        panel.setBackground(backgroundColor);
        dateLabel.setText(dateFormatter.format(value));
        timeLabel.setText(timeFormatter.format(value));
        return panel;
    }
}
