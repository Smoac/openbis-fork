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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout.HBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayoutData;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IGenericImageBundle;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionUI;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;

/**
 * @author Tomasz Pylak
 */
public class ColumnDefsAndConfigs<T>
{
    private static final IGenericImageBundle IMAGE_BUNDLE = GWT.<IGenericImageBundle> create(IGenericImageBundle.class);

    private final List<ColumnConfig> columnConfigs;

    private final Set<IColumnDefinition<T>> columnDefs;

    public static <T> ColumnDefsAndConfigs<T> create(
            List<? extends IColumnDefinitionUI<T>> columnsSchema)
    {
        ColumnDefsAndConfigs<T> result = new ColumnDefsAndConfigs<T>();
        result.addColumns(columnsSchema);
        return result;
    }

    private ColumnDefsAndConfigs()
    {
        this.columnConfigs = new ArrayList<ColumnConfig>();
        this.columnDefs = new HashSet<IColumnDefinition<T>>();
    }

    public void addColumns(List<? extends IColumnDefinitionUI<T>> columnsSchema)
    {
        for (IColumnDefinitionUI<T> column : columnsSchema)
        {
            addColumn(column);
        }
    }

    public void addColumn(IColumnDefinitionUI<T> column,
            GridCellRenderer<BaseEntityModel<?>> rendererOrNull)
    {
        ColumnConfig columnConfig = createColumn(column);
        if (rendererOrNull != null)
        {
            columnConfig.setRenderer(rendererOrNull);
        }
        addColumn(column, columnConfig);
    }
    
    private void addColumn(IColumnDefinitionUI<T> column)
    {
        addColumn(column, createColumn(column));
    }

    private void addColumn(IColumnDefinitionUI<T> column, ColumnConfig columnConfig)
    {
        columnConfigs.add(columnConfig);
        columnDefs.add(column);
    }

    public void setGridCellRendererFor(String columnID, GridCellRenderer<BaseEntityModel<?>> render)
    {
        for (ColumnConfig columnConfig : columnConfigs)
        {
            if (columnConfig.getDataIndex().equals(columnID))
            {
                columnConfig.setRenderer(render);
            }
        }
    }

    private static <T> ColumnConfig createColumn(IColumnDefinitionUI<T> column)
    {
        final ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setSortable(true);
        columnConfig.setId(column.getIdentifier());
        final String headerTitle = column.getHeader();
        columnConfig.setHeader(headerTitle);
        if (column.isNumeric())
        {
            columnConfig.setAlignment(HorizontalAlignment.RIGHT);
        }
        if (column.isLink())
        {
            columnConfig.setRenderer(LinkRenderer.createLinkRenderer());
        }
        columnConfig.setWidth(column.getWidth());
        columnConfig.setHidden(column.isHidden());
        String toolTip = "[Click] to sort, [SHIFT+Click] to adjust the width, "
            + "[Drag & Drop] to change order.";
        if (column.isEditable())
        {
            toolTip += " This is an editibale column. Just double-click on a cell or on the header icon.";
            CellEditor editor = new CellEditor(new TextField<String>()
                {
                    @Override
                    public void setValue(String value)
                    {
                        super.setValue(StringEscapeUtils.unescapeHtml(value));
                    }
                });
            columnConfig.setEditor(editor);
            LayoutContainer header = new LayoutContainer();
            HBoxLayout layout = new HBoxLayout();  
            layout.setHBoxLayoutAlign(HBoxLayoutAlign.TOP);  
            header.setLayout(layout);  
            AbstractImagePrototype editIcon =
                AbstractImagePrototype.create(IMAGE_BUNDLE.getEditableIcon());
            Button editButton = new Button("", editIcon);
            editButton.addSelectionListener(new SelectionListener<ButtonEvent>()
                {
                    @Override
                    public void componentSelected(ButtonEvent ce)
                    {
                        MessageBox.info("Edit", "Editing for column " + headerTitle + " not yet implemented.", null);
                    }
                });
            header.add(new Label(headerTitle), new HBoxLayoutData(new Margins(0, 0, 0, 0)));
            header.add(editButton, new HBoxLayoutData(new Margins(0, 0, 0, 3)));
            columnConfig.setWidget(header, headerTitle);
        }
        columnConfig.setToolTip(toolTip);
        return columnConfig;
    }

    public Set<IColumnDefinition<T>> getColumnDefs()
    {
        return columnDefs;
    }

    public List<ColumnConfig> getColumnConfigs()
    {
        return columnConfigs;
    }
}
