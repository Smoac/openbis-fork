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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment;

import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AppEvents;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.CategoriesBuilder;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.Login;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.OpenTab;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ListExperiments;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ShowExperiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;

/**
 * @author Tomasz Pylak
 */
public class GenericExperimentAttachmentDownloadTest extends AbstractGWTTestCase
{
    private static final String DEFAULT = "DEFAULT";

    private static final String EXP_REUSE = "EXP-REUSE";

    private static final String CISD_CISD_DEFAULT = "CISD:/CISD/DEFAULT";

    private static final String SIRNA_HCS = "SIRNA_HCS";

    private void prepareShowExperiment(final String projectName, final String experimentTypeName,
            final String experimentCode)
    {
        remoteConsole.prepare(new Login("test", "a"));
        remoteConsole.prepare(new OpenTab(CategoriesBuilder.CATEGORIES.EXPERIMENTS,
                CategoriesBuilder.MENU_ELEMENTS.LIST));
        remoteConsole.prepare(new ListExperiments(projectName, experimentTypeName));
        remoteConsole.prepare(new ShowExperiment(experimentCode));
    }

    public final void testDownloadAttachment()
    {
        prepareShowExperiment(DEFAULT, SIRNA_HCS, EXP_REUSE);
        remoteConsole.prepare(new ClickDownloadAttachmentCmdTest("cellPlates.txt",
                CISD_CISD_DEFAULT + "/" + EXP_REUSE));
        // ensures that opening the url will trigger calling the appropriate callback
        OpenedUrlCallback openedUrlCallback = new OpenedUrlCallback(client.tryToGetViewContext());
        registerOpenURLController(openedUrlCallback);

        remoteConsole.prepare(new CheckUrlContentCmdTest(openedUrlCallback, "3VCP1\n3VCP2\n3VCP3"));

        remoteConsole.finish(20000);
        client.onModuleLoad();
    }

    private UrlOpenedController registerOpenURLController(OpenedUrlCallback openedUrlCallback)
    {
        UrlOpenedController controller = new UrlOpenedController(openedUrlCallback);
        Dispatcher.get().addController(controller);
        return controller;
    }

    // called automatically by the controller when any URL is contacted
    static class OpenedUrlCallback extends AbstractAsyncCallback<String>
    {
        private String openedUrl;

        public OpenedUrlCallback(IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(String url)
        {
            this.openedUrl = url;
        }

        public String tryGetOpenedUrl()
        {
            return openedUrl;
        }

    }

    // executed after a callback opening an url is called. Checks if the opened url is the expected
    // one and if it has the expected content.
    class CheckUrlContentCmdTest extends AbstractDefaultTestCommand
    {
        private final String expectedContent;

        private final OpenedUrlCallback openedUrlCallback;

        public CheckUrlContentCmdTest(OpenedUrlCallback openedUrlCallback, String expectedContent)
        {
            this.expectedContent = expectedContent;
            this.openedUrlCallback = openedUrlCallback;
            addCallbackClass(OpenedUrlCallback.class);
        }

        public void execute()
        {
            String openedUrl = openedUrlCallback.tryGetOpenedUrl();
            assertNotNull("An URL was expected to be opened, but it did not happen.", openedUrl);
            assertUrlContent(openedUrl, expectedContent);
        }
    }

    private static void assertUrlContent(String url, String expectedContent)
    {
        // TODO 2009-01-12, Tomasz Pylak: assert that content at url is equal to the expected one
    }

    // listens to the event of opening an url and calls a specified callback in that case
    class UrlOpenedController extends Controller
    {
        private final OpenedUrlCallback openedUrlCallback;

        public UrlOpenedController(OpenedUrlCallback openedUrlCallback)
        {
            this.openedUrlCallback = openedUrlCallback;
            registerEventTypes(AppEvents.OPEN_URL_EVENT);
        }

        @Override
        public void handleEvent(AppEvent<?> event)
        {
            switch (event.type)
            {
                case AppEvents.OPEN_URL_EVENT:
                    String openedUrl = (String) event.data;
                    openedUrlCallback.onSuccess(openedUrl);
                    break;
                default:
                    throw new IllegalArgumentException("Unknow event '" + event + "'.");
            }
        }
    }
}
