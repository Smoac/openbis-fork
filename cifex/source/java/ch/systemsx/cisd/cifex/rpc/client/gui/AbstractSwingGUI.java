/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.rpc.client.gui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.springframework.remoting.RemoteAccessException;

import ch.systemsx.cisd.cifex.rpc.client.ICIFEXComponent;
import ch.systemsx.cisd.cifex.rpc.client.RPCServiceFactory;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public abstract class AbstractSwingGUI
{
    /**
     * The interface for communicating with CIFEX
     */
    protected final ICIFEXComponent cifex;

    protected final String sessionId;

    protected final Thread shutdownHook;

    private final JFrame windowFrame;

    private static final long KEEP_ALIVE_PERIOD_MILLIS = 60 * 1000; // Every minute.

    /**
     * Instantiates the Swing GUI with the necessary information to communicate with CIFEX.
     * 
     * @param communicationState
     */
    protected AbstractSwingGUI(CIFEXCommunicationState communicationState)
    {
        cifex = communicationState.getCifex();
        sessionId = communicationState.getSessionId();

        // create the window frame
        windowFrame = new JFrame(getTitle());
        windowFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // add callbacks to close the app properly
        shutdownHook = new Thread()
            {
                @Override
                public void run()
                {
                    cifex.logout(sessionId);
                }
            };
        addShutdownHook();
        startSessionKeepAliveTimer(KEEP_ALIVE_PERIOD_MILLIS);
        addWindowCloseHook();
    }

    /**
     * The main window
     */
    protected JFrame getWindowFrame()
    {
        return windowFrame;
    }

    /**
     * Checks if it is safe to quit, if not, asks the user before doing so.
     */
    protected void logout()
    {
        if (cancel())
        {
            cifex.logout(sessionId);
            System.exit(0);
        }
    }

    private void startSessionKeepAliveTimer(final long checkTimeIntervalMillis)
    {
        final Timer timer = new Timer("Session Keep Alive", true);
        timer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    try
                    {
                        cifex.checkSession(sessionId);
                    } catch (RemoteAccessException ex)
                    {
                        System.err.println("Error connecting to the server");
                        ex.printStackTrace();
                    } catch (InvalidSessionException ex)
                    {
                        JOptionPane.showMessageDialog(windowFrame,
                                "Your session has expired on the server. Please log in again",
                                "Error connecting to server", JOptionPane.ERROR_MESSAGE);
                        Runtime.getRuntime().removeShutdownHook(shutdownHook);
                        System.exit(1);
                    }
                }
            }, 0L, checkTimeIntervalMillis);
    }

    /**
     * Log the user out automatically if the window is closed.
     */
    private void addWindowCloseHook()
    {
        windowFrame.addWindowListener(new WindowAdapter()
            {
                @Override
                public void windowClosing(WindowEvent e)
                {
                    logout();
                }
            });
    }

    /**
     * Log the user out automatically if the app is shutdown.
     */
    private void addShutdownHook()
    {
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    protected abstract String getTitle();

    protected abstract boolean cancel();

    protected static void setLookAndFeelToMetal()
    {
        // Set the look and feel to Metal, if possible
        try
        {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ex)
        {
            // just ignore -- no big deal
        }
    }
}

class CIFEXCommunicationState
{
    final String sessionId;

    final ICIFEXComponent cifex;

    /**
     * If possible, create a new instance of the CIFEXCommunicationState based info in the
     * arguments.
     */
    protected CIFEXCommunicationState(String[] args)
            throws ch.systemsx.cisd.cifex.shared.basic.UserFailureException,
            EnvironmentFailureException
    {
        if (args.length < 2)
            throw new ConfigurationFailureException(
                    "The CIFEX File Download Client was improperly configured -- the arguments it requires were not supplied. Please talk to the CIFEX administrator.");

        final String serviceURL = args[0];
        cifex = RPCServiceFactory.createCIFEXComponent(serviceURL, true);

        switch (args.length)
        {
            case 2:
                sessionId = args[1];
                break;
            default:
                String userName = args[1];
                String passwd = args[2];
                sessionId = cifex.login(userName, passwd);
        }
    }

    String getSessionId()
    {
        return sessionId;
    }

    ICIFEXComponent getCifex()
    {
        return cifex;
    }
}