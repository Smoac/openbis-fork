/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.search;

import junit.framework.Assert;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.ExperimentSearchCriterion;
import ch.systemsx.cisd.openbis.generic.server.api.v1.SearchCriteriaToDetailedSearchCriteriaTranslator;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.CompareMode;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseTimeAttribute;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.SearchOperator;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchableEntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;

/**
 * @author pkupczyk
 */
public class ExperimentSearchCriterionTranslatorTest extends AbstractSearchCriterionTranslatorTest
{

    @Test
    public void testWithOrOperator()
    {
        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createPropertyMatch("PROPERTY", "testValue"));
        criteria.addMatchClause(MatchClause.createPropertyMatch("PROPERTY_2", "testValue_2"));
        criteria.setOperator(SearchOperator.MATCH_ANY_CLAUSES);

        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withOrOperator();
        criterion.withStringProperty("PROPERTY").thatEquals("testValue");
        criterion.withStringProperty("PROPERTY_2").thatEquals("testValue_2");

        translateAndAssertEqual(criteria, criterion);
    }

    @Test
    public void testWithAndOperator()
    {
        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createPropertyMatch("PROPERTY", "testValue"));
        criteria.addMatchClause(MatchClause.createPropertyMatch("PROPERTY_2", "testValue_2"));
        criteria.setOperator(SearchOperator.MATCH_ALL_CLAUSES);

        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withAndOperator();
        criterion.withStringProperty("PROPERTY").thatEquals("testValue");
        criterion.withStringProperty("PROPERTY_2").thatEquals("testValue_2");

        translateAndAssertEqual(criteria, criterion);
    }

    @Test
    public void testWithCode()
    {
        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "testCode"));

        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withCode().thatEquals("testCode");

        translateAndAssertEqual(criteria, criterion);
    }

    @Test
    public void testWithPermId()
    {
        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.PERM_ID, "testPermId"));

        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withPermId().thatEquals("testPermId");

        translateAndAssertEqual(criteria, criterion);
    }

    @Test
    public void testWithType()
    {
        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE, "testType"));

        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withType().withCode().thatEquals("testType");

        translateAndAssertEqual(criteria, criterion);
    }

    @Test
    public void testWithProject()
    {
        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.PROJECT, "testProject"));

        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withProject().withCode().thatEquals("testProject");

        translateAndAssertEqual(criteria, criterion);
    }

    @Test
    public void testWithSpace()
    {
        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.SPACE, "testSpace"));

        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withProject().withSpace().withCode().thatEquals("testSpace");

        translateAndAssertEqual(criteria, criterion);
    }

    @Test
    public void testWithTag()
    {
        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.METAPROJECT, "testTag"));

        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withTag().withCode().thatEquals("testTag");

        translateAndAssertEqual(criteria, criterion);
    }

    @Test
    public void testWithRegistrationDate()
    {
        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createTimeAttributeMatch(MatchClauseTimeAttribute.REGISTRATION_DATE, CompareMode.EQUALS, "2014-04-07",
                "0"));

        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withRegistrationDate().withShortFormat().thatEquals("2014-04-07");

        translateAndAssertEqual(criteria, criterion);
    }

    @Test
    public void testWithModificationDate()
    {
        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createTimeAttributeMatch(MatchClauseTimeAttribute.MODIFICATION_DATE, CompareMode.EQUALS, "2014-04-07",
                "0"));

        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withModificationDate().withShortFormat().thatEquals("2014-04-07");

        translateAndAssertEqual(criteria, criterion);
    }

    @Test
    public void testWithProperty()
    {
        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createPropertyMatch("PROPERTY", "testValue"));

        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withStringProperty("PROPERTY").thatEquals("testValue");

        translateAndAssertEqual(criteria, criterion);
    }

    @Test
    public void testWithAnyProperty()
    {
        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createAnyPropertyMatch("testValue"));

        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withAnyProperty().thatEquals("testValue");

        translateAndAssertEqual(criteria, criterion);
    }

    @Test
    public void testWithAnyField()
    {
        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createAnyFieldMatch("testValue"));

        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withAnyField().thatEquals("testValue");

        translateAndAssertEqual(criteria, criterion);
    }

    private void translateAndAssertEqual(SearchCriteria expected, ExperimentSearchCriterion actual)
    {
        DetailedSearchCriteria detailedExpected = translate(expected);
        DetailedSearchCriteria detailedActual = translate(actual);
        Assert.assertEquals(detailedExpected.toString(), detailedActual.toString());
    }

    private DetailedSearchCriteria translate(ExperimentSearchCriterion criterion)
    {
        ExperimentSearchCriterionTranslator translator = new ExperimentSearchCriterionTranslator(daoFactory, new EntityAttributeProviderFactory());
        DetailedSearchCriteria result = translator.translate(new SearchTranslationContext(null), criterion).getCriteria();
        System.out.println("From NEW:\n" + result);
        return result;
    }

    private DetailedSearchCriteria translate(SearchCriteria criteria)
    {
        DetailedSearchCriteria result =
                SearchCriteriaToDetailedSearchCriteriaTranslator.convert(daoFactory, SearchableEntityKind.EXPERIMENT, criteria);
        System.out.println("From OLD:\n" + result);
        return result;
    }

}
