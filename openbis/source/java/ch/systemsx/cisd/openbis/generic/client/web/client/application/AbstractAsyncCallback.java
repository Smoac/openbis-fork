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

import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.event.WindowListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.InvocationException;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.InvalidSessionException;

/**
 * Abstract super class of call backs. Subclasses have to implement {@link #process(Object)}. Note,
 * that instances of this class and its subclasses are stateful and can not be reused.
 * 
 * @author Franz-Josef Elmer
 */
public abstract class AbstractAsyncCallback<T> implements AsyncCallback<T>
{
    public static final ICallbackListener<Object> DEFAULT_CALLBACK_LISTENER =
            new CallbackListenerAdapter<Object>()
                {

                    //
                    // ICallbackListener
                    //

                    @Override
                    public final void onFailureOf(final AbstractAsyncCallback<Object> callback,
                            final String failureMessage, final Throwable throwable)
                    {
                        MessageBox.alert("Error", failureMessage, null);
                    }
                };

    private static ICallbackListener<?> staticCallbackListener = DEFAULT_CALLBACK_LISTENER;

    private static final AsyncCallbackCollection asyncCallbacks = new AsyncCallbackCollection();

    /**
     * Sets all callback objects silent.
     * <p>
     * <b>Note</b>: THIS METHOD SHOULD NEVER BE USED. It is only used inside the testing framework.
     * </p>
     */
    public static void setAllCallbackObjectsSilent()
    {
        asyncCallbacks.setSilent();
        asyncCallbacks.clear();
    }

    /**
     * Sets the global callback listener.
     * <p>
     * Note: THIS METHOD SHOULD NEVER BE USED. It is only used inside the testing framework.
     * </p>
     */
    public final static <T> void setStaticCallbackListener(
            final ICallbackListener<T> callbackListener)
    {
        assert callbackListener != null : "Unspecified ICallbackListener implementation.";
        staticCallbackListener = callbackListener;
    }

    private final ICallbackListener<T> callbackListener;

    boolean silent;

    // can be null only during tests
    protected final IViewContext<?> viewContext;

    // should the login page appear when the 'session terminated' exception occurs
    private final boolean reloadWhenSessionTerminated;

    public AbstractAsyncCallback(final IViewContext<?> viewContext)
    {
        this(viewContext, false);
    }

    public AbstractAsyncCallback(final IViewContext<?> viewContext,
            final ICallbackListener<T> callbackListenerOrNull)
    {
        this(viewContext, callbackListenerOrNull, true);
    }

    public AbstractAsyncCallback(final IViewContext<?> viewContext,
            boolean reloadWhenSessionTerminated)
    {
        this(viewContext, null, reloadWhenSessionTerminated);
    }

    private AbstractAsyncCallback(final IViewContext<?> viewContext,
            final ICallbackListener<T> callbackListenerOrNull, boolean reloadWhenSessionTerminated)
    {
        this.viewContext = viewContext;
        this.reloadWhenSessionTerminated = reloadWhenSessionTerminated;
        // If static ICallbackListener is not DEFAULT_CALLBACK_LISTENER, then we assume being in
        // testing mode. So no customized ICallbackListener (specified in the constructor) possible.
        if (staticCallbackListener != DEFAULT_CALLBACK_LISTENER)
        {
            callbackListener = cast(staticCallbackListener);
            asyncCallbacks.add(this);
        } else if (callbackListenerOrNull == null)
        {
            callbackListener = cast(staticCallbackListener);
        } else
        {
            callbackListener = callbackListenerOrNull;
        }
        assert callbackListener != null : "Unspecified ICallbackListener implementation.";
    }

    @SuppressWarnings("unchecked")
    private final static <T> ICallbackListener<T> cast(final ICallbackListener<?> callbackListener)
    {
        return (ICallbackListener<T>) staticCallbackListener;
    }

    /**
     * Terminates {@link #onFailure(Throwable)}.
     * <p>
     * Default behavior does nothing. Override this in subclasses.
     * </p>
     */
    protected void finishOnFailure(final Throwable caught)
    {
    }

    /**
     * Processes the specified result of an asynchronous method invocation.
     */
    protected abstract void process(final T result);

    /**
     * Returns the callback id which can be used in tests.
     * <p>
     * This method should be subclassed if differentiation between callbacks of the same class is
     * needed in the tests.
     * </p>
     */
    public String getCallbackId()
    {
        String id = getClass().getName();
        return id;
    }

    //
    // AsyncCallback
    //

    public final void onFailure(final Throwable caught)
    {
        if (silent)
        {
            return;
        }
        final String msg;
        if (caught instanceof InvocationException)
        {
            if (StringUtils.isBlank(caught.getMessage()))
            {
                msg = getMessage(Dict.EXCEPTION_INVOCATION_MESSAGE);
            } else
            {
                msg = caught.getMessage();
            }
        } else
        {
            final String message = caught.getMessage();
            if (StringUtils.isBlank(message))
            {
                msg = getMessage(Dict.EXCEPTION_WITHOUT_MESSAGE, caught.getClass().getName());
            } else
            {
                msg = message;
            }
        }
        if (caught instanceof InvalidSessionException)
        {
            showSessionTerminated(msg);
        } else
        {
            callbackListener.onFailureOf(this, msg, caught);
        }
        finishOnFailure(caught);
    }

    private String getMessage(String messageKey, Object... params)
    {
        if (viewContext != null)
        {
            return viewContext.getMessage(messageKey, params);
        } else
        {
            return messageKey;
        }
    }

    private void showSessionTerminated(String msg)
    {
        Dialog dialog = new Dialog();
        dialog.setTitle(getMessage(Dict.MESSAGEBOX_WARNING));

        dialog.addText(msg);
        dialog.setHideOnButtonClick(false);
        dialog.setModal(true);
        dialog.setHideOnButtonClick(true);
        dialog.show();
        // logout the user after his confirmation
        dialog.addWindowListener(new WindowListener()
            {
                @Override
                public void windowHide(WindowEvent we)
                {
                    if (viewContext != null && reloadWhenSessionTerminated)
                    {
                        final IPageController pageController = viewContext.getPageController();
                        pageController.reload(true);
                    }
                }
            });
    }

    public final void onSuccess(final T result)
    {
        if (silent)
        {
            return;
        }
        process(result);
        callbackListener.finishOnSuccessOf(this, result);
    }
}