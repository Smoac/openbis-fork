/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest.base;

import org.hamcrest.Matcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.servlet.SpringRequestContextProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientService;
import ch.systemsx.cisd.openbis.generic.server.ICommonServerForInternalUse;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.IndexMode;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientService;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;
import ch.systemsx.cisd.openbis.plugin.query.shared.IQueryServer;
import ch.systemsx.cisd.openbis.systemtest.base.auth.AndAuthorizationRule;
import ch.systemsx.cisd.openbis.systemtest.base.auth.AuthorizationRule;
import ch.systemsx.cisd.openbis.systemtest.base.auth.BasicAuthorizationRule;
import ch.systemsx.cisd.openbis.systemtest.base.auth.GuardedDomain;
import ch.systemsx.cisd.openbis.systemtest.base.auth.NotAuthorizationRule;
import ch.systemsx.cisd.openbis.systemtest.base.auth.OrAuthorizationRule;
import ch.systemsx.cisd.openbis.systemtest.base.builder.Builder;
import ch.systemsx.cisd.openbis.systemtest.base.builder.DataSetDeletionBuilder;
import ch.systemsx.cisd.openbis.systemtest.base.builder.DataSetUpdateBuilder;
import ch.systemsx.cisd.openbis.systemtest.base.builder.ExperimentBuilder;
import ch.systemsx.cisd.openbis.systemtest.base.builder.ExperimentUpdateBuilder;
import ch.systemsx.cisd.openbis.systemtest.base.builder.ExternalDataBuilder;
import ch.systemsx.cisd.openbis.systemtest.base.builder.ProjectBuilder;
import ch.systemsx.cisd.openbis.systemtest.base.builder.ProjectUpdateBuilder;
import ch.systemsx.cisd.openbis.systemtest.base.builder.SampleBuilder;
import ch.systemsx.cisd.openbis.systemtest.base.builder.SampleUpdateBuilder;
import ch.systemsx.cisd.openbis.systemtest.base.builder.SessionBuilder;
import ch.systemsx.cisd.openbis.systemtest.base.builder.SpaceBuilder;
import ch.systemsx.cisd.openbis.systemtest.base.builder.TrashEmptyBuilder;
import ch.systemsx.cisd.openbis.systemtest.base.builder.UpdateBuilder;
import ch.systemsx.cisd.openbis.systemtest.base.matcher.ExternalDataHasChildrenMatcher;
import ch.systemsx.cisd.openbis.systemtest.base.matcher.ExternalDataHasContainerMatcher;
import ch.systemsx.cisd.openbis.systemtest.base.matcher.ExternalDataHasNoSampleMatcher;
import ch.systemsx.cisd.openbis.systemtest.base.matcher.ExternalDataHasParentsMatcher;
import ch.systemsx.cisd.openbis.systemtest.base.matcher.HasNoChildrenMatcher;
import ch.systemsx.cisd.openbis.systemtest.base.matcher.HasNoContainerMatcher;
import ch.systemsx.cisd.openbis.systemtest.base.matcher.HasNoParentMatcher;
import ch.systemsx.cisd.openbis.systemtest.base.matcher.InExperimentMatcher;
import ch.systemsx.cisd.openbis.systemtest.base.matcher.InProjectMatcher;
import ch.systemsx.cisd.openbis.systemtest.base.matcher.InSampleMatcher;
import ch.systemsx.cisd.openbis.systemtest.base.matcher.InSpaceMatcher;
import ch.systemsx.cisd.openbis.systemtest.base.matcher.SampleHasContainerMatcher;
import ch.systemsx.cisd.openbis.systemtest.base.matcher.SampleHasParentsMatcher;

/**
 * @author anttil
 */
@ContextConfiguration(locations = "classpath:applicationContext.xml")
@TransactionConfiguration(transactionManager = "transaction-manager")
@Test(groups =
    { "system-cleandb" })
public abstract class BaseTest extends AbstractTransactionalTestNGSpringContextTests
{

    protected ICommonServerForInternalUse commonServer;

    protected IGenericServer genericServer;

    protected IQueryServer queryServer;

    protected ICommonClientService commonClientService;

    protected IGenericClientService genericClientService;

    protected IETLLIMSService etlService;

    protected MockHttpServletRequest request;

    protected IDAOFactory daoFactory;

    protected IRelationshipService relationshipService;

    protected ISessionManager<Session> sessionManager;

    protected String systemSessionToken;

    @BeforeSuite(groups = "system-cleandb")
    public void initializeLog() throws Exception
    {
        LogInitializer.init();
        initializeProperties();
        setContext();
        createDataStore();
    }

    private void initializeProperties()
    {
        System.setProperty("database.create-from-scratch", "true");
        System.setProperty("database.kind", "test_clean");
        System.setProperty("script-folder", "../openbis/source");
        System.setProperty("hibernate.search.index-mode", IndexMode.INDEX_FROM_SCRATCH.name());
        System.setProperty("hibernate.search.index-base", "../openbis/targets/lucene/cleandb");
        System.setProperty("hibernate.search.worker.execution", "sync");
    }

    private void setContext() throws Exception
    {
        super.springTestContextPrepareTestInstance();
    }

    private void createDataStore()
    {
        DataStorePE dataStore = new DataStorePE();
        dataStore.setCode("STANDARD");
        dataStore.setDatabaseInstance(this.daoFactory.getDatabaseInstanceDAO()
                .getDatabaseInstanceById(1));
        dataStore.setDownloadUrl("http://localhost");
        dataStore.setRemoteUrl("http://remotehost");
        dataStore.setSessionToken("");

        this.daoFactory.getDataStoreDAO().createOrUpdateDataStore(dataStore);
    }

    @AfterSuite(groups = "system-cleandb")
    public void testingThis()
    {
        ((GenericApplicationContext) applicationContext).destroy();
    }

    @BeforeClass(alwaysRun = true, dependsOnMethods = "springTestContextPrepareTestInstance")
    public void loginAsSystem()
    {
        systemSessionToken = commonServer.tryToAuthenticateAsSystem().getSessionToken();
    }

    @Autowired
    @Test(enabled = false)
    public final void setRequestContextProvider(final SpringRequestContextProvider contextProvider)
    {
        request = new MockHttpServletRequest();
        contextProvider.setRequest(request);
    }

    @Autowired
    @Test(enabled = false)
    public void setDaoFactory(IDAOFactory daoFactory)
    {
        this.daoFactory = daoFactory;
    }

    @Autowired
    @Test(enabled = false)
    public final void setCommonServer(final ICommonServerForInternalUse commonServer)
    {
        this.commonServer = commonServer;
    }

    @Autowired
    @Test(enabled = false)
    public final void setGenericServer(final IGenericServer genericServer)
    {
        this.genericServer = genericServer;
    }

    @Autowired
    @Test(enabled = false)
    public final void setQueryServer(final IQueryServer queryServer)
    {
        this.queryServer = queryServer;
    }

    @Autowired
    @Test(enabled = false)
    public final void setCommonClientService(final ICommonClientService commonClientService)
    {
        this.commonClientService = commonClientService;
    }

    @Autowired
    @Test(enabled = false)
    public final void setGenericClientService(final IGenericClientService genericClientService)
    {
        this.genericClientService = genericClientService;
    }

    @Autowired
    @Test(enabled = false)
    public void setETLService(IETLLIMSService etlService)
    {
        this.etlService = etlService;
    }

    @Autowired
    @Test(enabled = false)
    public void setRelationshipService(final IRelationshipService relationshipService)
    {
        this.relationshipService = relationshipService;
    }

    @Autowired
    @Test(enabled = false)
    public void setSessionManager(final ISessionManager<Session> sessionManager)
    {
        this.sessionManager = sessionManager;
    }

    protected static <T> T create(Builder<T> builder)
    {
        return builder.create();
    }

    protected void perform(UpdateBuilder<?> builder)
    {
        builder.perform();
    }

    protected SampleBuilder aSample()
    {
        return new SampleBuilder(commonServer, genericServer);
    }

    protected ExternalDataBuilder aDataSet()
    {
        return new ExternalDataBuilder(commonServer, genericServer, etlService);
    }

    protected ProjectUpdateBuilder anUpdateOf(Project project)
    {
        return new ProjectUpdateBuilder(commonServer, genericServer, refresh(project));
    }

    protected ExperimentUpdateBuilder anUpdateOf(Experiment experiment)
    {
        return new ExperimentUpdateBuilder(commonServer, genericServer, refresh(experiment));
    }

    protected SampleUpdateBuilder anUpdateOf(Sample sample)
    {
        return new SampleUpdateBuilder(commonServer, genericServer, refresh(sample));
    }

    protected DataSetUpdateBuilder anUpdateOf(ExternalData dataset)
    {
        return new DataSetUpdateBuilder(commonServer, genericServer, refresh(dataset));
    }

    protected DataSetDeletionBuilder trash(ExternalData dataset)
    {
        return new DataSetDeletionBuilder(commonServer, genericServer, refresh(dataset));
    }

    protected TrashEmptyBuilder emptyTrash()
    {
        return new TrashEmptyBuilder(commonServer, genericServer);
    }

    protected SessionBuilder aSession()
    {
        return new SessionBuilder(commonServer, genericServer);
    }

    protected ExperimentBuilder anExperiment()
    {
        return new ExperimentBuilder(commonServer, genericServer);
    }

    protected ProjectBuilder aProject()
    {
        return new ProjectBuilder(commonServer, genericServer);
    }

    protected SpaceBuilder aSpace()
    {
        return new SpaceBuilder(commonServer, genericServer);
    }

    protected Matcher<Object> inSpace(Space space)
    {
        return new InSpaceMatcher(refresh(space));
    }

    protected Matcher<Object> hasNoSpace()
    {
        return new InSpaceMatcher();
    }

    protected Matcher<Object> inExperiment(Experiment experiment)
    {
        return new InExperimentMatcher(refresh(experiment));
    }

    protected Matcher<Object> hasNoExperiment()
    {
        return new InExperimentMatcher();
    }

    protected Matcher<Experiment> inProject(Project project)
    {
        return new InProjectMatcher(refresh(project));
    }

    protected Matcher<ExternalData> inSample(Sample sample)
    {
        return new InSampleMatcher(refresh(sample));
    }

    protected Matcher<ExternalData> hasParents(ExternalData first, ExternalData... rest)
    {
        return new ExternalDataHasParentsMatcher(refresh(first), refresh(rest));
    }

    protected Matcher<ExternalData> hasChildren(ExternalData first, ExternalData... rest)
    {
        return new ExternalDataHasChildrenMatcher(refresh(first), refresh(rest));
    }

    protected Matcher<Object> hasNoParents()
    {
        return new HasNoParentMatcher();
    }

    protected Matcher<ExternalData> hasNoChildren()
    {
        return new HasNoChildrenMatcher();
    }

    protected Matcher<ExternalData> hasNoSample()
    {
        return new ExternalDataHasNoSampleMatcher();
    }

    protected Matcher<Sample> hasParents(Sample first, Sample... rest)
    {
        return new SampleHasParentsMatcher(refresh(first), refresh(rest));
    }

    protected Matcher<Object> hasNoParent()
    {
        return new HasNoParentMatcher();
    }

    protected Matcher<Sample> hasContainer(Sample container)
    {
        return new SampleHasContainerMatcher(refresh(container));
    }

    protected Matcher<ExternalData> hasContainer(ExternalData container)
    {
        return new ExternalDataHasContainerMatcher(refresh(container));
    }

    protected Matcher<Object> hasNoContainer()
    {
        return new HasNoContainerMatcher();
    }

    protected Experiment refresh(Experiment experiment)
    {
        return commonServer.getExperimentInfo(systemSessionToken, new TechId(experiment.getId()));
    }

    protected Project refresh(Project project)
    {
        return commonServer.getProjectInfo(systemSessionToken, new TechId(project.getId()));
    }

    protected Sample refresh(Sample sample)
    {
        SampleParentWithDerived result =
                commonServer.getSampleInfo(systemSessionToken, new TechId(sample.getId()));
        return result.getParent();
    }

    protected Sample[] refresh(Sample[] samples)
    {
        Sample[] result = new Sample[samples.length];
        int i = 0;
        for (Sample sample : samples)
        {
            result[i] = refresh(sample);
            i++;
        }
        return result;
    }

    protected ExternalData[] refresh(ExternalData[] dataSets)
    {
        ExternalData[] result = new ExternalData[dataSets.length];
        int i = 0;
        for (ExternalData data : dataSets)
        {
            result[i] = refresh(data);
            i++;
        }
        return result;
    }

    protected ExternalData refresh(ExternalData data)
    {
        return etlService.tryGetDataSet(systemSessionToken, data.getCode());
    }

    protected Space refresh(Space space)
    {
        for (Space refreshed : commonServer.listSpaces(systemSessionToken,
                new DatabaseInstanceIdentifier("CISD")))
        {
            if (space.getId() == refreshed.getId())
            {
                return space;
            }
        }
        throw new IllegalArgumentException("Space not found: " + space.getCode());
    }

    public static ExperimentIdentifier id(Experiment experiment)
    {
        return new ExperimentIdentifier(id(experiment.getProject()), experiment.getCode());
    }

    public static ProjectIdentifier id(Project project)
    {
        return new ProjectIdentifier(id(project.getSpace()), project.getCode());
    }

    public static SpaceIdentifier id(Space space)
    {
        return new SpaceIdentifier(id(space.getInstance()), space.getCode());
    }

    public static DatabaseInstanceIdentifier id(DatabaseInstance dbin)
    {
        return new DatabaseInstanceIdentifier(dbin.getCode());
    }

    public static SampleIdentifier id(Sample sample)
    {
        if (sample.getSpace() == null)
        {
            return new SampleIdentifier(id(sample.getDatabaseInstance()), sample.getCode());
        } else
        {
            return new SampleIdentifier(id(sample.getSpace()), sample.getCode());
        }
    }

    @SuppressWarnings("unchecked")
    @Test(enabled = false)
    public <T> void assertThat(T actual, Matcher<T> matcher)
    {
        T refreshed = actual;
        if (actual instanceof ExternalData)
        {
            refreshed = (T) refresh((ExternalData) actual);
        } else if (actual instanceof Sample)
        {
            refreshed = (T) refresh((Sample) actual);
        } else if (actual instanceof Experiment)
        {
            refreshed = (T) refresh((Experiment) actual);
        } else if (actual instanceof Project)
        {
            refreshed = (T) refresh((Project) actual);
        } else if (actual instanceof Space)
        {
            refreshed = (T) refresh((Space) actual);
        }

        org.hamcrest.MatcherAssert.assertThat(refreshed, matcher);
    }

    public static AuthorizationRule rule(GuardedDomain domain, RoleWithHierarchy role)
    {
        return new BasicAuthorizationRule(domain, role);
    }

    public static AuthorizationRule and(AuthorizationRule rule1, AuthorizationRule rule2,
            AuthorizationRule... rest)
    {
        AuthorizationRule main = new AndAuthorizationRule(rule1, rule2);
        for (AuthorizationRule rule : rest)
        {
            main = new AndAuthorizationRule(main, rule);
        }
        return main;
    }

    public static AuthorizationRule or(AuthorizationRule rule1, AuthorizationRule rule2,
            AuthorizationRule... rest)
    {
        AuthorizationRule main = new OrAuthorizationRule(rule1, rule2);
        for (AuthorizationRule rule : rest)
        {
            main = new OrAuthorizationRule(main, rule);
        }
        return main;
    }

    public static AuthorizationRule not(AuthorizationRule rule)
    {
        return new NotAuthorizationRule(rule);
    }
}
