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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.FullTextIndexerRunnable;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.HibernateSearchContext;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.IndexMode;

/**
 * Utility methods around database indexing with <i>Hibernate</i>.
 * 
 * @author Christian Ribeaud
 */
public final class IndexCreationUtil
{
    private static final String LUCENE_INDICES = "sourceTest/lucene/indices";

    static
    {
        LogInitializer.init();
        // Deactivate the indexing in the application context loaded by Spring.
        System.setProperty("hibernate.search.index-mode", "NO_INDEX");
        System.setProperty("hibernate.search.index-base", LUCENE_INDICES);
    }

    private static HibernateSearchContext hibernateSearchContext;

    private static BeanFactory beanFactory;

    private IndexCreationUtil()
    {
        // Can not be instantiated.
    }

    private final static BeanFactory getBeanFactory()
    {
        if (beanFactory == null)
        {
            final AbstractApplicationContext applicationContext =
                    new ClassPathXmlApplicationContext(new String[]
                        { "applicationContext.xml" }, true);
            IndexCreationUtil.beanFactory = applicationContext;
        }
        return beanFactory;
    }

    /**
     * Performs a full text index because the test database has been migrated.
     */
    private final static void performFullTextIndex() throws Exception
    {
        final BeanFactory factory = getBeanFactory();
        final FullTextIndexerRunnable fullTextIndexer =
                new FullTextIndexerRunnable((SessionFactory) factory
                        .getBean("hibernate-session-factory"), hibernateSearchContext);
        fullTextIndexer.run();
    }

    /**
     * Creates a freshly new {@link HibernateSearchContext} overriding the one loaded by <i>Spring</i>.
     */
    private final static HibernateSearchContext createHibernateSearchContext()
    {
        final HibernateSearchContext context = new HibernateSearchContext();
        context.setIndexBase(LUCENE_INDICES);
        context.setIndexMode(IndexMode.INDEX_FROM_SCRATCH);
        return context;
    }

    //
    // Main method
    //

    public static void main(final String[] args) throws Exception
    {
        if (args.length != 1)
        {
            System.out.println("Usage: java " + DatabaseCreationUtil.class.getName()
                    + "<database kind>");
            System.exit(1);
        }
        System.setProperty("database.kind", args[0]);
        hibernateSearchContext = createHibernateSearchContext();
        hibernateSearchContext.afterPropertiesSet();
        performFullTextIndex();
    }

}
