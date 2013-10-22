/*
 * Copyright 2011 ETH Zuerich, CISD
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
package ch.systemsx.cisd.openbis.generic.server.business.search.sort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;

/**
 * This Class sorts the given entity list trying to match the code, properties values and typeCode in a generic manner assigning different scores.
 * 
 * @author pkupczyk
 * @author juanf
 */
public class SearchResultSorterByScore implements ISearchResultSorter
{

    @Override
    public void sort(List<? extends IEntitySearchResult> entitiesToSort, DetailedSearchCriteria criteria)
    {
        // 0. Pre check that the results can be sorted
        if (criteria == null || criteria.getCriteria() == null || criteria.getCriteria().isEmpty())
        {
            return;
        }

        // 1. Get terms to use as input for the main algorithm
        List<Pattern> partialMatchTerms = new ArrayList<Pattern>();
        List<String> exactMatchTerms = new ArrayList<String>();
        for (DetailedSearchCriterion criterion : criteria.getCriteria())
        {
            partialMatchTerms.add(getPartialMatchTerm(criterion.getValue()));
            exactMatchTerms.add(getExactMatchTerm(criterion.getValue()));
        }

        // 2. Main algorithm
        final Map<IEntitySearchResult, Integer> scores = new HashMap<IEntitySearchResult, Integer>();

        for (IEntitySearchResult entity : entitiesToSort)
        {
            int score = getScore(entity, partialMatchTerms, exactMatchTerms);
            scores.put(entity, score);
        }

        // 3. Sort
        Collections.sort(entitiesToSort, new Comparator<IEntitySearchResult>()
            {
                @Override
                public int compare(IEntitySearchResult o1, IEntitySearchResult o2)
                {
                    Integer score1 = scores.get(o1);
                    Integer score2 = scores.get(o2);
                    int result = -score1.compareTo(score2);
                    if (result == 0)
                    {
                        return o1.getCode().compareTo(o2.getCode());
                    } else
                    {
                        return result;
                    }
                }
            });
    }

    private int getScore(IEntitySearchResult entity, List<Pattern> partialMatchTerms, List<String> exactMatchTerms)
    {
        int score = 0;
        for (int i = 0; i < exactMatchTerms.size(); i++)
        {
            Pattern partialTerm = partialMatchTerms.get(i);
            String exactTerm = exactMatchTerms.get(i);

            // 1. Code
            if (isPartialMatch(entity.getCode(), partialTerm))
            { // If code matches partially
                score += 100000;
                if (isExactMatch(entity.getCode(), exactTerm))
                { // If code matches exactly
                    score += 1000000;
                }
            }

            // 2. Entity type code
            if (isExactMatch(entity.getTypeCode(), exactTerm))
            { // If type matches exactly
                score += 1000;
            }

            // 3. Properties
            if (entity.getProperties() != null && entity.getProperties().values() != null)
            {
                for (String propertyValue : entity.getProperties().values())
                {
                    if (isPartialMatch(propertyValue, partialTerm))
                    { // If property matches partially
                        score += 100;
                        if (isExactMatch(propertyValue, exactTerm))
                        { // If property matches exactly
                            score += 10000;
                        }
                    }
                }
            }
        }
        System.out.println(entity.getCode() + " " + score);
        return score;
    }

    public Pattern getPartialMatchTerm(String term)
    {
        return Pattern.compile(("*" + term + "*").replace("*", ".*").replace("?", ".?"), Pattern.CASE_INSENSITIVE);
    }

    public String getExactMatchTerm(String term)
    {
        return term.replace("*", "").replace("?", "");
    }

    public boolean isExactMatch(String value, String term)
    {
        if (value != null && term != null)
        {
            return value.equalsIgnoreCase(term);
        } else
        {
            return false;
        }
    }

    public boolean isPartialMatch(String value, Pattern pattern)
    {
        if (value != null && pattern != null)
        {
            return pattern.matcher(value).matches();
        } else
        {
            return false;
        }
    }
}
