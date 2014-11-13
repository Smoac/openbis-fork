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
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.ProjectSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.SampleSearchCriterion;
import ch.systemsx.cisd.openbis.generic.server.api.v1.SearchCriteriaToDetailedSearchCriteriaTranslator;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.CompareMode;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseTimeAttribute;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.SearchOperator;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchSubCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchableEntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;

/**
 * @author pkupczyk
 */
public class SampleSearchCriterionTranslatorTest extends AbstractSearchCriterionTranslatorTest
{

    @Test
    public void testWithOrOperator()
    {
        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createPropertyMatch("PROPERTY", "testValue"));
        criteria.addMatchClause(MatchClause.createPropertyMatch("PROPERTY_2", "testValue_2"));
        criteria.setOperator(SearchOperator.MATCH_ANY_CLAUSES);

        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withOrOperator();
        criterion.withProperty("PROPERTY").thatEquals("testValue");
        criterion.withProperty("PROPERTY_2").thatEquals("testValue_2");

        translateAndAssertEqual(criteria, criterion);
    }

    @Test
    public void testWithAndOperator()
    {
        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createPropertyMatch("PROPERTY", "testValue"));
        criteria.addMatchClause(MatchClause.createPropertyMatch("PROPERTY_2", "testValue_2"));
        criteria.setOperator(SearchOperator.MATCH_ALL_CLAUSES);

        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withAndOperator();
        criterion.withProperty("PROPERTY").thatEquals("testValue");
        criterion.withProperty("PROPERTY_2").thatEquals("testValue_2");

        translateAndAssertEqual(criteria, criterion);
    }

    @Test
    public void testWithCode()
    {
        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "testCode"));

        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withCode().thatEquals("testCode");

        translateAndAssertEqual(criteria, criterion);
    }

    @Test
    public void testWithPermId()
    {
        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.PERM_ID, "testPermId"));

        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withPermId().thatEquals("testPermId");

        translateAndAssertEqual(criteria, criterion);
    }

    @Test
    public void testWithType()
    {
        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE, "testType"));

        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withType().withCode().thatEquals("testType");

        translateAndAssertEqual(criteria, criterion);
    }

    @Test
    public void testWithSpace()
    {
        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.SPACE, "testSpace"));

        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withSpace().withCode().thatEquals("testSpace");

        translateAndAssertEqual(criteria, criterion);
    }

    @Test
    public void testWithTag()
    {
        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.METAPROJECT, "testTag"));

        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withTag().withCode().thatEquals("testTag");

        translateAndAssertEqual(criteria, criterion);
    }

    @Test
    public void testWithParents()
    {
        SearchCriteria subCriteria = new SearchCriteria();
        subCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "parentCode"));

        SearchCriteria criteria = new SearchCriteria();
        criteria.addSubCriteria(SearchSubCriteria.createSampleParentCriteria(subCriteria));

        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withParents().withCode().thatEquals("parentCode");

        translateAndAssertEqual(criteria, criterion);
    }

    @Test
    public void testWithParentsWithParents()
    {
        SearchCriteria subSubCriteria = new SearchCriteria();
        subSubCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "parentCode"));

        SearchCriteria subCriteria = new SearchCriteria();
        subCriteria.addSubCriteria(SearchSubCriteria.createSampleParentCriteria(subSubCriteria));

        SearchCriteria criteria = new SearchCriteria();
        criteria.addSubCriteria(SearchSubCriteria.createSampleParentCriteria(subCriteria));

        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withParents().withParents().withCode().thatEquals("parentCode");

        translateAndAssertEqual(criteria, criterion);
    }

    @Test
    public void testWithChildren()
    {
        SearchCriteria subCriteria = new SearchCriteria();
        subCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "childCode"));

        SearchCriteria criteria = new SearchCriteria();
        criteria.addSubCriteria(SearchSubCriteria.createSampleChildCriteria(subCriteria));

        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withChildren().withCode().thatEquals("childCode");

        translateAndAssertEqual(criteria, criterion);
    }

    @Test
    public void testWithContainer()
    {
        SearchCriteria subCriteria = new SearchCriteria();
        subCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "containerCode"));

        SearchCriteria criteria = new SearchCriteria();
        criteria.addSubCriteria(SearchSubCriteria.createSampleContainerCriteria(subCriteria));

        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withContainer().withCode().thatEquals("containerCode");

        translateAndAssertEqual(criteria, criterion);
    }

    @Test
    public void testWithRegistrationDate()
    {
        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createTimeAttributeMatch(MatchClauseTimeAttribute.REGISTRATION_DATE, CompareMode.EQUALS, "2014-04-07",
                "0"));

        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withRegistrationDate().thatEquals("2014-04-07");

        translateAndAssertEqual(criteria, criterion);
    }

    @Test
    public void testWithModificationDate()
    {
        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createTimeAttributeMatch(MatchClauseTimeAttribute.MODIFICATION_DATE, CompareMode.EQUALS, "2014-04-07",
                "0"));

        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withModificationDate().thatEquals("2014-04-07");

        translateAndAssertEqual(criteria, criterion);
    }

    @Test
    public void testWithProperty()
    {
        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createPropertyMatch("PROPERTY", "testValue"));

        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withProperty("PROPERTY").thatEquals("testValue");

        translateAndAssertEqual(criteria, criterion);
    }

    @Test
    public void testWithAnyProperty()
    {
        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createAnyPropertyMatch("testValue"));

        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withAnyProperty().thatEquals("testValue");

        translateAndAssertEqual(criteria, criterion);
    }

    @Test
    public void testWithAnyField()
    {
        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createAnyFieldMatch("testValue"));

        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withAnyField().thatEquals("testValue");

        translateAndAssertEqual(criteria, criterion);
    }

    @Test
    public void testWithMany()
    {
        SearchCriteria experimentCriteria = new SearchCriteria();
        experimentCriteria.setOperator(SearchOperator.MATCH_ALL_CLAUSES);
        experimentCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "experimentCode"));
        experimentCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.PROJECT, "projectCode"));
        experimentCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.SPACE, "spaceCode"));

        SearchCriteria sampleCriteria = new SearchCriteria();
        sampleCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE, "sampleType"));
        sampleCriteria.addMatchClause(MatchClause.createPropertyMatch("PROPERTY", "sampleValue"));
        sampleCriteria.addSubCriteria(SearchSubCriteria.createExperimentCriteria(experimentCriteria));
        sampleCriteria.setOperator(SearchOperator.MATCH_ALL_CLAUSES);

        SampleSearchCriterion sampleCriterion = new SampleSearchCriterion();
        sampleCriterion.withAndOperator();
        sampleCriterion.withType().withCode().thatEquals("sampleType");
        sampleCriterion.withProperty("PROPERTY").thatEquals("sampleValue");

        ExperimentSearchCriterion experimentCriterion = sampleCriterion.withExperiment();
        experimentCriterion.withCode().thatEquals("experimentCode");

        ProjectSearchCriterion projectCriterion = experimentCriterion.withProject();
        projectCriterion.withCode().thatEquals("projectCode");
        projectCriterion.withSpace().withCode().thatEquals("spaceCode");

        translateAndAssertEqual(sampleCriteria, sampleCriterion);
    }

    private void translateAndAssertEqual(SearchCriteria expected, SampleSearchCriterion actual)
    {
        DetailedSearchCriteria detailedExpected = translate(expected);
        DetailedSearchCriteria detailedActual = translate(actual);
        Assert.assertEquals(detailedExpected.toString(), detailedActual.toString());
    }

    private DetailedSearchCriteria translate(SampleSearchCriterion criterion)
    {
        SampleSearchCriterionTranslator translator = new SampleSearchCriterionTranslator(daoFactory, new EntityAttributeProviderFactory());
        DetailedSearchCriteria result = translator.translate(new SearchTranslationContext(null), criterion).getCriteria();
        System.out.println("From NEW:\n" + result);
        return result;
    }

    private DetailedSearchCriteria translate(SearchCriteria criteria)
    {
        DetailedSearchCriteria result = SearchCriteriaToDetailedSearchCriteriaTranslator.convert(daoFactory, SearchableEntityKind.SAMPLE, criteria);
        System.out.println("From OLD:\n" + result);
        return result;
    }

}
