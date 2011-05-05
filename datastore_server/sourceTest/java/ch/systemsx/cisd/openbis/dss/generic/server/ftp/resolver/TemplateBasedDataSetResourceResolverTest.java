/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.ftp.resolver;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.ftpserver.ftplet.FtpFile;
import org.jmock.Expectations;
import org.springframework.beans.factory.BeanFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.io.IHierarchicalContent;
import ch.systemsx.cisd.common.io.IHierarchicalContentNode;
import ch.systemsx.cisd.common.test.TrackingMockery;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpConstants;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpPathResolverContext;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpServerConfig;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpServerConfigBuilder;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProviderTestWrapper;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;

/**
 * @author Kaloyan Enimanev
 */
public class TemplateBasedDataSetResourceResolverTest extends AssertJUnit
{

    private static final String SESSION_TOKEN = "token";

    private static final String EXP_ID = "/space/project/experiment";

    private static final long EXP_TECH_ID = 1L;

    private static final String SIMPLE_TEMPLATE = "${dataSetCode}";

    private static final String DS_TYPE1 = "DS_TYPE1";

    // private static final String TEMPLATE_WITH_FILENAMES =
    // "DS-${dataSetType}-${fileName}-${disambiguation}";


    private TrackingMockery context;
    private IETLLIMSService service;

    private IHierarchicalContentProvider hierarchicalContentProvider;

    private FtpPathResolverContext resolverContext;
    private TemplateBasedDataSetResourceResolver resolver;

    private Experiment experiment;

    @BeforeMethod
    public void setUp()
    {
        experiment = new Experiment();
        experiment.setIdentifier(EXP_ID);
        experiment.setId(EXP_TECH_ID);

        context = new TrackingMockery();
        service = context.mock(IETLLIMSService.class);
        hierarchicalContentProvider = context.mock(IHierarchicalContentProvider.class);

        resolverContext = new FtpPathResolverContext(SESSION_TOKEN, service, null);

        final BeanFactory beanFactory = context.mock(BeanFactory.class);
        context.checking(new Expectations()
            {
                {
                    allowing(beanFactory).getBean("hierarchical-content-provider");
                    will(returnValue(hierarchicalContentProvider));

                    ExperimentIdentifier experimentIdentifier =
                            new ExperimentIdentifierFactory(EXP_ID).createIdentifier();
                    allowing(service).tryToGetExperiment(SESSION_TOKEN, experimentIdentifier);
                    will(returnValue(experiment));
                }
            });
        ServiceProviderTestWrapper.setApplicationContext(beanFactory);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown()
    {
        ServiceProviderTestWrapper.restoreApplicationContext();
    }

    @Test
    public void testResolveSimpleTemplate()
    {
        FtpServerConfig config =
                new FtpServerConfigBuilder().withTemplate(SIMPLE_TEMPLATE).getConfig();
        resolver = new TemplateBasedDataSetResourceResolver(config);

        final String dataSetCode = "dataSetCode";

        String path = EXP_ID + FtpConstants.FILE_SEPARATOR + dataSetCode;

        List<ExternalData> dataSets =
                Arrays.asList(createDataSet("randomCode", "randomType"),
                        createDataSet(dataSetCode, DS_TYPE1),
                        createDataSet("randomCode2", "randomType2"));

        prepareExperimentListExpectations(dataSets);

        context.checking(new Expectations()
            {
                {
                    IHierarchicalContent content = getHierarchicalContentMock(dataSetCode);
                    IHierarchicalContentNode rootNode = getHierarchicalRootNodeMock(dataSetCode);

                    allowing(content).getNode(StringUtils.EMPTY);
                    will(returnValue(rootNode));
                }
            });

        FtpFile ftpFile = resolver.resolve(path, resolverContext);

        assertNotNull(ftpFile);
        assertEquals(dataSetCode, ftpFile.getName());

    }

    @Test
    public void testResolveSimpleTemplateRecursive()
    {
        FtpServerConfig config =
                new FtpServerConfigBuilder().withTemplate(SIMPLE_TEMPLATE).getConfig();
        resolver = new TemplateBasedDataSetResourceResolver(config);

        final String dataSetCode = "dataSetCode";
        final String subPath = "/level1/level2/fileName.txt";

        String path = EXP_ID + FtpConstants.FILE_SEPARATOR + dataSetCode + subPath;

        List<ExternalData> dataSets = Arrays.asList(createDataSet(dataSetCode, DS_TYPE1));

        prepareExperimentListExpectations(dataSets);

        context.checking(new Expectations()
            {
                {
                    IHierarchicalContent content = getHierarchicalContentMock(dataSetCode);
                    IHierarchicalContentNode mockNode =
                            context.mock(IHierarchicalContentNode.class);

                    allowing(content).getNode(subPath);
                    will(returnValue(mockNode));

                    one(mockNode).isDirectory();
                    will(returnValue(false));
                }
            });

        FtpFile ftpFile = resolver.resolve(path, resolverContext);

        assertNotNull(ftpFile);
        assertEquals("fileName.txt", ftpFile.getName());
        assertTrue(ftpFile.isFile());

    }

    private void prepareExperimentListExpectations(final List<ExternalData> dataSets)
    {
        context.checking(new Expectations()
            {
                {
                    allowing(service).listDataSetsByExperimentID(SESSION_TOKEN,
                            new TechId(experiment));
                    will(returnValue(dataSets));

                    for (ExternalData dataSet : dataSets)
                    {
                        String mockName = getHierarchicalContentMockName(dataSet.getCode());
                        IHierarchicalContent content =
                                context.mock(IHierarchicalContent.class, mockName);
                        allowing(hierarchicalContentProvider).asContent(dataSet.getCode());
                        will(returnValue(content));

                        String rootMockName = getRootNodeMockName(dataSet.getCode());
                        IHierarchicalContentNode rootNode =
                                context.mock(IHierarchicalContentNode.class, rootMockName);
                        allowing(content).getRootNode();
                        will(returnValue(rootNode));

                        allowing(rootNode).getRelativePath();
                        will(returnValue(StringUtils.EMPTY));

                        allowing(rootNode).getName();
                        will(returnValue(null));
                    }
                }
            });
    }

    private String getHierarchicalContentMockName(String dataSetCode)
    {
        return dataSetCode;
    }

    private String getRootNodeMockName(String dataSetCode)
    {
        return dataSetCode + "-rootNode";
    }

    private IHierarchicalContentNode getHierarchicalRootNodeMock(String dataSetCode)
    {
        String mockName = getRootNodeMockName(dataSetCode);
        return context.getMock(mockName, IHierarchicalContentNode.class);
    }

    protected IHierarchicalContent getHierarchicalContentMock(String dataSetCode)
    {
        String mockName = getHierarchicalContentMockName(dataSetCode);
        return context.getMock(mockName, IHierarchicalContent.class);
    }

    private ExternalData createDataSet(String dataSetCode, String dataSetType)
    {
        return createDataSet(dataSetCode, dataSetType, new Date());
    }

    private ExternalData createDataSet(String dataSetCode, String dataSetType, Date registrationDate)
    {
        ExternalData result = new ExternalData();
        result.setCode(dataSetCode);
        DataSetType type = new DataSetType(dataSetType);
        result.setDataSetType(type);
        result.setRegistrationDate(registrationDate);
        return result;
    }

}
