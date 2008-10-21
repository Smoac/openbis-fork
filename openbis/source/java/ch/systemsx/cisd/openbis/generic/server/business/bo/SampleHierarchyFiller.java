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

import org.hibernate.Hibernate;

import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * A {@link SamplePE} enricher.
 * 
 * @author Christian Ribeaud
 */
public final class SampleHierarchyFiller
{
    private SampleHierarchyFiller()
    {
    }

    /**
     * Enriches given samples with full hierarchy.
     */
    public static final void enrichWithFullHierarchy(final Iterable<SamplePE> samples)
    {
        for (final SamplePE sample : samples)
        {
            enrichWithFullHierarchy(sample);
        }
    }

    /**
     * Enriches given sample with full hierarchy.
     */
    public final static void enrichWithFullHierarchy(final SamplePE sample)
    {
        enrichParentHierarchy(sample);
        enrichWithTop(sample);
    }

    private final static void enrichWithTop(final SamplePE sample)
    {
        initialize(sample.getTop());
    }

    private final static void enrichParentHierarchy(final SamplePE sample)
    {
        SamplePE generatedFrom = sample;
        do
        {
            generatedFrom = generatedFrom.getGeneratedFrom();
            initialize(generatedFrom);
        } while (generatedFrom != null);
    }

    private final static void initialize(final SamplePE sample)
    {
        if (sample != null)
        {
            Hibernate.initialize(sample);
            Hibernate.initialize(sample.getControlLayout());
        }
    }
}
