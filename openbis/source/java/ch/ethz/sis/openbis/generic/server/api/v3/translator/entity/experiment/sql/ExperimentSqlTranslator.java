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

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationResults;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.Experiment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.experiment.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentPermId;

/**
 * @author pkupczyk
 */
public class ExperimentSqlTranslator extends AbstractCachingTranslator<Long, Experiment, ExperimentFetchOptions> implements IExperimentSqlTranslator
{

    @Autowired
    private IExperimentAuthorizationSqlValidator authorizationValidator;

    @Autowired
    private IExperimentBaseSqlTranslator baseTranslator;

    @Override
    protected Experiment createObject(TranslationContext context, Long experimentId, ExperimentFetchOptions fetchOptions)
    {
        Experiment experiment = new Experiment();
        experiment.setFetchOptions(new ExperimentFetchOptions());
        return experiment;
    }

    @Override
    protected Object getObjectsRelations(TranslationContext context, Collection<Long> experimentIds, ExperimentFetchOptions fetchOptions)
    {
        TranslationResults relations = new TranslationResults();

        relations.put(IExperimentBaseSqlTranslator.class, baseTranslator.translate(context, experimentIds, null));

        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, Long experimentId, Experiment result, Object objectRelations,
            ExperimentFetchOptions fetchOptions)
    {
        TranslationResults relations = (TranslationResults) objectRelations;
        ExperimentBaseRecord baseRecord = relations.get(IExperimentBaseSqlTranslator.class, experimentId);

        result.setCode(baseRecord.code);
        result.setPermId(new ExperimentPermId(baseRecord.permId));
        result.setIdentifier(new ExperimentIdentifier(baseRecord.spaceCode, baseRecord.projectCode, baseRecord.code));
        result.setRegistrationDate(baseRecord.registrationDate);
        result.setModificationDate(baseRecord.modificationDate);
    }

}
