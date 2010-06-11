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

import jline.ConsoleReader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.cifex.rpc.client.ICIFEXComponent;
import ch.systemsx.cisd.cifex.rpc.client.IProgressListenerHolder;
import ch.systemsx.cisd.cifex.rpc.client.IncompatibleAPIVersionsException;
import ch.systemsx.cisd.cifex.rpc.client.RPCServiceFactory;
import ch.systemsx.cisd.cifex.rpc.client.gui.IProgressListener;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;

/**
 * Abstract super class of all commands.
 * 
 * @author Bernd Rinn
 */
abstract class AbstractCommand implements ICommand
{

    private static final String APPLICATION_NAME = "cifex";

    private static final File HOME_DIRECTORY = SystemUtils.getUserHome();

    private static final String SESSION_TOKEN_FILE_NAME = "." + APPLICATION_NAME + "-session-token";

    protected static final File SESSION_TOKEN_FILE =
            new File(HOME_DIRECTORY, SESSION_TOKEN_FILE_NAME);

    static String configuredBaseURL;

    static
    {
        final File baseURLFile = getBaseURLFile();
        if (baseURLFile.exists())
        {
            try
            {
                configuredBaseURL = FileUtils.readFileToString(baseURLFile);
            } catch (final IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }
        if (configuredBaseURL == null
                && System.getProperty(Constants.CIFEX_BASE_URL_PROP_KEY) != null)
        {
            configuredBaseURL = System.getProperty(Constants.CIFEX_BASE_URL_PROP_KEY);
        }
    }

    static File getBaseURLFile()
    {
        final File baseURLFile = new File(RPCServiceFactory.getCIFEXConfigDir(), "server");
        return baseURLFile;
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
     * Returns the passphrase, either the one provided in <var>passphraseOrNull</var> or by asking
     * the user. If the user doesn't enter a passphrase, this method returns <code>null</code>.
     */
    protected final String tryGetPassphrase(final String passphraseOrNull)
    {
        String passphrase = passphraseOrNull;
        if (StringUtils.isBlank(passphrase))
        {
            try
            {
                passphrase = getConsoleReader().readLine("Passphrase: ", Character.valueOf('*'));
            } catch (IOException ex)
            {
                throw new EnvironmentFailureException("I/O Exception while getting passphrase.", ex);
            }
        }
        if (StringUtils.isBlank(passphrase))
        {
            return null;
        }
        return passphrase;
    }

    /**
     * Returns the component interface for using CIFEX.
     */
    protected final ICIFEXComponent tryGetComponent()
    {
        return tryGetComponent(configuredBaseURL, false);
    }

    /**
     * Returns the component interface for using CIFEX.
     */
    protected final ICIFEXComponent tryGetComponent(String baseURL, boolean initializeTrustStore)
    {
        if (StringUtils.isBlank(baseURL))
        {
            System.err
                    .println("This CIFEX client has not been initialized. Run the command 'init' first.");
            return null;
        }
        final String serviceURL = baseURL + Constants.CIFEX_RPC_PATH;
        try
        {
            final ICIFEXComponent cifexComponent =
                    RPCServiceFactory.createCIFEXComponent(serviceURL, initializeTrustStore);
            return cifexComponent;
        } catch (IncompatibleAPIVersionsException ex)
        {
            System.err.println(ex.getMessage());
            return null;
        }
    }

    protected void addConsoleProgressListener(final IProgressListenerHolder downloader,
            final boolean noProgressFeedback)
    {
        downloader.addProgressListener(new IProgressListener()
            {

                long size;

                public void start(File file, long fileSize, Long fileIdOrNull)
                {
                    size = fileSize;
                }

                public void reportProgress(int percentage, long numberOfBytes)
                {
                    if (noProgressFeedback == false)
                    {
                        System.out.print("\r" + percentage + "% (" + numberOfBytes + "/" + size
                                + ")");
                    }
                }

                public void finished(boolean successful)
                {
                    size = 0L;
                }

                public void warningOccured(String warningMessage)
                {
                    System.out.println();
                    System.err.println(warningMessage);
                }

                public void exceptionOccured(Throwable throwable)
                {
                    // Reporting handled in CIFEXClient
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
