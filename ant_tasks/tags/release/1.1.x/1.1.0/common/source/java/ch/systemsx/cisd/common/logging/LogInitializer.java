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
import java.net.URL;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * Initializes the logging system. The {@link #init()} method needs to be called once at system startup.
 * 
 * @author Bernd Rinn
 */
public class LogInitializer
{
    static {
        // Do not let log4j configure itself. We will do it our own way.
        // Look at page 84 of the manual to get to know the default initialization.
        System.setProperty("log4j.defaultInitOverride", "true");
    }
    
    private static boolean initialized = false;

    /**
     * Initializes logging system. Does nothing if already initialized.
     * Logging configuration file is assumed to be in <code>&lt;working directory&gt;/ect/log.xml</code>.
     * If not found we look for a classpath resource named <code>/etc/log.xml</code>. If nothing found in both
     * locations <code>org.apache.log4j.BaseConfigurator.configure()</code>
     */
    public static synchronized void init()
    {
        if (initialized)
        {
            return;
        }

        final String logDirectory = "etc";
        final String logFilename = "log.xml";
        final File logFile = new File(logDirectory, logFilename);
        if (logFile.exists())
        {
            // For non-XML files, you will use <code>PropertyConfigurator.configureAndWatch(String)</code>
            DOMConfigurator.configureAndWatch(logFile.getPath());
        } else
        {
            URL url = LogInitializer.class.getResource("/" + logDirectory + "/" + logFilename);
            if (url != null)
            {
                DOMConfigurator.configure(url);
            } else
            {
                BasicConfigurator.configure();
            }
        }
        initialized = true;
    }

}
