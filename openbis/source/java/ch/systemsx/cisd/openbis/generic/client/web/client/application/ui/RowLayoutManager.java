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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

/**
 * Helper class for doing {@link RowLayout} of a {@link LayoutContainer} with instances of
 * {@link ContentPanel} in a dynamic way. Collapsed row components lead to a resize of the
 * non-collapsed components if the row size is defined by a positive value &lt;= 1.
 * 
 * @author Franz-Josef Elmer
 */
public class RowLayoutManager
{
    private static interface IManipulator
    {
        double getFor(RowData rowData);

        void setFor(RowData rowData, double newValue);

        int getFrameSizeFor(ContentPanel panel);
    }

    private static final class DynamicRowData
    {
        private RowData rowData;

        private double originalValue;

        private ContentPanel panel;
    }

    private static final IManipulator HEIGHT_MANIPULATOR = new IManipulator()
        {
            public void setFor(RowData rowData, double newValue)
            {
                rowData.setHeight(newValue);
            }

            public double getFor(RowData rowData)
            {
                return rowData.getHeight();
            }

            public int getFrameSizeFor(ContentPanel panel)
            {
                return panel.isRendered() ? panel.getFrameHeight() : 0;
            }
        };

    private static final IManipulator WIDTH_MANIPULATOR = new IManipulator()
        {
            public void setFor(RowData rowData, double newValue)
            {
                rowData.setWidth(newValue);
            }

            public double getFor(RowData rowData)
            {
                return rowData.getWidth();
            }

            public int getFrameSizeFor(ContentPanel panel)
            {
                return panel.isRendered() ? panel.getFrameWidth() : 0;
            }
        };

    private final LayoutContainer container;

    private final IManipulator manipulator;

    private final List<DynamicRowData> dynamicRowDatas = new ArrayList<DynamicRowData>();

    /**
     * Creates an instance for the specified container and apply the specified layout.
     */
    public RowLayoutManager(LayoutContainer container, RowLayout rowLayout)
    {
        this.container = container;
        container.setLayout(rowLayout);
        Orientation orientation = rowLayout.getOrientation();
        if (orientation.equals(Orientation.VERTICAL))
        {
            manipulator = HEIGHT_MANIPULATOR;
        } else
        {
            manipulator = WIDTH_MANIPULATOR;
        }
    }

    /**
     * Adds the specified panel to the container in accordance to the specified row data.
     */
    public void addToContainer(ContentPanel contentPanel, RowData rowData)
    {
        contentPanel.setAnimCollapse(false);
        container.add(contentPanel, rowData);
        double value = manipulator.getFor(rowData);
        if (value < 0 || value > 1)
        {
            return;
        }
        DynamicRowData dynamicRowData = new DynamicRowData();
        dynamicRowData.rowData = rowData;
        dynamicRowData.originalValue = value;
        dynamicRowData.panel = contentPanel;
        dynamicRowDatas.add(dynamicRowData);
        adjustRelative();
        Listener<BaseEvent> listener = new Listener<BaseEvent>()
            {
                public void handleEvent(BaseEvent event)
                {
                    adjustRelative();
                }
            };
        contentPanel.addListener(Events.Collapse, listener);
        contentPanel.addListener(Events.Expand, listener);
    }

    private void adjustRelative()
    {
        double sum = 0;
        for (DynamicRowData data : dynamicRowDatas)
        {
            if (data.panel.isCollapsed() == false)
            {
                sum += data.originalValue;
            }
        }
        for (DynamicRowData data : dynamicRowDatas)
        {
            double value;
            if (data.panel.isCollapsed())
            {
                value = manipulator.getFrameSizeFor(data.panel);
            } else
            {
                value = data.originalValue / sum;
            }
            manipulator.setFor(data.rowData, value);
        }
        container.layout(true);
    }
}