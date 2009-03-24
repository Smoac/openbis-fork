/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.base.exceptions;

import java.io.IOException;

/**
 * A {@link CheckedExceptionTunnel} for an {@link IOException}. 
 *
 * @author Bernd Rinn
 */
public class IOExceptionUnchecked extends CheckedExceptionTunnel
{
    private static final long serialVersionUID = 1L;

    /**
     * Returns an unchecked exception from a <var>checkedException</var>.
     * 
     * @param checkedException The checked exception to tunnel.
     */
    public IOExceptionUnchecked(final IOException checkedException)
    {
        super(checkedException);

        assert checkedException != null;
    }

    @Override
    public IOException getCause()
    {
        return (IOException) super.getCause();
    }

}
