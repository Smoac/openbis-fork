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
public class SearchCriteriaTranslatorFactory extends AbstractSearchCriteriaTranslatorFactory
{

    private IDAOFactory daoFactory;

    private IEntityAttributeProviderFactory entityAttributeProviderFactory;

    public SearchCriteriaTranslatorFactory(IDAOFactory daoFactory, IEntityAttributeProviderFactory entityAttributeProviderFactory)
    {
        this.daoFactory = daoFactory;
        this.entityAttributeProviderFactory = entityAttributeProviderFactory;
    }

    @Override
    protected List<ISearchCriteriaTranslator> createTranslators()
    {
        List<ISearchCriteriaTranslator> translators = new LinkedList<ISearchCriteriaTranslator>();
        translators.add(new StringFieldSearchCriteriaTranslator(getDaoFactory(), getEntityAttributeProviderFactory()));
        translators.add(new DateFieldSearchCriteriaTranslator(getDaoFactory(), getEntityAttributeProviderFactory()));
        translators.add(new NumberFieldSearchCriteriaTranslator(getDaoFactory(), getEntityAttributeProviderFactory()));
        translators.add(new EntityTypeSearchCriteriaTranslator(getDaoFactory(), getEntityAttributeProviderFactory()));
        translators.add(new TagSearchCriteriaTranslator(getDaoFactory(), getEntityAttributeProviderFactory()));
        translators.add(new SpaceSearchCriteriaTranslator(getDaoFactory(), getEntityAttributeProviderFactory()));
        translators.add(new ProjectSearchCriteriaTranslator(getDaoFactory(), getEntityAttributeProviderFactory()));
        translators.add(new ExperimentSearchCriteriaTranslator(getDaoFactory(), getEntityAttributeProviderFactory()));
        translators.add(new SampleSearchCriteriaTranslator(getDaoFactory(), getEntityAttributeProviderFactory()));
        translators.add(new DataSetSearchCriteriaTranslator(getDaoFactory(), getEntityAttributeProviderFactory()));
        translators.add(new MaterialSearchCriteriaTranslator(getDaoFactory(), getEntityAttributeProviderFactory()));
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
