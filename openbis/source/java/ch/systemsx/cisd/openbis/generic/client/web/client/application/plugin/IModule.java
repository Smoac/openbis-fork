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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin;

import java.util.List;

import com.extjs.gxt.ui.client.widget.Component;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenuItem;

/**
 * Plugin that does not depend on entity kind and type. All methods except
 * {@link #initialize(AsyncCallback)} is invoked after invocation of the call back of the
 * initialization method.
 * 
 * @author Izabela Adamczyk
 */
public interface IModule
{
    /**
     * Initializes the module and invoke method {@link AsyncCallback#onSuccess(Object)} on the
     * specified call back after successful initialization. Otherwise
     * {@link AsyncCallback#onFailure(Throwable)} is invoked.
     */
    void initialize(AsyncCallback<Void> callback);
    
    /**
     * Returns a widget that will be used as a top menu element.
     * <p>
     * Note that although any widget may be returned by the method, the height of the top menu will
     * not be adjusted automatically, so make sure that your widget is not too high.
     * </p>
     * <p>
     * {@link TopMenuItem} should be used for standard menus.
     * </p>
     */
    Widget getMenu();
    
    String getModuleName();
    
    String getModuleDescription();

    /**
     * Returns a list with menu items. The list empty if this module isn't applicable. 
     */
    List<Component> getMenuItems();
}
