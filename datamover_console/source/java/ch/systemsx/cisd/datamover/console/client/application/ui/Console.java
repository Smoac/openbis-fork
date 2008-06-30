/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.datamover.console.client.application.ui;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.datamover.console.client.application.ViewContext;
import ch.systemsx.cisd.datamover.console.client.dto.DatamoverInfo;
import ch.systemsx.cisd.datamover.console.client.dto.DatamoverStatus;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class Console extends Composite
{
    private static final int MEGA = 1024;
    private static final int GIGA = 1024 * 1024;
    
    private static final String STYLE_PREFIX = "console-";
    private static final String STYLE_HEADER_PREFIX = STYLE_PREFIX + "header-";
    private static final String STYLE_TABLE_PREFIX = STYLE_PREFIX + "table-";
    
    private final ViewContext viewContext;
    private final VerticalPanel content;

    public Console(ViewContext viewContext)
    {
        this.viewContext = viewContext;
        VerticalPanel panel = new VerticalPanel();
        panel.setStyleName(STYLE_PREFIX + "main");
        panel.add(createHeaderPanel());
        content = new VerticalPanel();
        content.setStyleName(STYLE_PREFIX + "content");
        panel.add(content);
        initWidget(panel);
        refreshView();
    }

    private Widget createHeaderPanel()
    {
        DockPanel headerPanel = new DockPanel();
        headerPanel.setStyleName(STYLE_HEADER_PREFIX + "panel");
        
        Label label = new Label(viewContext.getModel().getUser().getUserFullName());
        label.setStyleName(STYLE_HEADER_PREFIX + "label");
        headerPanel.add(label, DockPanel.WEST);
        
        headerPanel.add(createHeaderButton("logout", new ClickListener()
            {
                public void onClick(Widget arg0)
                {
                    viewContext.getService().logout(new AsyncCallback<Void>()
                        {
                            public void onSuccess(Void v)
                            {
                                viewContext.getPageController().reload();
                            }
                            public void onFailure(Throwable t)
                            {
                                // TODO Auto-generated method stub
                            }
                        });
                }
            }), DockPanel.WEST);
        
        headerPanel.add(createHeaderButton("refresh", new ClickListener()
        {
            public void onClick(Widget arg0)
            {
                refreshView();
            }
        }), DockPanel.EAST);
        return headerPanel;
    }
    
    private Button createHeaderButton(String label, ClickListener listener)
    {
        Button button = new Button(label);
        button.setStyleName(STYLE_HEADER_PREFIX + "button");
        if (listener != null)
        {
            button.addClickListener(listener);
        }
        return button;
    }

    private void refreshView()
    {
        content.clear();
        content.add(new Label("Data will be loaded. Please wait."));
        viewContext.getService().listDatamoverInfos(new AsyncCallback<List<DatamoverInfo>>()
            {
        
                public void onSuccess(List<DatamoverInfo> list)
                {
                    content.clear();
                    content.add(createView(list));
                }
        
                public void onFailure(Throwable arg0)
                {
                    // TODO Auto-generated method stub
        
                }
        
            });
    }
    
    private Widget createView(List<DatamoverInfo> list)
    {
        Grid grid = new Grid(list.size() + 1, 5);
        grid.setStyleName(STYLE_TABLE_PREFIX + "table");
        grid.setText(0, 0, "Datamover");
        grid.setText(0, 1, "Target Location");
        grid.setText(0, 2, "Minimum Disk Space needed (in MB)");
        grid.setText(0, 3, "Status");
        grid.setText(0, 4, "Command");
        grid.getRowFormatter().setStyleName(0, STYLE_TABLE_PREFIX + "header");
        for (int i = 0, n = list.size(); i < n; i++)
        {
            DatamoverInfo datamoverInfo = list.get(i);
            createRow(grid, datamoverInfo, i + 1);
            grid.getRowFormatter().setStyleName(i + 1,
                    STYLE_TABLE_PREFIX + (i % 2 == 0 ? "odd-row" : "even-row"));
        }
        return grid;
    }

    private void createRow(Grid grid, DatamoverInfo datamoverInfo, int rowIndex)
    {
        grid.setText(rowIndex, 0, datamoverInfo.getName());
        grid.setText(rowIndex, 1, datamoverInfo.getTargetLocation());
        DatamoverStatus status = datamoverInfo.getStatus();
        grid.setText(rowIndex, 3, status.toString());
        if (status == DatamoverStatus.DOWN || status == DatamoverStatus.STALE)
        {
            grid.setWidget(rowIndex, 2, createWatermarkLevelListBox());
            Button button = new Button("start");
            button.setStyleName(STYLE_TABLE_PREFIX + "button");
            grid.setWidget(rowIndex, 4, button);
        } else
        {
            grid.setText(rowIndex, 2, renderInBytes(datamoverInfo.getWatermarkLevel()));
            if (status != DatamoverStatus.SHUTDOWN)
            {
                Button button = new Button("stop");
                button.setStyleName(STYLE_TABLE_PREFIX + "button");
                grid.setWidget(rowIndex, 4, button);
            }
        }
    }

    private ListBox createWatermarkLevelListBox()
    {
        ListBox listBox = new ListBox();
        for (int i = 0, base = 1; i < 3; i++, base *= 1024)
        {
            for (int level = base; level < 1024 * base; level *= 10)
            {
                addItemTo(listBox, level);
                addItemTo(listBox, 3 * level);
            }
        }
        return listBox;
    }

    private void addItemTo(ListBox listBox, int level)
    {
        listBox.addItem(renderInBytes(level), Integer.toString(level));
    }
    
    private String renderInBytes(int kiloBytes)
    {
        if (kiloBytes < MEGA)
        {
            return Integer.toString(kiloBytes) + "KB";
        }
        if (kiloBytes < GIGA)
        {
            return Integer.toString((kiloBytes + 512) / 1024) + "MB";
        }
        return Integer.toString((kiloBytes + 512 * 1024) / (1024 * 1024)) + "GB";
    }

}
