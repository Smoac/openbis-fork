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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.AbstractExternalDataGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ExperimentDataSetBrowser extends AbstractExternalDataGrid
{
    private static final String PREFIX = "experiment-data-section_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;
    
    static DisposableComponent create(IViewContext<?> viewContext, Experiment experiment)
    {
        IViewContext<ICommonClientServiceAsync> commonViewContext = viewContext.getCommonViewContext();
        String identifier = experiment.getIdentifier();
        return new ExperimentDataSetBrowser(commonViewContext, identifier).asDisposableWithoutToolbar();
    }

    private final String experimentIdentifier;

    private ExperimentDataSetBrowser(IViewContext<ICommonClientServiceAsync> viewContext,
            String experimentIdentifier)
    {
        super(viewContext, ID_PREFIX + experimentIdentifier);
        this.experimentIdentifier = experimentIdentifier;
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, ExternalData> resultSetConfig,
            AbstractAsyncCallback<ResultSet<ExternalData>> callback)
    {
        viewContext.getService().listExperimentDataSets(experimentIdentifier, resultSetConfig, callback);
    }

}
