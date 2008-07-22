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

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.test.EqualsHashCodeTestCase;

/**
 * Test cases for corresponding {@link DataSet} class.
 * 
 * @author Christian Ribeaud
 */
@Test
public final class DataSetTest extends EqualsHashCodeTestCase<DataSet>
{

    //
    // EqualsHashCodeTestCase
    //

    @Override
    protected final DataSet createInstance() throws Exception
    {
        return new DataSet("code", "HCS_IMAGE");
    }

    @Override
    protected final DataSet createNotEqualInstance() throws Exception
    {
        return new DataSet("code1", "HCS_IMAGE");
    }

}
