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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.authorizationgroup.AuthorizationGroupComparatorFactory;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.dataset.DataSetComparatorFactory;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.experiment.ExperimentComparatorFactory;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.globalsearch.GlobalSearchObjectComparatorFactory;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.material.MaterialComparatorFactory;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.project.ProjectComparatorFactory;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.property.PropertyAssignmentComparatorFactory;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sample.SampleComparatorFactory;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.space.SpaceComparatorFactory;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.tag.TagComparatorFactory;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.vocabulary.VocabularyTermComparatorFactory;

/**
 * @author pkupczyk
 */
@SuppressWarnings("rawtypes")
public abstract class ComparatorFactory
{

    private static List<ComparatorFactory> factories = new LinkedList<ComparatorFactory>();

    static
    {
        factories.add(new GlobalSearchObjectComparatorFactory());
        factories.add(new TagComparatorFactory());
        factories.add(new AuthorizationGroupComparatorFactory());
        factories.add(new VocabularyTermComparatorFactory());
        factories.add(new SpaceComparatorFactory());
        factories.add(new ProjectComparatorFactory());
        factories.add(new ExperimentComparatorFactory());
        factories.add(new SampleComparatorFactory());
        factories.add(new DataSetComparatorFactory());
        factories.add(new MaterialComparatorFactory());
        factories.add(new PropertyAssignmentComparatorFactory());
    }

    public abstract boolean accepts(Class<?> sortOptionsClass);

    public abstract Comparator getComparator(String field);

    public abstract Comparator getDefaultComparator();

    public static ComparatorFactory getInstance(Class<?> sortOptionsClass)
    {
        for (ComparatorFactory factory : factories)
        {
            if (factory.accepts(sortOptionsClass))
            {
                return factory;
            }
        }

        return null;
    }

}
