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

import com.extjs.gxt.ui.client.widget.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDataModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewColumnOrFilter;

/**
 * {@link Window} containing filter registration form.
 * 
 * @author Izabela Adamczyk
 * @author Piotr Buczek
 */
public class AddFilterDialog extends AbstractGridCustomExpressionEditRegisterDialog
{
    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public AddFilterDialog(final IViewContext<ICommonClientServiceAsync> viewContext,
            final IDelegatedAction postRegistrationCallback, String gridId,
            List<ColumnDataModel> columnModels)
    {
        super(viewContext, viewContext.getMessage(Dict.ADD_NEW_FILTER), postRegistrationCallback,
                gridId, columnModels);
        this.viewContext = viewContext;
    }

    @Override
    protected void register(AsyncCallback<Void> registrationCallback)
    {
        NewColumnOrFilter filter = new NewColumnOrFilter();
        filter.setGridId(gridId);
        filter.setDescription(extractDescription());
        filter.setExpression(extractExpression());
        filter.setName(extractName());
        filter.setPublic(extractIsPublic());
        viewContext.getService().registerFilter(filter, registrationCallback);
    }

}
