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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * A {@link SamplePE} enricher.
 * 
 * @author Christian Ribeaud
 */
public final class SampleHierarchyFiller
{
    private SampleHierarchyFiller()
    {
        // Can not be instantiated.
    }

    /**
     * Enriches given sample with <i>caontainer</i> and <i>generatedFrom</i> hierarchy.
     */
    public final static void enrichWithParentAndContainerHierarchy(final SamplePE sample)
    {
        enrichParentHierarchy(sample);
        enrichContainerHierarchy(sample);
    }

    private final static void enrichContainerHierarchy(final SamplePE sample)
    {
        SamplePE container = sample;
        final Integer integer = sample.getSampleType().getContainerHierarchyDepth();
        assert integer != null : "'partOf' hierarchy depth not specified.";
        int containerHierarchyDepth = getPositiveIntegerValue(integer);
        while (containerHierarchyDepth-- > 0 && container != null)
        {
            container = container.getContainer();
            initialize(container);
        }
    }

    private final static void enrichParentHierarchy(final SamplePE sample)
    {
        SamplePE generatedFrom = sample;
        final Integer integer = sample.getSampleType().getGeneratedFromHierarchyDepth();
        assert integer != null : "'generatedFrom' hierarchy depth not specified.";
        int generatedFromHierarchyDepth = getPositiveIntegerValue(integer);
        while (generatedFromHierarchyDepth-- > 0 && generatedFrom != null)
        {
            generatedFrom = generatedFrom.getGeneratedFrom();
            initialize(generatedFrom);
        }
    }

    public final static int getPositiveIntegerValue(int integer)
    {
        return integer == 0 ? 1 : integer;
    }

    private final static void initialize(final SamplePE sample)
    {
        if (sample != null)
        {
            HibernateUtils.initialize(sample);
        }
    }
}
