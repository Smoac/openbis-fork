/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.experiment.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.common.sql.ObjectToManyRelationTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.experiment.IExperimentTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.Experiment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.experiment.ExperimentFetchOptions;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;

/**
 * @author pkupczyk
 */
public abstract class ObjectToExperimentsSqlTranslator extends ObjectToManyRelationTranslator<Experiment, ExperimentFetchOptions> implements
        IObjectToExperimentsSqlTranslator
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IExperimentTranslator experimentTranslator;

    @Override
    protected Map<Long, Experiment> translateRelated(TranslationContext context, Collection<Long> relatedIds,
            ExperimentFetchOptions relatedFetchOptions)
    {
        // TODO call IExperimentSqlTranslator when ready
        List<ExperimentPE> related = daoFactory.getExperimentDAO().listByIDs(relatedIds);
        Map<ExperimentPE, Experiment> translated = experimentTranslator.translate(context, related, relatedFetchOptions);
        Map<Long, Experiment> result = new HashMap<Long, Experiment>();

        for (Map.Entry<ExperimentPE, Experiment> entry : translated.entrySet())
        {
            result.put(entry.getKey().getId(), entry.getValue());
        }
        return result;
    }

    @Override
    protected Collection<Experiment> createCollection()
    {
        return new ArrayList<Experiment>();
    }

}
