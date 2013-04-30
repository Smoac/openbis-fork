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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.TypedTableGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.entity_type.AbstractEntityTypeGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.entity_type.AddEntityTypeDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Grid displaying experiment types.
 * 
 * @author Tomasz Pylak
 */
public class ExperimentTypeGrid extends AbstractEntityTypeGrid<ExperimentType>
{
    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + "experiment-type-browser";

    public static final String GRID_ID = BROWSER_ID + TypedTableGrid.GRID_POSTFIX;

    public static IDisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        final ExperimentTypeGrid grid = new ExperimentTypeGrid(viewContext);
        return grid.asDisposableWithoutToolbar();
    }

    private ExperimentTypeGrid(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, BROWSER_ID, GRID_ID);
    }

    @Override
    public AddEntityTypeDialog<ExperimentType> getNewDialog(ExperimentType newType) {
        return (AddEntityTypeDialog<ExperimentType>) createRegisterEntityTypeDialog("New Experiment", newType, newType.getEntityKind());
    }
    
    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<ExperimentType>> resultSetConfig,
            AbstractAsyncCallback<TypedTableResultSet<ExperimentType>> callback)
    {
        viewContext.getService().listExperimentTypes(resultSetConfig, callback);
    }

    @Override
    protected void prepareExportEntities(
            TableExportCriteria<TableModelRowWithObject<ExperimentType>> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportExperimentTypes(exportCriteria, callback);
    }

    @Override
    protected void register(ExperimentType experimentType, AsyncCallback<Void> registrationCallback)
    {
        viewContext.getService().registerExperimentType(experimentType, registrationCallback);
    }

    @Override
    protected EntityKind getEntityKindOrNull()
    {
        return EntityKind.EXPERIMENT;
    }

    @Override
    protected ExperimentType createNewEntityType()
    {
        return new ExperimentType();
    }
}
