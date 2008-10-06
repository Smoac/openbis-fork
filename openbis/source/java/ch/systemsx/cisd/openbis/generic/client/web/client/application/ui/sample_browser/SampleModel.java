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

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleProperty;

/**
 * @author Izabela Adamczyk
 */
public class SampleModel extends BaseModelData
{

    static final String OBJECT = "object";

    static final String SAMPLE_TYPE = "sampleType";

    static final String SAMPLE_CODE = "code";

    static final String ATTACHED_TO_IDENTIFIER = "attachedToIdentifier";

    static final String SAMPLE_IDENTIFIER = "sampleIdentifier";

    static final long serialVersionUID = 1L;

    public static final String REGISTRATOR = "registrator";

    public static final String REGISTRATION_DATE = "registrationDate";

    public static final String PROPERTY_PREFIX = "property";

    public static final String CONTAINER_PARENT_PREFIX = "containerParent";

    public static final String GENERATED_FROM_PARENT_PREFIX = "generatedFromParent";

    public SampleModel(Sample s)
    {
        set(SAMPLE_CODE, s.getCode());
        set(SAMPLE_TYPE, s.getSampleType());
        set(OBJECT, s);
        set(SAMPLE_IDENTIFIER, s.getIdentifier());
        set(ATTACHED_TO_IDENTIFIER, s.getGroup() != null ? s.getGroup().getIdentifier() : s
                .getDatabaseInstance().getIdentifier());
        set(REGISTRATOR, s.getRegistrator());
        set(REGISTRATION_DATE, s.getRegistrationDate());
        setGeneratedFromParents(s, 1, s.getSampleType().getGeneratedFromHierarchyDepth());
        setContainerParents(s, 1, s.getSampleType().getPartOfHierarchyDepth());
        setProperties(s);

    }

    private void setProperties(Sample s)
    {
        for (SampleProperty p : s.getProperties())
        {
            set(PROPERTY_PREFIX + p.getSampleTypePropertyType().getPropertyType().getCode(), p
                    .getValue());
        }

    }

    private void setGeneratedFromParents(Sample s, int dep, int maxDep)
    {
        if (dep <= maxDep && s.getGeneratedFrom() != null)
        {
            set(GENERATED_FROM_PARENT_PREFIX + dep, s.getGeneratedFrom().getIdentifier());
            setGeneratedFromParents(s.getGeneratedFrom(), dep + 1, maxDep);
        }
    }

    private void setContainerParents(Sample s, int dep, int maxDep)
    {
        if (dep <= maxDep && s.getContainer() != null)
        {
            set(CONTAINER_PARENT_PREFIX + dep, s.getContainer().getIdentifier());
            setContainerParents(s.getContainer(), dep + 1, maxDep);
        }
    }

    void setProperty(String code, String value)
    {
        set(PROPERTY_PREFIX + code, value);
    }

}
