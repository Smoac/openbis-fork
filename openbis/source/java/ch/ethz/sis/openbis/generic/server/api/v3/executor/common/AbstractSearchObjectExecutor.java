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
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.IEntityTypeId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.IExperimentId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.IProjectId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.ISpaceId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.tag.ITagId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.AbstractCompositeSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.AbstractObjectSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.CodeSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.EntityTypeSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.ExperimentSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.ISearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.IdSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.PermIdSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.SampleSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.TagSearchCriterion;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
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

    private class SpaceIdCriterionReplacer implements ICriterionReplacer
    {

        @Override
        public boolean canReplace(IOperationContext context, Stack<ISearchCriterion> parentCriteria, ISearchCriterion criterion)
        {
            if (criterion instanceof IdSearchCriterion<?>)
            {
                return ((IdSearchCriterion<?>) criterion).getId() instanceof ISpaceId;
            } else
            {
                return false;
            }
        }

        @Override
        public Map<ISearchCriterion, ISearchCriterion> replace(IOperationContext context, Collection<ISearchCriterion> criteria)
        {
            Set<ISpaceId> spaceIds = new HashSet<ISpaceId>();

            for (ISearchCriterion criterion : criteria)
            {
                IdSearchCriterion<?> idCriterion = (IdSearchCriterion<?>) criterion;
                spaceIds.add((ISpaceId) idCriterion.getId());
            }

            Map<ISearchCriterion, ISearchCriterion> criterionMap = new HashMap<ISearchCriterion, ISearchCriterion>();
            Map<ISpaceId, SpacePE> spaceMap = mapSpaceByIdExecutor.map(context, spaceIds);

            for (ISearchCriterion criterion : criteria)
            {
                IdSearchCriterion<?> idCriterion = (IdSearchCriterion<?>) criterion;
                SpacePE space = spaceMap.get(idCriterion.getId());

                if (space == null)
                {
                    CodeSearchCriterion replacement = new CodeSearchCriterion();
                    replacement.thatEquals("#");
                    criterionMap.put(criterion, replacement);
                } else
                {
                    CodeSearchCriterion replacement = new CodeSearchCriterion();
                    replacement.thatEquals(space.getCode());
                    criterionMap.put(criterion, replacement);
                }
            }

            return criterionMap;
        }

    }

    private class ProjectIdCriterionReplacer implements ICriterionReplacer
    {

        @Override
        public boolean canReplace(IOperationContext context, Stack<ISearchCriterion> parentCriteria, ISearchCriterion criterion)
        {
            if (criterion instanceof IdSearchCriterion<?>)
            {
                return ((IdSearchCriterion<?>) criterion).getId() instanceof IProjectId;
            } else
            {
                return false;
            }
        }

        @Override
        public Map<ISearchCriterion, ISearchCriterion> replace(IOperationContext context, Collection<ISearchCriterion> criteria)
        {
            Set<IProjectId> projectIds = new HashSet<IProjectId>();

            for (ISearchCriterion criterion : criteria)
            {
                IdSearchCriterion<?> idCriterion = (IdSearchCriterion<?>) criterion;
                projectIds.add((IProjectId) idCriterion.getId());
            }

            Map<ISearchCriterion, ISearchCriterion> criterionMap = new HashMap<ISearchCriterion, ISearchCriterion>();
            Map<IProjectId, ProjectPE> projectMap = mapProjectByIdExecutor.map(context, projectIds);

            for (ISearchCriterion criterion : criteria)
            {
                IdSearchCriterion<?> idCriterion = (IdSearchCriterion<?>) criterion;
                ProjectPE project = projectMap.get(idCriterion.getId());

                if (project == null)
                {
                    PermIdSearchCriterion replacement = new PermIdSearchCriterion();
                    replacement.thatEquals("#");
                    criterionMap.put(criterion, replacement);
                } else
                {
                    PermIdSearchCriterion replacement = new PermIdSearchCriterion();
                    replacement.thatEquals(project.getPermId());
                    criterionMap.put(criterion, replacement);
                }
            }

            return criterionMap;
        }

    }

    private class ExperimentIdCriterionReplacer implements ICriterionReplacer
    {

        @Override
        public boolean canReplace(IOperationContext context, Stack<ISearchCriterion> parentCriteria, ISearchCriterion criterion)
        {
            if (criterion instanceof IdSearchCriterion<?>)
            {
                return ((IdSearchCriterion<?>) criterion).getId() instanceof IExperimentId;
            } else
            {
                return false;
            }
        }

        @Override
        public Map<ISearchCriterion, ISearchCriterion> replace(IOperationContext context, Collection<ISearchCriterion> criteria)
        {
            Set<IExperimentId> experimentIds = new HashSet<IExperimentId>();

            for (ISearchCriterion criterion : criteria)
            {
                IdSearchCriterion<?> idCriterion = (IdSearchCriterion<?>) criterion;
                experimentIds.add((IExperimentId) idCriterion.getId());
            }

            Map<ISearchCriterion, ISearchCriterion> criterionMap = new HashMap<ISearchCriterion, ISearchCriterion>();
            Map<IExperimentId, ExperimentPE> experimentMap = mapExperimentByIdExecutor.map(context, experimentIds);

            for (ISearchCriterion criterion : criteria)
            {
                IdSearchCriterion<?> idCriterion = (IdSearchCriterion<?>) criterion;
                ExperimentPE experiment = experimentMap.get(idCriterion.getId());

                if (experiment == null)
                {
                    PermIdSearchCriterion replacement = new PermIdSearchCriterion();
                    replacement.thatEquals("#");
                    criterionMap.put(criterion, replacement);
                } else
                {
                    PermIdSearchCriterion replacement = new PermIdSearchCriterion();
                    replacement.thatEquals(experiment.getPermId());
                    criterionMap.put(criterion, replacement);
                }
            }

            return criterionMap;
        }

    }

    private class SampleIdCriterionReplacer implements ICriterionReplacer
    {

        @Override
        public boolean canReplace(IOperationContext context, Stack<ISearchCriterion> parentCriteria, ISearchCriterion criterion)
        {
            if (criterion instanceof IdSearchCriterion<?>)
            {
                return ((IdSearchCriterion<?>) criterion).getId() instanceof ISampleId;
            } else
            {
                return false;
            }
        }

        @Override
        public Map<ISearchCriterion, ISearchCriterion> replace(IOperationContext context, Collection<ISearchCriterion> criteria)
        {
            Set<ISampleId> sampleIds = new HashSet<ISampleId>();

            for (ISearchCriterion criterion : criteria)
            {
                IdSearchCriterion<?> idCriterion = (IdSearchCriterion<?>) criterion;
                sampleIds.add((ISampleId) idCriterion.getId());
            }

            Map<ISearchCriterion, ISearchCriterion> criterionMap = new HashMap<ISearchCriterion, ISearchCriterion>();
            Map<ISampleId, SamplePE> sampleMap = mapSampleByIdExecutor.map(context, sampleIds);

            for (ISearchCriterion criterion : criteria)
            {
                IdSearchCriterion<?> idCriterion = (IdSearchCriterion<?>) criterion;
                SamplePE sample = sampleMap.get(idCriterion.getId());

                if (sample == null)
                {
                    PermIdSearchCriterion replacement = new PermIdSearchCriterion();
                    replacement.thatEquals("#");
                    criterionMap.put(criterion, replacement);
                } else
                {
                    PermIdSearchCriterion replacement = new PermIdSearchCriterion();
                    replacement.thatEquals(sample.getPermId());
                    criterionMap.put(criterion, replacement);
                }
            }

            return criterionMap;
        }

    }

    private class ExperimentTypeIdCriterionReplacer implements ICriterionReplacer
    {

        @Override
        public boolean canReplace(IOperationContext context, Stack<ISearchCriterion> parentCriteria, ISearchCriterion criterion)
        {
            Stack<ISearchCriterion> parentCriteriaCopy = new Stack<ISearchCriterion>();
            parentCriteriaCopy.addAll(parentCriteria);

            ISearchCriterion parentCriterion = parentCriteriaCopy.isEmpty() ? null : parentCriteriaCopy.pop();
            ISearchCriterion grandParentCriterion = parentCriteriaCopy.isEmpty() ? null : parentCriteriaCopy.pop();

            return criterion instanceof IdSearchCriterion<?> && parentCriterion instanceof EntityTypeSearchCriterion
                    && grandParentCriterion instanceof ExperimentSearchCriterion;
        }

        @Override
        public Map<ISearchCriterion, ISearchCriterion> replace(IOperationContext context, Collection<ISearchCriterion> criteria)
        {
            Set<IEntityTypeId> typeIds = new HashSet<IEntityTypeId>();

            for (ISearchCriterion criterion : criteria)
            {
                IdSearchCriterion<?> idCriterion = (IdSearchCriterion<?>) criterion;
                typeIds.add((IEntityTypeId) idCriterion.getId());
            }

            Map<ISearchCriterion, ISearchCriterion> criterionMap = new HashMap<ISearchCriterion, ISearchCriterion>();
            Map<IEntityTypeId, EntityTypePE> typeMap = mapEntityTypeByIdExecutor.map(context, EntityKind.EXPERIMENT, typeIds);

            for (ISearchCriterion criterion : criteria)
            {
                IdSearchCriterion<?> idCriterion = (IdSearchCriterion<?>) criterion;
                EntityTypePE type = typeMap.get(idCriterion.getId());

                if (type == null)
                {
                    CodeSearchCriterion replacement = new CodeSearchCriterion();
                    replacement.thatEquals("#");
                    criterionMap.put(criterion, replacement);
                } else
                {
                    CodeSearchCriterion replacement = new CodeSearchCriterion();
                    replacement.thatEquals(type.getCode());
                    criterionMap.put(criterion, replacement);
                }
            }

            return criterionMap;
        }

    }

    private class SampleTypeIdCriterionReplacer implements ICriterionReplacer
    {

        @Override
        public boolean canReplace(IOperationContext context, Stack<ISearchCriterion> parentCriteria, ISearchCriterion criterion)
        {
            Stack<ISearchCriterion> parentCriteriaCopy = new Stack<ISearchCriterion>();
            parentCriteriaCopy.addAll(parentCriteria);

            ISearchCriterion parentCriterion = parentCriteriaCopy.isEmpty() ? null : parentCriteriaCopy.pop();
            ISearchCriterion grandParentCriterion = parentCriteriaCopy.isEmpty() ? null : parentCriteriaCopy.pop();

            return criterion instanceof IdSearchCriterion<?> && parentCriterion instanceof EntityTypeSearchCriterion
                    && grandParentCriterion instanceof SampleSearchCriterion;
        }

        @Override
        public Map<ISearchCriterion, ISearchCriterion> replace(IOperationContext context, Collection<ISearchCriterion> criteria)
        {
            Set<IEntityTypeId> typeIds = new HashSet<IEntityTypeId>();

            for (ISearchCriterion criterion : criteria)
            {
                IdSearchCriterion<?> idCriterion = (IdSearchCriterion<?>) criterion;
                typeIds.add((IEntityTypeId) idCriterion.getId());
            }

            Map<ISearchCriterion, ISearchCriterion> criterionMap = new HashMap<ISearchCriterion, ISearchCriterion>();
            Map<IEntityTypeId, EntityTypePE> typeMap = mapEntityTypeByIdExecutor.map(context, EntityKind.SAMPLE, typeIds);

            for (ISearchCriterion criterion : criteria)
            {
                IdSearchCriterion<?> idCriterion = (IdSearchCriterion<?>) criterion;
                EntityTypePE type = typeMap.get(idCriterion.getId());

                if (type == null)
                {
                    CodeSearchCriterion replacement = new CodeSearchCriterion();
                    replacement.thatEquals("#");
                    criterionMap.put(criterion, replacement);
                } else
                {
                    CodeSearchCriterion replacement = new CodeSearchCriterion();
                    replacement.thatEquals(type.getCode());
                    criterionMap.put(criterion, replacement);
                }
            }

            return criterionMap;
        }

    }

    private class TagIdCriterionReplacer implements ICriterionReplacer
    {

        @Override
        public boolean canReplace(IOperationContext context, Stack<ISearchCriterion> parentCriteria, ISearchCriterion criterion)
        {
            Stack<ISearchCriterion> parentCriteriaCopy = new Stack<ISearchCriterion>();
            parentCriteriaCopy.addAll(parentCriteria);

            ISearchCriterion parentCriterion = parentCriteriaCopy.isEmpty() ? null : parentCriteriaCopy.pop();

            return criterion instanceof IdSearchCriterion<?> && parentCriterion instanceof TagSearchCriterion;
        }

        @Override
        public Map<ISearchCriterion, ISearchCriterion> replace(IOperationContext context, Collection<ISearchCriterion> criteria)
        {
            Set<ITagId> tagIds = new HashSet<ITagId>();

            for (ISearchCriterion criterion : criteria)
            {
                IdSearchCriterion<?> idCriterion = (IdSearchCriterion<?>) criterion;
                tagIds.add((ITagId) idCriterion.getId());
            }

            Map<ISearchCriterion, ISearchCriterion> criterionMap = new HashMap<ISearchCriterion, ISearchCriterion>();
            Map<ITagId, MetaprojectPE> tagMap = mapTagByIdExecutor.map(context, tagIds);

            for (ISearchCriterion criterion : criteria)
            {
                IdSearchCriterion<?> idCriterion = (IdSearchCriterion<?>) criterion;
                MetaprojectPE tag = tagMap.get(idCriterion.getId());

                if (tag == null)
                {
                    CodeSearchCriterion replacement = new CodeSearchCriterion();
                    replacement.thatEquals("#");
                    criterionMap.put(criterion, replacement);
                } else
                {
                    CodeSearchCriterion replacement = new CodeSearchCriterion();
                    replacement.thatEquals(tag.getName());
                    criterionMap.put(criterion, replacement);
                }
            }

            return criterionMap;
        }

    }

}
