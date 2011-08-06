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

package exceptions;

import static org.testng.AssertJUnit.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;

/**
 * Test cases for {@link IOExceptionUnchecked}.
 *
 * @author Bernd Rinn
 */
public class IOExceptionUncheckedTests
{
    private void generateFileNotFoundException() throws IOExceptionUnchecked
    {
        try
        {
            new FileInputStream(new File("doesnt.exist"));
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }
    
    @Test
    public void testWrapUnwrapIOException()
    {
        try
        {
            generateFileNotFoundException();
            fail("No IOException thrown");
        } catch (IOExceptionUnchecked e)
        {
            final Exception ex = CheckedExceptionTunnel.unwrapIfNecessary(e);
            assertTrue(ex instanceof FileNotFoundException);
            assertEquals("doesnt.exist (No such file or directory)", ex.getMessage());
        }
    }
    
    @Test
    public void testWrapUnwrapNonIOException()
    {
        try
        {
            throw new IOExceptionUnchecked(new IllegalStateException("Don't like this state"));
        } catch (CheckedExceptionTunnel e)
        {
            final Exception ex = CheckedExceptionTunnel.unwrapIfNecessary(e);
            assertTrue(ex instanceof IOException);
            assertEquals("IllegalStateException: Don't like this state", ex.getMessage());
            assertNotNull(ex.getCause());
            assertTrue(ex.getCause() instanceof IllegalStateException);
            assertEquals("Don't like this state", ex.getCause().getMessage());
        }
    }

    @Test
    public void testWrapUnwrapIOExceptionGivingMsg()
    {
        try
        {
            throw new IOExceptionUnchecked("Some message");
        } catch (CheckedExceptionTunnel e)
        {
            final Exception ex = CheckedExceptionTunnel.unwrapIfNecessary(e);
            assertTrue(ex instanceof IOException);
            assertEquals("Some message", ex.getMessage());
            assertNull(ex.getCause());
        }
    }

}
