/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.dataset.IMapDataSetByIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.entity.IMapEntityTypeByIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.experiment.IMapExperimentByIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.project.IMapProjectByIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.sample.IMapSampleByIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.space.IMapSpaceByIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.tag.IMapTagByIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.search.EntityAttributeProviderFactory;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.search.ISearchCriterionTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.search.SearchCriterionTranslationResult;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.search.SearchCriterionTranslatorFactory;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.search.SearchTranslationContext;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.IObjectId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.IDataSetId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.IEntityTypeId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.IExperimentId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.IProjectId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.ISpaceId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.tag.ITagId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.AbstractCompositeSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.AbstractObjectSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.CodeSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.DataSetSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.EntityTypeSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.ExperimentSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.ISearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.IdSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.PermIdSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.SampleSearchCriterion;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author pkupczyk
 */
public abstract class AbstractSearchObjectExecutor<CRITERION extends AbstractObjectSearchCriterion<?>, OBJECT> implements
        ISearchObjectExecutor<CRITERION, OBJECT>
{

    @Autowired
    private IMapSpaceByIdExecutor mapSpaceByIdExecutor;

    @Autowired
    private IMapProjectByIdExecutor mapProjectByIdExecutor;

    @Autowired
    private IMapExperimentByIdExecutor mapExperimentByIdExecutor;

    @Autowired
    private IMapSampleByIdExecutor mapSampleByIdExecutor;

    @Autowired
    private IMapDataSetByIdExecutor mapDataSetByIdExecutor;

    @Autowired
    private IMapEntityTypeByIdExecutor mapEntityTypeByIdExecutor;

    @Autowired
    private IMapTagByIdExecutor mapTagByIdExecutor;

    @Resource(name = ComponentNames.COMMON_BUSINESS_OBJECT_FACTORY)
    protected ICommonBusinessObjectFactory businessObjectFactory;

    @Autowired
    protected IDAOFactory daoFactory;

    protected abstract List<OBJECT> doSearch(IOperationContext context, DetailedSearchCriteria criteria);

    @Override
    public List<OBJECT> search(IOperationContext context, CRITERION criterion)
    {
        if (context == null)
        {
            throw new IllegalArgumentException("Context cannot be null");
        }
        if (criterion == null)
        {
            throw new IllegalArgumentException("Criterion cannot be null");
        }

        replaceCriteria(context, criterion);

        ISearchCriterionTranslator translator =
                new SearchCriterionTranslatorFactory(daoFactory, new EntityAttributeProviderFactory()).getTranslator(criterion);
        SearchCriterionTranslationResult translationResult = translator.translate(new SearchTranslationContext(context.getSession()), criterion);

        return doSearch(context, translationResult.getCriteria());
    }

    private void replaceCriteria(IOperationContext context, AbstractCompositeSearchCriterion criterion)
    {
        List<ICriterionReplacer> replacers = new LinkedList<ICriterionReplacer>();
        replacers.add(new SpaceIdCriterionReplacer());
        replacers.add(new ProjectIdCriterionReplacer());
        replacers.add(new ExperimentIdCriterionReplacer());
        replacers.add(new ExperimentTypeIdCriterionReplacer());
        replacers.add(new SampleIdCriterionReplacer());
        replacers.add(new SampleTypeIdCriterionReplacer());
        replacers.add(new DataSetIdCriterionReplacer());
        replacers.add(new DataSetTypeIdCriterionReplacer());
        replacers.add(new TagIdCriterionReplacer());

        Map<ICriterionReplacer, Set<ISearchCriterion>> toReplaceMap = new HashMap<ICriterionReplacer, Set<ISearchCriterion>>();
        collectCriteriaToReplace(context, new Stack<ISearchCriterion>(), criterion, replacers, toReplaceMap);

        if (false == toReplaceMap.isEmpty())
        {
            Map<ISearchCriterion, ISearchCriterion> replacementMap = createCriteriaReplacements(context, toReplaceMap);
            replaceCriteria(criterion, replacementMap);
        }
    }

    private void collectCriteriaToReplace(IOperationContext context, Stack<ISearchCriterion> parentCriteria,
            AbstractCompositeSearchCriterion criterion,
            List<ICriterionReplacer> replacers, Map<ICriterionReplacer, Set<ISearchCriterion>> toReplaceMap)
    {
        parentCriteria.push(criterion);

        for (ISearchCriterion subCriterion : criterion.getCriteria())
        {
            if (subCriterion instanceof AbstractCompositeSearchCriterion)
            {
                collectCriteriaToReplace(context, parentCriteria, (AbstractCompositeSearchCriterion) subCriterion, replacers, toReplaceMap);
            } else
            {
                for (ICriterionReplacer replacer : replacers)
                {
                    if (replacer.canReplace(context, parentCriteria, subCriterion))
                    {
                        Set<ISearchCriterion> toReplace = toReplaceMap.get(replacer);
                        if (toReplace == null)
                        {
                            toReplace = new HashSet<ISearchCriterion>();
                            toReplaceMap.put(replacer, toReplace);
                        }
                        toReplace.add(subCriterion);
                        break;
                    }
                }
            }
        }

        parentCriteria.pop();
    }

    private Map<ISearchCriterion, ISearchCriterion> createCriteriaReplacements(IOperationContext context,
            Map<ICriterionReplacer, Set<ISearchCriterion>> toReplaceMap)
    {
        Map<ISearchCriterion, ISearchCriterion> result = new HashMap<ISearchCriterion, ISearchCriterion>();

        for (ICriterionReplacer replacer : toReplaceMap.keySet())
        {
            Set<ISearchCriterion> toReplace = toReplaceMap.get(replacer);
            Map<ISearchCriterion, ISearchCriterion> replaced = replacer.replace(context, toReplace);
            result.putAll(replaced);
        }

        return result;
    }

    private void replaceCriteria(AbstractCompositeSearchCriterion criterion, Map<ISearchCriterion, ISearchCriterion> replacementMap)
    {
        List<ISearchCriterion> newSubCriteria = new LinkedList<ISearchCriterion>();

        for (ISearchCriterion subCriterion : criterion.getCriteria())
        {
            ISearchCriterion newSubCriterion = subCriterion;

            if (subCriterion instanceof AbstractCompositeSearchCriterion)
            {
                replaceCriteria((AbstractCompositeSearchCriterion) subCriterion, replacementMap);
            } else if (replacementMap.containsKey(subCriterion))
            {
                newSubCriterion = replacementMap.get(subCriterion);
            }

            newSubCriteria.add(newSubCriterion);
        }

        criterion.setCriteria(newSubCriteria);
    }

    private interface ICriterionReplacer
    {

        public boolean canReplace(IOperationContext context, Stack<ISearchCriterion> parentCriteria, ISearchCriterion criterion);

        public Map<ISearchCriterion, ISearchCriterion> replace(IOperationContext context, Collection<ISearchCriterion> criteria);

    }

    @SuppressWarnings("hiding")
    private abstract class AbstractIdCriterionReplacer<ID extends IObjectId, OBJECT> implements ICriterionReplacer
    {

        @Override
        public boolean canReplace(IOperationContext context, Stack<ISearchCriterion> parentCriteria, ISearchCriterion criterion)
        {
            if (criterion instanceof IdSearchCriterion<?>)
            {
                IObjectId id = ((IdSearchCriterion<?>) criterion).getId();
                return getIdClass().isAssignableFrom(id.getClass());
            } else
            {
                return false;
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public Map<ISearchCriterion, ISearchCriterion> replace(IOperationContext context, Collection<ISearchCriterion> criteria)
        {
            Set<ID> objectIds = new HashSet<ID>();

            for (ISearchCriterion criterion : criteria)
            {
                IdSearchCriterion<?> idCriterion = (IdSearchCriterion<?>) criterion;
                objectIds.add((ID) idCriterion.getId());
            }

            Map<ISearchCriterion, ISearchCriterion> criterionMap = new HashMap<ISearchCriterion, ISearchCriterion>();
            Map<ID, OBJECT> objectMap = getObjectMap(context, objectIds);

            for (ISearchCriterion criterion : criteria)
            {
                IdSearchCriterion<?> idCriterion = (IdSearchCriterion<?>) criterion;
                OBJECT object = objectMap.get(idCriterion.getId());
                criterionMap.put(criterion, createReplacement(context, idCriterion, object));
            }

            return criterionMap;
        }

        protected abstract Class<ID> getIdClass();

        protected abstract Map<ID, OBJECT> getObjectMap(IOperationContext context, Collection<ID> objectIds);

        protected abstract ISearchCriterion createReplacement(IOperationContext context, ISearchCriterion criterion, OBJECT object);

    }

    private class SpaceIdCriterionReplacer extends AbstractIdCriterionReplacer<ISpaceId, SpacePE>
    {

        @Override
        protected Class<ISpaceId> getIdClass()
        {
            return ISpaceId.class;
        }

        @Override
        protected Map<ISpaceId, SpacePE> getObjectMap(IOperationContext context, Collection<ISpaceId> spaceIds)
        {
            return mapSpaceByIdExecutor.map(context, spaceIds);
        }

        @Override
        protected ISearchCriterion createReplacement(IOperationContext context, ISearchCriterion criterion, SpacePE space)
        {
            CodeSearchCriterion replacement = new CodeSearchCriterion();
            if (space == null)
            {
                replacement.thatEquals("#");
            } else
            {
                replacement.thatEquals(space.getCode());
            }
            return replacement;
        }

    }

    private class ProjectIdCriterionReplacer extends AbstractIdCriterionReplacer<IProjectId, ProjectPE>
    {

        @Override
        protected Class<IProjectId> getIdClass()
        {
            return IProjectId.class;
        }

        @Override
        protected Map<IProjectId, ProjectPE> getObjectMap(IOperationContext context, Collection<IProjectId> projectIds)
        {
            return mapProjectByIdExecutor.map(context, projectIds);
        }

        @Override
        protected ISearchCriterion createReplacement(IOperationContext context, ISearchCriterion criterion, ProjectPE project)
        {
            PermIdSearchCriterion replacement = new PermIdSearchCriterion();
            if (project == null)
            {
                replacement.thatEquals("#");
            } else
            {
                replacement.thatEquals(project.getPermId());
            }
            return replacement;
        }

    }

    private class ExperimentIdCriterionReplacer extends AbstractIdCriterionReplacer<IExperimentId, ExperimentPE>
    {

        @Override
        protected Class<IExperimentId> getIdClass()
        {
            return IExperimentId.class;
        }

        @Override
        protected Map<IExperimentId, ExperimentPE> getObjectMap(IOperationContext context, Collection<IExperimentId> experimentIds)
        {
            return mapExperimentByIdExecutor.map(context, experimentIds);
        }

        @Override
        protected ISearchCriterion createReplacement(IOperationContext context, ISearchCriterion criterion, ExperimentPE experiment)
        {
            PermIdSearchCriterion replacement = new PermIdSearchCriterion();
            if (experiment == null)
            {
                replacement.thatEquals("#");
            } else
            {
                replacement.thatEquals(experiment.getPermId());
            }
            return replacement;
        }

    }

    private class SampleIdCriterionReplacer extends AbstractIdCriterionReplacer<ISampleId, SamplePE>
    {

        @Override
        protected Class<ISampleId> getIdClass()
        {
            return ISampleId.class;
        }

        @Override
        protected Map<ISampleId, SamplePE> getObjectMap(IOperationContext context, Collection<ISampleId> sampleIds)
        {
            return mapSampleByIdExecutor.map(context, sampleIds);
        }

        @Override
        protected ISearchCriterion createReplacement(IOperationContext context, ISearchCriterion criterion, SamplePE sample)
        {
            PermIdSearchCriterion replacement = new PermIdSearchCriterion();
            if (sample == null)
            {
                replacement.thatEquals("#");
            } else
            {
                replacement.thatEquals(sample.getPermId());
            }
            return replacement;
        }

    }

    private class DataSetIdCriterionReplacer extends AbstractIdCriterionReplacer<IDataSetId, DataPE>
    {

        @Override
        protected Class<IDataSetId> getIdClass()
        {
            return IDataSetId.class;
        }

        @Override
        protected Map<IDataSetId, DataPE> getObjectMap(IOperationContext context, Collection<IDataSetId> dataSetIds)
        {
            return mapDataSetByIdExecutor.map(context, dataSetIds);
        }

        @Override
        protected ISearchCriterion createReplacement(IOperationContext context, ISearchCriterion criterion, DataPE dataSet)
        {
            PermIdSearchCriterion replacement = new PermIdSearchCriterion();
            if (dataSet == null)
            {
                replacement.thatEquals("#");
            } else
            {
                replacement.thatEquals(dataSet.getPermId());
            }
            return replacement;
        }

    }

    private class TagIdCriterionReplacer extends AbstractIdCriterionReplacer<ITagId, MetaprojectPE>
    {

        @Override
        protected Class<ITagId> getIdClass()
        {
            return ITagId.class;
        }

        @Override
        protected Map<ITagId, MetaprojectPE> getObjectMap(IOperationContext context, Collection<ITagId> tagIds)
        {
            return mapTagByIdExecutor.map(context, tagIds);
        }

        @Override
        protected ISearchCriterion createReplacement(IOperationContext context, ISearchCriterion criterion, MetaprojectPE tag)
        {
            CodeSearchCriterion replacement = new CodeSearchCriterion();
            if (tag == null)
            {
                replacement.thatEquals("#");
            } else
            {
                replacement.thatEquals(tag.getName());
            }
            return replacement;
        }

    }

    private abstract class AbstractEntityTypeIdCriterionReplacer extends AbstractIdCriterionReplacer<IEntityTypeId, EntityTypePE>
    {

        @Override
        protected Class<IEntityTypeId> getIdClass()
        {
            return IEntityTypeId.class;
        }

        @Override
        public boolean canReplace(IOperationContext context, Stack<ISearchCriterion> parentCriteria, ISearchCriterion criterion)
        {
            if (false == super.canReplace(context, parentCriteria, criterion))
            {
                return false;
            }

            Stack<ISearchCriterion> parentCriteriaCopy = new Stack<ISearchCriterion>();
            parentCriteriaCopy.addAll(parentCriteria);

            ISearchCriterion parentCriterion = parentCriteriaCopy.isEmpty() ? null : parentCriteriaCopy.pop();
            ISearchCriterion grandParentCriterion = parentCriteriaCopy.isEmpty() ? null : parentCriteriaCopy.pop();

            return parentCriterion instanceof EntityTypeSearchCriterion && grandParentCriterion != null
                    && getEntityCriterionClass().isAssignableFrom(grandParentCriterion.getClass());
        }

        @Override
        protected Map<IEntityTypeId, EntityTypePE> getObjectMap(IOperationContext context, Collection<IEntityTypeId> typeIds)
        {
            return mapEntityTypeByIdExecutor.map(context, getEntityKind(), typeIds);
        }

        @Override
        protected ISearchCriterion createReplacement(IOperationContext context, ISearchCriterion criterion, EntityTypePE type)
        {
            CodeSearchCriterion replacement = new CodeSearchCriterion();
            if (type == null)
            {
                replacement.thatEquals("#");
            } else
            {
                replacement.thatEquals(type.getCode());
            }
            return replacement;
        }

        protected abstract EntityKind getEntityKind();

        protected abstract Class<?> getEntityCriterionClass();

    }

    private class ExperimentTypeIdCriterionReplacer extends AbstractEntityTypeIdCriterionReplacer
    {

        @Override
        protected EntityKind getEntityKind()
        {
            return EntityKind.EXPERIMENT;
        }

        @Override
        protected Class<?> getEntityCriterionClass()
        {
            return ExperimentSearchCriterion.class;
        }

    }

    private class SampleTypeIdCriterionReplacer extends AbstractEntityTypeIdCriterionReplacer
    {

        @Override
        protected EntityKind getEntityKind()
        {
            return EntityKind.SAMPLE;
        }

        @Override
        protected Class<?> getEntityCriterionClass()
        {
            return SampleSearchCriterion.class;
        }

    }

    private class DataSetTypeIdCriterionReplacer extends AbstractEntityTypeIdCriterionReplacer
    {

        @Override
        protected EntityKind getEntityKind()
        {
            return EntityKind.DATA_SET;
        }

        @Override
        protected Class<?> getEntityCriterionClass()
        {
            return DataSetSearchCriterion.class;
        }

    }

}
