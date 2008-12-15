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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type;

import com.extjs.gxt.ui.client.widget.LayoutContainer;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;

/**
 * A {@link LayoutContainer} extension for registering a new property type.
 * 
 * @author Christian Ribeaud
 */
public final class PropertyTypeRegistration extends AbstractRegistrationForm
{
    private static final String PREFIX = "property-type-registration_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public PropertyTypeRegistration(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, ID_PREFIX);
        this.viewContext = viewContext;
    }

    //
    // AbstractRegistrationForm
    //

    @Override
    protected final void submitForm()
    {

    }
}
