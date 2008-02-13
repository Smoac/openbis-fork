package ch.systemsx.cisd.cifex.client.application;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.gwtext.client.widgets.QuickTips;
import com.gwtext.client.widgets.form.Field;

import ch.systemsx.cisd.cifex.client.ICIFEXService;
import ch.systemsx.cisd.cifex.client.ICIFEXServiceAsync;
import ch.systemsx.cisd.cifex.client.InvalidSessionException;
import ch.systemsx.cisd.cifex.client.application.ui.FileDownloadHelper;
import ch.systemsx.cisd.cifex.client.application.utils.GWTUtils;
import ch.systemsx.cisd.cifex.client.application.utils.StringUtils;
import ch.systemsx.cisd.cifex.client.dto.Configuration;
import ch.systemsx.cisd.cifex.client.dto.User;

/**
 * Entry point of <i>GWT</i> <i>CIFEX</i>.
 * <p>
 * {@link #onModuleLoad()} gets called when the user enters the application (by calling <code>index.html</code> page)
 * or when the user pushes the navigator refresh button.
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
        endpoint.setServiceEntryPoint(Constants.CIFEX_SERVLET_NAME);
        return service;
    }

    private final ViewContext createViewContext(final ICIFEXServiceAsync cifexService)
    {
        final IMessageResources messageResources = (IMessageResources) GWT.create(IMessageResources.class);
        final PageController pageController = new PageController();
        final ViewContext viewContext = new ViewContext(pageController, cifexService, new Model(), messageResources);
        pageController.setViewContext(viewContext);
        return viewContext;
    }

    //
    // EntryPoint
    //

    public final void onModuleLoad()
    {
        Field.setMsgTarget("side");
        QuickTips.init();
        final ICIFEXServiceAsync cifexService = createCIFEXService();
        final ViewContext viewContext = createViewContext(cifexService);
        final String paramString = GWTUtils.getParamString();
        if (StringUtils.isBlank(paramString) == false)
        {
            viewContext.getModel().setUrlParams(GWTUtils.parseParamString(paramString));
        }
        cifexService.getConfiguration(new AbstractAsyncCallback(viewContext)
            {

                //
                // AsyncCallbackAdapter
                //

                public final void onSuccess(final Object result)
                {
                    viewContext.getModel().setConfiguration((Configuration) result);
                    cifexService.getCurrentUser(new AsyncCallback()
                        {

                            //
                            // AsyncCallback
                            //

                            public final void onSuccess(final Object res)
                            {
                                final IPageController pageController = viewContext.getPageController();
                                if (res != null)
                                {
                                    final Model model = viewContext.getModel();
                                    model.setUser((User) res);
                                    FileDownloadHelper.startFileDownload(model);
                                    pageController.createMainPage();
                                } else
                                {
                                    pageController.createLoginPage();
                                }
                            }

                            public final void onFailure(final Throwable caught)
                            {
                                if (caught instanceof InvalidSessionException)
                                {
                                    viewContext.getPageController().createLoginPage();
                                }
                            }
                        });
                }

            });
    }
}
