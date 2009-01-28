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

import com.extjs.gxt.ui.client.data.BaseModelData;

import ch.systemsx.cisd.openbis.generic.client.shared.Vocabulary;

/**
 * A {@link BaseModelData} extension suitable for {@link Vocabulary}.
 * 
 * @author Christian Ribeaud
 */
public final class VocabularyModel extends BaseModelData
{
    public static final String NEW_VOCABULARY_CODE = "(New Vocabulary)";

    private static final long serialVersionUID = 1L;

    private VocabularyModel()
    {
    }

    public VocabularyModel(final Vocabulary vocabulary)
    {
        assert vocabulary != null : "Unspecified data type.";
        set(ModelDataPropertyNames.CODE, vocabulary.getCode());
        set(ModelDataPropertyNames.DESCRIPTION, vocabulary.getDescription());
        set(ModelDataPropertyNames.IS_MANAGED_INTERNALLY, vocabulary.isManagedInternally());
        set(ModelDataPropertyNames.REGISTRATOR, vocabulary.getRegistrator());
        set(ModelDataPropertyNames.REGISTRATION_DATE, vocabulary.getRegistrationDate());
        set(ModelDataPropertyNames.OBJECT, vocabulary);
    }

    public final static VocabularyModel createNewVocabularyVocabularyModel()
    {
        final VocabularyModel model = new VocabularyModel();
        model.set(ModelDataPropertyNames.CODE, NEW_VOCABULARY_CODE);
        model.set(ModelDataPropertyNames.OBJECT, null);
        return model;
    }

    public final static List<VocabularyModel> convert(final List<Vocabulary> vocabularies)
    {
        assert vocabularies != null : "Unspecified vocabularies.";
        final List<VocabularyModel> vocabularyModels =
                new ArrayList<VocabularyModel>(vocabularies.size());
        for (final Vocabulary vocabulary : vocabularies)
        {
            vocabularyModels.add(new VocabularyModel(vocabulary));
        }
        return vocabularyModels;
    }

}
