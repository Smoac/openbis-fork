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

package ch.systemsx.cisd.openbis.generic.client.web.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import ch.systemsx.cisd.common.utilities.Template;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;

/**
 * Takes a help page identifier (i.e., the context for the help request) and redirects to the
 * appropriate page for that identifier.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
@Controller
@RequestMapping(
    { "/help", "/openbis/help" })
public class HelpRedirectServlet extends AbstractController
{

    /**
     * Write an HTTP redirect to the help page identified by the request parameters into the
     * response.
     */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception
    {
        response.sendRedirect(getHelpPageAbsoluteURLForRequest(request));
        return null;
    }

    private String getDocumentationRootURL()
    {
        return "https://wiki-bsse.ethz.ch/display/CISDDoc/OnlineHelp";
    }

    private Template getHelpPageWikiURLTemplate()
    {
        return new Template(
                "https://wiki-bsse.ethz.ch/pages/createpage.action?spaceKey=CISDDoc&title=${title}&linkCreation=true&fromPageId=40633829");
    }

    /**
     * @return The URL for the help page specified by the request parameters.
     */
    private String getHelpPageAbsoluteURLForRequest(HttpServletRequest request)
    {
        // Use the page title to generate an absolute URL for the documentation page
        String pageTitle = tryGetHelpPageTitleForRequest(request);
        if (null == pageTitle)
            return getDocumentationRootURL();
        else
        {
            Template urlTemplate = getHelpPageWikiURLTemplate();
            urlTemplate.bind("title", pageTitle);
            return urlTemplate.createText();
        }
    }

    /**
     * Construct the portion of the URL that identifies a particular help page from the request. The
     * parameters GenericConstants.HELP_REDIRECT_DOMAIN_KEY and
     * GenericConstants.HELP_REDIRECT_ACTION_KEY are used to do this.
     */
    String tryGetHelpPageTitleForRequest(HttpServletRequest request)
    {
        final String helpPageDomain =
                request.getParameter(GenericConstants.HELP_REDIRECT_DOMAIN_KEY);
        final String helpPageAction =
                request.getParameter(GenericConstants.HELP_REDIRECT_ACTION_KEY);
        if (helpPageDomain == null)
            return null;
        StringBuffer sb = new StringBuffer();

        String helpTitleDomain = helpPageDomain.replace("_", "+");
        sb.append(helpTitleDomain);

        if (helpPageAction != null)
        {
            sb.append("+");
            sb.append(helpPageAction);
        }
        return sb.toString();
    }

}
