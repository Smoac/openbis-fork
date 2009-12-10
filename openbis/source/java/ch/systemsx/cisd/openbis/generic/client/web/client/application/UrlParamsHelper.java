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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import java.util.HashMap;
import java.util.Map;

import com.extjs.gxt.ui.client.widget.MessageBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.URLMethodWithParameters;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchOperationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;

/**
 * A class with helper methods for URL parameters handling and opening initial tab.
 * 
 * @author Piotr Buczek
 */
public final class UrlParamsHelper
{

    private static final String KEY_VALUE_SEPARATOR = "=";

    private static final String PARAMETER_SEPARATOR = "&";

    /**
     * Parses given URL <var>string</var> and returns the key-value pairs
     */
    private final Map<String, String> parseParamString(final String string)
    {
        assert string != null : "Given text can not be null.";
        final String[] params = string.split(PARAMETER_SEPARATOR);
        final Map<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < params.length; i++)
        {
            final String[] keyVal = params[i].split(KEY_VALUE_SEPARATOR);
            assert keyVal.length == 2 : "Only two items should be found here.";
            map.put(keyVal[0], keyVal[1]);
        }
        return map;
    }

    /**
     * The URL parameters.
     * <p>
     * Is never <code>null</code> but could be empty.
     * </p>
     */
    private Map<String, String> urlParams = new HashMap<String, String>();

    private IViewContext<?> viewContext;

    public UrlParamsHelper(IViewContext<?> viewContext)
    {
        this.viewContext = viewContext;
    }

    private final String tryGetUrlParamValue(String paramKey)
    {
        return urlParams.get(paramKey);
    }

    private final void setUrlParams(final Map<String, String> urlParams)
    {
        assert urlParams != null : "URL params can not be null.";
        this.urlParams = urlParams;
    }

    public final void initUrlParams()
    {
        final String paramString = GWTUtils.getParamString();
        if (StringUtils.isBlank(paramString) == false)
        {
            setUrlParams(parseParamString(paramString));
        }
    }

    public final IDelegatedAction getOpenInitialTabAction()
    {
        return new OpenInitialTabAction();
    }

    private class OpenInitialTabAction implements IDelegatedAction
    {

        public void execute()
        {
            openInitialTab();
        }

        /** opens an initial tab if a parameter is specified in URL */
        private void openInitialTab()
        {
            String entityKindValueOrNull =
                    tryGetUrlParamValue(PermlinkUtilities.ENTITY_KIND_PARAMETER_KEY);
            String permIdValueOrNull = tryGetUrlParamValue(PermlinkUtilities.PERM_ID_PARAMETER_KEY);
            try
            {
                if (entityKindValueOrNull != null || permIdValueOrNull != null)
                {
                    checkMissingURLParameter(entityKindValueOrNull,
                            PermlinkUtilities.ENTITY_KIND_PARAMETER_KEY);
                    checkMissingURLParameter(permIdValueOrNull,
                            PermlinkUtilities.PERM_ID_PARAMETER_KEY);
                    EntityKind entityKind = getEntityKind(entityKindValueOrNull);
                    openEntityDetailsTab(entityKind, permIdValueOrNull);
                }
            } catch (UserFailureException exception)
            {
                MessageBox.alert("Error", exception.getMessage(), null);
            }
        }

        private void checkMissingURLParameter(String valueOrNull, String parameter)
        {
            if (valueOrNull == null)
            {
                throw new UserFailureException("Missing URL parameter: " + parameter);
            }
        }

        private void openEntityDetailsTab(EntityKind entityKind, String permId)
        {
            viewContext.getCommonService().getEntityInformationHolder(entityKind, permId,
                    new OpenEntityDetailsTabCallback(viewContext));

        }

        private EntityKind getEntityKind(String entityKindValueOrNull)
        {
            try
            {
                return EntityKind.valueOf(entityKindValueOrNull);
            } catch (IllegalArgumentException exception)
            {
                throw new UserFailureException("Invalid '"
                        + PermlinkUtilities.ENTITY_KIND_PARAMETER_KEY + "' URL parameter value.");
            }
        }

    }

    private class OpenEntityDetailsTabCallback extends
            AbstractAsyncCallback<IEntityInformationHolder>
    {

        private OpenEntityDetailsTabCallback(final IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        //
        // AbstractAsyncCallback
        //

        /**
         * Opens the tab with <var>result</var> entity details.
         */
        @Override
        protected final void process(final IEntityInformationHolder result)
        {
            new OpenEntityDetailsTabAction(result, viewContext).execute();
        }
    }

    public static final String createTemplateURL(EntityKind kind, EntityType type,
            boolean withCodes, boolean withExperiments, BatchOperationKind operationKind)
    {
        URLMethodWithParameters methodWithParameters =
                new URLMethodWithParameters(GenericConstants.TEMPLATE_SERVLET_NAME);
        methodWithParameters.addParameter(GenericConstants.ENTITY_KIND_KEY_PARAMETER, kind.name());
        methodWithParameters.addParameter(GenericConstants.ENTITY_TYPE_KEY_PARAMETER, type
                .getCode());
        methodWithParameters.addParameter(GenericConstants.AUTO_GENERATE, withCodes);
        methodWithParameters.addParameter(GenericConstants.WITH_EXPERIMENTS, withExperiments);
        methodWithParameters.addParameter(GenericConstants.BATCH_OPERATION_KIND, operationKind
                .name());
        return methodWithParameters.toString();
    }

}
