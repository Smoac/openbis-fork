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

package ch.systemsx.cisd.common.utilities;

import java.security.SecureRandom;
import java.util.Formatter;

import ch.rinn.restrictions.Private;

/**
 * A generator for (pseudo-)random session tokens. It uses the lower half of a time stamp and adds a
 * 128 bit pseudo-random number. Tokens are returned in hexadecimal format.
 * 
 * @author Bernd Rinn
 */
public final class TokenGenerator
{
    @Private
    static final String TIMESTAMP_FORMAT = "%1$ty%1$tm%1$td%1$tH%1$tM%1$tS%1$tL-";

    private final SecureRandom pseudoRandomNumberGenerator = new SecureRandom();

    /**
     * @param timeStamp The time stamp (in milli-seconds since start of the epoch) to base token
     *            generation on.
     * @return A new (pseudo-)random session token in hex format.
     */
    public synchronized String getNewToken(final long timeStamp)
    {
        final Formatter formatter = new Formatter();
        final byte[] bytes = new byte[16];
        formatter.format(TIMESTAMP_FORMAT, timeStamp);
        pseudoRandomNumberGenerator.nextBytes(bytes);
        hexify(formatter, bytes);
        return formatter.toString();
    }

    private static void hexify(final Formatter formatter, final byte[] bytes)
    {
        for (final byte b : bytes)
        {
            formatter.format("%02X", b);
        }
    }

}
