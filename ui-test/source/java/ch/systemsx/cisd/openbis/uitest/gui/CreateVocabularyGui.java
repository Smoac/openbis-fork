/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.gui;

import ch.systemsx.cisd.openbis.uitest.dsl.Executor;
import ch.systemsx.cisd.openbis.uitest.layout.AddVocabularyLocation;
import ch.systemsx.cisd.openbis.uitest.page.AddVocabularyDialog;
import ch.systemsx.cisd.openbis.uitest.request.CreateVocabulary;
import ch.systemsx.cisd.openbis.uitest.type.Vocabulary;

/**
 * @author anttil
 */
public class CreateVocabularyGui extends Executor<CreateVocabulary, Vocabulary>
{

    @Override
    public Vocabulary run(CreateVocabulary request)
    {
        Vocabulary vocabulary = request.getVocabulary();
        AddVocabularyDialog dialog = goTo(new AddVocabularyLocation());
        dialog.fillWith(vocabulary);
        dialog.save();
        return vocabulary;
    }

}
