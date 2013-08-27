/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.common.controlfile;

import java.io.File;

import ch.systemsx.cisd.common.filesystem.control.ControlDirectoryEventFeed;
import ch.systemsx.cisd.common.filesystem.control.DelayingDecorator;
import ch.systemsx.cisd.common.filesystem.control.IValueFilter;
import ch.systemsx.cisd.common.filesystem.control.ParameterMap;

/**
 * @author pkupczyk
 */
public class ControlFileReader
{

    private static final String CONTROL_FILE_DIRECTORY = ".control";

    private static final long CONTROL_FILE_MAX_DELAY = 10 * 1000L;

    private static final String LOG_SERVICE_CALL_START = "log-service-call-start";

    private static final String ON = "on";

    private static final String OFF = "off";

    private ParameterMap parameterMap;

    public ControlFileReader()
    {
        this(new File(CONTROL_FILE_DIRECTORY), CONTROL_FILE_MAX_DELAY);
    }

    public ControlFileReader(File controlFileDirectory, long controlFileMaxDelay)
    {
        parameterMap =
                new ParameterMap(new DelayingDecorator(controlFileMaxDelay, new ControlDirectoryEventFeed(controlFileDirectory)));
        parameterMap.addParameter(LOG_SERVICE_CALL_START, OFF, new IValueFilter()
            {

                @Override
                public boolean isValid(String value)
                {
                    return ON.equalsIgnoreCase(value) || OFF.equalsIgnoreCase(value);
                }
            });
    }

    public boolean isLogServiceCallStartEnabled()
    {
        String value = parameterMap.get(LOG_SERVICE_CALL_START);
        return ON.equalsIgnoreCase(value);
    }

}
