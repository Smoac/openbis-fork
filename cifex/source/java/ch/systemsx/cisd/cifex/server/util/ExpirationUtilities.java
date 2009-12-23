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

package ch.systemsx.cisd.cifex.server.util;

import static ch.systemsx.cisd.common.utilities.DateTimeUtils.extendUntilEndOfDay;

import java.util.Date;

import org.apache.commons.lang.time.DateUtils;

/**
 * Utilities for user and file expiration.
 * 
 * @author Bernd Rinn
 */
public class ExpirationUtilities
{

    /**
     * Checks that the new expiration date is in the valid range, otherwise sets it to the limit of
     * what is allowed ('fixes it').
     * 
     * @param proposedExpirationDateOrNull The expiration date the user would like to set, or
     *            <code>null</code>, if the default should be used.
     * @param registrationDateOrNull The date when the entity to compute the expiration date for was
     *            registered or <code>null</code>. If <code>null</code>, the current date will be
     *            used as registration date.
     * @param maxRetentionDaysOrNull The maximal days of retention for the entity to compute the
     *            expiration date for or <code>null</code>. If <code>null</code>, no limit applies.
     * @param defaultRetentionDays The default number of days of retention.
     * @return The new expiration date of the entity
     */
    public static Date fixExpiration(final Date proposedExpirationDateOrNull,
            final Date registrationDateOrNull, final Integer maxRetentionDaysOrNull,
            final int defaultRetentionDays)
    {
        assert defaultRetentionDays >= 0;
        
        final Date registrationDate =
                (registrationDateOrNull == null) ? new Date() : registrationDateOrNull;
        final Date proposedExpirationDate =
                (proposedExpirationDateOrNull == null) ? DateUtils.addDays(registrationDate,
                        defaultRetentionDays) : proposedExpirationDateOrNull;

        if (maxRetentionDaysOrNull == null)
        {
            return extendUntilEndOfDay(proposedExpirationDate);
        }
        final Date maxExpirationDate = DateUtils.addDays(registrationDate, maxRetentionDaysOrNull);
        if (proposedExpirationDate.getTime() > maxExpirationDate.getTime())
        {
            return extendUntilEndOfDay(maxExpirationDate);
        } else
        {
            return extendUntilEndOfDay(proposedExpirationDate);
        }
    }

}
