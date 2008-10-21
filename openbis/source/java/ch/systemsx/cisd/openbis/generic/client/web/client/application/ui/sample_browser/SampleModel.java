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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample_browser;

import com.extjs.gxt.ui.client.data.BaseModelData;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Procedure;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleProperty;

/**
 * @author Izabela Adamczyk
 */
public class SampleModel extends BaseModelData
{

    private static final String SEPARATOR = "/";

    static final String OBJECT = "object";

    static final String SAMPLE_TYPE = "sampleType";

    static final String SAMPLE_CODE = "code";

    static final String IS_INSTANCE_SAMPLE_COLUMN = "isShared";

    static final String IS_GROUP_SAMPLE = "isGroupSample";

    static final String SAMPLE_IDENTIFIER = "sampleIdentifier";

    static final long serialVersionUID = 1L;

    public static final String REGISTRATOR = "registrator";

    public static final String REGISTRATION_DATE = "registrationDate";

    public static final String PROPERTY_PREFIX = "property";

    public static final String CONTAINER_PARENT_PREFIX = "containerParent";

    public static final String GENERATED_FROM_PARENT_PREFIX = "generatedFromParent";

    public static final String IS_INVALID = "isInvalid";

    public static final String EXPERIMENT = "experiment";

    public SampleModel(Sample s)
    {
        set(SAMPLE_CODE, printShortIdentifier(s));
        set(SAMPLE_TYPE, s.getSampleType());
        set(OBJECT, s);
        set(SAMPLE_IDENTIFIER, s.getIdentifier());
        set(IS_INSTANCE_SAMPLE_COLUMN, (s.getDatabaseInstance() != null));
        set(REGISTRATOR, s.getRegistrator());
        set(REGISTRATION_DATE, s.getRegistrationDate());
        set(IS_GROUP_SAMPLE, s.getGroup() != null);
        set(IS_INVALID, s.isInvalid());
        set(EXPERIMENT, printExperimentIdentifier(s));
        setGeneratedFromParents(s, 1, s.getSampleType().getGeneratedFromHierarchyDepth());
        setContainerParents(s, 1, s.getSampleType().getPartOfHierarchyDepth());
        setProperties(s);

    }

    private void setProperties(Sample s)
    {
        for (SampleProperty p : s.getProperties())
        {
            PropertyType propertyType = p.getEntityTypePropertyType().getPropertyType();
            set(
                    PROPERTY_PREFIX + propertyType.isInternalNamespace()
                            + propertyType.getSimpleCode(), p.getValue());
        }

    }

    private void setGeneratedFromParents(Sample s, int depth, int maxDepth)
    {
        if (depth <= maxDepth && s.getGeneratedFrom() != null)
        {
            set(GENERATED_FROM_PARENT_PREFIX + depth, printShortIdentifier(s.getGeneratedFrom()));
            setGeneratedFromParents(s.getGeneratedFrom(), depth + 1, maxDepth);
        }
    }

    private void setContainerParents(Sample s, int depth, int maxDepth)
    {
        if (depth <= maxDepth && s.getContainer() != null)
        {
            set(CONTAINER_PARENT_PREFIX + depth, printShortIdentifier(s.getContainer()));
            setContainerParents(s.getContainer(), depth + 1, maxDepth);
        }
    }

    private static String printExperimentIdentifier(Sample s)
    {
        Procedure procedure = s.getValidProcedure();
        if (procedure != null)
        {
            Experiment experiment = procedure.getExperiment();
            if (experiment != null)
            {
                return printExperimentidentifier(experiment);
            }
        }
        return null;
    }

    private static String printExperimentidentifier(Experiment experiment)
    {
        return experiment.getProject().getCode() + SEPARATOR + experiment.getCode();
    }

    private static String printShortIdentifier(Sample sample)
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
