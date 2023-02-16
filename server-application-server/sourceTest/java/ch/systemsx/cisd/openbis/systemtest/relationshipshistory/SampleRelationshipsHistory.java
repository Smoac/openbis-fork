/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.systemtest.relationshipshistory;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;

/**
 * @author Pawel Glyzewski
 */
class SampleRelationshipsHistory extends AbstractRelationshipsHistory
{
    private Long mainSampId;

    private Long expeId;

    public Long getMainSampId()
    {
        return mainSampId;
    }

    public void setMainSampId(Long mainSampId)
    {
        this.mainSampId = mainSampId;
    }

    public Long getExpeId()
    {
        return expeId;
    }

    public void setExpeId(Long expeId)
    {
        this.expeId = expeId;
    }

    @Override
    protected String getConnectedEntityString()
    {
        String entity = super.getConnectedEntityString();
        if (false == StringUtils.isBlank(entity))
        {
            return entity;
        } else if (expeId == null)
        {
            return "";
        }

        return "expeId=" + expeId + "; entityPermId=" + getEntityPermId();
    }

    @Override
    protected String getMainEntityString()
    {
        return "mainSampId=" + mainSampId;
    }
}
