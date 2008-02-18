package ch.systemsx.cisd.cifex.client.application;

import ch.systemsx.cisd.cifex.client.application.ui.ModelBasedGrid;
import ch.systemsx.cisd.cifex.client.dto.User;

/**
 * A <code>AbstractAsyncCallback</code> extension to refresh the list of users.
 * 
 * @author Christian Ribeaud
 */
final class UserGridRefresherCallback extends AbstractAsyncCallback
{

    private final ModelBasedGrid userGrid;

    UserGridRefresherCallback(final ViewContext context, final ModelBasedGrid userGrid)
    {
        super(context);
        this.userGrid = userGrid;
    }

    //
    // AbstractAsyncCallback
    //

    public final void onSuccess(final Object object)
    {
        final ViewContext viewContext = getViewContext();
        // Only administrators have access to the admin page, so no need to check the currently logged user here.
        if (viewContext.getPageController().getActivePage().equals(PageController.ADMIN_PAGE))
        {
            viewContext.getCifexService().listUsers(new ListUsersCallback());
        } else
        {
            viewContext.getCifexService().listUsersRegisteredBy(viewContext.getModel().getUser(),
                    new ListUsersCallback());
        }
    }

    //
    // Helper classes
    //

    private final class ListUsersCallback extends AbstractAsyncCallback
    {

        ListUsersCallback()
        {
            super(UserGridRefresherCallback.this.getViewContext());
        }

        //
        // AbstractAsyncCallback
        //

        public final void onSuccess(final Object res)
        {
            userGrid.reloadStore((User[]) res, userGrid.getModel());
        }
    }
}