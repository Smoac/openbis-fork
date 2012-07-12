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
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.servlet.SpringRequestContextProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientService;
import ch.systemsx.cisd.openbis.generic.server.ICommonServerForInternalUse;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.IndexMode;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
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
import ch.systemsx.cisd.openbis.systemtest.base.builder.Builder;
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

    protected ICommonClientService commonClientService;

    protected IGenericClientService genericClientService;

    protected IETLLIMSService etlService;

    protected MockHttpServletRequest request;

    protected IDAOFactory daoFactory;

    protected IRelationshipService relationshipService;

    protected ISessionManager<Session> sessionManager;

    protected String systemSessionToken;

    @BeforeTest(groups =
        { "system-cleandb" })
    public void initializeLog()
    {
        LogInitializer.init();
    }

    @BeforeTest(groups =
        { "system-cleandb" }, dependsOnMethods =
        { "initializeLog" })
    public void initializeProperties()
    {
        System.setProperty("database.create-from-scratch", "true");
        System.setProperty("database.kind", "test");
        System.setProperty("script-folder", "../openbis/source");
        System.setProperty("hibernate.search.index-mode", IndexMode.INDEX_FROM_SCRATCH.name());
        System.setProperty("hibernate.search.index-base", "../openbis/targets/lucene/cleandb");
        System.setProperty("hibernate.search.worker.execution", "sync");
    }

    @BeforeTest(groups =
        { "system-cleandb" }, dependsOnMethods =
        { "initializeProperties" })
    public void setContext() throws Exception
    {
        super.springTestContextPrepareTestInstance();
    }

    @BeforeTest(groups =
        { "system-cleandb" }, dependsOnMethods =
        { "setContext" })
    public void createDataStore()
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

    @AfterTest(groups =
        { "system-cleandb" })
    public void testingThis()
    {
        ((GenericApplicationContext) applicationContext).destroy();
    }

    @BeforeClass(alwaysRun = true)
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

    protected static <T> T create(Builder<T> builder) throws Exception
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
        return RefreshingMatcherProxy.newInstance(new InSpaceMatcher(space), this);
    }

    protected Matcher<Object> hasNoSpace()
    {
        return RefreshingMatcherProxy.newInstance(new InSpaceMatcher(), this);
    }

    protected Matcher<Object> inExperiment(Experiment experiment)
    {
        return RefreshingMatcherProxy.newInstance(new InExperimentMatcher(refresh(experiment)),
                this);
    }

    protected Matcher<Object> hasNoExperiment()
    {
        return RefreshingMatcherProxy.newInstance(new InExperimentMatcher(), this);
    }

    protected Matcher<Experiment> inProject(Project project)
    {
        return RefreshingMatcherProxy.newInstance(new InProjectMatcher(refresh(project)), this);
    }

    protected Matcher<ExternalData> inSample(Sample sample)
    {
        return RefreshingMatcherProxy.newInstance(new InSampleMatcher(refresh(sample)), this);
    }

    protected Matcher<ExternalData> hasParents(ExternalData first, ExternalData... rest)
    {
        return RefreshingMatcherProxy.newInstance(new ExternalDataHasParentsMatcher(first, rest),
                this);
    }

    protected Matcher<ExternalData> hasChildren(ExternalData first, ExternalData... rest)
    {
        return RefreshingMatcherProxy.newInstance(new ExternalDataHasChildrenMatcher(first, rest),
                this);
    }

    protected Matcher<Object> hasNoParents()
    {
        return RefreshingMatcherProxy.newInstance(new HasNoParentMatcher(), this);
    }

    protected Matcher<ExternalData> hasNoChildren()
    {
        return RefreshingMatcherProxy.newInstance(new HasNoChildrenMatcher(), this);
    }

    protected Matcher<ExternalData> hasNoSample()
    {
        return RefreshingMatcherProxy.newInstance(new ExternalDataHasNoSampleMatcher(), this);
    }

    protected Matcher<Sample> hasParents(Sample first, Sample... rest)
    {
        return RefreshingMatcherProxy.newInstance(new SampleHasParentsMatcher(first, rest), this);
    }

    protected Matcher<Object> hasNoParent()
    {
        return RefreshingMatcherProxy.newInstance(new HasNoParentMatcher(), this);
    }

    protected Matcher<Sample> hasContainer(Sample container)
    {
        return RefreshingMatcherProxy.newInstance(
                new SampleHasContainerMatcher(refresh(container)), this);
    }

    protected Matcher<ExternalData> hasContainer(ExternalData container)
    {
        return RefreshingMatcherProxy.newInstance(new ExternalDataHasContainerMatcher(
                refresh(container)), this);
    }

    protected Matcher<Object> hasNoContainer()
    {
        return RefreshingMatcherProxy.newInstance(new HasNoContainerMatcher(), this);
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

    protected ExternalData refresh(ExternalData data)
    {
        return etlService.tryGetDataSet(systemSessionToken, data.getCode());
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
}
