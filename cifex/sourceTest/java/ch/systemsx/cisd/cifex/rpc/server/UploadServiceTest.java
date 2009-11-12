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

package ch.systemsx.cisd.cifex.rpc.server;

import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.cifex.rpc.FilePreregistrationDTO;
import ch.systemsx.cisd.cifex.rpc.UploadState;
import ch.systemsx.cisd.cifex.rpc.UploadStatus;
import ch.systemsx.cisd.cifex.server.business.IFileManager;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;

/**
 * @author Franz-Josef Elmer
 */
public class UploadServiceTest extends AssertJUnit
{
    private Mockery context;

    private IFileManager fileManager;

    private CIFEXRPCService uploadService;

    private String sessionID;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        fileManager = context.mock(IFileManager.class);
        uploadService =
                new CIFEXRPCService(fileManager, null, null, null, null, new SessionManager(null,
                        null, "false"), 60000L, 10, "false");
        sessionID = uploadService.createSession(new UserDTO(), "exmaple-url");
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testInitialStatus()
    {
        UploadStatus status = uploadService.getUploadStatus(sessionID);

        assertEquals(0, status.getFilePointer());
        assertEquals(UploadState.INITIALIZED, status.getUploadState());

        context.assertIsSatisfied();
    }

    @Test
    public void testGetUploadStatusWithInvalidSessionID()
    {
        try
        {
            uploadService.getUploadStatus("invalid");
            fail("EnvironmentFailureException expected");
        } catch (InvalidSessionException e)
        {
            assertEquals("No session found for ID invalid", e.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testDefineUploadParameters()
    {
        uploadService.defineUploadParameters(sessionID, new FilePreregistrationDTO[]
            { new FilePreregistrationDTO("path/a", 100L), new FilePreregistrationDTO("b", 10L) },
                "", "");

        UploadStatus status = uploadService.getUploadStatus(sessionID);
        assertEquals("path/a", status.getCurrentFile());
        assertEquals("a", status.getNameOfCurrentFile());
        assertEquals(0, status.getFilePointer());
        assertEquals(UploadState.READY_FOR_NEXT_FILE, status.getUploadState());

        context.assertIsSatisfied();
    }

    @Test
    public void testDefineUploadParametersInIllegalState()
    {
        uploadService.defineUploadParameters(sessionID, new FilePreregistrationDTO[]
            { new FilePreregistrationDTO("path/a", 100L), new FilePreregistrationDTO("b", 10L) },
                "", "");
        try
        {
            uploadService.defineUploadParameters(sessionID,
                    new FilePreregistrationDTO[]
                        { new FilePreregistrationDTO("path/a", 100L),
                                new FilePreregistrationDTO("b", 10L) }, "", "");
            fail("IllegalStateException expected");
        } catch (IllegalStateException e)
        {
            assertEquals("Expected one of [INITIALIZED] but was READY_FOR_NEXT_FILE", e
                    .getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testDefineUploadParametersWithInvalidSessionID()
    {
        try
        {
            uploadService.defineUploadParameters("invalid", new FilePreregistrationDTO[0], "", "");
            fail("EnvironmentFailureException expected");
        } catch (InvalidSessionException e)
        {
            assertEquals("No session found for ID invalid", e.getMessage());
        }

        context.assertIsSatisfied();
    }

}
