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

import com.extjs.gxt.ui.client.widget.Layout;
import com.extjs.gxt.ui.client.widget.layout.AnchorLayout;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

import ch.systemsx.cisd.cifex.client.Configuration;
import ch.systemsx.cisd.cifex.client.ICIFEXService;
import ch.systemsx.cisd.cifex.client.ICIFEXServiceAsync;
import ch.systemsx.cisd.cifex.client.InvalidSessionException;
import ch.systemsx.cisd.cifex.client.application.ui.PageControllerHelper;
import ch.systemsx.cisd.cifex.client.application.utils.GWTUtils;
import ch.systemsx.cisd.cifex.shared.basic.dto.CurrentUserInfoDTO;
import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;

/**
 * Entry point of <i>GWT</i> <i>CIFEX</i>.
 * <p>
 * {@link #onModuleLoad()} gets called when the user enters the application (by calling
 * <code>index.html</code> page) or when the user pushes the navigator refresh button.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class CIFEXEntryPoint implements EntryPoint
{

    private final static ICIFEXServiceAsync createCIFEXService()
    {
        final ICIFEXServiceAsync service = (ICIFEXServiceAsync) GWT.create(ICIFEXService.class);
        final ServiceDefTarget endpoint = (ServiceDefTarget) service;
        // 'GWT.getModuleBaseURL()/GWT.getHostPageBaseURL()' returns
        // 'http://localhost:8888/ch.systemsx.cisd.cifex.Cifex/' in Hosted/Web
        // mode and 'http://localhost:8080/cifex/' when deployed.
        //
        // 'GWT.getModuleName()' always returns 'ch.systemsx.cisd.cifex.Cifex'.
        // Do not prepend 'GWT.getModuleBaseURL()' here as we want
        // '/cifex/cifex' in Hosted Mode.
        endpoint.setServiceEntryPoint(ServletPathConstants.CIFEX_SERVLET_NAME);
        return service;
    }

    private final ViewContext createViewContext(final ICIFEXServiceAsync cifexService)
    {
        final PageController pageController = new PageController();
        final ViewContext viewContext =
                new ViewContext(pageController, pageController, cifexService, new Model());
        pageController.setViewContext(viewContext);
        return viewContext;
    }

    //
    // EntryPoint
    //

    // WORKAROUND
    // There is some weird dependency that requires that the class
    // com.extjs.gxt.ui.client.widget.Layout be loaded before the LoginPage is instantiated.
    // Otherwise, there is a strange crash deep in the VM that occurs when Layout is
    // loaded.
    // Class.forName does not work as an alternative, so we need to explicitly reference the
    // class.
    // This seems to be as good a place as any for this.
    @SuppressWarnings("unused")
    private final Layout junk = new AnchorLayout();

    public final void onModuleLoad()
    {

        final ICIFEXServiceAsync cifexService = createCIFEXService();
        final ViewContext viewContext = createViewContext(cifexService);
        final String paramString = GWTUtils.getParamString();
        if (StringUtils.isBlank(paramString) == false)
        {
            viewContext.getModel().setUrlParams(GWTUtils.parseParamString(paramString));
        }
        cifexService.getConfiguration(new AbstractAsyncCallback<Configuration>(viewContext)
            {

                //
                // AsyncCallbackAdapter
                //

                public final void onSuccess(final Configuration result)
                {
                    viewContext.getModel().setConfiguration(result);
                    cifexService.getCurrentUser(new AsyncCallback<CurrentUserInfoDTO>()
                        {

                            //
                            // AsyncCallback
                            //

                            public final void onSuccess(final CurrentUserInfoDTO currentUser)
                            {
                                PageControllerHelper.activatePageBasedOnCurrentContext(viewContext,
                                        currentUser);
                            }

                            public final void onFailure(final Throwable caught)
                            {
                                if (caught instanceof InvalidSessionException)
                                {
                                    viewContext.getPageController().showLoginPage();
                                }
                            }
                        });
                }

            });
    }
}
