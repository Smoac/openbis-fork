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
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Group;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Procedure;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Project;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleProperty;

/**
 * {@link ModelData} for {@link Sample}
 * 
 * @author Izabela Adamczyk
 */
public final class SampleModel extends BaseModelData
{
    private static final long serialVersionUID = 1L;

    private static final String SEPARATOR = "/";

    public static final String PROPERTY_PREFIX = "property";

    public static final String CONTAINER_PARENT_PREFIX = "containerParent";

    public static final String GENERATED_FROM_PARENT_PREFIX = "generatedFromParent";

    public SampleModel(final Sample sample)
    {
        set(ModelDataPropertyNames.INSTANCE, printInstance(sample));
        set(ModelDataPropertyNames.GROUP, printGroup(sample));
        set(ModelDataPropertyNames.CODE, sample.getCode());
        set(ModelDataPropertyNames.SAMPLE_TYPE, sample.getSampleType());
        set(ModelDataPropertyNames.OBJECT, sample);
        set(ModelDataPropertyNames.SAMPLE_IDENTIFIER, sample.getIdentifier());
        set(ModelDataPropertyNames.IS_INSTANCE_SAMPLE, (sample.getDatabaseInstance() != null));
        set(ModelDataPropertyNames.REGISTRATOR, PersonRenderer.createPersonAnchor(sample.getRegistrator()));
        set(ModelDataPropertyNames.REGISTRATION_DATE, DateRenderer.renderDate(sample.getRegistrationDate()));
        set(ModelDataPropertyNames.IS_GROUP_SAMPLE, sample.getGroup() != null);
        set(ModelDataPropertyNames.IS_INVALID, sample.getInvalidation() != null);
        Experiment experiment = tryToGetExperiment(sample);
        if (experiment != null)
        {
            set(ModelDataPropertyNames.PROJECT, experiment.getProject().getCode());
            set(ModelDataPropertyNames.EXPERIMENT, experiment.getCode());
            set(ModelDataPropertyNames.EXPERIMENT_IDENTIFIER, printExperimentIdentifier(experiment));
        }
        setGeneratedFromParents(sample, 1, sample.getSampleType().getGeneratedFromHierarchyDepth());
        setContainerParents(sample, 1, sample.getSampleType().getPartOfHierarchyDepth());
        setProperties(sample);
    }

    private final void setProperties(final Sample sample)
    {
        for (final SampleProperty p : sample.getProperties())
        {
            final PropertyType propertyType = p.getEntityTypePropertyType().getPropertyType();
            set(
                    PROPERTY_PREFIX + propertyType.isInternalNamespace()
                            + propertyType.getSimpleCode(), p.getValue());
        }

    }

    private final void setGeneratedFromParents(final Sample sample, final int depth,
            final int maxDepth)
    {
        if (depth <= maxDepth && sample.getGeneratedFrom() != null)
        {
            set(GENERATED_FROM_PARENT_PREFIX + depth, printShortIdentifier(sample
                    .getGeneratedFrom()));
            setGeneratedFromParents(sample.getGeneratedFrom(), depth + 1, maxDepth);
        }
    }

    private final void setContainerParents(final Sample sample, final int depth, final int maxDepth)
    {
        if (depth <= maxDepth && sample.getContainer() != null)
        {
            set(CONTAINER_PARENT_PREFIX + depth, printShortIdentifier(sample.getContainer()));
            setContainerParents(sample.getContainer(), depth + 1, maxDepth);
        }
    }

    public final static List<SampleModel> asSampleModels(final List<Sample> samples)
    {
        final List<SampleModel> sampleModels = new ArrayList<SampleModel>(samples.size());
        for (final Sample sample : samples)
        {
            sampleModels.add(new SampleModel(sample));
        }
        return sampleModels;
    }
    
    private final static Experiment tryToGetExperiment(final Sample sample)
    {
        final Procedure procedure = sample.getValidProcedure();
        if (procedure != null)
        {
            return procedure.getExperiment();
        }
        return null;
    }
    
    private final static String printGroup(final Sample sample)
    {
        Group group = sample.getGroup();
        return group == null ? "" : group.getCode();
    }

    private final static String printExperimentIdentifier(final Experiment experiment)
    {
        Project project = experiment.getProject();
        Group group = project.getGroup();
        DatabaseInstance instance = group.getInstance();
        return instance.getCode() + ":/" + group.getCode() + SEPARATOR + project.getCode()
                + SEPARATOR + experiment.getCode();
    }

    private final static String printInstance(final Sample sample)
    {
        DatabaseInstance databaseInstance = sample.getDatabaseInstance();
        if (databaseInstance == null)
        {
            databaseInstance = sample.getGroup().getInstance();
        }
        return databaseInstance.getCode();
    }

    private final static String printShortIdentifier(final Sample sample)
    {
        if (sample.getDatabaseInstance() != null)
        {
            return SEPARATOR + sample.getCode();
        } else
        {
            return sample.getCode();
        }
    }

}
