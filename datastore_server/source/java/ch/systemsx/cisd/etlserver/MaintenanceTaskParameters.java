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

package ch.systemsx.cisd.etlserver;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.utilities.PropertyUtils;

/**
 * @author Izabela Adamczyk
 */
public class MaintenanceTaskParameters
{
    private static final String TIME_FORMAT = "HH:mm";

    private static final int ONE_DAY_IN_SEC = 60 * 60 * 24;

    private static final String CLASS_KEY = "class";

    private static final String INTERVAL_KEY = "interval";

    private static final String START_KEY = "start";

    private final String pluginName;

    private final long interval;

    private final String className;

    private final Properties properties;

    private final Date startDate;

    public MaintenanceTaskParameters(Properties properties, String pluginName)
    {
        this.properties = properties;
        this.pluginName = pluginName;
        interval = PropertyUtils.getLong(properties, INTERVAL_KEY, ONE_DAY_IN_SEC);
        className = PropertyUtils.getMandatoryProperty(properties, CLASS_KEY);
        startDate = extractStartDate(PropertyUtils.getProperty(properties, START_KEY));
    }

    private static Date extractStartDate(String timeOrNull)
    {
        try
        {
            if (StringUtils.isBlank(timeOrNull))
            {
                return GregorianCalendar.getInstance().getTime();
            }
            DateFormat format = new SimpleDateFormat(TIME_FORMAT);
            Date parsedDate = format.parse(timeOrNull);
            Calendar rightHourAndMinutes1970 = GregorianCalendar.getInstance();
            rightHourAndMinutes1970.setTime(parsedDate);
            Calendar result = GregorianCalendar.getInstance();
            result.set(Calendar.HOUR_OF_DAY, rightHourAndMinutes1970.get(Calendar.HOUR_OF_DAY));
            result.set(Calendar.MINUTE, rightHourAndMinutes1970.get(Calendar.MINUTE));
            Calendar now = GregorianCalendar.getInstance();
            if (now.after(result))
            {
                result.add(Calendar.DAY_OF_MONTH, 1);
            }
            return result.getTime();
        } catch (ParseException ex)
        {
            throw new ConfigurationFailureException(String.format(
                    "Start date <%s> does not match the required format <%s>", timeOrNull,
                    TIME_FORMAT));
        }
    }

    public long getIntervalSeconds()
    {
        return interval;
    }

    public String getClassName()
    {
        return className;
    }

    public String getPluginName()
    {
        return pluginName;
    }

    public Properties getProperties()
    {
        return properties;
    }

    public Date getStartDate()
    {
        return startDate;
    }
}
