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

package ch.systemsx.cisd.cifex.rpc.client.cli;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import jline.ConsoleReader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;

import ch.systemsx.cisd.cifex.rpc.ICIFEXRPCService;
import ch.systemsx.cisd.cifex.rpc.client.IProgressListenerHolder;
import ch.systemsx.cisd.cifex.rpc.client.RPCServiceFactory;
import ch.systemsx.cisd.cifex.rpc.client.gui.IProgressListener;
import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.utilities.PropertyUtils;

/**
 * Abstract super class of all commands.
 * 
 * @author Bernd Rinn
 */
abstract class AbstractCommand implements ICommand
{

    private static final String BASE_URL = "base-url";

    private static final String APPLICATION_NAME = "cifex";

    private static final File HOME_DIRECTORY = SystemUtils.getUserHome();

    private static final String SESSION_TOKEN_FILE_NAME = "." + APPLICATION_NAME + "-session-token";

    protected static final File SESSION_TOKEN_FILE =
            new File(HOME_DIRECTORY, SESSION_TOKEN_FILE_NAME);
    
    /**
     * A hidden properties file which contains default property values that do not need to be typed
     * in.
     */
    private static final String CIFEX_DEFAULT_PROPERTIES_FILE_NAME = ".cifex-default";

    private static final File CIFEX_DEFAULT_PROPERTIES_FILE =
            new File(HOME_DIRECTORY, CIFEX_DEFAULT_PROPERTIES_FILE_NAME);

    final static Properties cifexDefaultProperties = new Properties();

    static
    {
        if (CIFEX_DEFAULT_PROPERTIES_FILE.exists())
        {
            InputStream stream = null;
            try
            {
                stream = FileUtils.openInputStream(CIFEX_DEFAULT_PROPERTIES_FILE);
                cifexDefaultProperties.load(stream);
                PropertyUtils.trimProperties(cifexDefaultProperties);
            } catch (final IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            } finally
            {
                IOUtils.closeQuietly(stream);
            }
        }

    }

    private final String name;

    private ConsoleReader consoleReader;

    AbstractCommand(final String name)
    {
        assert StringUtils.isNotBlank(name);
        this.name = name;
    }

    /** Returns a <code>ConsoleReader</code> instance after having lazily instantiated it. */
    protected final ConsoleReader getConsoleReader()
    {
        if (consoleReader == null)
        {
            try
            {
                consoleReader = new ConsoleReader();
            } catch (final IOException ex)
            {
                throw new EnvironmentFailureException("ConsoleReader could not be instantiated.",
                        ex);
            }
        }
        return consoleReader;
    }

    /**
     * Returns the service interface for accessing the server.
     */
    protected final ICIFEXRPCService getService(final MinimalParameters parameters)
    {
        return RPCServiceFactory.createServiceProxy(getServiceURL(parameters));
    }

    protected final String getServiceURL(final MinimalParameters parameters)
    {
        String baseURL = parameters.getBaseURL();
        if (baseURL == null)
        {

            baseURL = cifexDefaultProperties.getProperty(BASE_URL);
            if (baseURL == null)
            {
                throw new EnvironmentFailureException(
                        "Service URL is neither defined as a command-line option nor in the file '"
                                + CIFEX_DEFAULT_PROPERTIES_FILE_NAME + "'.");
            }
        }
        return baseURL;
    }

    protected void addConsoleProgressListener(final IProgressListenerHolder downloader)
    {
        downloader.addProgressListener(new IProgressListener() {

            long size;
            
            public void start(File file, long fileSize)
            {
                size = fileSize; 
                System.out.print("0% (0/" + size + ")");
            }

            public void reportProgress(int percentage, long numberOfBytes)
            {
                System.out.print("\r" + percentage + "% (" + numberOfBytes + "/" + size + ")");
            }

            public void finished(boolean successful)
            {
                System.out.println("\r100% (" + size + "/" + size + ")");
                size = 0L;
            }

            public void warningOccured(String warningMessage)
            {
                System.out.println();
                System.err.println(warningMessage);
            }
            
            public void exceptionOccured(Throwable throwable)
            {
                System.out.println();
                throwable.printStackTrace();
            }

        });
    }

    //
    // ICommand
    //

    public final String getName()
    {
        return name;
    }

}
