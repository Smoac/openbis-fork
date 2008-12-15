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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns.CommonSampleColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns.ParentGeneratedFromSampleColDef;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns.PropertySampleColDef;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.Row;

/**
 * @author Franz-Josef Elmer
 */
public class SampleRow extends Row
{

    private final String code;

    private String groupIdentifier;

    public SampleRow(String code)
    {
        super();
        this.code = code;
        withCell(CommonSampleColDefKind.CODE, code);
    }

    public SampleRow identifier(String instanceCode)
    {
        return identifier(instanceCode, null);
    }

    public SampleRow identifier(String instanceCode, String groupCodeOrNull)
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

    public SampleRow experiment(String projectCode, String experimentCode)
    {
        withCell(CommonSampleColDefKind.PROJECT_FOR_SAMPLE, projectCode);
        withCell(CommonSampleColDefKind.EXPERIMENT_FOR_SAMPLE, experimentCode);
        String experimentIdentifier = groupIdentifier + projectCode + "/" + experimentCode;
        withCell(CommonSampleColDefKind.EXPERIMENT_IDENTIFIER_FOR_SAMPLE, experimentIdentifier);
        return this;
    }

    public SampleRow noExperiment()
    {
        withCell(CommonSampleColDefKind.PROJECT_FOR_SAMPLE, null);
        withCell(CommonSampleColDefKind.EXPERIMENT_FOR_SAMPLE, null);
        withCell(CommonSampleColDefKind.EXPERIMENT_IDENTIFIER_FOR_SAMPLE, null);
        return this;
    }

    private String createGroupIdentifier(String instanceCode, String groupCodeOrNull)
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
        withCell(CommonSampleColDefKind.IS_INVALID, "yes");
        // overwrite previous code
        withCell(CommonSampleColDefKind.CODE, invalidCode(code));
        return this;
    }

    public SampleRow valid()
    {
        withCell(CommonSampleColDefKind.IS_INVALID, "no");
        // just to be sure - overwrite previous code
        withCell(CommonSampleColDefKind.CODE, code);
        return this;
    }

    public static String invalidCode(String code)
    {
        return "<div class=\"invalid\">" + code + "</div>";
    }

    public SampleRow derivedFromAncestor(String ancestorCode, int level)
    {
        String identifier = new ParentGeneratedFromSampleColDef(level, "dummy").getIdentifier();
        withCell(identifier, ancestorCode);
        return this;
    }

    public SampleRow property(String propertyCode, Object value)
    {
        PropertyType propertyType = new PropertyType();
        propertyType.setInternalNamespace(true);
        propertyType.setSimpleCode(propertyCode);
        String identifier = new PropertySampleColDef(propertyType, true).getIdentifier();
        withCell(identifier, value);
        return this;
    }

    private void withCell(CommonSampleColDefKind columnKind, String value)
    {
        withCell(columnKind.id(), value);
    }
}
