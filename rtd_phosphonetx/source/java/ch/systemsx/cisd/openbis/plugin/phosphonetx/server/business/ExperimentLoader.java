/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExperimentTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExperimentTranslator.LoadableFields;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ExperimentLoader
{
    private final IDAOFactory daoFactory;

    public ExperimentLoader(IDAOFactory daoFactory)
    {
        this.daoFactory = daoFactory;
        
    }
    
    public void enrichWithExperiments(Collection<Sample> samples)
    {
        Map<Long, List<Sample>> samplesByID = new LinkedHashMap<Long, List<Sample>>();
        for (Sample sample : samples)
        {
            Experiment experiment = sample.getExperiment();
            if (experiment != null)
            {
                Long id = experiment.getId();
                List<Sample> list = samplesByID.get(id);
                if (list == null)
                {
                    list = new ArrayList<Sample>();
                    samplesByID.put(id, list);
                }
                list.add(sample);
            }
        }
        List<ExperimentPE> experiments =
                daoFactory.getExperimentDAO().listExperimentsWithProperties(samplesByID.keySet());
        for (ExperimentPE experiment : experiments)
        {
            Experiment e = ExperimentTranslator.translate(experiment, "", LoadableFields.PROPERTIES);
            List<Sample> list = samplesByID.get(experiment.getId());
            for (Sample sample : list)
            {
                sample.setExperiment(e);
            }
        }
    }
}
