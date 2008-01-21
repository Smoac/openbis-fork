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

package ch.systemsx.cisd.cifex.client.application;

/**
 * Some constants used through the whole web application.
 * 
 * @author Christian Ribeaud
 */
public final class Constants
{
    private Constants()
    {
        // Can not be instantiated.
    }

    /** Name of the <code>RemoteServiceServlet</code> extension (The <i>GWT</i> server side). */
    public static final String CIFEX_SERVLET_NAME = "/cifex";
}
