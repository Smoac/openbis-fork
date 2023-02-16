/*
 * Copyright ETH 2009 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import com.extjs.gxt.ui.client.widget.Component;

/**
 * Helper class which sets CSS styles of components.
 * 
 * @author Tomasz Pylak
 */
public class PlateStyleSetter
{
    public static final int WELL_BOX_SIZE_PX = 20;

    public static final int WELL_SPACING_PX = 2;

    public static Component setWellStyle(Component component)
    {
        setPointerCursor(component);
        setWellSize(component);
        return component;
    }

    public static Component setWellLabelStyle(Component component)
    {
        setWellSize(component);
        return component;
    }

    private static void setWellSize(Component component)
    {
        component.setWidth("" + WELL_BOX_SIZE_PX);
        component.setHeight("" + WELL_BOX_SIZE_PX);
    }

    public static void setPointerCursor(Component component)
    {
        component.setStyleAttribute("cursor", "pointer");
    }

    public static void setBackgroudColor(Component component, String color)
    {
        component.setStyleAttribute("background-color", color);
    }
}
