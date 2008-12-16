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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.model;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.DateRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.PersonRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyType;

/**
 * {@link ModelData} for {@link Experiment}
 * 
 * @author Izabela Adamczyk
 */
public final class ExperimentModel extends BaseModelData
{
    private static final long serialVersionUID = 1L;

    private static final String PROPERTY_PREFIX = "property";

    public static String createID(final PropertyType propertyType)
    {
        return PROPERTY_PREFIX + propertyType.isInternalNamespace() + propertyType.getCode();
    }

    public ExperimentModel(final Experiment experiment)
    {
        set(ModelDataPropertyNames.CODE, experiment.getCode());
        set(ModelDataPropertyNames.EXPERIMENT_TYPE_CODE_FOR_EXPERIMENT, experiment
                .getExperimentType().getCode());
        set(ModelDataPropertyNames.EXPERIMENT_TYPE, experiment.getExperimentType());
        set(ModelDataPropertyNames.EXPERIMENT_IDENTIFIER, experiment.getExperimentIdentifier());
        set(ModelDataPropertyNames.GROUP_FOR_EXPERIMENT, experiment.getProject().getGroup()
                .getCode());
        set(ModelDataPropertyNames.PROJECT, experiment.getProject().getCode());
        set(ModelDataPropertyNames.OBJECT, experiment);
        set(ModelDataPropertyNames.REGISTRATOR, PersonRenderer.createPersonAnchor(experiment
                .getRegistrator()));
        set(ModelDataPropertyNames.REGISTRATION_DATE, DateRenderer.renderDate(experiment
                .getRegistrationDate()));
        set(ModelDataPropertyNames.IS_INVALID, experiment.getInvalidation() != null);
    }

    public final static List<ExperimentModel> asExperimentModels(final List<Experiment> experiments)
    {
        final List<ExperimentModel> sampleModels =
                new ArrayList<ExperimentModel>(experiments.size());
        for (final Experiment exp : experiments)
        {
            sampleModels.add(new ExperimentModel(exp));
        }
        return sampleModels;
    }

}
