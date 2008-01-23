package ch.systemsx.cisd.cifex.client.application;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.gwtext.client.widgets.QuickTips;
import com.gwtext.client.widgets.form.Field;

import ch.systemsx.cisd.cifex.client.ICIFEXService;
import ch.systemsx.cisd.cifex.client.ICIFEXServiceAsync;

/**
 * Entry point of <i>GWT</i> based <i>LIMS</i> client.
 * 
 * @author Christian Ribeaud
 */
public final class CIFEXEntryPoint implements EntryPoint
{
    private final static ICIFEXServiceAsync createLIMSService()
    {
        final ICIFEXServiceAsync service = (ICIFEXServiceAsync) GWT.create(ICIFEXService.class);
        final ServiceDefTarget endpoint = (ServiceDefTarget) service;
        endpoint.setServiceEntryPoint(Constants.CIFEX_SERVLET_NAME);
        return service;
    }

    //
    // EntryPoint
    //

    public final void onModuleLoad()
    {
        Field.setMsgTarget("side");
        QuickTips.init();
        ICIFEXServiceAsync cifexService = createLIMSService();
        IMessageResources messageResources = (IMessageResources) GWT.create(IMessageResources.class);
        final PageController pageController = new PageController();
        ViewContext viewContext = new ViewContext(pageController, cifexService, new Model(), messageResources);
        pageController.setViewContext(viewContext);
        cifexService.isAuthenticated(new AsyncCallbackAdapter()
            {

                //
                // AsyncCallbackAdapter
                //

                public final void onSuccess(final Object result)
                {
                    if (((Boolean) result).booleanValue())
                    {
                        pageController.createMainPage();
                    } else
                    {
                        pageController.createLoginPage();
                    }
                }
            });
    }

}
