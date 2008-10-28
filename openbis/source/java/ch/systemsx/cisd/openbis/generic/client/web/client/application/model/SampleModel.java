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

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.DateRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.PersonRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Procedure;
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

    public static final String OBJECT = "object";

    public static final String SAMPLE_TYPE = "sampleType";

    public static final String SAMPLE_CODE = "code";

    public static final String IS_INSTANCE_SAMPLE_COLUMN = "isShared";

    public static final String IS_GROUP_SAMPLE = "isGroupSample";

    public static final String SAMPLE_IDENTIFIER = "sampleIdentifier";

    public static final String REGISTRATOR = "registrator";

    public static final String REGISTRATION_DATE = "registrationDate";

    public static final String PROPERTY_PREFIX = "property";

    public static final String CONTAINER_PARENT_PREFIX = "containerParent";

    public static final String GENERATED_FROM_PARENT_PREFIX = "generatedFromParent";

    public static final String IS_INVALID = "isInvalid";

    public static final String EXPERIMENT = "experiment";

    public SampleModel(final Sample sample)
    {
        set(SAMPLE_CODE, printShortIdentifier(sample));
        set(SAMPLE_TYPE, sample.getSampleType());
        set(OBJECT, sample);
        set(SAMPLE_IDENTIFIER, sample.getIdentifier());
        set(IS_INSTANCE_SAMPLE_COLUMN, (sample.getDatabaseInstance() != null));
        set(REGISTRATOR, PersonRenderer.createPersonAnchor(sample.getRegistrator()));
        set(REGISTRATION_DATE, DateRenderer.renderDate(sample.getRegistrationDate()));
        set(IS_GROUP_SAMPLE, sample.getGroup() != null);
        set(IS_INVALID, sample.getInvalidation() != null);
        set(EXPERIMENT, printExperimentIdentifier(sample));
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

    private final static String printExperimentIdentifier(final Sample sample)
    {
        final Procedure procedure = sample.getValidProcedure();
        if (procedure != null)
        {
            final Experiment experiment = procedure.getExperiment();
            if (experiment != null)
            {
                return printExperimentidentifier(experiment);
            }
        }
        return null;
    }

    private final static String printExperimentidentifier(final Experiment experiment)
    {
        return experiment.getProject().getCode() + SEPARATOR + experiment.getCode();
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
