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
import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.commons.lang.WordUtils;
import org.springframework.remoting.RemoteAccessException;

import ch.systemsx.cisd.cifex.rpc.client.ICIFEXComponent;
import ch.systemsx.cisd.cifex.rpc.client.RPCServiceFactory;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public abstract class AbstractSwingGUI
{
    private static final int MESSAGE_WRAP_MAX_CHAR = 100;

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

    /**
     * Creates a error log listener who doesn't block when logging warnings and exceptions.
     */
    protected IProgressListener createErrorLogListener()
    {
        return new IProgressListener()
            {
                private String lastExceptionMessage;

                public void exceptionOccured(Throwable throwable)
                {
                    final String message;
                    if (throwable instanceof UserFailureException)
                    {
                        message = throwable.getMessage();
                    } else
                    {
                        message = "ERROR: " + throwable;
                    }
                    if (message.equals(lastExceptionMessage) == false)
                    {
                        lastExceptionMessage = message;
                        SwingUtilities.invokeLater(new Runnable()
                            {
                                public void run()
                                {
                                    JOptionPane.showMessageDialog(getWindowFrame(), WordUtils.wrap(
                                            message, MESSAGE_WRAP_MAX_CHAR), "Error",
                                            JOptionPane.ERROR_MESSAGE);
                                }
                            });
                    }
                }

                private String lastWarningMessage;

                public void warningOccured(final String warningMessage)
                {
                    if (warningMessage.equals(lastWarningMessage) == false)
                    {
                        lastWarningMessage = warningMessage;
                        SwingUtilities.invokeLater(new Runnable()
                            {
                                public void run()
                                {
                                    JOptionPane.showMessageDialog(getWindowFrame(), WordUtils.wrap(
                                            warningMessage, 100), "Warning",
                                            JOptionPane.WARNING_MESSAGE);
                                }
                            });
                    }
                }

                public void finished(boolean successful)
                {
                }

                public void reportProgress(int percentage, long numberOfBytes)
                {
                    lastWarningMessage = null;
                    lastExceptionMessage = null;
                }

                public void start(File file, long fileSize, Long fileIdOrNull)
                {
                }
            };
    }

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

    // -------- errors reporting -----------------

    protected static void showErrorsAndWarningsIfAny(JFrame frame, String firstMessageOrNull,
            List<String> warningMessages, List<Throwable> exceptions)
    {
        String message = (firstMessageOrNull == null ? "" : firstMessageOrNull + "\n");
        message += joinMessages(warningMessages, exceptions);
        if (exceptions.size() > 0)
        {
            showErrorMessage(frame, message);
        } else if (warningMessages.size() > 0)
        {
            showWarningMessage(frame, message);
        }
    }

    private static void showErrorMessage(JFrame frame, String message)
    {
        showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private static void showWarningMessage(JFrame frame, String message)
    {
        showMessageDialog(frame, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    private static void showMessageDialog(JFrame frame, String message, String title,
            int messageType)
    {
        JOptionPane.showMessageDialog(frame, message, title, messageType);
    }

    private static String joinMessages(List<String> warningMessages, List<Throwable> exceptions)
    {
        StringBuffer sb = new StringBuffer();
        addErrorMessages(exceptions, sb);
        addWarningMessages(warningMessages, sb);
        return sb.toString();
    }

    private static void addErrorMessages(List<Throwable> exceptions, StringBuffer sb)
    {
        if (exceptions.size() > 0)
        {
            if (exceptions.size() > 1)
            {
                sb.append("Following errors occured: \n");
            }
            for (Throwable exception : exceptions)
            {
                sb.append(getErrorMessage(exception));
                sb.append("\n");
            }
        }
    }

    private static void addWarningMessages(List<String> warningMessages, StringBuffer sb)
    {
        if (warningMessages.size() > 0)
        {
            sb.append("Following warnings occured (you can most probably ignore them): \n");
            String lastWarningMessage = "";
            for (String warningMessage : warningMessages)
            {
                if (lastWarningMessage.equals(warningMessage) == false)
                {
                    sb.append(warningMessage);
                    sb.append("\n");
                    lastWarningMessage = warningMessage;
                }
            }
        }
    }

    private static String getErrorMessage(Throwable throwable)
    {
        final String message;
        if (throwable instanceof UserFailureException)
        {
            message = throwable.getMessage();
        } else
        {
            message = "ERROR: " + throwable;
        }
        return message;
    }
}

class CIFEXCommunicationState
{
    private final String sessionId;

    private final ICIFEXComponent cifex;

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