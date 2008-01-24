/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.server.business.dataaccess.db;

import java.io.File;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.AbstractAnnotationAwareTransactionalTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import ch.systemsx.cisd.cifex.server.business.dataaccess.IDAOFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;

/**
 * Abstract test case for database related unit testing.
 * 
 * @author Christian Ribeaud
 */
public abstract class AbstractDbUnitTest extends AbstractAnnotationAwareTransactionalTests
{
    /** Statically save the <code>ConfigurableApplicationContext</code> to ensure that it only gets loaded once. */
    private static ConfigurableApplicationContext context;

    protected DatabaseConfigurationContext configurationContext;

    protected IDAOFactory daoFactory;

    private final void checkDaoFactory()
    {
        assert daoFactory != null;
        assert daoFactory.getUserDAO() != null;
    }

    //
    // Bean methods used by the <i>Spring</i> framework
    //

    /**
     * Sets <code>configurationContext</code>.
     * 
     * @param configurationContext new value. Can be <code>null</code>.
     */
    public final void setConfigurationContext(DatabaseConfigurationContext configurationContext)
    {
        this.configurationContext = configurationContext;
    }

    /**
     * Sets <code>daoFactory</code>.
     * <p>
     * This is used by <i>Spring</i> framework to set the <code>DAOFactory</code>.
     * </p>
     */
    public final void setDaoFactory(IDAOFactory daoFactory)
    {
        this.daoFactory = daoFactory;
        checkDaoFactory();
    }

    //
    // TestNG annotations. We put '(alwaysRun = true)' so that these methods get called as well when we run the 'db'
    // group for instance (by default, they do not when running groups).
    //

    @BeforeClass(alwaysRun = true)
    public void beforeClass() throws Exception
    {
        LogInitializer.init();
        setUp();
        endTransaction();
    }

    @AfterClass(alwaysRun = true)
    public void afterClass() throws Exception
    {
        tearDown();
    }

    //
    // AbstractDependencyInjectionSpringContextTests
    //

    @Override
    protected final ConfigurableApplicationContext loadContextLocations(String[] locations) throws Exception
    {
        ConfigurableApplicationContext appContext = super.loadContextLocations(locations);
        if (context == null)
        {
            context = appContext;
        }
        return context;
    }

    @Override
    protected final String[] getConfigLocations()
    {
        // Override default behavior defined in <code>applicationContext.xml</code> file.
        System.setProperty("database.create-from-scratch", "true");
        System.setProperty("database.kind", "test");
        System.setProperty("script-folder", "source");
        try
        {
            File file = new File("source/java/applicationContext.xml");
            return new String[]
                { "file:" + file.getCanonicalPath() };
        } catch (Exception ex)
        {
            fail(ex.getMessage());
        }
        return super.getConfigLocations();
    }
}
