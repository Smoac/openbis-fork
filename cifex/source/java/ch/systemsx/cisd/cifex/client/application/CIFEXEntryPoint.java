package ch.systemsx.cisd.cifex.client.application;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.RootPanel;
import com.gwtext.client.widgets.QuickTips;
import com.gwtext.client.widgets.form.Field;

import ch.systemsx.cisd.cifex.client.ICIFEXService;
import ch.systemsx.cisd.cifex.client.ICIFEXServiceAsync;

/**
 * Entry point of <i>GWT</i> based <i>LIMS</i> client.
 * 
 * @author Christian Ribeaud
 */
public final class CIFEXEntryPoint implements EntryPoint, IPageController
{
    private ICIFEXServiceAsync cifexService;

    private IMessageResources messageResources;

    private final static ICIFEXServiceAsync createLIMSService()
    {
        final ICIFEXServiceAsync service = (ICIFEXServiceAsync) GWT.create(ICIFEXService.class);
        final ServiceDefTarget endpoint = (ServiceDefTarget) service;
        endpoint.setServiceEntryPoint(Constants.CIFEX_SERVLET_NAME);
        return service;
    }

    /**
     * This method clears <code>RootPanel</code>.
     * <p>
     * Note that this method should be called in a very early stage, before adding and/or building any new GUI stuff.
     * </p>
     */
    private final void clearRootPanel()
    {
        final RootPanel rootPanel = RootPanel.get();
        rootPanel.clear();
    }

    final ICIFEXServiceAsync getCifexService()
    {
        return cifexService;
    }

    final IMessageResources getMessageResources()
    {
        return messageResources;
    }

    //
    // EntryPoint
    //

    public final void onModuleLoad()
    {
        Field.setMsgTarget("side");
        QuickTips.init();
        cifexService = createLIMSService();
        messageResources = (IMessageResources) GWT.create(IMessageResources.class);
        cifexService.isAuthenticated(new AsyncCallbackAdapter()
            {

                //
                // AsyncCallbackAdapter
                //

                public final void onSuccess(final Object result)
                {
                    if (((Boolean) result).booleanValue())
                    {
                        createMainPage();
                    } else
                    {
                        createLoginPage();
                    }
                }
            });
    }

    //
    // IPageController
    //

    public final void createLoginPage()
    {
        clearRootPanel();
        final LoginPage loginPage = new LoginPage(this, getCifexService(), getMessageResources());
        RootPanel.get().add(loginPage);
    }

    public final void createMainPage()
    {
        clearRootPanel();
        // TODO 2008-01-21, Christian Ribeaud: Make something more useful here.
    }

}
