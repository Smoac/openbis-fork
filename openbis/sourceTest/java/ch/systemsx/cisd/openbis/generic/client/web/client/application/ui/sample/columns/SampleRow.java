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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.sample.CommonSampleColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.sample.ParentContainerSampleColDef;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.sample.ParentGeneratedFromSampleColDef;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.RowWithProperties;
import ch.systemsx.cisd.openbis.generic.shared.basic.SimpleYesNoRenderer;

/**
 * @author Franz-Josef Elmer
 */
public class SampleRow extends RowWithProperties
{

    private final String code;

    private String groupIdentifier;

    public SampleRow(final String code)
    {
        super();
        this.code = code;
        withCell(CommonSampleColDefKind.CODE, code);
    }

    public SampleRow(final String code, final String typeCode)
    {
        this(code);
        withCell(CommonSampleColDefKind.SAMPLE_TYPE.id(), typeCode);
    }

    public SampleRow identifier(final String instanceCode)
    {
        return identifier(instanceCode, null);
    }

    public SampleRow identifier(final String instanceCode, final String groupCodeOrNull)
    {
        withCell(CommonSampleColDefKind.DATABASE_INSTANCE, instanceCode);
        if (groupCodeOrNull == null)
        {
            withCell(CommonSampleColDefKind.GROUP, "");
        } else
        {
            withCell(CommonSampleColDefKind.GROUP, groupCodeOrNull);
        }
        groupIdentifier = createGroupIdentifier(instanceCode, groupCodeOrNull);
        withCell(CommonSampleColDefKind.SAMPLE_IDENTIFIER, groupIdentifier + code);
        return this;
    }

    public SampleRow experiment(final String groupCode, final String projectCode,
            final String experimentCode)
    {
        withCell(CommonSampleColDefKind.PROJECT, projectCode);
        withCell(CommonSampleColDefKind.EXPERIMENT, createLinkString(experimentCode));
        final String experimentIdentifier =
                "/" + groupCode + "/" + projectCode + "/" + experimentCode;
        withCell(CommonSampleColDefKind.EXPERIMENT_IDENTIFIER, experimentIdentifier);
        return this;
    }

    private static final String LINK_PREFIX = "<div><a href=\"#\">";

    private static final String LINK_SUFFIX = "</a></div>";

    private String createLinkString(String string)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(LINK_PREFIX);
        sb.append(string);
        sb.append(LINK_SUFFIX);
        return sb.toString();
    }

    public SampleRow noExperiment()
    {
        withCell(CommonSampleColDefKind.PROJECT, null);
        withCell(CommonSampleColDefKind.EXPERIMENT, null);
        withCell(CommonSampleColDefKind.EXPERIMENT_IDENTIFIER, null);
        return this;
    }

    private String createGroupIdentifier(final String instanceCode, final String groupCodeOrNull)
    {
        String identifier = instanceCode + ":/";
        if (groupCodeOrNull != null)
        {
            identifier += groupCodeOrNull + "/";
        }
        return identifier;
    }

    public SampleRow invalid()
    {
        withInvalidation(true);
        return this;
    }

    public SampleRow valid()
    {
        withInvalidation(false);
        return this;
    }

    private void withInvalidation(boolean isInvalid)
    {
        withCell(CommonSampleColDefKind.IS_INVALID, SimpleYesNoRenderer.render(isInvalid));
    }

    public SampleRow derivedFromAncestors(final String... ancestorCodes)
    {
        for (String ancestorCode : ancestorCodes)
        {
            derivedFromAncestor(ancestorCode);
        }
        return this;
    }

    private SampleRow derivedFromAncestor(final String ancestorCode)
    {
        final String identifier = new ParentGeneratedFromSampleColDef("dummy").getIdentifier();
        withCell(identifier, ancestorCode);
        return this;
    }

    public SampleRow partOfContainer(final String containerCode)
    {
        final String identifier = new ParentContainerSampleColDef("dummy").getIdentifier();
        withCell(identifier, containerCode);
        return this;
    }

    private final void withCell(final CommonSampleColDefKind columnKind, final String value)
    {
        withCell(columnKind.id(), value);
    }
}
