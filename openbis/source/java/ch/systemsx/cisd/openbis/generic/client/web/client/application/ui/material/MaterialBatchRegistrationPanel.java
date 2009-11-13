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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.material;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.MaterialTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifiable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;

/**
 * The {@link LayoutContainer} extension for importing materials.
 * 
 * @author Izabela Adamczyk
 */
public final class MaterialBatchRegistrationPanel extends LayoutContainer
{
    private static final String ID_SUFFIX = "material-batch-registration";

    public static final String ID = GenericConstants.ID_PREFIX + ID_SUFFIX;

    private final MaterialTypeSelectionWidget materialTypeSelection;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public static DatabaseModificationAwareComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        MaterialBatchRegistrationPanel panel = new MaterialBatchRegistrationPanel(viewContext);
        return new DatabaseModificationAwareComponent(panel, panel.materialTypeSelection);
    }

    private MaterialBatchRegistrationPanel(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        this.viewContext = viewContext;
        setId(ID);
        setScrollMode(Scroll.AUTO);
        materialTypeSelection = new MaterialTypeSelectionWidget(viewContext, ID_SUFFIX);
        final ToolBar toolBar = createToolBar();
        add(toolBar);
        materialTypeSelection
                .addSelectionChangedListener(new SelectionChangedListener<MaterialTypeModel>()
                    {

                        @Override
                        public final void selectionChanged(
                                final SelectionChangedEvent<MaterialTypeModel> se)
                        {
                            final MaterialType materialType =
                                    materialTypeSelection.tryGetSelectedMaterialType();
                            if (materialType != null)
                            {
                                removeAll();
                                final EntityKind entityKind = EntityKind.MATERIAL;
                                add(toolBar);
                                final IClientPluginFactory clientPluginFactory =
                                        viewContext.getClientPluginFactoryProvider()
                                                .getClientPluginFactory(entityKind, materialType);
                                final IClientPlugin<EntityType, IIdentifiable> createClientPlugin =
                                        clientPluginFactory.createClientPlugin(entityKind);
                                add(createClientPlugin
                                        .createBatchRegistrationForEntityType(materialType));
                                layout();
                            }
                        }
                    });
    }

    private final ToolBar createToolBar()
    {
        final ToolBar toolBar = new ToolBar();
        toolBar.add(new LabelToolItem(viewContext.getMessage(Dict.MATERIAL_TYPE)
                + GenericConstants.LABEL_SEPARATOR));
        toolBar.add(materialTypeSelection);
        return toolBar;
    }
}
