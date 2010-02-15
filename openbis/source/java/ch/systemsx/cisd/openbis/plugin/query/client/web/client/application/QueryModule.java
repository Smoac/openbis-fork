/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.query.client.web.client.application;

import java.util.List;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IModule;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.IQueryClientServiceAsync;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class QueryModule implements IModule
{
    private final IViewContext<IQueryClientServiceAsync> viewContext;

    QueryModule(IViewContext<IQueryClientServiceAsync> viewContext)
    {
        this.viewContext = viewContext;
    }
    
    public Widget getMenu()
    {
        return new Button("hello");
    }

    public List<Component> getMenuItems()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String getModuleDescription()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String getModuleName()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void initialize(AsyncCallback<Void> callback)
    {
        // TODO Auto-generated method stub
        
    }

}
