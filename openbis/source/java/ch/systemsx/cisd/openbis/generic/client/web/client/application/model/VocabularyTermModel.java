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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.model;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.BaseModel;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;

/**
 * @author Izabela Adamczyk
 */
public class VocabularyTermModel extends BaseModel
{

    private static final long serialVersionUID = 1L;

    public VocabularyTermModel(VocabularyTerm term)
    {
        this(term.getCode());
    }

    public VocabularyTermModel(String termCode)
    {
        this(termCode, termCode);
    }

    private VocabularyTermModel(String termCode, String object)
    {
        set(ModelDataPropertyNames.CODE, termCode);
        set(ModelDataPropertyNames.OBJECT, object);
    }

    public static final List<VocabularyTermModel> convert(List<VocabularyTerm> terms)
    {
        final ArrayList<VocabularyTermModel> list = new ArrayList<VocabularyTermModel>();
        for (VocabularyTerm t : terms)
        {
            list.add(new VocabularyTermModel(t));
        }
        return list;
    }

    public String getTerm()
    {
        final String code = get(ModelDataPropertyNames.OBJECT);
        return code;
    }

}