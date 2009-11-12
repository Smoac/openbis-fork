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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data;

import static ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants.DATA_STORE_SERVER_WEB_APPLICATION_NAME;

import com.extjs.gxt.ui.client.event.MvcEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.DispatcherListener;

import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.dataset.GenericDataSetViewer;

/**
 * A {@link AbstractDefaultTestCommand} extension for browsing a data set with given code from its
 * detail view. Dispatch is canceled by default before opening URL after URL check succeeds.
 * 
 * @author Piotr Buczek
 */
public final class BrowseDataSet extends AbstractDefaultTestCommand
{
    private final boolean cancelDispatch;

    private final String code;

    private final TechId id;

    public BrowseDataSet(final String code)
    {
        this(code, true);
    }

    public BrowseDataSet(final String code, final boolean cancelDispatch)
    {
        this.code = code;
        this.id = TechId.createWildcardTechId();
        this.cancelDispatch = cancelDispatch;
    }

    public void execute()
    {
        DispatcherListener dispatcherListener = createDispatcherListener();
        Dispatcher dispatcher = Dispatcher.get();
        dispatcher.addDispatcherListener(dispatcherListener);

        GWTTestUtil.clickButtonWithID(GenericDataSetViewer.createId(id)
                + GenericDataSetViewer.VIEW_BUTTON_ID_SUFFIX);

        dispatcher.removeDispatcherListener(dispatcherListener);
    }

    private DispatcherListener createDispatcherListener()
    {
        return new DispatcherListener()
            {
                @Override
                public void beforeDispatch(MvcEvent mvce)
                {
                    String url = String.valueOf(mvce.appEvent.data);
                    assertTrue("Invalid URL: " + url, url.startsWith("https://localhost:8889/"
                            + DATA_STORE_SERVER_WEB_APPLICATION_NAME + "/" + code
                            + "?sessionID=test-"));
                    if (cancelDispatch)
                    {
                        mvce.doit = false;
                    }
                }
            };
    }
}
