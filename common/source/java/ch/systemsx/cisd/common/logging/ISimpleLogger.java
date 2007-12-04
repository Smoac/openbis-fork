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

/**
 * A role that represents a simple logger (may be an ant task or a log4j logger}.
 *
 * @author Bernd Rinn
 */
public interface ISimpleLogger
{

    /** A simple form of log levels. */
    public enum Level { ERROR, WARN, INFO, DEBUG }
    
    /** Log <var>message</var> at log <var>level</var> out to some log file or display. */
    public void log(Level level, String message);
    
}
