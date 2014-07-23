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

import java.util.LinkedList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;

/**
 * @author pkupczyk
 */
public class SearchCriterionTranslatorFactory extends AbstractSearchCriterionTranslatorFactory
{

    private IDAOFactory daoFactory;

    private IEntityAttributeProviderFactory entityAttributeProviderFactory;

    public SearchCriterionTranslatorFactory(IDAOFactory daoFactory, IEntityAttributeProviderFactory entityAttributeProviderFactory)
    {
        this.daoFactory = daoFactory;
        this.entityAttributeProviderFactory = entityAttributeProviderFactory;
    }

    @Override
    protected List<ISearchCriterionTranslator> createTranslators()
    {
        List<ISearchCriterionTranslator> translators = new LinkedList<ISearchCriterionTranslator>();
        translators.add(new StringFieldSearchCriterionTranslator(getDaoFactory(), getEntityAttributeProviderFactory()));
        translators.add(new DateFieldSearchCriterionTranslator(getDaoFactory(), getEntityAttributeProviderFactory()));
        translators.add(new NumberFieldSearchCriterionTranslator(getDaoFactory(), getEntityAttributeProviderFactory()));
        translators.add(new EntityTypeSearchCriterionTranslator(getDaoFactory(), getEntityAttributeProviderFactory()));
        translators.add(new TagSearchCriterionTranslator(getDaoFactory(), getEntityAttributeProviderFactory()));
        translators.add(new SpaceSearchCriterionTranslator(getDaoFactory(), getEntityAttributeProviderFactory()));
        translators.add(new ProjectSearchCriterionTranslator(getDaoFactory(), getEntityAttributeProviderFactory()));
        translators.add(new ExperimentSearchCriterionTranslator(getDaoFactory(), getEntityAttributeProviderFactory()));
        translators.add(new SampleSearchCriterionTranslator(getDaoFactory(), getEntityAttributeProviderFactory()));
        return translators;
    }

    public IDAOFactory getDaoFactory()
    {
        return daoFactory;
    }

    public IEntityAttributeProviderFactory getEntityAttributeProviderFactory()
    {
        return entityAttributeProviderFactory;
    }

}
