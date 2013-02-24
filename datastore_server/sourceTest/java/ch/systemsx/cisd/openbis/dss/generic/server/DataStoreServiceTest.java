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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.cifex.rpc.client.ICIFEXComponent;
import ch.systemsx.cisd.common.exceptions.InvalidAuthenticationException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.mail.MailClientParameters;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IPluginTaskInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PluginUtilTest;
import ch.systemsx.cisd.openbis.generic.shared.IDataStoreService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUploadContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.DatasetDescriptionBuilder;

/**
 * @author Franz-Josef Elmer
 */
public class DataStoreServiceTest extends AssertJUnit
{
    private static final String INVALID_SESSION_TOKEN_MSG = "Invalid session token.";

    private static final String CIFEX_URL = "cifexURL";

    private static final File TEST_FOLDER = new File("targets/data-store-service-test");

    private static final File TEST_STORE = new File(TEST_FOLDER, "store");

    private static final class MockDataStoreService extends DataStoreService
    {
        private final ICIFEXRPCServiceFactory cifexServiceFactory;

        private final String expectedCIFEXURL;

        MockDataStoreService(SessionTokenManager sessionTokenManager,
                IDataSetCommandExecutorFactory commandExecutorFactory,
                MailClientParameters mailClientParameters,
                ICIFEXRPCServiceFactory cifexServiceFactory, String expectedCIFEXURL,
                IPluginTaskInfoProvider pluginTaskParameters)
        {
            super(sessionTokenManager, commandExecutorFactory, mailClientParameters,
                    pluginTaskParameters);
            this.cifexServiceFactory = cifexServiceFactory;
            this.expectedCIFEXURL = expectedCIFEXURL;
        }

        @Override
        protected ICIFEXRPCServiceFactory createCIFEXRPCServiceFactory(String cifexURL)
        {
            AssertJUnit.assertEquals(expectedCIFEXURL, cifexURL);
            return cifexServiceFactory;
        }
    }

    private SessionTokenManager sessionTokenManager;

    private String sessionToken;

    private Mockery context;

    private IDataSetCommandExecutorFactory commandExecutorFactory;

    private IDataSetCommandExecutor commandExecutor;

    private ICIFEXRPCServiceFactory cifexServiceFactory;

    private MailClientParameters mailClientParameters;

    private ICIFEXComponent cifex;

    private IPluginTaskInfoProvider pluginTaskParameters;

    private IShareIdManager shareIdManager;

    @BeforeMethod
    public void setup()
    {
        context = new Mockery();
        sessionTokenManager = new SessionTokenManager();
        sessionToken = sessionTokenManager.drawSessionToken();
        commandExecutorFactory = context.mock(IDataSetCommandExecutorFactory.class);
        commandExecutor = context.mock(IDataSetCommandExecutor.class);
        shareIdManager = context.mock(IShareIdManager.class);
        cifexServiceFactory = context.mock(ICIFEXRPCServiceFactory.class);
        cifex = context.mock(ICIFEXComponent.class);
        mailClientParameters = new MailClientParameters();
        mailClientParameters.setFrom("a@bc.de");
        mailClientParameters.setSmtpHost("file://targets/email");
        FileUtilities.deleteRecursively(TEST_FOLDER);
        pluginTaskParameters = PluginUtilTest.createPluginTaskProviders(TEST_STORE);
        TEST_STORE.mkdirs();
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testGetVersionForInvalidSessionToken()
    {
        try
        {
            createService().getVersion("invalid");
            fail("InvalidAuthenticationException expected");
        } catch (InvalidAuthenticationException e)
        {
            assertEquals(INVALID_SESSION_TOKEN_MSG, e.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testGetVersion()
    {
        assertEquals(IDataStoreService.VERSION, createService().getVersion(sessionToken));

        context.assertIsSatisfied();
    }

    @Test
    public void testGetKnownDataSetsForInvalidSessionToken()
    {
        try
        {
            createService().getKnownDataSets("invalid", null, false);
            fail("InvalidAuthenticationException expected");
        } catch (InvalidAuthenticationException e)
        {
            assertEquals(INVALID_SESSION_TOKEN_MSG, e.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testGetKnownDataSets() throws IOException
    {
        final String shareId = "share-1";
        String location = "ds1";
        DatasetDescriptionBuilder ds1 = new DatasetDescriptionBuilder("ds1").location(location);
        DatasetDescriptionBuilder ds2 = new DatasetDescriptionBuilder("ds2").location(location);
        File share = new File(TEST_STORE, shareId);
        share.mkdirs();
        new File(share, location).createNewFile();
        context.checking(new Expectations()
            {
                {
                    one(shareIdManager).lock("ds1");
                    one(shareIdManager).isKnown("ds1");
                    will(returnValue(true));
                    one(shareIdManager).getShareId("ds1");
                    will(returnValue(shareId));
                    one(shareIdManager).releaseLock("ds1");

                    one(shareIdManager).lock("ds2");
                    one(shareIdManager).isKnown("ds2");
                    will(returnValue(false));
                    one(shareIdManager).releaseLock("ds2");
                }
            });

        IDataStoreService service = createService();
        List<String> knownDataSets =
                service.getKnownDataSets(sessionToken,
                        Arrays.asList(ds1.getDatasetDescription(), ds2.getDatasetDescription()),
                        false);

        assertEquals(1, knownDataSets.size());
        assertSame(location, knownDataSets.get(0));
        context.assertIsSatisfied();
    }

    @Test
    public void testUploadDataSetsForInvalidSessionToken()
    {
        try
        {
            createService().uploadDataSetsToCIFEX("invalid", null, null);
            fail("InvalidAuthenticationException expected");
        } catch (InvalidAuthenticationException e)
        {
            assertEquals(INVALID_SESSION_TOKEN_MSG, e.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testUploadDataSetsForInvalidPassword()
    {
        final List<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        final DataSetUploadContext uploadContext = new DataSetUploadContext();
        uploadContext.setCifexURL(CIFEX_URL);
        uploadContext.setUserID("user");
        uploadContext.setPassword("pwd");
        context.checking(new Expectations()
            {
                {
                    one(cifexServiceFactory).createCIFEXComponent();
                    will(returnValue(cifex));

                    one(cifex).login(uploadContext.getUserID(), uploadContext.getPassword());
                    will(returnValue(null));
                }
            });

        try
        {
            createService().uploadDataSetsToCIFEX(sessionToken, dataSets, uploadContext);
            fail("InvalidSessionException expected");
        } catch (InvalidSessionException e)
        {
            assertEquals("User failed to be authenticated by CIFEX.", e.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testUploadDataSets()
    {
        final List<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        final DataSetUploadContext uploadContext = new DataSetUploadContext();
        uploadContext.setCifexURL(CIFEX_URL);
        uploadContext.setUserID("user");
        uploadContext.setPassword("pwd");
        context.checking(new Expectations()
            {
                {
                    one(cifexServiceFactory).createCIFEXComponent();
                    will(returnValue(cifex));

                    one(cifex).login(uploadContext.getUserID(), uploadContext.getPassword());
                    will(returnValue("token"));
                    one(cifex).logout("token");

                    one(commandExecutor).scheduleUploadingDataSetsToCIFEX(cifexServiceFactory,
                            mailClientParameters, dataSets, uploadContext, null, null);
                }
            });

        createService().uploadDataSetsToCIFEX(sessionToken, dataSets, uploadContext);

        context.assertIsSatisfied();
    }

    private IDataStoreService createService()
    {
        context.checking(new Expectations()
            {
                {
                    one(commandExecutorFactory).create(TEST_STORE, TEST_STORE);
                    will(returnValue(commandExecutor));
                }
            });
        MockDataStoreService service =
                new MockDataStoreService(sessionTokenManager, commandExecutorFactory,
                        mailClientParameters, cifexServiceFactory, CIFEX_URL, pluginTaskParameters);
        service.setShareIdManager(shareIdManager);
        service.afterPropertiesSet();
        return service;
    }
}
