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

package ch.systemsx.cisd.openbis.generic.client.web.server;

import static ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants.DATA_STORE_SERVER_WEB_APPLICATION_NAME;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridCustomColumnInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetWithEntityTypes;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.CacheManager;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.CacheManager.TokenBasedResultSetKeyGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.CachedResultSetManager;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.CachedResultSetManagerTest;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.DefaultResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.ICustomColumnsProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IOriginalDataProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IResultSetKeyGenerator;
import ch.systemsx.cisd.openbis.generic.server.SessionConstants;
import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.shared.CommonTestUtils;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericValueEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomColumn;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewVocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.FileFormatTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExternalDataTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.VocabularyTranslator;

/**
 * Test cases for corresponding {@link CommonClientService} class.
 * 
 * @author Christian Ribeaud
 */
public final class CommonClientServiceTest extends AbstractClientServiceTest
{
    private static final String DATA_STORE_BASE_URL = "baseURL";

    private static final String BASE_INDEX_URL = "indexURL";

    private static final String CIFEX_URL = "cifexURL";

    private static final String CIFEX_RECIPIENT = "cifexRecipient";

    private static final String TERMS_SESSION_KEY = "termsSessionKey";

    private CommonClientService commonClientService;

    private ICommonServer commonServer;

    private final static ListSampleDisplayCriteria createListCriteria()
    {
        final ListSampleCriteria criteria = new ListSampleCriteria();
        final SampleType sampleType = createSampleType("MASTER_PLATE", "DB1");
        criteria.setSampleType(sampleType);
        return new ListSampleDisplayCriteria(criteria);
    }

    private final static SampleType createSampleType(final String code, final String dbCode)
    {
        final SampleType sampleType = new SampleType();
        sampleType.setCode(code);
        sampleType.setDatabaseInstance(createDatabaseInstance(dbCode));
        return sampleType;
    }

    private final static void assertDataTypeEquals(final DataType dataType1,
            final DataType dataType2)
    {
        assertEquals(dataType1.getCode().name(), dataType2.getCode().name());
        assertEquals(dataType1.getDescription(), dataType2.getDescription());
    }

    private final static VocabularyPE createVocabulary()
    {
        final VocabularyPE vocabularyPE = new VocabularyPE();
        vocabularyPE.setCode("USER.COLOR");
        vocabularyPE.setDescription("Vocabulary color");
        vocabularyPE.setRegistrator(ManagerTestTool.EXAMPLE_PERSON);
        vocabularyPE.setDatabaseInstance(ManagerTestTool.EXAMPLE_DATABASE_INSTANCE);
        vocabularyPE.addTerm(createVocabularyTerm("RED"));
        vocabularyPE.addTerm(createVocabularyTerm("BLACK"));
        vocabularyPE.addTerm(createVocabularyTerm("WHITE"));
        return vocabularyPE;
    }

    private final static VocabularyTermPE createVocabularyTerm(final String code)
    {
        final VocabularyTermPE vocabularyTermPE = new VocabularyTermPE();
        vocabularyTermPE.setCode(code);
        vocabularyTermPE.setRegistrator(ManagerTestTool.EXAMPLE_PERSON);
        return vocabularyTermPE;
    }

    //
    // AbstractClientServiceTest
    //

    @Override
    @BeforeMethod
    public final void setUp()
    {
        super.setUp();
        commonServer = context.mock(ICommonServer.class);
        commonClientService = new CommonClientService(commonServer, requestContextProvider);
        commonClientService.setCifexURL(CIFEX_URL);
        commonClientService.setCifexRecipient(CIFEX_RECIPIENT);
    }

    @Test
    public void testGetExperimentInfoByIdentifier()
    {
        context.checking(new Expectations()
            {
                {
                    prepareGetSessionToken(this);

                    one(commonServer).getExperimentInfo(SESSION_TOKEN,
                            new ExperimentIdentifier("p1", "exp1"));
                    Experiment experiment = new Experiment();
                    // Check that escaping is performed
                    experiment.setPermId("<b>permId</b>");
                    experiment.setProperties(Arrays.asList(createXmlProperty()));
                    will(returnValue(experiment));
                }
            });

        Experiment info = commonClientService.getExperimentInfo("p1/exp1");

        IEntityProperty transformedXMLProperty = info.getProperties().get(0);
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><b>hello</b>",
                transformedXMLProperty.tryGetAsString());
        assertEquals("<root>hello</root>", transformedXMLProperty.tryGetOriginalValue());
        assertEquals("<b>permId</b>", info.getPermId());
        context.assertIsSatisfied();
    }

    @Test
    public void testGetExperimentInfoByTechId()
    {
        final TechId id = new TechId(4711L);
        context.checking(new Expectations()
            {
                {
                    prepareGetSessionToken(this);

                    one(commonServer).getExperimentInfo(SESSION_TOKEN, id);
                    Experiment experiment = new Experiment();
                    experiment.setProperties(Arrays.asList(createXmlProperty()));
                    will(returnValue(experiment));
                }
            });

        Experiment info = commonClientService.getExperimentInfo(id);

        IEntityProperty transformedXMLProperty = info.getProperties().get(0);
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><b>hello</b>",
                transformedXMLProperty.tryGetAsString());
        assertEquals("<root>hello</root>", transformedXMLProperty.tryGetOriginalValue());
        context.assertIsSatisfied();
    }

    private IEntityProperty createXmlProperty()
    {
        GenericValueEntityProperty property = new GenericValueEntityProperty();
        PropertyType propertyType = new PropertyType();
        propertyType.setDataType(new DataType(DataTypeCode.XML));
        propertyType
                .setTransformation(("<xsl:stylesheet version='1.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>"
                        + "<xsl:template match='/'><b><xsl:value-of select='.'/></b></xsl:template>"
                        + "</xsl:stylesheet>"));
        property.setPropertyType(propertyType);
        property.setValue("<root>hello</root>");
        return property;
    }

    @SuppressWarnings("unchecked")
    @Test
    public final void testListSamples()
    {
        List<Sample> entities = Arrays.asList(new Sample());
        final ListSampleDisplayCriteria criteria = createListCriteria();
        prepareListEntities(entities, criteria);
        // this call is to obtain sample types
        context.checking(new Expectations()
            {
                {
                    IResultSet<String, String> entityTypeResultMock =
                            context.mock(IResultSet.class);
                    one(entityTypeResultMock).getList();
                    will(returnValue(createGridRowModels(new ArrayList<GridCustomColumnInfo>())));

                    one(resultSetManager).getResultSet(with(SESSION_TOKEN),
                            with(Expectations.any(IResultSetConfig.class)),
                            with(Expectations.any(IOriginalDataProvider.class)));
                    will(returnValue(entityTypeResultMock));
                }
            });
        final ResultSetWithEntityTypes<Sample> resultSet =
                commonClientService.listSamples(criteria);
        assertEqualEntities(entities, resultSet.getResultSet());
        context.assertIsSatisfied();
    }

    @Test
    public final void testListProjects()
    {
        List<Project> entities = Arrays.asList(new Project());
        final DefaultResultSetConfig<String, Project> criteria =
                DefaultResultSetConfig.createFetchAll();
        prepareListEntities(entities, criteria);

        final ResultSet<Project> resultSet = commonClientService.listProjects(criteria);
        assertEqualEntities(entities, resultSet);
        context.assertIsSatisfied();
    }

    @Test
    public final void testListVocabularies()
    {
        final Vocabulary vocabulary = VocabularyTranslator.translate(createVocabulary());
        final List<Vocabulary> entities = Collections.singletonList(vocabulary);
        final boolean excludeInternals = true;
        final DefaultResultSetConfig<String, Vocabulary> criteria =
                DefaultResultSetConfig.createFetchAll();
        prepareListEntities(entities, criteria);

        ResultSet<Vocabulary> resultSet =
                commonClientService.listVocabularies(false, excludeInternals, criteria);
        assertEqualEntities(entities, resultSet);
        context.assertIsSatisfied();
    }

    private final <T> void prepareListEntities(List<T> entities,
            final DefaultResultSetConfig<String, T> criteria)
    {
        final String resultSetKey = "131";
        GridRowModels<T> rowModels = createGridRowModels(entities);
        final DefaultResultSet<String, T> defaultResultSet =
                new DefaultResultSet<String, T>(resultSetKey, rowModels, entities.size());
        context.checking(new Expectations()
            {
                {
                    prepareGetHttpSession(this);
                    prepareGetSessionToken(this);
                    prepareGetResultSetManager(this);

                    prepareGetResultSet(this, criteria);
                    will(returnValue(defaultResultSet));
                }
            });
    }

    private <T> GridRowModels<T> createGridRowModels(List<T> entities)
    {
        return CachedResultSetManagerTest.createGridRowModels(entities);
    }

    @SuppressWarnings("unchecked")
    private <T> void prepareGetResultSet(Expectations exp, IResultSetConfig<String, T> criteria)
    {
        exp.one(resultSetManager).getResultSet(exp.with(SESSION_TOKEN), exp.with(criteria),
                exp.with(Expectations.any(IOriginalDataProvider.class)));
    }

    private <T> void assertEqualEntities(List<T> entities, final ResultSet<T> resultSet)
    {
        assertEquals(entities.size(), resultSet.getList().size());
        assertEquals(entities.get(0), resultSet.getList().get(0).getOriginalObject());
        assertEquals(entities.size(), resultSet.getTotalLength());
    }

    @Test
    public final void testListDataTypes()
    {
        final DataType dataType = new DataType();
        dataType.setCode(DataTypeCode.INTEGER);
        dataType.setDescription("The description");
        context.checking(new Expectations()
            {
                {
                    prepareGetSessionToken(this);

                    one(commonServer).listDataTypes(SESSION_TOKEN);
                    will(returnValue(Collections.singletonList(dataType)));
                }
            });
        final List<DataType> dataTypes = commonClientService.listDataTypes();
        assertEquals(1, dataTypes.size());
        assertDataTypeEquals(dataType, dataTypes.get(0));
        context.assertIsSatisfied();
    }

    @Test
    public final void testPrepareExportSamples()
    {
        final TableExportCriteria<Sample> criteria = new TableExportCriteria<Sample>();
        final CacheManager<String, TableExportCriteria<Sample>> manager = createCacheManager();
        context.checking(new Expectations()
            {
                {
                    prepareGetSessionToken(this);
                    prepareGetCacheManager(this, manager);
                }
            });
        final String key = commonClientService.prepareExportSamples(criteria);
        assertEquals("" + CounterBasedResultSetKeyGenerator.INIT_VALUE, key);
        assertEquals(criteria, manager.tryGetData(key));
        context.assertIsSatisfied();
    }

    private void prepareGetCacheManager(final Expectations exp,
            final CacheManager<String, TableExportCriteria<Sample>> manager)
    {
        prepareGetHttpSession(exp);
        exp.allowing(httpSession).getAttribute(SessionConstants.OPENBIS_EXPORT_MANAGER);
        exp.will(Expectations.returnValue(manager));
    }

    private <T> CacheManager<String, T> createCacheManager()
    {
        return new CacheManager<String, T>(new CounterBasedResultSetKeyGenerator());
    }

    private static final class CounterBasedResultSetKeyGenerator implements
            IResultSetKeyGenerator<String>
    {
        public static final int INIT_VALUE = 123;

        private static final long serialVersionUID = 1L;

        private int counter = INIT_VALUE;

        public final String createKey()
        {
            return "" + counter++;
        }
    }

    @Test
    public final void testRegisterPropertyType()
    {
        context.checking(new Expectations()
            {
                {
                    prepareGetSessionToken(this);

                    one(commonServer).registerPropertyType(with(SESSION_TOKEN),
                            with(aNonNull(PropertyType.class)));
                }
            });
        commonClientService.registerPropertyType(new PropertyType());
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterVocabulary()
    {
        context.checking(new Expectations()
            {
                {
                    prepareGetSessionToken(this);

                    one(commonServer).registerVocabulary(with(SESSION_TOKEN),
                            with(aNonNull(NewVocabulary.class)));
                }
            });
        commonClientService.registerVocabulary(TERMS_SESSION_KEY, new NewVocabulary());
        context.assertIsSatisfied();
    }

    @Test
    public void testListExternalDataForExperiment()
    {
        final TechId experimentId = CommonTestUtils.TECH_ID;
        final ExternalDataPE externalDataPE = new ExternalDataPE();
        final DataStorePE dataStorePE = new DataStorePE();
        dataStorePE.setDownloadUrl(DATA_STORE_BASE_URL);
        externalDataPE.setDataStore(dataStorePE);
        FileFormatTypePE fileFormatTypePE = new FileFormatTypePE();
        fileFormatTypePE.setCode("PNG");
        fileFormatTypePE.setDescription("Portable Network Graphics");
        externalDataPE.setFileFormatType(fileFormatTypePE);
        final ExternalData externalData =
                ExternalDataTranslator.translate(externalDataPE, BASE_INDEX_URL, false);
        context.checking(new Expectations()
            {
                {
                    prepareGetSessionToken(this);
                    allowing(httpSession).getAttribute(SessionConstants.OPENBIS_RESULT_SET_MANAGER);
                    will(returnValue(new CachedResultSetManager<String>(
                            new TokenBasedResultSetKeyGenerator(), new ICustomColumnsProvider()
                                {
                                    public List<GridCustomColumn> getGridCustomColumn(
                                            String sessionToken, String gridDisplayId)
                                    {
                                        return new ArrayList<GridCustomColumn>();
                                    }

                                })));

                    one(commonServer).listExperimentExternalData(SESSION_TOKEN, experimentId);
                    will(returnValue(Collections.singletonList(externalData)));
                }
            });

        DefaultResultSetConfig<String, ExternalData> resultSetConfig =
                DefaultResultSetConfig.createFetchAll();
        ResultSetWithEntityTypes<ExternalData> resultSet =
                commonClientService.listExperimentDataSets(experimentId, resultSetConfig);
        List<ExternalData> list = resultSet.getResultSet().getList().extractOriginalObjects();
        assertEquals(1, list.size());
        ExternalData data = list.get(0);
        assertEquals(DATA_STORE_BASE_URL + "/" + DATA_STORE_SERVER_WEB_APPLICATION_NAME, data
                .getDataStore().getDownloadUrl());
        assertEquals("PNG", data.getFileFormatType().getCode());
        assertEquals("Portable Network Graphics", data.getFileFormatType().getDescription());

        context.assertIsSatisfied();
    }

    @Test
    public void testUpdateDisplaySettings()
    {
        final DisplaySettings displaySettings = new DisplaySettings();
        context.checking(new Expectations()
            {
                {
                    prepareGetSessionToken(this);

                    one(commonServer).saveDisplaySettings(SESSION_TOKEN, displaySettings);
                }
            });
        commonClientService.updateDisplaySettings(displaySettings);

        context.assertIsSatisfied();
    }

    @Test
    public void testChangeUserHomeGroup()
    {
        final TechId groupId = CommonTestUtils.TECH_ID;
        context.checking(new Expectations()
            {
                {
                    prepareGetSessionToken(this);

                    one(commonServer).changeUserHomeSpace(SESSION_TOKEN, groupId);
                }
            });

        commonClientService.changeUserHomeGroup(groupId);

        context.assertIsSatisfied();
    }

    @Test
    public void testLogout()
    {
        final DisplaySettings displaySettings = new DisplaySettings();
        context.checking(new Expectations()
            {
                {
                    allowing(requestContextProvider).getHttpServletRequest();
                    will(returnValue(servletRequest));

                    allowing(servletRequest).getSession(false);
                    will(returnValue(httpSession));

                    one(httpSession).getAttribute(
                            SessionConstants.OPENBIS_SESSION_TOKEN_ATTRIBUTE_KEY);
                    will(returnValue(SESSION_TOKEN));

                    one(httpSession).removeAttribute(
                            SessionConstants.OPENBIS_SESSION_TOKEN_ATTRIBUTE_KEY);
                    one(httpSession).removeAttribute(SessionConstants.OPENBIS_SERVER_ATTRIBUTE_KEY);
                    one(httpSession).removeAttribute(SessionConstants.OPENBIS_RESULT_SET_MANAGER);
                    one(httpSession).removeAttribute(SessionConstants.OPENBIS_EXPORT_MANAGER);
                    one(httpSession).invalidate();

                    one(commonServer).saveDisplaySettings(SESSION_TOKEN, displaySettings);
                    one(commonServer).logout(SESSION_TOKEN);
                }
            });

        commonClientService.logout(displaySettings);

        context.assertIsSatisfied();
    }
}
