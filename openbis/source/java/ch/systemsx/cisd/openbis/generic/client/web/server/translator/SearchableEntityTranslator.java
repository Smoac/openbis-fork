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

package ch.systemsx.cisd.openbis.generic.client.web.server.translator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.common.collection.SimpleComparator;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFileBlastSearchResultLocation;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFileSearchResultLocation;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityPropertyBlastSearchResultLocation;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ISearchDomainResultLocation;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyMatch;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchDomainSearchResultWithFullEntity;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Span;
import ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity;

/**
 * A {@link SearchableEntity} &lt;---&gt; {@link ch.systemsx.cisd.openbis.generic.client.web.client.dto.SearchableEntity} translator.
 * 
 * @author Christian Ribeaud
 */
public final class SearchableEntityTranslator
{

    private SearchableEntityTranslator()
    {
        // Can not be instantiated.
    }

    // if null all possible entities are returned
    public final static SearchableEntity[] translate(
            final ch.systemsx.cisd.openbis.generic.client.web.client.dto.SearchableEntity searchableEntityOrNull)
    {
        SearchableEntity[] matchingEntities = {};
        if (searchableEntityOrNull == null)
        {
            matchingEntities = SearchableEntity.values();
        } else
        {
            if (searchableEntityOrNull.getType() == ch.systemsx.cisd.openbis.generic.client.web.client.dto.Type.ENTITY)
            {
                matchingEntities = new SearchableEntity[] { SearchableEntity.valueOf(searchableEntityOrNull.getName()) };
            }
        }
        return matchingEntities;
    }

    public final static List<MatchingEntity> translateSearchDomains(List<SearchDomainSearchResultWithFullEntity> searchDomains, String searchString)
    {
        List<MatchingEntity> translatedEntities = new ArrayList<MatchingEntity>();
        for (SearchDomainSearchResultWithFullEntity searchDomain : searchDomains)
        {
            MatchingEntity matchingEntity = new MatchingEntity();
            IEntityInformationHolderWithPermId entity = searchDomain.getEntity();
            matchingEntity.setCode(entity.getCode());
            matchingEntity.setEntityKind(entity.getEntityKind());
            matchingEntity.setEntityType(entity.getEntityType());
            matchingEntity.setSearchDomain(searchDomain.getSearchResult().getSearchDomain().getLabel());

            if (entity instanceof IIdentifierHolder)
            {
                matchingEntity.setIdentifier(((IIdentifierHolder) entity).getIdentifier());
            }
            if (searchDomain.getSearchResult().getScore() != null)
            {
                matchingEntity.setScore(searchDomain.getSearchResult().getScore().getScore());
            }

            ISearchDomainResultLocation resultLocation = searchDomain.getSearchResult().getResultLocation();
            List<PropertyMatch> matches = new ArrayList<PropertyMatch>();
            if (resultLocation instanceof DataSetFileBlastSearchResultLocation)
            {
                PropertyMatch match = new PropertyMatch();
                DataSetFileBlastSearchResultLocation fileBlastLocation = (DataSetFileBlastSearchResultLocation) resultLocation;
                match.setCode("File '" + fileBlastLocation.getPathInDataSet() + "'");
                match.setValue(fileBlastLocation.getAlignmentMatch().toString());
                matches.add(match);
            } else if (resultLocation instanceof EntityPropertyBlastSearchResultLocation)
            {
                PropertyMatch match = new PropertyMatch();
                EntityPropertyBlastSearchResultLocation entityBlastLocation = (EntityPropertyBlastSearchResultLocation) resultLocation;
                match.setCode("Property '" + entityBlastLocation.getPropertyType() + "'");
                match.setValue(entityBlastLocation.getAlignmentMatch().toString());
                matches.add(match);
            } else if (resultLocation instanceof DataSetFileSearchResultLocation)
            {
                PropertyMatch match = new PropertyMatch();
                match.setValue(((DataSetFileSearchResultLocation) resultLocation).getPathInDataSet());
                String pathString = ((DataSetFileSearchResultLocation) resultLocation).getPathInDataSet();
                List<Span> spans = createSpanList(searchString, pathString);
                match.setSpans(spans);
                matches.add(match);
            } else
            {
                PropertyMatch match = new PropertyMatch();
                match.setValue(((DataSetFileSearchResultLocation) resultLocation).toString());
                matches.add(match);
            }
            matchingEntity.setMatches(matches);
            translatedEntities.add(matchingEntity);
        }
        Collections.sort(translatedEntities, new SimpleComparator<MatchingEntity, Double>()
            {
                @Override
                public Double evaluate(MatchingEntity item)
                {
                    return -item.getScore();
                }
            });
        return translatedEntities;
    }

    public static List<Span> createSpanList(String searchString, String filePath)
    {
        searchString = searchString.toLowerCase();
        filePath = filePath.toLowerCase();
        List<Span> spans = new ArrayList<Span>();
        if (searchString.indexOf("*") == 0)
        {
            searchString = searchString.substring(1);
        }
        if (searchString.lastIndexOf("*") == searchString.length() - 1)
        {
            searchString = searchString.substring(0, searchString.length() - 1);
        }
        String[] fragments = { searchString };
        // if searchString still contains the asterisk, then split it up
        if (searchString.indexOf("*") != -1)
        {
            fragments = searchString.split("\\*");
        }

        int start = 0;
        int end = 0;
        int offset = 0;
        int i = 0;
        String fragment = fragments[i];
        while (filePath.length() > 0 && i < fragments.length)
        {

            int exists = filePath.indexOf(fragment);
            if (exists != -1)
            {
                start = filePath.indexOf(fragment) + offset;
                end = start + fragment.length();
                Span span = new Span();
                span.setStart(start);
                span.setEnd(end);
                spans.add(span);
                offset = offset + fragment.length() + (filePath.substring(0, filePath.indexOf(fragment))).length();
                filePath = filePath.substring(filePath.indexOf(fragment) + 1, filePath.length());

                if (filePath.length() > 0 && (i + 1) < fragments.length)
                {
                    i++;
                    fragment = fragments[i];
                } else
                {
                    break; // don't break here in order to handle the "repeating character" case ex. search string "e" highlights 4x "eeee.txt"
                }

            } else
            {
                Span span = new Span();
                span.setStart(start);
                span.setEnd(end);
                spans.add(span);
                break;
            }
        }
        return spans;
    }

    // trim stars
    public static String trimStars(String text)
    {
        if (text == null)
            return null;
        if (!text.equals("") && text.substring(0, 1).equals("*"))
        {
            text = text.substring(1, text.length());
        }
        if (!text.equals("") && text.substring(text.length() - 1, text.length()).equals("*"))
        {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }

    // add stars to beginning and end
    public static String addStarsToBeginningAndEnd(String text)
    {
        if (text == null)
            return null;
        if (!text.equals("") && !text.substring(0, 1).equals("*"))
        {
            text = "*" + text;
        }
        if (!text.equals("") && !text.substring(text.length() - 1, text.length()).equals("*"))
        {
            text = text + "*";
        }
        return text;
    }

    public static String removeDuplicateStars(String text)
    {
        boolean existsDuplicate = (text.equals("") || text.indexOf("**") != -1);
        while (existsDuplicate)
        {
            text = text.replace("**", "*");
            existsDuplicate = (text.equals("") || text.indexOf("**") != -1);
        }
        return text;
    }

}
