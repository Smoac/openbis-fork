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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import java.util.List;

import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.ToolBarEvent;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.EntityTypeColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractSimpleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.material.AddEntityTypeDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;

/**
 * Abstarct grid displaying entity types.
 * 
 * @author Tomasz Pylak
 */
abstract public class AbstractEntityTypeGrid extends AbstractSimpleBrowserGrid<EntityType>
{
    private static final String LABEL_REGISTER_NEW_TYPE = "New Type";

    abstract protected void registerEntityType(String code, String descriptionOrNull,
            AsyncCallback<Void> registrationCallback);

    protected AbstractEntityTypeGrid(IViewContext<ICommonClientServiceAsync> viewContext,
            String browserId, String gridId)
    {
        super(viewContext, browserId, gridId);
    }

    public final Component createToolbar(final String title)
    {
        ToolBar toolbar = new ToolBar();
        toolbar.add(new FillToolItem());
        TextToolItem addTypeButton =
                new TextToolItem(LABEL_REGISTER_NEW_TYPE, new SelectionListener<ToolBarEvent>()
                    {
                        @Override
                        public void componentSelected(ToolBarEvent ce)
                        {
                            createRegisterEntityTypeDialog(title).show();
                        }
                    });
        toolbar.add(addTypeButton);
        return toolbar;
    }

    private Window createRegisterEntityTypeDialog(final String title)
    {
        IDelegatedAction postRegistrationCallback = new IDelegatedAction()
            {
                public void execute()
                {
                    AbstractEntityTypeGrid.this.refresh();
                }
            };
        return new AddEntityTypeDialog(viewContext, title, postRegistrationCallback)
            {
                @Override
                protected void register(String code, String descriptionOrNull,
                        AsyncCallback<Void> registrationCallback)
                {
                    registerEntityType(code, descriptionOrNull, registrationCallback);
                }
            };
    }

    @Override
    protected IColumnDefinitionKind<EntityType>[] getStaticColumnsDefinition()
    {
        return EntityTypeColDefKind.values();
    }

    @Override
    protected List<IColumnDefinition<EntityType>> getAvailableFilters()
    {
        return asColumnFilters(new EntityTypeColDefKind[]
            { EntityTypeColDefKind.CODE });
    }
}