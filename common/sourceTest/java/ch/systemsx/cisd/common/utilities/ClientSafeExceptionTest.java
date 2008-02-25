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

package ch.systemsx.cisd.common.utilities;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotSame;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.Arrays;

import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * Test cases for the {@link ClientSafeExceptionTest}.
 * 
 * @author Christian Ribeaud
 */
public final class ClientSafeExceptionTest
{

    private void checkReturnedClientSafeException(final String message, final Exception rootException,
            final Exception clientSafeException)
    {
        if (ClientSafeException.isClientSafe(rootException))
        {
            assertSame(clientSafeException, rootException);
        } else
        {
            assertNotSame(clientSafeException, rootException);
            assertTrue(clientSafeException instanceof ClientSafeException);
        }
        assertEquals(message, clientSafeException.getMessage());
        assertTrue(Arrays.equals(rootException.getStackTrace(), clientSafeException.getStackTrace()));
        if (rootException.getCause() != null)
        {
            assertTrue(clientSafeException.getCause() != null);
        }
    }

    @Test
    public final void testWithNullException()
    {
        try
        {
            ClientSafeException.createClientSafeExceptionIfNeeded(null);
            fail("Null exception not allowed.");
        } catch (final AssertionError ex)
        {
            // Nothing to do here.
        }
    }

    @Test
    public final void testWithClientSafeExceptionOneLevel()
    {
        final String message = "Oooops!";
        final UserFailureException exception = new UserFailureException(message);
        final Exception clientSafeException = ClientSafeException.createClientSafeExceptionIfNeeded(exception);
        assertSame(clientSafeException, exception);
    }

    @Test
    public final void testWithNonClientSafeExceptionOneLevel()
    {
        final String message = "Oooops!";
        final Exception exception = new SAXException(message);
        final Exception clientSafeException = ClientSafeException.createClientSafeExceptionIfNeeded(exception);
        checkReturnedClientSafeException(message, exception, clientSafeException);
    }

    @Test
    public final void testWithClientSafeExceptionMultipleLevel()
    {
        final String userFailureText = "Oooops!";
        final UserFailureException userFailureException = new UserFailureException(userFailureText);
        final String runtimeText = "Oooops! I did it again...";
        final RuntimeException runtimeException = new RuntimeException(runtimeText, userFailureException);
        final String unsupportedOperationText = "Wishiiiii!";
        final UnsupportedOperationException unsupportedOperationException =
                new UnsupportedOperationException(unsupportedOperationText, runtimeException);
        final Exception clientSafeException =
                ClientSafeException.createClientSafeExceptionIfNeeded(unsupportedOperationException);
        checkReturnedClientSafeException(unsupportedOperationText, unsupportedOperationException, clientSafeException);
        checkReturnedClientSafeException(runtimeText, runtimeException, (Exception) clientSafeException.getCause());
        checkReturnedClientSafeException(userFailureText, userFailureException, (Exception) clientSafeException
                .getCause().getCause());
    }

    @Test
    public final void testWithNonClientSafeExceptionMultipleLevel()
    {
    }

    @Test
    public final void testWithCheckedExceptionTunnel()
    {
    }

    @Test
    public final void testWithGetCauseReturningItSelf()
    {
    }

}
