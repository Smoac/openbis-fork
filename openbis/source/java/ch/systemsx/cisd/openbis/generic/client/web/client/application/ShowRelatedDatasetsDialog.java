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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import java.util.List;

import com.extjs.gxt.ui.client.core.XDOM;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.RelatedDataSetGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractDataConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WidgetUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.RelatedDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;

/**
 * Dialog used to show a tab with Data Sets related to entities like samples and experiments.
 * 
 * @author Piotr Buczek
 */
public final class ShowRelatedDatasetsDialog extends
        AbstractDataConfirmationDialog<List<? extends IEntityInformationHolder>>
{
    public static void showRelatedDatasetsTab(
            final IViewContext<ICommonClientServiceAsync> viewContext,
            final RelatedDataSetCriteria criteria)
    {
        final ITabItemFactory tabFactory = new ITabItemFactory()
            {
                public ITabItem create()
                {
                    IDisposableComponent component =
                            RelatedDataSetGrid.create(viewContext, criteria);
                    return DefaultTabItem.create(getTabTitle(), component, viewContext);
                }

                public String getId()
                {
                    return RelatedDataSetGrid.BROWSER_ID + XDOM.getUniqueId();
                }

                private String getTabTitle()
                {
                    return "Related Data Sets";
                }
            };
        DispatcherHelper.dispatchNaviEvent(tabFactory);
    }

    private static final int FIELD_WIDTH = 200;

    private static final int LABEL_WIDTH = 120;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final TableExportCriteria<? extends IEntityInformationHolder> displayedEntities;

    private final int displayedEntitiesCount;

    private Radio allOrSelectedRadio;

    public ShowRelatedDatasetsDialog(IViewContext<ICommonClientServiceAsync> viewContext,
            List<? extends IEntityInformationHolder> selectedEntities,
            TableExportCriteria<? extends IEntityInformationHolder> displayedEntities,
            int displayedEntitiesCount)
    {
        super(viewContext, selectedEntities, viewContext
                .getMessage(Dict.SHOW_RELATED_DATASETS_DIALOG_TITLE));
        this.viewContext = viewContext;
        this.displayedEntities = displayedEntities;
        this.displayedEntitiesCount = displayedEntitiesCount;
        setWidth(LABEL_WIDTH + FIELD_WIDTH + 50);
    }

    @Override
    protected String createMessage()
    {
        return viewContext.getMessage(Dict.SHOW_RELATED_DATASETS_DIALOG_MESSAGE, data.size());
    }

    @Override
    protected final void extendForm()
    {
        formPanel.setLabelWidth(LABEL_WIDTH);
        formPanel.setFieldWidth(FIELD_WIDTH);
        formPanel.setBodyBorder(false);
        formPanel.setHeaderVisible(false);

        formPanel.add(createAllOrSelectedRadio());
    }

    private final RadioGroup createAllOrSelectedRadio()
    {
        final String radioGroupLabel =
                viewContext.getMessage(Dict.SHOW_RELATED_DATASETS_DIALOG_RADIO_LABEL);
        final String selectedLabel = viewContext.getMessage(Dict.ONLY_SELECTED_RADIO, data.size());
        final String allLabel = viewContext.getMessage(Dict.ALL_RADIO, displayedEntitiesCount);

        return WidgetUtils.createAllOrSelectedRadioGroup(allOrSelectedRadio =
                WidgetUtils.createRadio(selectedLabel), WidgetUtils.createRadio(allLabel),
                radioGroupLabel, data.size());
    }

    private boolean getSelected()
    {
        return WidgetUtils.isSelected(allOrSelectedRadio);
    }

    @Override
    protected void executeConfirmedAction()
    {
        final boolean selected = getSelected();
        RelatedDataSetCriteria criteria = createCriteria(selected);
        showRelatedDatasetsTab(viewContext, criteria);
    }

    private RelatedDataSetCriteria createCriteria(boolean selected)
    {
        if (getSelected())
        {
            return RelatedDataSetCriteria.createSelectedEntities(data);
        } else
        {
            return RelatedDataSetCriteria.createDisplayedEntities(displayedEntities);
        }
    }

}
