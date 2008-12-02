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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import com.google.gwt.core.client.GWT;

/**
 * @author Franz-Josef Elmer
 */
public class GenericConstants
{
    /**
     * Prefix all widget IDs have to start with.
     */
    public static final String ID_PREFIX = "openbis_";

    private static final String APPLICATION_NAME = "genericopenbis";

    public static final String COMMON_SERVER_NAME = createServicePath("common");

    /**
     * Creates for the specified service name the service path.
     */
    public final static String createServicePath(final String serviceName)
    {
        // Kind of hack. Unclear why an additional APPLICATION_NAME in productive mode is needed.
        return "/" + APPLICATION_NAME + "/" + (GWT.isScript() ? APPLICATION_NAME + "/" : "")
                + serviceName;
    }

    /**
     * The pattern for a correct code.
     * <p>
     * Note that this one accepts letters in lower case.
     * </p>
     */
    public final static String CODE_PATTERN = "^[a-zA-Z0-9_\\-]+$";

    public static final String DEFAULT_DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss zzz";
}
