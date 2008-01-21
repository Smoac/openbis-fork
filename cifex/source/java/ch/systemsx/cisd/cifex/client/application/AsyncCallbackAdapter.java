package ch.systemsx.cisd.cifex.client.application;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * An empty <code>AsyncCallback</code> implementation.
 * <p>
 * This implementation does nothing and should be used when you are not interested at all in the callback methods coming
 * from the server. Instead of putting <code>null</code> as <code>AsyncCallback</code> method parameter you should
 * use {@link #EMPTY_ASYNC_CALLBACK} as <i>GWT</i> does not seem to like it very much (an exception is thrown).
 * </p>
 * 
 * @author Christian Ribeaud
 */
public class AsyncCallbackAdapter implements AsyncCallback
{

    /** A unique instance of this class that does nothing. */
    public final static AsyncCallbackAdapter EMPTY_ASYNC_CALLBACK = new AsyncCallbackAdapter();

    public AsyncCallbackAdapter()
    {
    }

    //
    // AsyncCallback
    //

    public void onFailure(Throwable caught)
    {
    }

    public void onSuccess(Object result)
    {
    }
}