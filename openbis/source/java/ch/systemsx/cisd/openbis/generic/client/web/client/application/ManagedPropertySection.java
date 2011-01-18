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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import java.util.Set;

import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.managed_property.ManagedPropertyGridGeneratedCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.managed_property.ManagedPropertyGridGeneratedCallback.IOnGridComponentGeneratedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableModelReference;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IManagedPropertyGridInformationProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ManagedTableWidgetDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ManagedWidgetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedUiDescription;

/**
 * {@link TabContent} handled by managed property script.
 * 
 * @author Piotr Buczek
 */
public class ManagedPropertySection extends DisposableTabContent
{
    private static final IDisposableComponent DUMMY_CONTENT = new IDisposableComponent()
        {

            public void update(Set<DatabaseModificationKind> observedModifications)
            {
            }

            public DatabaseModificationKind[] getRelevantModifications()
            {
                return DatabaseModificationKind.EMPTY_ARRAY;
            }

            public Component getComponent()
            {
                return new ContentPanel();
            }

            public void dispose()
            {
            }

        };

    private static String ID_PREFIX = "managed_property_section_";

    private final String gridIdSuffix;

    private final IDelegatedAction refreshAction;

    private final IManagedEntityProperty managedProperty;

    private final IEntityInformationHolder entity;

    public ManagedPropertySection(final String header, IViewContext<?> viewContext,
            IEntityInformationHolder entity, IManagedEntityProperty managedProperty,
            IDelegatedAction refreshAction)
    {
        super(header, viewContext, entity);
        this.entity = entity;
        this.managedProperty = managedProperty;
        this.gridIdSuffix = Format.hyphenize(header);
        this.refreshAction = refreshAction;
        setIds(new IDisplayTypeIDGenerator()
            {

                public String createID(String suffix)
                {
                    return createID() + suffix;
                }

                public String createID()
                {
                    return ID_PREFIX + gridIdSuffix;
                }
            });
    }

    @Override
    protected IDisposableComponent createDisposableContent()
    {
        final ManagedTableWidgetDescription tableDescriptionOrNull = tryGetTableDescription();
        if (tableDescriptionOrNull == null)
        {
            MessageBox.alert("Error", "Failed to create content", null);
            return DUMMY_CONTENT;
        } else
        {
            final IManagedPropertyGridInformationProvider gridInfo =
                    new IManagedPropertyGridInformationProvider()
                        {
                            public String getKey()
                            {
                                return gridIdSuffix;
                            }
                        };
            // refresh reloads the table and replaces tab component
            final IOnGridComponentGeneratedAction gridGeneratedAction =
                    new IOnGridComponentGeneratedAction()
                        {

                            public void execute(IDisposableComponent gridComponent)
                            {
                                replaceContent(gridComponent);
                            }

                        };

            IDelegatedAction loadGrid = new IDelegatedAction()
                {

                    public void execute()
                    {
                        AsyncCallback<TableModelReference> callback =
                                ManagedPropertyGridGeneratedCallback.create(
                                        viewContext.getCommonViewContext(), entity,
                                        managedProperty, gridInfo, gridGeneratedAction,
                                        refreshAction);
                        viewContext.getCommonService().createReportForManagedProperty(
                                tableDescriptionOrNull, callback);
                    }

                };
            loadGrid.execute();
            return null;
        }

    }

    private ManagedTableWidgetDescription tryGetTableDescription()
    {
        final IManagedUiDescription uiDescription = managedProperty.getUiDescription();
        if (uiDescription.getOutputWidgetDescription() != null
                && uiDescription.getOutputWidgetDescription().getManagedWidgetType() == ManagedWidgetType.TABLE
                && uiDescription.getOutputWidgetDescription() instanceof ManagedTableWidgetDescription)
        {
            return (ManagedTableWidgetDescription) uiDescription.getOutputWidgetDescription();
        } else
        {
            return null;
        }
    }

}
