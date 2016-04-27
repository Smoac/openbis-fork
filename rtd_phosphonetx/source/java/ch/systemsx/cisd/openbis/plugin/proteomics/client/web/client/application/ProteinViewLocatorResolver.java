/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.application;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.AbstractViewLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ViewLocator;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.URLMethodWithParameters;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.IPhosphoNetXClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.ProteinByExperiment;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.ProteinInfo;

/**
 * Locator resolver for protein details view.
 *
 * @author Franz-Josef Elmer
 */
public class ProteinViewLocatorResolver extends AbstractViewLocatorResolver
{
    private static final String ACTION = "PROTEIN";

    private static final String EXPERIMENT_PERM_ID = "experimentPermId";

    private static final String PROTEIN_ID = "id";

    private final IViewContext<IPhosphoNetXClientServiceAsync> viewContext;

    public ProteinViewLocatorResolver(IViewContext<IPhosphoNetXClientServiceAsync> viewContext)
    {
        super(ACTION);
        this.viewContext = viewContext;
    }

    @Override
    public void resolve(ViewLocator locator) throws UserFailureException
    {
        String experimentPermID = getMandatoryParameter(locator, EXPERIMENT_PERM_ID);
        final TechId proteinID = new TechId(new Long(getMandatoryParameter(locator, PROTEIN_ID)));
        viewContext.log("resolve protein " + proteinID + " for experiment " + experimentPermID);
        viewContext.getCommonService().getExperimentInfoByPermId(experimentPermID,
                new AbstractAsyncCallback<Experiment>(viewContext)
                    {
                        @Override
                        protected void process(final Experiment experiment)
                        {
                            IPhosphoNetXClientServiceAsync service =
                                    ProteinViewLocatorResolver.this.viewContext.getService();
                            service.getProteinByExperiment(new TechId(experiment.getId()),
                                    proteinID, new AbstractAsyncCallback<ProteinByExperiment>(
                                            viewContext)
                                        {
                                            @Override
                                            protected void process(final ProteinByExperiment protein)
                                            {
                                                AbstractTabItemFactory tabItemFactory =
                                                        ProteinViewer
                                                                .createTabItemFactory(
                                                                        ProteinViewLocatorResolver.this.viewContext,
                                                                        experiment, protein);
                                                DispatcherHelper.dispatchNaviEvent(tabItemFactory);
                                            }

                                        });
                        }
                    });
    }

    static String createLink(final Experiment experiment, ProteinInfo entity)
    {
        URLMethodWithParameters url = new URLMethodWithParameters("");
        url.addParameter(ViewLocator.ACTION_PARAMETER, ACTION);
        url.addParameter(ProteinViewLocatorResolver.EXPERIMENT_PERM_ID,
                experiment.getPermId());
        url.addParameter(ProteinViewLocatorResolver.PROTEIN_ID,
                Long.toString(entity.getId().getId()));
        return url.toStringWithoutDelimiterPrefix();
    }

}
