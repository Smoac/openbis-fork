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
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.datamover.console.client.application.AbstractAsyncCallback;
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
    
    private final AsyncCallback<Void> refreshCallBack;
        
    private List<String> targets;

    public Console(ViewContext viewContext)
    {
        this.viewContext = viewContext;
        VerticalPanel panel = new VerticalPanel();
        panel.setStyleName(STYLE_PREFIX + "main");
        panel.add(createTitle());
        panel.add(createHeaderPanel());
        content = new VerticalPanel();
        content.setStyleName(STYLE_PREFIX + "content");
        panel.add(content);
        initWidget(panel);
        viewContext.getService().getTargets(new AbstractAsyncCallback<List<String>>(viewContext)
            {
                public void onSuccess(List<String> list)
                {
                    targets = list;
                    refreshView();
                }
            });
        refreshCallBack = new AbstractAsyncCallback<Void>(viewContext)
            {
                public void onSuccess(Void arg0)
                {
                    refreshView();
                }
            };
    }
    
    private Widget createTitle()
    {
        HorizontalPanel titlePanel = new HorizontalPanel();
        titlePanel.add(viewContext.getImageBundle().getLogo().createImage());
        Label label = new Label(viewContext.getMessageResources().getConsoleTitle());
        label.setStyleName(STYLE_PREFIX + "title");
        titlePanel.add(label);
        return titlePanel;
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
                    viewContext.getService().logout(new AbstractAsyncCallback<Void>(viewContext)
                        {
                            public void onSuccess(Void v)
                            {
                                viewContext.getPageController().reload();
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
        showWaitMessage();
        viewContext.getService().listDatamoverInfos(
                new AbstractAsyncCallback<List<DatamoverInfo>>(viewContext)
                    {
                        public void onSuccess(List<DatamoverInfo> list)
                        {
                            content.clear();
                            content.add(createView(list));
                        }
                    });
    }

    private void showWaitMessage()
    {
        content.clear();
        content.add(new Label("Data will be loaded. Please wait."));
    }
    
    private Widget createView(List<DatamoverInfo> list)
    {
        Grid grid = new Grid(list.size() + 1, 5);
        grid.setStyleName(STYLE_TABLE_PREFIX + "table");
        grid.setText(0, 0, "Datamover");
        grid.setText(0, 1, "Target Location");
        grid.setText(0, 2, "Minimum Target Disk Space needed");
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

    private void createRow(Grid grid, final DatamoverInfo datamoverInfo, int rowIndex)
    {
        grid.setText(rowIndex, 0, datamoverInfo.getName());
        DatamoverStatus status = datamoverInfo.getStatus();
        grid.setText(rowIndex, 3, status.toString());
        if (status == DatamoverStatus.DOWN || status == DatamoverStatus.STALE)
        {
            final ListBox targetListBox = createTargetListBox();
            final ListBox levelListBox = createLevelListBox();
            grid.setWidget(rowIndex, 1, targetListBox);
            grid.setWidget(rowIndex, 2, levelListBox);
            Button button = createTableButton("start", new ClickListener()
                {
                    public void onClick(Widget arg0)
                    {
                        startDatamover(datamoverInfo.getName(), targetListBox, levelListBox);
                    }
                });
            grid.setWidget(rowIndex, 4, button);
        } else
        {
            grid.setText(rowIndex, 1, datamoverInfo.getTargetLocation());
            grid.setText(rowIndex, 2, renderInBytes(datamoverInfo.getWatermarkLevel()));
            if (status != DatamoverStatus.SHUTDOWN)
            {
                Button button = createTableButton("stop", new ClickListener()
                    {
                        public void onClick(Widget arg0)
                        {
                            stopDatamover(datamoverInfo.getName());
                        }
                    });
                grid.setWidget(rowIndex, 4, button);
            }
        }
    }
    
    private void startDatamover(String name, ListBox targetListBox, ListBox levelListBox)
    {
        String target = getSelectedValueOf(targetListBox);
        String level = getSelectedValueOf(levelListBox);
        viewContext.getService().startDatamover(name, target, level, refreshCallBack);
    }
    
    private String getSelectedValueOf(ListBox listBox)
    {
        return listBox.getValue(listBox.getSelectedIndex());
    }
    
    private void stopDatamover(String name)
    {
        viewContext.getService().stopDatamover(name, refreshCallBack);
    }

    private Button createTableButton(String label, ClickListener clickListener)
    {
        Button button = new Button(label);
        button.setStyleName(STYLE_TABLE_PREFIX + "button");
        button.addClickListener(clickListener);
        return button;
    }
    
    private ListBox createTargetListBox()
    {
        ListBox list = new ListBox();
        for (String target : targets)
        {
            list.addItem(target, target);
        }
        return list;
    }

    private ListBox createLevelListBox()
    {
        ListBox listBox = new ListBox();
        for (int i = 0, base = 1; i < 3; i++, base *= 1024)
        {
            for (int level = base; level < 101 * base; level *= 10)
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
