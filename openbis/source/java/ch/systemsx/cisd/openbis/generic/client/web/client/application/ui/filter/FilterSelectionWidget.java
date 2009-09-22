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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.filter;

import java.util.List;

import com.extjs.gxt.ui.client.widget.form.ComboBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisplayTypeIDProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Filter;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;

/**
 * {@link ComboBox} containing list of filters loaded from the server.
 * 
 * @author Izabela Adamczyk
 */
public final class FilterSelectionWidget extends DropDownList<FilterModel, Filter> implements
        IDelegatedAction
{
    private static final String LIST_ITEMS_CALLBACK = "ListItemsCallback";

    public static final String SUFFIX = "filter";

    private final IViewContext<?> viewContext;

    private final boolean withStandard;

    private final IDisplayTypeIDProvider displayTypeIDProvider;

    public FilterSelectionWidget(final IViewContext<?> viewContext, final String idSuffix,
            IDisplayTypeIDProvider displayTypeIDProvider)
    {
        super(viewContext, SUFFIX + idSuffix, Dict.FILTER, ModelDataPropertyNames.NAME, "filter",
                "filters");
        this.viewContext = viewContext;
        this.displayTypeIDProvider = displayTypeIDProvider;
        this.withStandard = true;
        setAutoSelectFirst(withStandard);
        setCallbackId(createCallbackId());
    }

    public static String createCallbackId()
    {
        return FilterSelectionWidget.class + LIST_ITEMS_CALLBACK;
    }

    @Override
    protected List<FilterModel> convertItems(List<Filter> result)
    {
        return FilterModel.convert(result, withStandard);
    }

    @Override
    protected void loadData(AbstractAsyncCallback<List<Filter>> callback)
    {
        viewContext.getCommonService().listFilters(displayTypeIDProvider.getGridDisplayTypeID(),
                callback);
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return DatabaseModificationKind.any(ObjectKind.FILTER);
    }

    public void execute()
    {
        refreshStore();
    }
}
