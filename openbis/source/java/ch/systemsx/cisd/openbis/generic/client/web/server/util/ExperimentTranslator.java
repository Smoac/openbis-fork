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

package ch.systemsx.cisd.openbis.generic.client.web.server.util;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;

/**
 * A {@link Experiment} &lt;---&gt; {@link ExperimentPE} translator.
 * 
 * @author Tomasz Pylak
 */
public final class ExperimentTranslator
{

    private ExperimentTranslator()
    {
        // Can not be instantiated.
    }

    public final static Experiment translate(final ExperimentPE experiment,
            final boolean withDetails)
    {
        if (experiment == null)
        {
            return null;
        }
        final Experiment result = new Experiment();
        result.setCode(experiment.getCode());
        result.setExperimentType(translate(experiment.getExperimentType()));
        result.setExperimentIdentifier(experiment.getIdentifier());
        result.setProject(ProjectTranslator.translate(experiment.getProject()));
        result.setRegistrationDate(experiment.getRegistrationDate());
        result.setRegistrator(PersonTranslator.translate(experiment.getRegistrator()));
        if (withDetails)
        {
            result.setInvalidation(InvalidationTranslator.translate(experiment.getInvalidation()));
            result
                    .setProperties(ExperimentPropertyTranslator.translate(experiment
                            .getProperties()));
        }
        return result;
    }

    public final static ExperimentType translate(final ExperimentTypePE experimentType)
    {
        final ExperimentType result = new ExperimentType();
        result.setCode(experimentType.getCode());
        result.setDescription(experimentType.getDescription());
        result.setDatabaseInstance(DatabaseInstanceTranslator.translate(experimentType
                .getDatabaseInstance()));
        return result;
    }

    public final static ExperimentTypePE translate(final ExperimentType experimentType)
    {
        final ExperimentTypePE result = new ExperimentTypePE();
        result.setCode(experimentType.getCode());
        result.setDescription(experimentType.getDescription());
        result.setDatabaseInstance(DatabaseInstanceTranslator.translate(experimentType
                .getDatabaseInstance()));
        return result;
    }

}
