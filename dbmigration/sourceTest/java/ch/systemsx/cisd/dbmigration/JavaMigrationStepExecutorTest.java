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

package ch.systemsx.cisd.dbmigration;

import javax.sql.DataSource;

import org.jmock.Mockery;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.Script;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.StatusFlag;
import ch.systemsx.cisd.common.logging.LogInitializer;

/**
 * Test cases for the {@link JavaMigrationStepExecutor}.
 * 
 * @author Izabela Adamczyk
 */
public class JavaMigrationStepExecutorTest
{
    private Mockery context;

    DataSource dataSource;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        dataSource = context.mock(DataSource.class);
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() throws Exception
    {
        LogInitializer.init();
    }

    @Test
    public void testHappyCase()
    {
        final JavaMigrationStepExecutor javaMigrationStepExecutor =
                new JavaMigrationStepExecutor(dataSource);
        final Script script =
                new Script("001To002.sql",
                        "-- JAVA ch.systemsx.cisd.dbmigration.MigrationStepFrom001To002");
        Assert.assertTrue(javaMigrationStepExecutor.tryPerformPreMigration(script).getFlag()
                .equals(StatusFlag.OK));
        Assert.assertTrue(javaMigrationStepExecutor.tryPerformPostMigration(script).getFlag()
                .equals(StatusFlag.OK));

    }

    @Test
    public void testUnhappyCase()
    {
        final JavaMigrationStepExecutor javaMigrationStepExecutor =
                new JavaMigrationStepExecutor(dataSource);
        final Script script =
                new Script("002To003.sql",
                        "-- JAVA ch.systemsx.cisd.dbmigration.MigrationStepFrom002To003");

        Assert.assertTrue(javaMigrationStepExecutor.tryPerformPreMigration(script).getFlag()
                .equals(StatusFlag.ERROR));
        Assert.assertTrue(javaMigrationStepExecutor.tryPerformPreMigration(script)
                .tryGetErrorMessage().equals("bad pre"));

        Assert.assertTrue(javaMigrationStepExecutor.tryPerformPostMigration(script).getFlag()
                .equals(StatusFlag.ERROR));
        Assert.assertTrue(javaMigrationStepExecutor.tryPerformPostMigration(script)
                .tryGetErrorMessage().equals("bad post"));

    }

    @Test
    public void testClassNotFound()
    {
        final JavaMigrationStepExecutor javaMigrationStepExecutor =
                new JavaMigrationStepExecutor(dataSource);
        final Script script =
                new Script("003To004.sql",
                        "-- JAVA ch.systemsx.cisd.dbmigration.MigrationStepFrom003To003");
        boolean exceptionThrown = false;
        try
        {
            javaMigrationStepExecutor.tryPerformPreMigration(script);
        } catch (final EnvironmentFailureException e)
        {
            exceptionThrown = true;
            Assert.assertEquals(e.getMessage(),
                    "Class 'ch.systemsx.cisd.dbmigration.MigrationStepFrom003To003' not found.");
        }
        Assert.assertTrue(exceptionThrown);

    }

}
