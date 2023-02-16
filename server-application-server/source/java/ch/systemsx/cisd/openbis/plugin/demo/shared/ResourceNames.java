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
package ch.systemsx.cisd.openbis.plugin.demo.shared;

/**
 * Resource name used in <i>demo</i> plug-in.
 * <p>
 * Be aware about the uniqueness of the bean names loaded by <i>Spring</i>. Names defined here should not conflict with already existing bean names.
 * Look for other <code>ResourceNames</code> classes.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class ResourceNames
{
    private ResourceNames()
    {
        // Can not be instantiated.
    }

    public final static String DEMO_PLUGIN_SERVICE = "demo-plugin-service";

    public final static String DEMO_PLUGIN_SERVER = "demo-plugin-server";

    public final static String DEMO_BUSINESS_OBJECT_FACTORY = "demo-business-object-factory";

    public final static String DEMO_SAMPLE_SERVER_PLUGIN = "demo-sample-server-plugin";
}
