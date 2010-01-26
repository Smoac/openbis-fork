/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.ComponentHelper;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.WidgetComponent;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Image;

/**
 * Utilities for {@link Field} class.
 * 
 * @author Tomasz Pylak
 */
public class FieldUtil
{

    private static final int[] INFO_LINK_FIELD_OFFSETS = new int[]
        { 20, 3 };

    private static final String INFO_LINK_POSITION = "tl-tr";

    private static final String MANDATORY_LABEL_SEPARATOR = ":&nbsp;*";

    public static void setMandatoryFlag(Field<?> field, boolean isMandatory)
    {
        if (isMandatory)
        {
            markAsMandatory(field);
        }
    }

    public static void setMandatoryFlag(TextField<?> field, boolean isMandatory)
    {
        if (isMandatory)
        {
            markAsMandatory(field);
        } else
        {
            field.setAllowBlank(true);
        }
    }

    public static void markAsMandatory(TextField<?> field)
    {
        markAsMandatory((Field<?>) field);
        field.setAllowBlank(false);
    }

    public static void markAsMandatory(Field<?> field)
    {
        field.setLabelSeparator(MANDATORY_LABEL_SEPARATOR);
    }

    /** makes all given fields visible or invisible including validation dependent on given value */
    public static void setVisibility(boolean visible, Field<?>... fields)
    {
        for (Field<?> field : fields)
        {
            setVisibility(field, visible);
        }
    }

    /** makes field visible or invisible including validation dependent on given value */
    private static void setVisibility(Field<?> field, boolean visible)
    {
        field.setEnabled(visible);
        field.setVisible(visible);
        field.syncSize();
        if (visible == false)
        {
            // invalidation mark is not removed automatically when we make field invisible
            field.clearInvalid();
        } else if (field.isDirty())
        {
            // validate only if something have been modified and field is shown
            field.validate();
        }
    }

    /**
     * Adds '?' to the field and allows to display chosen message in the info box.
     * @param image 
     */
    public static void addInfoIcon(final Field<?> field, final String message, final Image image)
    {
        // WORKAROUND: Based on GXT functionality for displaying error icon, until there is no
        // dedicated mechanism for showing info icons in GXT
        field.addListener(Events.Render, new Listener<BaseEvent>()
            {
                public void handleEvent(BaseEvent be)
                {
                    final WidgetComponent info = new WidgetComponent(image);
                    info.setStyleName("hands");
                    info.setToolTip(message);
                    info.sinkEvents(Events.OnClick.getEventCode());
                    info.addListener(Events.OnClick, new Listener<BaseEvent>(){
                        public void handleEvent(BaseEvent e)
                        {
                            MessageBox.info("Info", message, null);
                        }});
                    
                    Element parent = field.el().getParent().dom;
                    info.render(parent);
                    info.setStyleAttribute("display", "block");
                    info.el().makePositionable(true);
                    if (!info.isAttached())
                    {
                        ComponentHelper.doAttach(info);
                    }
                    DeferredCommand.addCommand(new Command()
                        {
                            public void execute()
                            {
                                info.el().alignTo(field.getElement(), INFO_LINK_POSITION,
                                        INFO_LINK_FIELD_OFFSETS);
                            }
                        });
                    if (GXT.isIE || GXT.isOpera)
                    {
                        DeferredCommand.addCommand(new Command()
                            {
                                public void execute()
                                {
                                    info.el().alignTo(field.getElement(), INFO_LINK_POSITION,
                                            INFO_LINK_FIELD_OFFSETS);
                                }
                            });
                    }
                    field.el().repaint();
                }
            });
    }
}
