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

package ch.systemsx.cisd.openbis.generic.shared.translator;

import java.util.List;

import ch.systemsx.cisd.common.utilities.BeanUtils;
import ch.systemsx.cisd.common.utilities.BeanUtils.Converter;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.dto.SearchHit;

/**
 * Translator methods from {@link SearchHit} to {@link MatchingEntity}.
 * 
 * @author Bernd Rinn
 */
public class SearchHitTranslator
{
    /**
     * Translates a {@link SearchHit} into a {@link MatchingEntity}.
     */
    public static MatchingEntity translate(SearchHit entity)
    {
        final Converter converter = DtoConverters.getMatchingEntityConverter();
        return BeanUtils.createBean(MatchingEntity.class, entity, converter);
    }

    /**
     * Translates a list of {@link SearchHit}s into a list {@link MatchingEntity}s.
     */
    public static List<MatchingEntity> translate(List<SearchHit> entities)
    {
        final Converter converter = DtoConverters.getMatchingEntityConverter();
        return BeanUtils.createBeanList(MatchingEntity.class, entities, converter);
    }
    
}
