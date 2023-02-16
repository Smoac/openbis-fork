/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.server;

/**
 * The name of the components declared on <code>genericApplicationContext.xml</code>.
 * 
 * @author Christian Ribeaud
 */
public final class ComponentNames
{

    private ComponentNames()
    {
        // Can not be instantiated.
    }

    public final static String AUTHENTICATION_SERVICE = "authentication-service";

    public final static String SESSION_MANAGER = "session-manager";

    public final static String DISPLAY_SETTINGS_PROVIDER = "display-settings-provider";

    public static final String LOG_INTERCEPTOR = "log-interceptor";

    public static final String DAO_FACTORY = "dao-factory";

    public static final String DSS_FACTORY = "dss-factory";

    public static final String COMMON_BUSINESS_OBJECT_FACTORY = "common-business-object-factory";

    public static final String REMOTE_HOST_VALIDATOR = "remote-host-validator";

    public static final String MANAGED_PROPERTY_EVALUATOR_FACTORY =
            "managed-property-evaluator-factory";

    public static final String RELATIONSHIP_SERVICE = "relationship-service";

    public static final String PROPERTIES_BATCH_MANAGER = "properties-batch-manager";
    
    public static final String JYTHON_EVALUATOR_POOL = "jython-evaluator-pool";

    public static final String OPENBIS_SUPPORT_EMAIL = "openbis.support.email";
}
