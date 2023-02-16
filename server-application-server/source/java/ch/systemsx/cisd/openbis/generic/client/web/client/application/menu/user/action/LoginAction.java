/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.user.action;

import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.Window;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;

/**
 * Action which shows login page.
 *
 * @author Franz-Josef Elmer
 */
public final class LoginAction implements IDelegatedAction
{
    private final LogoutAction logoutAction;

    public LoginAction(final IViewContext<?> viewContext)
    {
        logoutAction = new LogoutAction(viewContext);
    }

    @Override
    public void execute()
    {
        UrlBuilder urlBuilder = Window.Location.createUrlBuilder();
        urlBuilder.removeParameter(BasicConstant.ANONYMOUS_KEY);
        urlBuilder.setParameter(BasicConstant.ANONYMOUS_KEY, "false");
        String url = urlBuilder.buildString();
        Window.Location.replace(url);
        logoutAction.execute();
    }
}