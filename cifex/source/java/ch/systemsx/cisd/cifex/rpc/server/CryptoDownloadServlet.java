/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.rpc.server;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.cifex.server.AbstractFileUploadDownloadServlet;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.string.Template;

/**
 * A class that creates the JNLP file for the Bouncy Caste Crypto extension. 
 *
 * @author Bernd Rinn
 */
public class CryptoDownloadServlet extends AbstractFileUploadDownloadServlet
{

    private static final long serialVersionUID = 1L;

    @Private
    static final Template CRYPTO_COMPONENT_JNLP_TEMPLATE =
            new Template("<?xml version='1.0' encoding='utf-8'?>\n" + 
            		"<jnlp spec='1.0+' codebase='${base-URL}'>\n" + 
            		"  <information>\n" + 
            		"    <title>Bouncy Castle Crypto Provider</title>\n" + 
            		"    <vendor>The Legion of the Bouncy Castle</vendor>\n" + 
            		"    <description>JCA crypto provider / OpenPGP implementation</description>\n" + 
            		"  </information>\n" + 
            		"  <security>\n" + 
            		"    <all-permissions/>\n" + 
            		"  </security>\n" + 
            		"  <resources>\n" + 
            		"    <j2se version='1.5+'/>\n" + 
            		"    <jar href='bcprov.jar'/>\n" + 
            		"    <jar href='bcpg.jar'/>\n" + 
            		"  </resources>\n" + 
            		"  <component-desc/>\n" + 
            		"</jnlp>\n");

    @Override
    // @Protected
    public void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException, InvalidSessionException
    {
        response.setContentType("application/x-java-jnlp-file");
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(response.getOutputStream()));
        Template template = CRYPTO_COMPONENT_JNLP_TEMPLATE.createFreshCopy();
        template.attemptToBind("base-URL", createBaseURL(request));
        writer.print(template.createText());
        writer.close();
    }

    @Override
    protected String getMainClassName()
    {
        // Doesn't apply
        return null;
    }

    @Override
    protected String getOperationName()
    {
        // Doesn't apply
        return null;
    }

    @Override
    protected String getTitle()
    {
        // Doesn't apply
        return null;
    }

}
