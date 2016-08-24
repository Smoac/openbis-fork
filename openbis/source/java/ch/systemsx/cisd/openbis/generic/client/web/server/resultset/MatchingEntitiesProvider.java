/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.MatchingEntitiesPanelColumnIDs.ENTITY_KIND;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.MatchingEntitiesPanelColumnIDs.ENTITY_TYPE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.MatchingEntitiesPanelColumnIDs.IDENTIFIER;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.MatchingEntitiesPanelColumnIDs.MATCH;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.MatchingEntitiesPanelColumnIDs.RANK;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.MatchingEntitiesPanelColumnIDs.REGISTRATOR;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.MatchingEntitiesPanelColumnIDs.SEARCH_DOMAIN_TYPE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.server.translator.SearchableEntityTranslator;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchDomain;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyMatch;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchDomainSearchResultWithFullEntity;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Span;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;

/**
 * @author Franz-Josef Elmer
 */
public class MatchingEntitiesProvider implements ITableModelProvider<MatchingEntity>
{
    public static final String START_HIGHLIGHT = "|||startHighlight|||";

    public static final String END_HIGHLIGHT = "|||endHighlight|||";

    private final ICommonServer commonServer;

    private final String sessionToken;

    private final SearchableEntity[] matchingEntities;
    
    private final SearchDomain[] matchingSearchDomains;
    
    private String queryText;

    private final boolean useWildcardSearchMode;

    public MatchingEntitiesProvider(ICommonServer commonServer, String sessionToken,
            SearchableEntity[] matchingEntities, SearchDomain[] matchingSearchDomains, String queryText, boolean useWildcardSearchMode)
    {
        this.commonServer = commonServer;
        this.sessionToken = sessionToken;

        this.matchingEntities = matchingEntities;
        this.matchingSearchDomains = matchingSearchDomains;
        
        this.queryText = queryText;
        this.useWildcardSearchMode = useWildcardSearchMode;
    }
    
    @Override
    public TypedTableModel<MatchingEntity> getTableModel(int maxSize)
    {
    	List<MatchingEntity> entities =
                commonServer.listMatchingEntities(sessionToken, matchingEntities, queryText,
                        useWildcardSearchMode, Integer.MAX_VALUE);

        for (SearchDomain searchDomain : matchingSearchDomains)
        {
            String preferredSearchDomainOrNull = searchDomain.getName();
            queryText = SearchableEntityTranslator.addStarsToBeginningAndEnd(queryText);
            queryText = SearchableEntityTranslator.removeDuplicateStars(queryText);
            List<SearchDomainSearchResultWithFullEntity> searchDomainSearchResults =
                    commonServer.searchOnSearchDomain(sessionToken, preferredSearchDomainOrNull, queryText, null);
            List<MatchingEntity> matchingSearchDomainsTranslatedToEntities =
                    SearchableEntityTranslator.translateSearchDomains(searchDomainSearchResults, queryText);
            entities.addAll(matchingSearchDomainsTranslatedToEntities);
        }

        TypedTableModelBuilder<MatchingEntity> builder =
                new TypedTableModelBuilder<MatchingEntity>();
        builder.addColumn(ENTITY_KIND); 
        builder.addColumn(ENTITY_TYPE);
        builder.addColumn(SEARCH_DOMAIN_TYPE);
        builder.addColumn(IDENTIFIER).withDefaultWidth(140);
        builder.addColumn(REGISTRATOR);
        builder.addColumn(MATCH).withDefaultWidth(200).withDataType(DataTypeCode.MULTILINE_VARCHAR);
        builder.addColumn(RANK);
        long rank = 1;
        for (MatchingEntity matchingEntity : entities)
        {
            builder.addRow(matchingEntity);
            builder.column(ENTITY_KIND).addString(matchingEntity.getEntityKind().getDescription());
            builder.column(ENTITY_TYPE).addString(matchingEntity.getEntityType().getCode());
            builder.column(SEARCH_DOMAIN_TYPE).addString(matchingEntity.getSearchDomain());
            builder.column(IDENTIFIER).addString(matchingEntity.getIdentifier());
            builder.column(REGISTRATOR).addPerson(matchingEntity.getRegistrator());            
            builder.column(MATCH).addMatch(addHighLights(matchingEntity));
            builder.column(RANK).addInteger(rank);
            rank++;
        }
        return builder.getModel();
    }

    private class Block
    {
        private boolean highlighted;

        private int length;

        public Block(boolean highlighted, int length)
        {
            this.highlighted = highlighted;
            this.length = length;

        }

        public boolean isHighlighted()
        {
            return highlighted;
        }

        public int getLength()
        {
            return length;
        }
    }

    private MatchingEntity addHighLights(MatchingEntity entity)
    {
        for (PropertyMatch match : entity.getMatches())
        {
            String rawValue = match.getValue();
            String highlighted = "";
            for (Block block : getBlocks(match.getSpans()))
            {
                if (rawValue.length() == 0)
                {
                    break;
                }

                if (block.isHighlighted())
                {
                    highlighted += START_HIGHLIGHT;
                }

                highlighted += rawValue.substring(0, Math.min(block.getLength(), rawValue.length()));

                if (block.isHighlighted())
                {
                    highlighted += END_HIGHLIGHT;
                }

                rawValue = rawValue.substring(Math.min(block.getLength(), rawValue.length()));
            }
            match.setValue(highlighted);
        }
        return entity;
    }

    private List<Block> getBlocks(List<Span> input)
    {
        List<Span> spans = new ArrayList<Span>(input);
        Collections.sort(spans, new Comparator<Span>()
            {
                @Override
                public int compare(Span s1, Span s2)
                {
                    return new Integer(s1.getStart()).compareTo(new Integer(s2.getStart()));
                }
            });

        List<Span> result = new ArrayList<Span>();

        for (Span span : spans)
        {
            if (result.isEmpty())
            {
                result.add(span);
            } else
            {
                Span last = result.get(result.size() - 1);
                if (overlap(last, span))
                {
                    if (span.getEnd() > last.getEnd())
                    {
                        last.setEnd(span.getEnd());
                    } // else last shadows span completely
                } else
                {
                    result.add(span);
                }
            }
        }

        List<Block> blocks = new ArrayList<Block>();
        int lastEnd = 0;
        for (Span span : result)
        {
            if (span.getStart() - lastEnd > 0)
            {
                blocks.add(new Block(false, span.getStart() - lastEnd));
            }
            blocks.add(new Block(true, span.getEnd() - span.getStart()));
            lastEnd = span.getEnd();
        }
        blocks.add(new Block(false, Integer.MAX_VALUE));
        return blocks;
    }

    private boolean overlap(Span s1, Span s2)
    {
        return s1.getEnd() >= s2.getStart();
    }

}
