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

package ch.systemsx.cisd.ant.task.subversion;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;

/**
 * An adapter of a ant task to an {@link ISimpleLogger}.
 * 
 * @author Bernd Rinn
 */
public class AntTaskSimpleLoggerAdapter implements ISimpleLogger
{

    private final Task antTask;

    private final static int toAntLogLevel(LogLevel level)
    {
        switch (level)
        {
            case OFF:
                return Project.MSG_DEBUG;
            case TRACE:
                return Project.MSG_VERBOSE;
            case DEBUG:
                return Project.MSG_DEBUG;
            case INFO:
                return Project.MSG_INFO;
            case WARN:
                return Project.MSG_WARN;
            case ERROR:
                return Project.MSG_ERR;
            default:
                throw new IllegalArgumentException("Illegal log level " + level);
        }
    }

    public AntTaskSimpleLoggerAdapter(Task antTask)
    {
        this.antTask = antTask;
    }

    public void log(LogLevel level, String message)
    {
        antTask.log(message, toAntLogLevel(level));
    }

}
