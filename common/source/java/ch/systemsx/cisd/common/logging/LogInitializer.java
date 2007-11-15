/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.logging;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * Initializes the logging system. The {@link #init()} method needs to be called once at system startup.
 * 
 * @author Bernd Rinn
 */
public class LogInitializer
{
    private static final String LOG_DIRECTORY = "etc";

    private static final String LOG_FILENAME = "log.xml";

    static
    {
        // Do not let log4j configure itself. We will do it our own way.
        // Look at page 84 of the manual to get to know the default initialization.
        System.setProperty("log4j.defaultInitOverride", "true");
    }

    private static boolean initialized = false;

    private final static URL createURL()
    {
        return LogInitializer.class.getResource("/" + LOG_DIRECTORY + "/" + LOG_FILENAME);
    }

    private final static File createLogFile()
    {
        return new File(LOG_DIRECTORY, LOG_FILENAME);
    }

    private final static void configureFromFile(final File logFile)
    {
        assert logFile != null && logFile.exists() : "Given log file must be not null and must exist.";
        // For non-XML files, you will use <code>PropertyConfigurator.configureAndWatch(String)</code>
        DOMConfigurator.configureAndWatch(logFile.getPath());
        LogLog.debug(String.format("Log configured from file '%s' (watching).", logFile.getAbsolutePath()));
    }

    private final static void configureFromURL(final URL url)
    {
        assert url != null : "Given url can not be null.";
        try
        {
            final File logFile = new File(url.toURI());
            if (logFile.exists())
            {
                configureFromFile(logFile);
                return;
            }
        } catch (URISyntaxException ex)
        {
            LogLog.warn(String.format("Given url '%s' could not be parsed.", url), ex);
        }
        DOMConfigurator.configure(url);
        LogLog.debug(String.format("Log configured from URL '%s' (NOT watching).", url));
    }

    /**
     * Initializes logging system. Does nothing if already initialized.
     * <p>
     * Logging configuration file is assumed to be in <code>&lt;working directory&gt;/etc/log.xml</code>. If not
     * found we look for a classpath resource named <code>/etc/log.xml</code>.<br>
     * If nothing found in both locations <code>org.apache.log4j.BaseConfigurator.configure()</code> is used.
     * </p>
     */
    public final static synchronized void init()
    {
        if (initialized)
        {
            return;
        }
        final File logFile = createLogFile();
        if (logFile.exists())
        {
            configureFromFile(logFile);
        } else
        {
            final URL url = createURL();
            if (url != null)
            {
                configureFromURL(url);
            } else
            {
                BasicConfigurator.configure();
            }
        }
        initialized = true;
        LogLog.setQuietMode(true);
    }
}
