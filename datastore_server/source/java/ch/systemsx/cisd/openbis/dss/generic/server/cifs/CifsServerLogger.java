/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.dss.generic.server.cifs;

import org.alfresco.jlan.debug.Debug;
import org.alfresco.jlan.debug.DebugInterface;
import org.alfresco.jlan.server.config.ServerConfiguration;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.extensions.config.ConfigElement;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class CifsServerLogger implements DebugInterface
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            CifsServerLogger.class);
    
    private Level level = Level.INFO;
    private ThreadLocal<StringBuilder> messageBuilder = new ThreadLocal<>();

    @Override
    public void initialize(ConfigElement params, ServerConfiguration config) throws Exception
    {
        operationLog.info("init CIFS server logger:\n" + Utils.render(params));
        ConfigElement logLevel = params.getChild("log-level");
        if (logLevel != null)
        {
            logLevel.getValue();
            level = Level.toLevel(logLevel.getValue(), Level.INFO);
        }
    }

    @Override
    public int getLogLevel()
    {
        return Debug.Debug;
    }

    @Override
    public void debugPrint(String str)
    {
        debugPrint(str, Debug.Debug);
    }

    @Override
    public void debugPrint(String str, int l)
    {
        StringBuilder builder = messageBuilder.get();
        if (builder == null)
        {
            builder = new StringBuilder();
            messageBuilder.set(builder);
        }
        builder.append(str);
    }
    
    @Override
    public void debugPrintln(String str)
    {
        debugPrintln(str, Debug.Debug);
    }
    
    @Override
    public void debugPrintln(String str, int l)
    {
        StringBuilder builder = messageBuilder.get();
        if (builder == null)
        {
            operationLog.log(level, str);
        } else
        {
            operationLog.log(level, builder.append(str).toString());
            builder.setLength(0);
        }
    }
    @Override
    public void debugPrintln(Exception ex, int l)
    {
        operationLog.log(level, "", ex);
        
    }
    
    @Override
    public void close()
    {
        operationLog.info("CifsServerLogger.close()");
    }
    
}
