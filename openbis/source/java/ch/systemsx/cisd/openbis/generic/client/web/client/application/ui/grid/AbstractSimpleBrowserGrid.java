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

import java.util.Set;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;

/**
 * Grid displaying all the entities without any criteria (useful when there is no specific toolbar).
 * 
 * @author Tomasz Pylak
 */
abstract public class AbstractSimpleBrowserGrid<T/* Entity */> extends
        AbstractBrowserGrid<T, BaseEntityModel<T>>
{
    abstract protected IColumnDefinitionKind<T>[] getStaticColumnsDefinition();

    protected AbstractSimpleBrowserGrid(IViewContext<ICommonClientServiceAsync> viewContext,
            String browserId, String gridId, IDisplayTypeIDGenerator displayTypeIDGenerator)
    {
        this(viewContext, browserId, gridId, true, displayTypeIDGenerator);
    }

    protected AbstractSimpleBrowserGrid(IViewContext<ICommonClientServiceAsync> viewContext,
            String browserId, String gridId, boolean refreshAutomatically,
            IDisplayTypeIDGenerator displayTypeIDGenerator)
    {
        super(viewContext, gridId, refreshAutomatically, displayTypeIDGenerator);
        setId(browserId);
        updateDefaultRefreshButton();
    }

    @Override
    protected ColumnDefsAndConfigs<T> createColumnsDefinition()
    {
        return BaseEntityModel.createColumnConfigs(getStaticColumnsDefinition(), viewContext);
    }

    @Override
    protected boolean isRefreshEnabled()
    {
        return true;
    }

    @Override
    protected void refresh()
    {
        super.refresh(false);
    }

    @Override
    protected void showEntityViewer(T entity, boolean editMode, boolean active)
    {
        // do nothing
    }

    @Override
    protected BaseEntityModel<T> createModel(GridRowModel<T> entity)
    {
        return new BaseEntityModel<T>(entity, getStaticColumnsDefinition());
    }

    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        refreshGridSilently();
    }

}
