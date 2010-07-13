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

package ch.systemsx.cisd.openbis.generic.server.plugin;


/**
 * A registry for data set server plug-ins.
 * <p>
 * Note that this class is instantiated via following <i>Spring</i> configuration entry:
 * 
 * <pre>
 * &lt;bean class=&quot;ch.systemsx.cisd.openbis.plugin.DataSetServerPluginRegistry&quot;
 *   factory-method=&quot;getInstance&quot; /&gt;
 * </pre>
 * 
 * making sure that we have one and only one instance of this class.
 * </p>
 *
 * @author     Franz-Josef Elmer
 */
public class DataSetServerPluginRegistry extends AbstractPluginRegistry<IDataSetServerPlugin>
{
    private static DataSetServerPluginRegistry instance = new DataSetServerPluginRegistry();

    private DataSetServerPluginRegistry()
    {
    }
    
    public static DataSetServerPluginRegistry init()
    {
        instance = new DataSetServerPluginRegistry();
        return instance;
    }

    /**
     * Returns the unique instance of this class.
     */
    public final static synchronized DataSetServerPluginRegistry getInstance()
    {
        return instance;
    }

    @Override
    protected String getBeanNameOfGenericPlugin()
    {
        return ch.systemsx.cisd.openbis.generic.shared.ResourceNames.GENERIC_DATA_SET_SERVER_PLUGIN;
    }

}
