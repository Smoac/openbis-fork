package ch.systemsx.cisd.cifex.client.application.ui;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.google.gwt.event.dom.client.KeyCodes;

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
        if (event.getKeyCode() == KeyCodes.KEY_ENTER)
        {
            onEnterKey();
            event.stopEvent();
        }
    }
}
