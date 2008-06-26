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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.test.context.transaction.TransactionConfiguration;

import ch.systemsx.cisd.cifex.server.business.dataaccess.IDAOFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;

/**
 * Abstract test case for database related unit testing.
 * 
 * @author Christian Ribeaud
 */
@ContextConfiguration(locations = "classpath:applicationContext.xml")
// In 'commonContext.xml', our transaction manager is called 'transaction-manager' (by default
// Spring looks for 'transactionManager').
@TransactionConfiguration(transactionManager = "transaction-manager")
public abstract class AbstractDAOTest extends AbstractTransactionalTestNGSpringContextTests
{

    static
    {
        LogInitializer.init();
        // Override default behavior defined in <code>applicationContext.xml</code> file.
        System.setProperty("database.create-from-scratch", "true");
        System.setProperty("database.kind", "test");
        System.setProperty("script-folder", "source");
    }

    protected IDAOFactory daoFactory;

    private final void checkDaoFactory()
    {
        assert daoFactory != null;
        assert daoFactory.getUserDAO() != null;
        assert daoFactory.getFileDAO() != null;
    }

    /**
     * Sets <code>daoFactory</code>.
     */
    @Autowired
    public final void setDaoFactory(final IDAOFactory daoFactory)
    {
        this.daoFactory = daoFactory;
        checkDaoFactory();
    }
}
