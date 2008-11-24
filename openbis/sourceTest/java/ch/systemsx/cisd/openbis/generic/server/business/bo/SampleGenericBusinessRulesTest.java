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

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * Test cases for corresponding {@link SampleGenericBusinessRules} class.
 * 
 * @author Izabela Adamczyk
 */
public final class SampleGenericBusinessRulesTest
{

    private static final String DB = "db";

    private static final String GROUP_1 = "group-1";

    private static final String GROUP_2 = "group-2";

    private static final String SAMPLE_1 = "sample-1";

    private static final String SAMPLE_2 = "sample-2";

    private static final String SAMPLE_3 = "sample-3";

    private static DatabaseInstancePE createDatabaseInstance(String code)
    {
        DatabaseInstancePE d = new DatabaseInstancePE();
        d.setCode(code);
        return d;
    }

    private static GroupPE createGroup(DatabaseInstancePE db, String code)
    {
        GroupPE g = new GroupPE();
        g.setCode(code);
        g.setDatabaseInstance(db);
        return g;
    }

    private static SamplePE createGroupSample(GroupPE g, String code)
    {
        SamplePE s = new SamplePE();
        s.setCode(code);
        s.setGroup(g);
        return s;
    }

    private static SamplePE createSharedSample(DatabaseInstancePE db, String code)
    {
        SamplePE s = new SamplePE();
        s.setCode(code);
        s.setDatabaseInstance(db);
        return s;
    }

    @Test
    public void testGroupSampleCanHaveParentFromTheSameGroup() throws Exception
    {
        DatabaseInstancePE databaseInstance = createDatabaseInstance(DB);
        SamplePE newSample = createGroupSample(createGroup(databaseInstance, GROUP_1), SAMPLE_1);

        SamplePE generator = createGroupSample(createGroup(databaseInstance, GROUP_1), SAMPLE_2);
        newSample.setGeneratedFrom(generator);

        SamplePE container = createGroupSample(createGroup(databaseInstance, GROUP_1), SAMPLE_3);
        newSample.setContainer(container);

        SampleGenericBusinessRules.assertValidParents(newSample);
    }

    @Test
    public void testGroupSampleCanHaveSharedParent() throws Exception
    {
        DatabaseInstancePE databaseInstance = createDatabaseInstance(DB);
        SamplePE newSample = createGroupSample(createGroup(databaseInstance, GROUP_1), SAMPLE_1);

        SamplePE parent = createSharedSample(databaseInstance, SAMPLE_2);
        newSample.setGeneratedFrom(parent);

        SamplePE container = createSharedSample(databaseInstance, SAMPLE_3);
        newSample.setContainer(container);

        SampleGenericBusinessRules.assertValidParents(newSample);
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testGroupSampleCannotBeContainedByParentFromDifferentGroup() throws Exception
    {
        DatabaseInstancePE databaseInstance = createDatabaseInstance(DB);
        SamplePE newSample = createGroupSample(createGroup(databaseInstance, GROUP_1), SAMPLE_1);

        SamplePE parent = createGroupSample(createGroup(databaseInstance, GROUP_2), SAMPLE_2);
        newSample.setContainer(parent);

        SampleGenericBusinessRules.assertValidParents(newSample);
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testGroupSampleCannotBeDerivedFromParentFromDifferentGroup() throws Exception
    {
        DatabaseInstancePE databaseInstance = createDatabaseInstance(DB);
        SamplePE newSample = createGroupSample(createGroup(databaseInstance, GROUP_1), SAMPLE_1);

        SamplePE parent = createGroupSample(createGroup(databaseInstance, GROUP_2), SAMPLE_2);
        newSample.setGeneratedFrom(parent);

        SampleGenericBusinessRules.assertValidParents(newSample);
    }

    @Test
    public void testInstanceSampleCanHaveInstanceSampleParents() throws Exception
    {
        DatabaseInstancePE databaseInstance = createDatabaseInstance(DB);
        SamplePE newSample = createSharedSample(databaseInstance, SAMPLE_1);

        SamplePE generator = createSharedSample(databaseInstance, SAMPLE_2);
        newSample.setGeneratedFrom(generator);

        SamplePE container = createSharedSample(databaseInstance, SAMPLE_3);
        newSample.setContainer(container);

        SampleGenericBusinessRules.assertValidParents(newSample);
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testInstanceSampleCannotBeContainedByGroupSample() throws Exception
    {
        DatabaseInstancePE databaseInstance = createDatabaseInstance(DB);
        SamplePE newSample = createSharedSample(databaseInstance, SAMPLE_1);
        SamplePE parent = createGroupSample(createGroup(databaseInstance, GROUP_1), SAMPLE_2);
        newSample.setContainer(parent);
        SampleGenericBusinessRules.assertValidParents(newSample);
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testInstanceSampleCannotBeDerivedFromGroupSample() throws Exception
    {
        DatabaseInstancePE databaseInstance = createDatabaseInstance(DB);
        SamplePE newSample = createSharedSample(databaseInstance, SAMPLE_1);
        SamplePE parent = createGroupSample(createGroup(databaseInstance, GROUP_1), SAMPLE_2);
        newSample.setGeneratedFrom(parent);
        SampleGenericBusinessRules.assertValidParents(newSample);
    }

}