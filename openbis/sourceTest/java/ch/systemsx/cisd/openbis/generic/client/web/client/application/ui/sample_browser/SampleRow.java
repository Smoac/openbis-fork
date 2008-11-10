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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample_browser;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SampleModel;
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
        withCell(ModelDataPropertyNames.CODE, code);
    }

    public SampleRow identifier(String instanceCode)
    {
        return identifier(instanceCode, null);
    }

    public SampleRow identifier(String instanceCode, String groupCodeOrNull)
    {
        withCell(ModelDataPropertyNames.INSTANCE, instanceCode);
        if (groupCodeOrNull == null)
        {
            withCell(ModelDataPropertyNames.GROUP, "");
            withCell(ModelDataPropertyNames.IS_GROUP_SAMPLE, Boolean.FALSE);
        } else
        {
            withCell(ModelDataPropertyNames.GROUP, groupCodeOrNull);
            withCell(ModelDataPropertyNames.IS_GROUP_SAMPLE, Boolean.TRUE);
        }
        groupIdentifier = createGroupIdentifier(instanceCode, groupCodeOrNull);
        withCell(ModelDataPropertyNames.SAMPLE_IDENTIFIER, groupIdentifier + code);
        return this;
    }

    public SampleRow experiment(String projectCode, String experimentCode)
    {
        withCell(ModelDataPropertyNames.PROJECT, projectCode);
        withCell(ModelDataPropertyNames.EXPERIMENT, experimentCode);
        String experimentIdentifier = groupIdentifier + projectCode + "/" + experimentCode;
        withCell(ModelDataPropertyNames.EXPERIMENT_IDENTIFIER, experimentIdentifier);
        return this;
    }
    
    public SampleRow noExperiment()
    {
        withCell(ModelDataPropertyNames.PROJECT, null);
        withCell(ModelDataPropertyNames.EXPERIMENT, null);
        withCell(ModelDataPropertyNames.EXPERIMENT_IDENTIFIER, null);
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
        withCell(ModelDataPropertyNames.IS_INVALID, Boolean.TRUE);
        return this;
    }

    public SampleRow valid()
    {
        withCell(ModelDataPropertyNames.IS_INVALID, Boolean.FALSE);
        return this;
    }
    
    public SampleRow derivedFromAncestor(String ancestorCode, int level)
    {
        withCell(SampleModel.GENERATED_FROM_PARENT_PREFIX + level, ancestorCode);
        return this;
    }
    
    public SampleRow property(String propertyCode, Object value)
    {
        PropertyType propertyType = new PropertyType();
        propertyType.setInternalNamespace(true);
        propertyType.setSimpleCode(propertyCode);
        withCell(SampleModel.createID(propertyType), value);
        return this;
    }

}
