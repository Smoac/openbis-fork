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

package ch.systemsx.cisd.openbis.dss.generic.server.api.v1;

import java.util.Arrays;
import java.util.Collections;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.io.IHierarchicalContent;
import ch.systemsx.cisd.common.io.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.server.DatasetSessionAuthorizer;
import ch.systemsx.cisd.openbis.dss.generic.server.DssServiceRpcAuthorizationAdvisor;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProviderTestWrapper;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization.DssSessionAuthorizationHolder;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;

/**
 * @author Franz-Josef Elmer
 */
public class DssServiceRpcGenericTest extends AssertJUnit
{

    private static final String SESSION_TOKEN = "SESSION";

    private IEncapsulatedOpenBISService service;

    private Mockery context;

    private IDssServiceRpcGeneric dssService;

    private IShareIdManager shareIdManager;

    private IHierarchicalContentProvider contentProvider;

    private IHierarchicalContent content;

    @BeforeMethod
    public void beforeMethod()
    {
        DssSessionAuthorizationHolder.setAuthorizer(new DatasetSessionAuthorizer());
        final StaticListableBeanFactory applicationContext = new StaticListableBeanFactory();
        ServiceProviderTestWrapper.setApplicationContext(applicationContext);
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        shareIdManager = context.mock(IShareIdManager.class);
        contentProvider = context.mock(IHierarchicalContentProvider.class);
        content = context.mock(IHierarchicalContent.class);
        applicationContext.addBean("openBIS-service", service);
        ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
        proxyFactoryBean.setInterfaces(new Class[]
            { IDssServiceRpcGeneric.class });
        DssServiceRpcGeneric nakedDssService =
                new DssServiceRpcGeneric(service, shareIdManager, contentProvider);
        proxyFactoryBean.setTarget(nakedDssService);
        proxyFactoryBean.addAdvisor(new DssServiceRpcAuthorizationAdvisor(shareIdManager));
        dssService = (IDssServiceRpcGeneric) proxyFactoryBean.getObject();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown()
    {
        ServiceProviderTestWrapper.restoreApplicationContext();
        context.assertIsSatisfied();
    }

    @Test
    public void testFilesForData()
    {
        final String dataSetCode = "ds-1";
        final String path = "abc/de";
        prepareLockDataSet(dataSetCode);
        prepareAuthorizationCheck(dataSetCode);
        prepareGetContent(dataSetCode);
        context.checking(new Expectations()
            {
                {
                    IHierarchicalContentNode mainNode = createNodeMock("mainNode");
                    one(content).getNode(path);
                    will(returnValue(mainNode));

                    IHierarchicalContentNode childNode1 = createNodeMock("childNode1");
                    IHierarchicalContentNode childNode1Child1 = createNodeMock("childNode1Child1");
                    IHierarchicalContentNode childNode1Child2 = createNodeMock("childNode1Child2");
                    IHierarchicalContentNode childNode2 = createNodeMock("childNode2");
                    IHierarchicalContentNode childNode2Child1 = createNodeMock("childNode2Child1");
                    IHierarchicalContentNode childNode3 = createNodeMock("childNode3");

                    prepareDirectoryNode(mainNode, path, childNode1, childNode2, childNode3);
                    // child1
                    prepareDirectoryNode(childNode1, path + "/child1", childNode1Child1,
                            childNode1Child2);
                    prepareFileNode(childNode1Child1, path + "/child1/child1", 11);
                    prepareFileNode(childNode1Child2, path + "/child1/child2", 12);
                    // child2
                    prepareDirectoryNode(childNode2, path + "/child2", childNode2Child1);
                    prepareFileNode(childNode2Child1, path + "/child2/child1", 21);
                    // child3
                    prepareFileNode(childNode3, path + "/child3", 3);
                }

                private IHierarchicalContentNode createNodeMock(String mockName)
                {
                    return context.mock(IHierarchicalContentNode.class, mockName);
                }

                private void prepareFileNode(IHierarchicalContentNode node,
                        final String relativePath, long length)
                {
                    allowing(node).isDirectory();
                    will(returnValue(false));
                    allowing(node).getRelativePath();
                    will(returnValue(relativePath));
                    one(node).getFileLength();
                    will(returnValue(length));
                }

                private void prepareDirectoryNode(IHierarchicalContentNode node,
                        final String relativePath, IHierarchicalContentNode... childNodes)
                {
                    allowing(node).isDirectory();
                    will(returnValue(true));
                    allowing(node).getRelativePath();
                    will(returnValue(relativePath));
                    one(node).getChildNodes();
                    will(returnValue(Arrays.asList(childNodes)));
                }
            });

        FileInfoDssDTO[] dataSets =
                dssService.listFilesForDataSet(SESSION_TOKEN, dataSetCode, path, true);

        assertEquals(6, dataSets.length);
        assertEquals(fileInfoString(path, "child1", -1), dataSets[0].toString());
        assertEquals(fileInfoString(path, "child1/child1", 11), dataSets[1].toString());
        assertEquals(fileInfoString(path, "child1/child2", 12), dataSets[2].toString());
        assertEquals(fileInfoString(path, "child2", -1), dataSets[3].toString());
        assertEquals(fileInfoString(path, "child2/child1", 21), dataSets[4].toString());
        assertEquals(fileInfoString(path, "child3", 3), dataSets[5].toString());
        context.assertIsSatisfied();
    }

    private static String fileInfoString(String startPath, String pathInListing, long length)
    {
        return String.format("FileInfoDssDTO[%s/%s,%s,%d]", startPath, pathInListing,
                pathInListing, length);
    }

    private void prepareLockDataSet(final String dataSetCode)
    {
        // NOTE: this is done by the DssServiceRpcAuthorizationAdvisor
        context.checking(new Expectations()
            {
                {
                    one(shareIdManager).lock(dataSetCode);
                    one(shareIdManager).releaseLocks();
                }
            });
    }

    private void prepareAuthorizationCheck(final String dataSetCode)
    {
        context.checking(new Expectations()
            {
                {
                    one(service).checkDataSetCollectionAccess(SESSION_TOKEN,
                            Collections.singletonList(dataSetCode));
                }
            });

    }

    private void prepareGetContent(final String dataSetCode)
    {
        context.checking(new Expectations()
            {
                {
                    one(contentProvider).asContent(dataSetCode);
                    will(returnValue(content));

                    one(content).close(); // content should be always closed
                }
            });
    }
}
