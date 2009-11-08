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
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.PluginTaskProviders;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PluginUtilTest;
import ch.systemsx.cisd.openbis.generic.shared.IDataStoreService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUploadContext;

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
                PluginTaskProviders pluginTaskParameters)
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

    private PluginTaskProviders pluginTaskParameters;

    @BeforeMethod
    public void setup()
    {
        context = new Mockery();
        sessionTokenManager = new SessionTokenManager();
        sessionToken = sessionTokenManager.drawSessionToken();
        commandExecutorFactory = context.mock(IDataSetCommandExecutorFactory.class);
        commandExecutor = context.mock(IDataSetCommandExecutor.class);
        cifexServiceFactory = context.mock(ICIFEXRPCServiceFactory.class);
        cifex = context.mock(ICIFEXComponent.class);
        mailClientParameters = new MailClientParameters();
        mailClientParameters.setFrom("a@bc.de");
        mailClientParameters.setSmtpHost("file://targets/email");
        FileUtilities.deleteRecursively(TEST_FOLDER);
        pluginTaskParameters = PluginUtilTest.createPluginTaskProviders();
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
            createService().getKnownDataSets("invalid", null);
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
        String location = "ds1";
        new File(TEST_STORE, location).createNewFile();

        IDataStoreService service = createService();
        List<String> knownDataSets =
                service.getKnownDataSets(sessionToken, Arrays.asList(location, "ds2"));

        assertEquals(1, knownDataSets.size());
        assertSame(location, knownDataSets.get(0));
        context.assertIsSatisfied();
    }

    @Test
    public void testDeleteDataSetsForInvalidSessionToken()
    {
        try
        {
            createService().deleteDataSets("invalid", null);
            fail("InvalidAuthenticationException expected");
        } catch (InvalidAuthenticationException e)
        {
            assertEquals(INVALID_SESSION_TOKEN_MSG, e.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testDeleteDataSets()
    {
        final List<String> locations = Arrays.asList("d1", "d2");
        context.checking(new Expectations()
            {
                {
                    one(commandExecutor).scheduleDeletionOfDataSets(locations);
                }
            });
        createService().deleteDataSets(sessionToken, locations);

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
        final List<ExternalData> dataSets = new ArrayList<ExternalData>();
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
            assertEquals("User couldn't be authenticated at CIFEX.", e.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testUploadDataSets()
    {
        final List<ExternalData> dataSets = new ArrayList<ExternalData>();
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

                    one(commandExecutor).scheduleUploadingDataSetsToCIFEX(cifexServiceFactory,
                            mailClientParameters, dataSets, uploadContext);
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

                    one(commandExecutor).start();
                }
            });
        MockDataStoreService service =
                new MockDataStoreService(sessionTokenManager, commandExecutorFactory,
                        mailClientParameters, cifexServiceFactory, CIFEX_URL, pluginTaskParameters);
        service.setStoreRoot(TEST_STORE);
        service.afterPropertiesSet();
        return service;
    }
}
