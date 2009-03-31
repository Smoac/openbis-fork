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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample;

import com.extjs.gxt.ui.client.widget.Component;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractEntityTypeGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * Grid displaying sample types.
 * 
 * @author Tomasz Pylak
 */
public class SampleTypeGrid extends AbstractEntityTypeGrid
{
    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + "sample-type-browser";

    public static final String GRID_ID = BROWSER_ID + "_grid";

    public static IDisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        final SampleTypeGrid grid = new SampleTypeGrid(viewContext);
        Component toolbar = grid.createToolbar("Register a new sample type");
        return grid.asDisposableWithToolbar(toolbar);
    }

    private SampleTypeGrid(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, BROWSER_ID, GRID_ID);
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, EntityType> resultSetConfig,
            AbstractAsyncCallback<ResultSet<EntityType>> callback)
    {
        viewContext.getService().listSampleTypes(resultSetConfig, callback);
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<EntityType> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportSampleTypes(exportCriteria, callback);
    }

    @Override
    protected void registerEntityType(String code, String descriptionOrNull,
            AsyncCallback<Void> registrationCallback)
    {
        SampleType sampleType = new SampleType();
        sampleType.setCode(code);
        sampleType.setDescription(descriptionOrNull);
        sampleType.setListable(true);
        sampleType.setPartOfHierarchyDepth(0);
        sampleType.setGeneratedFromHierarchyDepth(3);
        viewContext.getService().registerSampleType(sampleType, registrationCallback);
    }
}