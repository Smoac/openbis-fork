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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
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

import ch.systemsx.cisd.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.common.io.hierarchical_content.api.IHierarchicalContentNode;
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

    private static final String DS_TYPE2 = "DS_TYPE2";

    private static final String TEMPLATE_WITH_FILENAMES =
            "DS-${dataSetType}-${fileName}-${disambiguation}";


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

        final BeanFactory beanFactory = context.mock(BeanFactory.class);
        ServiceProviderTestWrapper.setApplicationContext(beanFactory);
        hierarchicalContentProvider = ServiceProviderTestWrapper.mock(context, IHierarchicalContentProvider.class);

        resolverContext = new FtpPathResolverContext(SESSION_TOKEN, service, null);
        context.checking(new Expectations()
            {
                {
                    ExperimentIdentifier experimentIdentifier =
                            new ExperimentIdentifierFactory(EXP_ID).createIdentifier();
                    allowing(service).tryToGetExperiment(SESSION_TOKEN, experimentIdentifier);
                    will(returnValue(experiment));
                }
            });
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(Method m)
    {
        ServiceProviderTestWrapper.restoreApplicationContext();
        try
        {
            context.assertIsSatisfied();
        } catch (Throwable t)
        {
            throw new Error(m.getName() + "() : ", t);
        }
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

                    one(content).getNode(StringUtils.EMPTY);
                    will(returnValue(rootNode));

                    exactly(2).of(rootNode).isDirectory();
                    will(returnValue(true));

                    one(rootNode).getFile();
                    will(throwException(new UnsupportedOperationException()));

                }
            });

        FtpFile ftpFile = resolver.resolve(path, resolverContext);

        assertNotNull(ftpFile);
        assertEquals(dataSetCode, ftpFile.getName());

    }

    @Test
    public void testResolveNestedFilesWithSimpleTemplate()
    {
        FtpServerConfig config =
                new FtpServerConfigBuilder().withTemplate(SIMPLE_TEMPLATE).getConfig();
        resolver = new TemplateBasedDataSetResourceResolver(config);

        final String dataSetCode = "dataSetCode";
        final String subPath = "level1/level2/fileName.txt";

        String path =
                EXP_ID + FtpConstants.FILE_SEPARATOR + dataSetCode + FtpConstants.FILE_SEPARATOR
                        + subPath;

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

                    one(mockNode).getRelativePath();
                    will(returnValue(subPath));

                    exactly(2).of(mockNode).isDirectory();
                    will(returnValue(false));

                    one(mockNode).getFileLength();
                    will(returnValue(2L));

                    one(mockNode).getFile();
                    will(returnValue(null));
                }
            });

        FtpFile ftpFile = resolver.resolve(path, resolverContext);

        assertNotNull(ftpFile);
        assertEquals("fileName.txt", ftpFile.getName());
        assertTrue(ftpFile.isFile());
    }

    @Test
    public void testHierarchicalContentClosed() throws IOException
    {
        FtpServerConfig config =
                new FtpServerConfigBuilder().withTemplate(SIMPLE_TEMPLATE).getConfig();
        resolver = new TemplateBasedDataSetResourceResolver(config);

        final String dataSetCode = "dataSetCode";
        final String subPath = "fileName.txt";

        String path =
                EXP_ID + FtpConstants.FILE_SEPARATOR + dataSetCode + FtpConstants.FILE_SEPARATOR
                        + subPath;

        List<ExternalData> dataSets = Arrays.asList(createDataSet(dataSetCode, DS_TYPE1));

        prepareExperimentListExpectations(dataSets);

        context.checking(new Expectations()
            {
                {
                    IHierarchicalContent content = getHierarchicalContentMock(dataSetCode);

                    one(hierarchicalContentProvider).asContent(dataSetCode);
                    will(returnValue(content));
                    one(content).close();

                    IHierarchicalContentNode mockNode =
                            context.mock(IHierarchicalContentNode.class);

                    ByteArrayInputStream is = new ByteArrayInputStream(new byte[] {});

                    allowing(content).getNode(subPath);
                    will(returnValue(mockNode));

                    one(mockNode).getRelativePath();
                    will(returnValue(subPath));

                    exactly(2).of(mockNode).isDirectory();
                    will(returnValue(false));

                    one(mockNode).getFileLength();
                    will(returnValue(2L));

                    one(mockNode).getFile();
                    will(returnValue(null));

                    one(mockNode).getInputStream();
                    will(returnValue(is));

                }
            });

        FtpFile ftpFile = resolver.resolve(path, resolverContext);

        assertNotNull(ftpFile);
        assertEquals("fileName.txt", ftpFile.getName());
        assertTrue(ftpFile.isFile());
        InputStream fileContent = ftpFile.createInputStream(0);
        // this call will also close the IHierarchicalContent
        fileContent.close();
    }

    @Test
    public void testFileFilters()
    {
        String filterPattern = "[^.]*\\.txt";
        FtpServerConfig config =
                new FtpServerConfigBuilder().withTemplate(TEMPLATE_WITH_FILENAMES)
                        .withFileListFilter(DS_TYPE1, filterPattern).getConfig();
        resolver = new TemplateBasedDataSetResourceResolver(config);

        final String dataSetCode = "dataSetCode";
        final String dataSetCode2 = "dataSetCode2";

        List<ExternalData> dataSets =
                Arrays.asList(createDataSet(dataSetCode, DS_TYPE1),
                        createDataSet(dataSetCode2, DS_TYPE2));

        prepareExperimentListExpectations(dataSets);

        context.checking(new Expectations()
            {
                {
                    IHierarchicalContentNode rootNode = getHierarchicalRootNodeMock(dataSetCode);
                    allowing(rootNode).isDirectory();
                    will(returnValue(true));

                    allowing(rootNode).getChildNodes();
                    List<IHierarchicalContentNode> children =
                            createFileNodeMocks("file.exe", "file.txt", "file");
                    will(returnValue(children));

                    IHierarchicalContentNode rootNode2 = getHierarchicalRootNodeMock(dataSetCode2);
                    allowing(rootNode2).isDirectory();
                    will(returnValue(true));

                    allowing(rootNode2).getChildNodes();
                    List<IHierarchicalContentNode> children2 =
                            createFileNodeMocks("file.jar", "file.sh");
                    will(returnValue(children2));
                }
            });

        List<FtpFile> files =
                resolver.listExperimentChildrenPaths(experiment, EXP_ID, resolverContext);

        assertNotNull(files);
        assertEquals(3, files.size());

        assertTrue(files.get(0).isFile());
        assertEquals("DS-DS_TYPE1-file.txt-A", files.get(0).getName());
        assertTrue(files.get(1).isFile());
        assertEquals("DS-DS_TYPE2-file.jar-B", files.get(1).getName());
        assertTrue(files.get(2).isFile());
        assertEquals("DS-DS_TYPE2-file.sh-B", files.get(2).getName());

    }


    private void prepareExperimentListExpectations(final List<ExternalData> dataSets)
    {
        context.checking(new Expectations()
            {
                {
                    one(service).listDataSetsByExperimentID(SESSION_TOKEN,
                            new TechId(experiment));
                    will(returnValue(dataSets));

                    for (ExternalData dataSet : dataSets)
                    {
                        String mockName = getHierarchicalContentMockName(dataSet.getCode());
                        IHierarchicalContent content =
                                context.mock(IHierarchicalContent.class, mockName);
                        one(hierarchicalContentProvider).asContent(dataSet);
                        will(returnValue(content));
                        one(content).close();

                        String rootMockName = getRootNodeMockName(dataSet.getCode());
                        IHierarchicalContentNode rootNode =
                                context.mock(IHierarchicalContentNode.class, rootMockName);
                        allowing(content).getRootNode();
                        will(returnValue(rootNode));

                        allowing(rootNode).getRelativePath();
                        will(returnValue(StringUtils.EMPTY));

                        allowing(rootNode).getName();
                        will(returnValue(StringUtils.EMPTY));
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

    private List<IHierarchicalContentNode> createFileNodeMocks(final String... fileNames)
    {
        final List<IHierarchicalContentNode> result = new ArrayList<IHierarchicalContentNode>();

        context.checking(new Expectations()
            {
                {
                    for (String fileName : fileNames)
                    {
                        IHierarchicalContentNode mockNode =
                                context.mock(IHierarchicalContentNode.class, fileName);
                        result.add(mockNode);

                        allowing(mockNode).getFileLength();
                        will(returnValue(10L));

                        allowing(mockNode).getFile();
                        will(throwException(new UnsupportedOperationException()));

                        allowing(mockNode).getName();
                        will(returnValue(fileName));

                        allowing(mockNode).isDirectory();
                        will(returnValue(false));

                        allowing(mockNode).getRelativePath();
                        will(returnValue(fileName));
                    }
                }
            });
        return result;
    }
}
