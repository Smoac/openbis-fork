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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.Set;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProcedurePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * Test cases for the {@link PackageBasedIndexedEntityFinder}.
 * 
 * @author Christian Ribeaud
 */
public final class PackageBasedIndexedEntityFinderTest
{

    @Test
    public final void testGetIndexedEntitiesFailed()
    {
        boolean fail = true;
        // Null value
        try
        {
            new PackageBasedIndexedEntityFinder(null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        // Package does not exist.
        try
        {
            new PackageBasedIndexedEntityFinder("does.not.exist");
            fail("'" + IllegalArgumentException.class.getName() + "' expected.");
        } catch (final IllegalArgumentException e)
        {
            // Nothing to do here.
        }
    }

    @Test
    public final void testGetIndexedEntities()
    {
        final PackageBasedIndexedEntityFinder entityFinder =
                new PackageBasedIndexedEntityFinder("ch.systemsx.cisd.openbis.generic.shared.dto");
        final Set<Class<?>> entities = entityFinder.getIndexedEntities();
        assertEquals(5, entities.size());
        assertTrue(entities.contains(SamplePE.class));
        assertTrue(entities.contains(ExperimentPE.class));
        assertTrue(entities.contains(MaterialPE.class));
        assertTrue(entities.contains(ExternalDataPE.class));
        assertTrue(entities.contains(ProcedurePE.class));
    }
}
