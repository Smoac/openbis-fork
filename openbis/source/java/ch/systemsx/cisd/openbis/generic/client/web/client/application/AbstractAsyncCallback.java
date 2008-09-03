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

import com.extjs.gxt.ui.client.widget.Info;
import com.google.gwt.user.client.rpc.AsyncCallback;


/**
 * Abstract super class of call backs. Its implements {@link #onFailure(Throwable)}.
 *
 * @author Franz-Josef Elmer
 */
public abstract class AbstractAsyncCallback<T> implements AsyncCallback<T>
{
    protected final GenericViewContext viewContext;
    
    /**
     * Creates an instance for the specified view context.
     */
    public AbstractAsyncCallback(GenericViewContext viewContext)
    {
        this.viewContext = viewContext;
    }
    
    public void onFailure(Throwable caught)
    {
        Info.display("Error", caught.toString());
    }

}
