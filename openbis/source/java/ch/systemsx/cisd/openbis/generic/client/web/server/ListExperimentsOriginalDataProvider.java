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

package ch.systemsx.cisd.openbis.generic.client.web.server;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListExperimentsCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IOriginalDataProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.ExperimentTranslator;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

/**
 * A {@link IOriginalDataProvider} implementation for listing experiments.
 * 
 * @author Christian Ribeaud
 */
final class ListExperimentsOriginalDataProvider extends AbstractOriginalDataProvider<Experiment>
{
    private final ListExperimentsCriteria listCriteria;

    ListExperimentsOriginalDataProvider(final ICommonServer commonServer,
            final ListExperimentsCriteria listCriteria, final String sessionToken)
    {
        super(commonServer, sessionToken);
        this.listCriteria = listCriteria;
    }

    //
    // AbstractOriginalDataProvider
    //

    public final List<Experiment> getOriginalData()
    {
        final List<ExperimentPE> experiments =
                commonServer.listExperiments(sessionToken, ExperimentTranslator
                        .translate(listCriteria.getExperimentType()), new ProjectIdentifier(
                        listCriteria.getGroupCode(), listCriteria.getProjectCode()));
        final List<Experiment> list = new ArrayList<Experiment>(experiments.size());
        for (final ExperimentPE experiment : experiments)
        {
            list.add(ExperimentTranslator.translate(experiment,
                    ExperimentTranslator.LoadableFields.PROPERTIES));
        }
        return list;
    }
}
