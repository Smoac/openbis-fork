/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.dataset;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.Relations;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.FileFormatType;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.dataset.FileFormatTypeFetchOptions;
import ch.systemsx.cisd.openbis.generic.shared.dto.FileFormatTypePE;

/**
 * @author pkupczyk
 */
public class FileFormatTypeTranslator extends AbstractCachingTranslator<FileFormatTypePE, FileFormatType, FileFormatTypeFetchOptions>
{
    public FileFormatTypeTranslator(TranslationContext translationContext, FileFormatTypeFetchOptions fetchOptions)
    {
        super(translationContext, fetchOptions);
    }

    @Override
    protected FileFormatType createObject(FileFormatTypePE type)
    {
        FileFormatType result = new FileFormatType();

        result.setCode(type.getCode());
        result.setDescription(type.getDescription());
        result.setFetchOptions(new FileFormatTypeFetchOptions());

        return result;
    }

    @Override
    protected void updateObject(FileFormatTypePE type, FileFormatType result, Relations relations)
    {
    }

}
