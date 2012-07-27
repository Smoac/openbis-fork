/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.DatasetDescriptionBuilder;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetAndPathInfoDBConsistencyCheckProcessingPluginTest extends
        AbstractFileSystemTestCase
{
    private static final String SHARE_ID = "42";

    private static final String USER_EMAIL = "a@bc.de";

    private DataSetAndPathInfoDBConsistencyCheckProcessingPlugin plugin;

    private Mockery context;

    private IMailClient mailClient;

    private IHierarchicalContentProvider fileProvider;

    private IHierarchicalContent fileContent;

    private IHierarchicalContentNode fileRootNode;

    private IHierarchicalContentNode fileChildNode;

    private IHierarchicalContentProvider pathInfoProvider;

    private IHierarchicalContent pathInfoContent;

    private IHierarchicalContentNode pathInfoRootNode;

    private IHierarchicalContentNode pathInfoChildNode;

    private DataSetProcessingContext processingContext;

    @Override
    @BeforeMethod
    public void setUp() throws IOException
    {
        super.setUp();

        context = new Mockery();
        mailClient = context.mock(IMailClient.class);
        fileProvider = context.mock(IHierarchicalContentProvider.class, "fileProvider");
        fileContent = context.mock(IHierarchicalContent.class, "fileContent");
        fileRootNode = context.mock(IHierarchicalContentNode.class, "fileRootNode");
        fileChildNode = context.mock(IHierarchicalContentNode.class, "fileChildNode");
        pathInfoProvider = context.mock(IHierarchicalContentProvider.class, "pathInfoProvider");
        pathInfoContent = context.mock(IHierarchicalContent.class, "pathInfoContent");
        pathInfoRootNode = context.mock(IHierarchicalContentNode.class, "pathInfoRootNode");
        pathInfoChildNode = context.mock(IHierarchicalContentNode.class, "pathInfoChildNode");

        plugin =
                new DataSetAndPathInfoDBConsistencyCheckProcessingPlugin(new Properties(),
                        workingDirectory, fileProvider, pathInfoProvider);
        processingContext =
                new DataSetProcessingContext(null, new MockDataSetDirectoryProvider(
                        workingDirectory, SHARE_ID), null, mailClient, USER_EMAIL);
    }

    @Test
    public void testNoDifferences()
    {
        final String ds1Code = "ds-1";
        final DatasetDescription ds1 =
                new DatasetDescriptionBuilder(ds1Code).location("a").getDatasetDescription();
        context.checking(new Expectations()
            {
                {
                    // The test consists of parallel calls to the file and path-info structures
                    // In this test, all call return the same thing
                    getContent(ds1Code);
                    getRootNode();
                    rootNodeExists();
                    getRelativePath();
                    rootNodeIsDirectory();
                    getRootChildren();
                    getChildRelativePath();
                    childNodeExists();
                    childIsDirectory();
                    getChildFileLength();
                    childIsChecksumPrecalculated();
                    closeContent();
                    sendEmail();
                }

                protected void sendEmail()
                {
                    oneOf(mailClient).sendEmailMessage(
                            "File system and path info DB consistency check report",
                            "Data sets checked:\n\nds-1\n\nDifferences found:\n\nNone", null, null,
                            new EMailAddress("a@bc.de"));
                }

                protected void closeContent()
                {
                    oneOf(fileContent).close();
                    oneOf(pathInfoContent).close();
                }

                protected void childIsChecksumPrecalculated()
                {
                    oneOf(pathInfoChildNode).isChecksumCRC32Precalculated();
                    will(returnValue(false));
                }

                protected void getChildFileLength()
                {
                    oneOf(fileChildNode).getFileLength();
                    will(returnValue(1024L));
                    oneOf(pathInfoChildNode).getFileLength();
                    will(returnValue(1024L));
                }

                protected void childIsDirectory()
                {
                    exactly(2).of(fileChildNode).isDirectory();
                    will(returnValue(false));
                    exactly(1).of(pathInfoChildNode).isDirectory();
                    will(returnValue(false));
                }

                protected void childNodeExists()
                {
                    oneOf(fileChildNode).exists();
                    will(returnValue(true));
                    oneOf(pathInfoChildNode).exists();
                    will(returnValue(true));
                }

                protected void getChildRelativePath()
                {
                    exactly(3).of(fileChildNode).getRelativePath();
                    will(returnValue("data.txt"));
                    exactly(3).of(pathInfoChildNode).getRelativePath();
                    will(returnValue("data.txt"));
                }

                protected void getRootChildren()
                {
                    oneOf(fileRootNode).getChildNodes();
                    will(returnValue(Arrays.asList(fileChildNode)));
                    oneOf(pathInfoRootNode).getChildNodes();
                    will(returnValue(Arrays.asList(pathInfoChildNode)));
                }

                protected void rootNodeIsDirectory()
                {
                    oneOf(fileRootNode).isDirectory();
                    will(returnValue(true));
                    oneOf(pathInfoRootNode).isDirectory();
                    will(returnValue(true));
                }

                protected void getRelativePath()
                {
                    oneOf(fileRootNode).getRelativePath();
                    will(returnValue(workingDirectory.getPath()));
                    oneOf(pathInfoRootNode).getRelativePath();
                    will(returnValue(workingDirectory.getPath()));
                }

                protected void rootNodeExists()
                {
                    oneOf(fileRootNode).exists();
                    will(returnValue(true));
                    oneOf(pathInfoRootNode).exists();
                    will(returnValue(true));
                }

                protected void getRootNode()
                {
                    oneOf(fileContent).getRootNode();
                    will(returnValue(fileRootNode));
                    oneOf(pathInfoContent).getRootNode();
                    will(returnValue(pathInfoRootNode));
                }

                protected void getContent(final String dataSetCode)
                {
                    oneOf(fileProvider).asContent(dataSetCode);
                    will(returnValue(fileContent));
                    oneOf(pathInfoProvider).asContent(dataSetCode);
                    will(returnValue(pathInfoContent));
                }
            });
        plugin.process(Arrays.asList(ds1), processingContext);
        context.assertIsSatisfied();
    }
}
