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

import static org.hamcrest.CoreMatchers.is;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import org.hamcrest.CoreMatchers;
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
import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.DataSetNode;
import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.EntityGraphGenerator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.EntityNode;
import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.ExperimentNode;
import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.SampleNode;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.DataSetTypeWithoutExperimentChecker;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.IndexMode;
import ch.systemsx.cisd.openbis.generic.shared.IServiceForDataStoreServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeWithRegistrationAndModificationDate;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
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
import ch.systemsx.cisd.openbis.systemtest.base.matcher.ExternalDataHasComponentsMatcher;
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

    protected IServiceForDataStoreServer etlService;

    protected MockHttpServletRequest request;

    protected IDAOFactory daoFactory;

    protected IRelationshipService relationshipService;

    protected ISessionManager<Session> sessionManager;

    protected String systemSessionToken;

    protected EntityRepository repository;
    
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
        System.setProperty(DataSetTypeWithoutExperimentChecker.PROPERTY_KEY, "  NO-EXP-.* ,   NE.*  ");
    }

    private void setContext() throws Exception
    {
        super.springTestContextPrepareTestInstance();
    }

    private void createDataStore()
    {
        DataStorePE dataStore = new DataStorePE();
        dataStore.setCode("STANDARD");
        dataStore.setDatabaseInstanceUUID(UUID.randomUUID().toString());
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
    public void setETLService(IServiceForDataStoreServer etlService)
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

    protected DataSetUpdateBuilder anUpdateOf(AbstractExternalData dataset)
    {
        return new DataSetUpdateBuilder(commonServer, genericServer, refresh(dataset));
    }

    protected DataSetDeletionBuilder trash(AbstractExternalData dataset)
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

    protected Matcher<AbstractExternalData> inSample(Sample sample)
    {
        return new InSampleMatcher(refresh(sample));
    }

    protected Matcher<AbstractExternalData> hasParents(AbstractExternalData first, AbstractExternalData... rest)
    {
        return new ExternalDataHasParentsMatcher(refresh(first), refresh(rest));
    }

    protected Matcher<AbstractExternalData> hasChildren(AbstractExternalData first, AbstractExternalData... rest)
    {
        return new ExternalDataHasChildrenMatcher(refresh(first), refresh(rest));
    }

    protected Matcher<Object> hasNoParents()
    {
        return new HasNoParentMatcher();
    }

    protected Matcher<AbstractExternalData> hasNoChildren()
    {
        return new HasNoChildrenMatcher();
    }

    protected Matcher<AbstractExternalData> hasNoSample()
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

    protected ExternalDataHasContainerMatcher hasContainer(AbstractExternalData container)
    {
        return new ExternalDataHasContainerMatcher(refresh(container));
    }

    protected Matcher<AbstractExternalData> hasComponents(AbstractExternalData first, AbstractExternalData... rest)
    {
        return new ExternalDataHasComponentsMatcher(refresh(first), refresh(rest));
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

    protected AbstractExternalData[] refresh(AbstractExternalData[] dataSets)
    {
        AbstractExternalData[] result = new AbstractExternalData[dataSets.length];
        int i = 0;
        for (AbstractExternalData data : dataSets)
        {
            result[i] = refresh(data);
            i++;
        }
        return result;
    }

    protected AbstractExternalData refresh(AbstractExternalData data)
    {
        return etlService.tryGetDataSet(systemSessionToken, data.getCode());
    }

    protected Space refresh(Space space)
    {
        for (Space refreshed : commonServer.listSpaces(systemSessionToken))
        {
            if (space.getId() == refreshed.getId())
            {
                return space;
            }
        }
        throw new IllegalArgumentException("Space not found: " + space.getCode());
    }

    protected EntityGraphGenerator parseAndCreateGraph(String graphDefinition)
    {
        EntityGraphGenerator g = new EntityGraphGenerator();
        g.parse(graphDefinition);
        createGraph(g);
        assertEquals(removeSolitaryNodes(graphDefinition), renderGraph(g, true));
        repository.gatherModifictaionInfos();
        return g;
    }
    
    private String removeSolitaryNodes(String graphDefinition)
    {
        StringBuilder builder = new StringBuilder();
        String[] lines = graphDefinition.split("\n");
        for (String line : lines)
        {
            if (line.contains(","))
            {
                builder.append(line).append("\n");
            }
        }
        return builder.toString();
    }
    
    protected String renderGraph(EntityGraphGenerator g)
    {
        return renderGraph(g, false);
    }
    
    private String renderGraph(EntityGraphGenerator g, boolean showWhereIAm)
    {
        repository.refreshGraph(showWhereIAm);
        StringBuilder builder = new StringBuilder();
        for (ExperimentNode experimentNode : g.getExperiments().values())
        {
            StringBuilder builder2 = new StringBuilder();
            render(builder2, "samples", repository.getSampleNode(experimentNode));
            render(builder2, "data sets", repository.getDataSetNodes(experimentNode));
            appendNodeTo(builder, experimentNode, builder2);
        }
        for (SampleNode sampleNode : g.getSamples().values())
        {
            StringBuilder builder2 = new StringBuilder();
            render(builder2, "data sets", repository.getDataSetNodes(sampleNode));
            appendNodeTo(builder, sampleNode, builder2);
        }
        for (DataSetNode dataSetNode : g.getDataSets().values())
        {
            StringBuilder builder2 = new StringBuilder();
            render(builder2, "components", repository.getComponentDataSetNodes(dataSetNode));
            render(builder2, "parents", repository.getParentDataSetNodes(dataSetNode));
            appendNodeTo(builder, dataSetNode, builder2);
        }
        return builder.toString();
    }

    private void appendNodeTo(StringBuilder builder, EntityNode entityNode, StringBuilder builder2)
    {
        if (builder2.length() > 0)
        {
            builder.append(entityNode.getCodeAndType());
            
            builder.append(builder2).append("\n");
        }
    }
    
    private void render(StringBuilder builder, String name, Collection<? extends EntityNode> nodes)
    {
        if (nodes.isEmpty())
        {
            return;
        }
        builder.append(", ").append(name).append(":");
        for (EntityNode node : nodes)
        {
            builder.append(' ').append(node.getCodeAndType());
        }
    }
    
    private void createGraph(EntityGraphGenerator g)
    {
        repository = new EntityRepository();
        Space space = create(aSpace());
        Project project = create(aProject().inSpace(space));
        createExperiments(project, g);
        createSamples(space, g);
        createDataSets(g);
    }

    private void createDataSets(EntityGraphGenerator g)
    {
        List<DataSetNode> dataSetNodes = new ArrayList<DataSetNode>(g.getDataSets().values());
        Collections.reverse(dataSetNodes);
        for (DataSetNode dataSetNode : dataSetNodes)
        {
            ExternalDataBuilder dataSet = aDataSet().withType(dataSetNode.getType());
            if (dataSetNode.getSample() != null)
            {
                dataSet.inSample(repository.getSample(dataSetNode.getSample()));
            } else if (dataSetNode.getExperiment() != null)
            {
                dataSet.inExperiment(repository.getExperiment(dataSetNode.getExperiment()));
            }
            List<DataSetNode> components = dataSetNode.getComponents();
            if (components.isEmpty() == false)
            {
                dataSet.asContainer();
                for (DataSetNode component : components)
                {
                    AbstractExternalData componentDataSet = repository.getDataSet(component);
                    if (componentDataSet == null)
                    {
                        throw new IllegalStateException("Data set " + component.getCode() 
                                + " is specified as component of " + dataSetNode.getCode() 
                                + " but hasn't yet created.");
                    }
                    dataSet.withComponent(componentDataSet);
                }
            }
            List<DataSetNode> parents = dataSetNode.getParents();
            for (DataSetNode parent : parents)
            {
                AbstractExternalData parentDataSet = repository.getDataSet(parent);
                if (parentDataSet == null)
                {
                    throw new IllegalStateException("Data set " + parent.getCode() 
                            + " is specified as parent of " + dataSetNode.getCode() 
                            + " but hasn't yet created.");
                }
                dataSet.withParent(parentDataSet);
            }
            addToRepository(dataSetNode, dataSet);
        }
    }

    private void createSamples(Space space, EntityGraphGenerator g)
    {
        for (SampleNode sampleNode : g.getSamples().values())
        {
            SampleBuilder sample = aSample().inSpace(space);
            ExperimentNode experimentNode = sampleNode.getExperiment();
            if (experimentNode != null)
            {
                sample.inExperiment(repository.getExperiment(experimentNode));
            }
            addToRepository(sampleNode, sample);
        }
    }

    private void createExperiments(Project project, EntityGraphGenerator g)
    {
        for (ExperimentNode experimentNode : g.getExperiments().values())
        {
            addToRepository(experimentNode, create(anExperiment().inProject(project)));
        }
    }

    private void addToRepository(ExperimentNode experimentNode, Experiment experiment)
    {
        try
        {
            repository.put(experimentNode, experiment);
        } catch (Exception ex)
        {
            throw new RuntimeException("Error while creating experiment for node " + experimentNode.getCode() 
                    + ": " + ex.getMessage(), ex);
        }
    }

    private void addToRepository(SampleNode sampleNode, SampleBuilder sample)
    {
        try
        {
            repository.put(sampleNode, create(sample));
        } catch (Exception ex)
        {
            throw new RuntimeException("Error while creating sample for node " + sampleNode.getCode() 
                    + ": " + ex.getMessage(), ex);
        }
    }

    private void addToRepository(DataSetNode dataSetNode, ExternalDataBuilder dataSet)
    {
        try
        {
            repository.put(dataSetNode, create(dataSet));
        } catch (Exception ex)
        {
            throw new RuntimeException("Error while creating data set for node " + dataSetNode.getCodeAndType() 
                    + ": " + ex.getMessage(), ex);
        }
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
        return new SpaceIdentifier(space.getCode());
    }

    public static DatabaseInstanceIdentifier id(DatabaseInstance dbin)
    {
        return new DatabaseInstanceIdentifier(dbin.getCode());
    }

    public static SampleIdentifier id(Sample sample)
    {
        if (sample.getSpace() == null)
        {
            return new SampleIdentifier(sample.getCode());
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
        if (actual instanceof AbstractExternalData)
        {
            refreshed = (T) refresh((AbstractExternalData) actual);
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
    
    protected static <T> Matcher<T> isNot(Matcher<T> matcher)
    {
        return CoreMatchers.not(is(matcher));
    }
    
    public final class EntityRepository
    {
        private Map<Long, Experiment> experimentsNodeToDtoMap = new TreeMap<Long, Experiment>();
        private Map<Long, ExperimentNode> experimentDtoToNodeMap = new TreeMap<Long, ExperimentNode>();
        private Map<Long, ModificationInfo> experimentModificationInfoByNodeId = new HashMap<Long, ModificationInfo>();
        private Set<ExperimentNode> modifiedExperimentNodes = new HashSet<ExperimentNode>();
        
        private Map<Long, Sample> samplesNodeToDtoMap = new TreeMap<Long, Sample>();
        private Map<Long, SampleNode> samplesDtoToNodeMap = new TreeMap<Long, SampleNode>();
        private Map<Long, ModificationInfo> sampleModificationInfoByNodeId = new HashMap<Long, ModificationInfo>();
        private Set<SampleNode> modifiedSampleNodes = new HashSet<SampleNode>();

        private Map<Long, AbstractExternalData> dataSetsNodeToDtoMap = new TreeMap<Long, AbstractExternalData>();
        private Map<Long, DataSetNode> dataSetsDtoToNodeMap = new TreeMap<Long, DataSetNode>();
        private Map<Long, ModificationInfo> dataSetModificationInfoByNodeId = new HashMap<Long, ModificationInfo>();
        private Set<DataSetNode> modifiedDataSetNodes = new HashSet<DataSetNode>();

        private Map<Long, Set<Long>> experimentSamplesMap = new HashMap<Long, Set<Long>>();
        private Map<Long, Set<Long>> experimentDataSetsMap = new HashMap<Long, Set<Long>>();
        private Map<Long, Set<Long>> sampleDataSetsMap = new HashMap<Long, Set<Long>>();
        private Map<Long, Set<Long>> componentDataSetsMap = new HashMap<Long, Set<Long>>();
        private Map<Long, Set<Long>> parentsDataSetsMap = new HashMap<Long, Set<Long>>();
        
        public String renderNodeToDtoMapping()
        {
            StringBuilder builder = new StringBuilder();
            for (Entry<Long, Experiment> entry : experimentsNodeToDtoMap.entrySet())
            {
                render(builder, "E", entry.getKey(), entry.getValue());
            }
            for (Entry<Long, Sample> entry : samplesNodeToDtoMap.entrySet())
            {
                render(builder, "S", entry.getKey(), entry.getValue());
            }
            for (Entry<Long, AbstractExternalData> entry : dataSetsNodeToDtoMap.entrySet())
            {
                render(builder, "DS", entry.getKey(), entry.getValue());
            }
            return builder.toString();
        }
        
        private void render(StringBuilder builder, String prefix, Long id, 
                CodeWithRegistrationAndModificationDate<?> entity)
        {
            builder.append(prefix).append(id).append(" -> ").append(entity.getCode()).append(" (");
            builder.append(entity.getModifier().getUserId()).append(", ");
            builder.append(entity.getModificationDate()).append(")\n");
        }
        
        public Experiment getExperiment(ExperimentNode experimentNode)
        {
            return experimentsNodeToDtoMap.get(experimentNode.getId());
        }
        
        public Sample getSample(SampleNode sampleNode)
        {
            return samplesNodeToDtoMap.get(sampleNode.getId());
        }
        
        public AbstractExternalData getDataSet(DataSetNode dataSetNode)
        {
            return dataSetsNodeToDtoMap.get(dataSetNode.getId());
        }
        
        void refreshGraph(boolean showWhereIAm)
        {
            for (Long id : experimentsNodeToDtoMap.keySet())
            {
                experimentsNodeToDtoMap.put(id, refresh(experimentsNodeToDtoMap.get(id)));
            }
            for (Long id : samplesNodeToDtoMap.keySet())
            {
                samplesNodeToDtoMap.put(id, refresh(samplesNodeToDtoMap.get(id)));
            }
            for (Long id : dataSetsNodeToDtoMap.keySet())
            {
                dataSetsNodeToDtoMap.put(id, refresh(dataSetsNodeToDtoMap.get(id)));
            }
            experimentSamplesMap.clear();
            for (Sample sample : samplesNodeToDtoMap.values())
            {
                Experiment experiment = sample.getExperiment();
                if (experiment != null)
                {
                    Long id = experiment.getId();
                    Set<Long> sampleIds = experimentSamplesMap.get(id);
                    if (sampleIds == null)
                    {
                        sampleIds = new TreeSet<Long>();
                        experimentSamplesMap.put(id, sampleIds);
                    }
                    sampleIds.add(sample.getId());
                }
            }
            experimentDataSetsMap.clear();
            for (AbstractExternalData dataSet : dataSetsNodeToDtoMap.values())
            {
                addToDataSetsMap(experimentDataSetsMap, dataSet, dataSet.getExperiment());
            }
            sampleDataSetsMap.clear();
            componentDataSetsMap.clear();
            for (AbstractExternalData dataSet : dataSetsNodeToDtoMap.values())
            {
                addToDataSetsMap(sampleDataSetsMap, dataSet, dataSet.getSample());
                List<ContainerDataSet> containerDataSets = dataSet.getContainerDataSets();
                for (ContainerDataSet containerDataSet : containerDataSets)
                {
                    addToDataSetsMap(componentDataSetsMap, dataSet, containerDataSet);
                }
                Collection<AbstractExternalData> parents = dataSet.getParents();
                for (AbstractExternalData parentDataSet : parents)
                {
                    addToDataSetsMap(parentsDataSetsMap, parentDataSet, dataSet);
                }
            }
            if (showWhereIAm)
            {
                printWhereIAm();
            }
            System.out.println("Entities mapping:\n" + renderNodeToDtoMapping());
        }

        private void printWhereIAm()
        {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            for (int i = 5, n = Math.min(stackTrace.length, 9); i < n; i++)
            {
                StackTraceElement element = stackTrace[i];
                System.out.print(element.getMethodName() + " <- ");
            }
            System.out.println("...");
        }
        
        private void addToDataSetsMap(Map<Long, Set<Long>> holderDataSetsMap, 
                AbstractExternalData dataSet, IIdHolder idHolder)
        {
            if (idHolder != null)
            {
                Long id = idHolder.getId();
                Set<Long> dataSetIds = holderDataSetsMap.get(id);
                if (dataSetIds == null)
                {
                    dataSetIds = new TreeSet<Long>();
                    holderDataSetsMap.put(id, dataSetIds);
                }
                dataSetIds.add(dataSet.getId());
            }
        }
        
        void gatherModifictaionInfos()
        {
            for (Entry<Long, Experiment> entry : experimentsNodeToDtoMap.entrySet())
            {
                experimentModificationInfoByNodeId.put(entry.getKey(), new ModificationInfo(entry.getValue()));
            }
            for (Entry<Long, Sample> entry : samplesNodeToDtoMap.entrySet())
            {
                sampleModificationInfoByNodeId.put(entry.getKey(), new ModificationInfo(entry.getValue()));
            }
            for (Entry<Long, AbstractExternalData> entry : dataSetsNodeToDtoMap.entrySet())
            {
                dataSetModificationInfoByNodeId.put(entry.getKey(), new ModificationInfo(entry.getValue()));
            }
        }
        
        void put(ExperimentNode experimentNode, Experiment experiment)
        {
            experimentsNodeToDtoMap.put(experimentNode.getId(), experiment);
            experimentDtoToNodeMap.put(experiment.getId(), experimentNode);
        }
        
        public void assertModified(ExperimentNode...experimentNodes)
        {
            assertModificationInfo(true, experimentModificationInfoByNodeId, experimentsNodeToDtoMap, 
                    Arrays.<EntityNode>asList(experimentNodes));
            modifiedExperimentNodes.addAll(Arrays.asList(experimentNodes));
        }
        
        void put(SampleNode sampleNode, Sample sample)
        {
            samplesNodeToDtoMap.put(sampleNode.getId(), sample);
            samplesDtoToNodeMap.put(sample.getId(), sampleNode);
        }

        public void assertModified(SampleNode...sampleNodes)
        {
            assertModificationInfo(true, sampleModificationInfoByNodeId, samplesNodeToDtoMap, 
                    Arrays.<EntityNode>asList(sampleNodes));
            modifiedSampleNodes.addAll(Arrays.asList(sampleNodes));
        }
        
        void put(DataSetNode dataSetNode, AbstractExternalData dataSet)
        {
            dataSetsNodeToDtoMap.put(dataSetNode.getId(), dataSet);
            dataSetsDtoToNodeMap.put(dataSet.getId(), dataSetNode);
        }
        
        public void assertModified(DataSetNode...dataSetNodes)
        {
            assertModificationInfo(true, dataSetModificationInfoByNodeId, dataSetsNodeToDtoMap, 
                    Arrays.<EntityNode>asList(dataSetNodes));
            modifiedDataSetNodes.addAll(Arrays.asList(dataSetNodes));
        }
        
        public void assertUnmodified(EntityGraphGenerator g)
        {
            Set<EntityNode> unmodifiedExperimentNodes = new HashSet<EntityNode>(g.getExperiments().values());
            unmodifiedExperimentNodes.removeAll(modifiedExperimentNodes);
            assertModificationInfo(false, experimentModificationInfoByNodeId, experimentsNodeToDtoMap, unmodifiedExperimentNodes);
            Set<EntityNode> unmodifiedSampleNodes = new HashSet<EntityNode>(g.getSamples().values());
            unmodifiedSampleNodes.removeAll(modifiedSampleNodes);
            assertModificationInfo(false, sampleModificationInfoByNodeId, samplesNodeToDtoMap, unmodifiedSampleNodes);
            Set<EntityNode> unmodifiedDataSetNodes = new HashSet<EntityNode>(g.getDataSets().values());
            unmodifiedDataSetNodes.removeAll(modifiedDataSetNodes);
            assertModificationInfo(false, dataSetModificationInfoByNodeId, dataSetsNodeToDtoMap, unmodifiedDataSetNodes);
        }
        
        private void assertModificationInfo(boolean modified, Map<Long, ModificationInfo> previousInfos,
                Map<Long, ? extends CodeWithRegistrationAndModificationDate<?>> nodeToDtoMap,
                Collection<EntityNode> entityNodes)
        {
            for (EntityNode node : entityNodes)
            {
                ModificationInfo previous = previousInfos.get(node.getId());
                assertNotNull(node.getCode() + " no previous modification info", previous);
                CodeWithRegistrationAndModificationDate<?> entity = nodeToDtoMap.get(node.getId());
                assertNotNull(node.getCode() + " unknown", entity);
                ModificationInfo current = new ModificationInfo(entity);
                if (modified)
                {
                    assertEquals(node.getCode() + " has unexpectedly still the old modifier: " + current.modifier, 
                            false, current.modifier.equals(previous.modifier));
                    assertEquals(node.getCode() + " has unexpectedly still the old modification date: " + current.modificationDate, 
                            true, current.modificationDate.getTime() > previous.modificationDate.getTime());
                } else
                {
                    assertEquals(node.getCode() + " has unexpectedly a new modifier: ",
                            previous.modifier, current.modifier);
                    assertEquals(node.getCode() + " has unexpectedly a new modification date:", 
                            previous.modificationDate, current.modificationDate);
                }
            }
        }

        Set<SampleNode> getSampleNode(ExperimentNode experimentNode)
        {
            Set<SampleNode> result = new LinkedHashSet<SampleNode>();
            Experiment experiment = experimentsNodeToDtoMap.get(experimentNode.getId());
            if (experiment != null)
            {
                Set<Long> sampleDtoIds = experimentSamplesMap.get(experiment.getId());
                if (sampleDtoIds != null)
                {
                    for (Long dtoId : sampleDtoIds)
                    {
                        result.add(samplesDtoToNodeMap.get(dtoId));
                    }
                }
            }
            return result;
        }
        
        Set<DataSetNode> getDataSetNodes(ExperimentNode experimentNode)
        {
            return getDataSetNodes(experimentDataSetsMap, experimentsNodeToDtoMap.get(experimentNode.getId()));
        }

        Set<DataSetNode> getDataSetNodes(SampleNode sampleNode)
        {
            return getDataSetNodes(sampleDataSetsMap, samplesNodeToDtoMap.get(sampleNode.getId()));
        }
        
        Set<DataSetNode> getComponentDataSetNodes(DataSetNode containerDataSetNode)
        {
            return getDataSetNodes(componentDataSetsMap, dataSetsNodeToDtoMap.get(containerDataSetNode.getId()));
        }
        
        Set<DataSetNode> getParentDataSetNodes(DataSetNode childDataSetNode)
        {
            return getDataSetNodes(parentsDataSetsMap, dataSetsNodeToDtoMap.get(childDataSetNode.getId()));
        }
        
        private Set<DataSetNode> getDataSetNodes(Map<Long, Set<Long>> idHolderDataSetsMap, IIdHolder experiment)
        {
            Set<DataSetNode> result = new TreeSet<DataSetNode>();
            if (experiment != null)
            {
                Set<Long> dataSetDtoIds = idHolderDataSetsMap.get(experiment.getId());
                if (dataSetDtoIds != null)
                {
                    for (Long dtoId : dataSetDtoIds)
                    {
                        result.add(dataSetsDtoToNodeMap.get(dtoId));
                    }
                }
            }
            return result;
        }
    }
    
    private static final class ModificationInfo
    {
        private final String modifier;
        private final Date modificationDate;

        ModificationInfo(CodeWithRegistrationAndModificationDate<?> entity)
        {
            modifier = entity.getModifier().getUserId();
            modificationDate = entity.getModificationDate();
        }
    }
    
}
