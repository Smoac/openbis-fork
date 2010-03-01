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

package ch.systemsx.cisd.bds;

import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import ch.systemsx.cisd.bds.v1_1.SampleWithOwner;
import ch.systemsx.cisd.common.test.EqualsHashCodeTestCase;

/**
 * Test cases for corresponding {@link ExperimentIdentifier} class.
 * 
 * @author Christian Ribeaud
 */
@Test
public final class ExperimentIdentifierTest extends EqualsHashCodeTestCase<ExperimentIdentifier>
{

    public static final String EXPERMENT_CODE = "EXP1";

    public static final String PROJECT_CODE = "PROJ1";

    @Test
    public final void testConstructor()
    {
        boolean fail = true;
        try
        {
            new ExperimentIdentifier(SampleWithOwner.INSTANCE_CODE, SampleWithOwner.SPACE_CODE,
                    null, null);
        } catch (final AssertionError ex)
        {
            fail = false;
        }
        assertFalse(fail);
        fail = true;
        try
        {
            new ExperimentIdentifier(SampleWithOwner.INSTANCE_CODE, SampleWithOwner.SPACE_CODE,
                    PROJECT_CODE, "");
        } catch (final AssertionError ex)
        {
            fail = false;
        }
        assertFalse(fail);
    }

    //
    // EqualsHashCodeTestCase
    //

    @Override
    protected final ExperimentIdentifier createInstance() throws Exception
    {
        return new ExperimentIdentifier("instance", "space", "project", "experiment1");
    }

    @Override
    protected final ExperimentIdentifier createNotEqualInstance() throws Exception
    {
        return new ExperimentIdentifier("instance", "space", "project", "experiment2");
    }
}
