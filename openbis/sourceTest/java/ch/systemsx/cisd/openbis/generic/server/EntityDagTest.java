/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * @author Jakub Straszewski
 */
public class EntityDagTest

{
    private class DependencyHolder
    {
        String code;

        Collection<String> dependants;

        public DependencyHolder(String code, Collection<String> dependants)
        {
            super();
            this.code = code;
            this.dependants = dependants;
        }

        @Override
        public String toString()
        {
            return code;
        }
    }

    private EntityDAG<DependencyHolder> createDag(List<DependencyHolder> entities)
    {
        return new EntityDAG<EntityDagTest.DependencyHolder>(entities)
            {
                @Override
                protected Collection<String> getDependentEntitiesCodes(DependencyHolder entity)
                {
                    return entity.dependants;
                }

                @Override
                protected String getCode(DependencyHolder entity)
                {
                    return entity.code;
                }
            };
    }

    @Test
    public void testSortedDependencyOrderIsGood()
    {
        List<DependencyHolder> entities = new ArrayList<DependencyHolder>();

        List<String> empty = Collections.emptyList();
        
        entities.add(new DependencyHolder("A1", empty));
        entities.add(new DependencyHolder("A2", Collections.singleton("A1")));
        entities.add(new DependencyHolder("A3", Collections.singleton("A2")));

        EntityDAG<DependencyHolder> dag = createDag(entities);

        List<? extends DependencyHolder> orderedRegistrations = dag.getOrderedRegistrations();

        Assert.assertEquals(entities, orderedRegistrations);
    }
    
    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = "Graph cycle detected. Cannot execute topological sort.")
    public void testCircularDependency()
    {
        List<DependencyHolder> entities = new ArrayList<DependencyHolder>();

        entities.add(new DependencyHolder("A1", Collections.singleton("A3")));
        entities.add(new DependencyHolder("A2", Collections.singleton("A1")));
        entities.add(new DependencyHolder("A3", Collections.singleton("A2")));

        EntityDAG<DependencyHolder> dag = createDag(entities);

        List<? extends DependencyHolder> orderedRegistrations = dag.getOrderedRegistrations();

        Assert.assertEquals(entities, orderedRegistrations);
    }

}
