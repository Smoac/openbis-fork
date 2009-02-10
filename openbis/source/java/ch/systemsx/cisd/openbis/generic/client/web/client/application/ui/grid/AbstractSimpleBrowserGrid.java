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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid;

import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;

/**
 * Grid displaying all the entities without any criteria (useful when there is no specific toolbar).
 * 
 * @author Tomasz Pylak
 */
abstract public class AbstractSimpleBrowserGrid<T/* Entity */, M extends ModelData> extends
        AbstractBrowserGrid<T, M>
{
    abstract protected IColumnDefinitionKind<T>[] getStaticColumnsDefinition();

    protected AbstractSimpleBrowserGrid(IViewContext<ICommonClientServiceAsync> viewContext,
            String browserId, String gridId)
    {
        super(viewContext, gridId, false, true);
        setId(browserId);
        updateDefaultRefreshButton();
    }

    @Override
    protected ColumnDefsAndConfigs<T> createColumnsDefinition()
    {
        IColumnDefinitionKind<T>[] colDefKinds = getStaticColumnsDefinition();
        List<IColumnDefinitionUI<T>> colDefs =
                BaseEntityModel.createColumnsDefinition(colDefKinds, viewContext);
        return ColumnDefsAndConfigs.create(colDefs);
    }

    @Override
    protected boolean isRefreshEnabled()
    {
        return true;
    }

    @Override
    protected void refresh()
    {
        super.refresh(null, false);
    }

    @Override
    protected void showEntityViewer(M modelData)
    {
        // do nothing
    }
}