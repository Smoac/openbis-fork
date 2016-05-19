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

import org.alfresco.config.ConfigElement;
import org.alfresco.jlan.debug.DebugInterface;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

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
    public void initialize(ConfigElement params) throws Exception
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
    public void debugPrint(String str)
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
    public void close()
    {
        operationLog.info("CifsServerLogger.close()");
    }
    
}
