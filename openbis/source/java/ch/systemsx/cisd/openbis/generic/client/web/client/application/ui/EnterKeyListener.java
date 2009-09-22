package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.google.gwt.user.client.ui.KeyboardListener;

/**
 * A {@link KeyListener} implementation which does something when enter key is entered.
 * 
 * @author Christian Ribeaud
 */
public abstract class EnterKeyListener extends KeyListener
{
    protected abstract void onEnterKey();

    //
    // KeyListener
    //

    @Override
    public final void componentKeyUp(final ComponentEvent event)
    {
        if (event.getKeyCode() == KeyboardListener.KEY_ENTER)
        {
            onEnterKey();
            event.stopEvent();
            event.doit = false;
        }
    }
}
