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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * A <code>PropertyValueRenderer</code> implementation for <code>Date</code>.
 * 
 * @author Christian Ribeaud
 */
public class DatePropertyValueRenderer extends AbstractPropertyValueRenderer<Date>
{
    /** Default date/time format. */
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss zzz";

    /** Default <code>DateTimeFormat</code> used here. */
    public static final DateTimeFormat defaultDateTimeFormat =
            DateTimeFormat.getFormat(DEFAULT_DATE_TIME_FORMAT);

    private final DateTimeFormat dateTimeFormat;

    public DatePropertyValueRenderer(final IMessageProvider messageProvider, final String pattern)
    {
        this(messageProvider, DateTimeFormat.getFormat(pattern));
    }

    public DatePropertyValueRenderer(final IMessageProvider messageProvider,
            final DateTimeFormat dateTimeFormat)
    {
        super(messageProvider);
        this.dateTimeFormat = dateTimeFormat;
    }

    public DatePropertyValueRenderer(final IMessageProvider messageProvider)
    {
        this(messageProvider, defaultDateTimeFormat);
    }

    //
    // AbstractPropertyValueRenderer
    //

    @Override
    protected final String renderNotNull(final Date value)
    {
        return dateTimeFormat.format(value);
    }
}