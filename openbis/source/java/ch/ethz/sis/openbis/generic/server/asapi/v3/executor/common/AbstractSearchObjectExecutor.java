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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common;

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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractCompositeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractObjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.PermIdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.TechIdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.search.AbstractEntityTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.IMaterialId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset.IMapDataSetByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.IMapEntityTypeByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.experiment.IMapExperimentByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.material.IMapMaterialByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.project.IMapProjectByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample.IMapSampleByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.space.IMapSpaceByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.tag.IMapTagByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.search.EntityAttributeProviderFactory;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.search.ISearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.search.SearchCriteriaTranslationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.search.SearchCriteriaTranslatorFactory;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.search.SearchTranslationContext;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author pkupczyk
 */
public abstract class AbstractSearchObjectExecutor<CRITERIA extends AbstractObjectSearchCriteria<?>, OBJECT> implements
        ISearchObjectExecutor<CRITERIA, OBJECT>
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
    private IMapMaterialByIdExecutor mapMaterialByIdExecutor;

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
    public List<OBJECT> search(IOperationContext context, CRITERIA critera)
    {
        if (context == null)
        {
            throw new IllegalArgumentException("Context cannot be null");
        }
        if (critera == null)
        {
            throw new IllegalArgumentException("Criteria cannot be null");
        }

        replaceCriteria(context, critera);

        ISearchCriteriaTranslator translator =
                new SearchCriteriaTranslatorFactory(daoFactory, new EntityAttributeProviderFactory()).getTranslator(critera);
        SearchCriteriaTranslationResult translationResult = translator.translate(new SearchTranslationContext(context.getSession()), critera);

        return doSearch(context, translationResult.getCriteria());
    }

    private void replaceCriteria(IOperationContext context, AbstractCompositeSearchCriteria criteria)
    {
        List<ICriteriaReplacer> replacers = new LinkedList<ICriteriaReplacer>();
        replacers.add(new SpaceIdCriteriaReplacer());
        replacers.add(new ProjectIdCriteriaReplacer());
        replacers.add(new ExperimentIdCriteriaReplacer());
        replacers.add(new ExperimentTypeIdCriteriaReplacer());
        replacers.add(new SampleIdCriteriaReplacer());
        replacers.add(new SampleTypeIdCriteriaReplacer());
        replacers.add(new DataSetIdCriteriaReplacer());
        replacers.add(new DataSetTypeIdCriteriaReplacer());
        replacers.add(new MaterialIdCriteriaReplacer());
        replacers.add(new MaterialPermIdCriteriaReplacer());
        replacers.add(new MaterialTypeIdCriteriaReplacer());
        replacers.add(new TagIdCriteriaReplacer());

        Map<ICriteriaReplacer, Set<ISearchCriteria>> toReplaceMap = new HashMap<ICriteriaReplacer, Set<ISearchCriteria>>();
        collectCriteriaToReplace(context, new Stack<ISearchCriteria>(), criteria, replacers, toReplaceMap);

        if (false == toReplaceMap.isEmpty())
        {
            Map<ISearchCriteria, ISearchCriteria> replacementMap = createCriteriaReplacements(context, toReplaceMap);
            replaceCriteria(criteria, replacementMap);
        }
    }

    private void collectCriteriaToReplace(IOperationContext context, Stack<ISearchCriteria> parentCriteria,
            AbstractCompositeSearchCriteria criteria,
            List<ICriteriaReplacer> replacers, Map<ICriteriaReplacer, Set<ISearchCriteria>> toReplaceMap)
    {
        parentCriteria.push(criteria);

        for (ISearchCriteria subCriterion : criteria.getCriteria())
        {
            if (subCriterion instanceof AbstractCompositeSearchCriteria)
            {
                collectCriteriaToReplace(context, parentCriteria, (AbstractCompositeSearchCriteria) subCriterion, replacers, toReplaceMap);
            } else
            {
                for (ICriteriaReplacer replacer : replacers)
                {
                    if (replacer.canReplace(context, parentCriteria, subCriterion))
                    {
                        Set<ISearchCriteria> toReplace = toReplaceMap.get(replacer);
                        if (toReplace == null)
                        {
                            toReplace = new HashSet<ISearchCriteria>();
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

    private Map<ISearchCriteria, ISearchCriteria> createCriteriaReplacements(IOperationContext context,
            Map<ICriteriaReplacer, Set<ISearchCriteria>> toReplaceMap)
    {
        Map<ISearchCriteria, ISearchCriteria> result = new HashMap<ISearchCriteria, ISearchCriteria>();

        for (ICriteriaReplacer replacer : toReplaceMap.keySet())
        {
            Set<ISearchCriteria> toReplace = toReplaceMap.get(replacer);
            Map<ISearchCriteria, ISearchCriteria> replaced = replacer.replace(context, toReplace);
            result.putAll(replaced);
        }

        return result;
    }

    private void replaceCriteria(AbstractCompositeSearchCriteria criteria, Map<ISearchCriteria, ISearchCriteria> replacementMap)
    {
        List<ISearchCriteria> newSubCriteria = new LinkedList<ISearchCriteria>();

        for (ISearchCriteria subCriterion : criteria.getCriteria())
        {
            ISearchCriteria newSubCriterion = subCriterion;

            if (subCriterion instanceof AbstractCompositeSearchCriteria)
            {
                replaceCriteria((AbstractCompositeSearchCriteria) subCriterion, replacementMap);
            } else if (replacementMap.containsKey(subCriterion))
            {
                newSubCriterion = replacementMap.get(subCriterion);
            }

            newSubCriteria.add(newSubCriterion);
        }

        criteria.setCriteria(newSubCriteria);
    }

    private interface ICriteriaReplacer
    {

        public boolean canReplace(IOperationContext context, Stack<ISearchCriteria> parentCriteria, ISearchCriteria criteria);

        public Map<ISearchCriteria, ISearchCriteria> replace(IOperationContext context, Collection<ISearchCriteria> criteria);

    }

    @SuppressWarnings("hiding")
    private abstract class AbstractIdCriteriaReplacer<ID extends IObjectId, OBJECT> implements ICriteriaReplacer
    {

        @Override
        public boolean canReplace(IOperationContext context, Stack<ISearchCriteria> parentCriteria, ISearchCriteria criteria)
        {
            if (criteria instanceof IdSearchCriteria<?>)
            {
                IObjectId id = ((IdSearchCriteria<?>) criteria).getId();
                return getIdClass().isAssignableFrom(id.getClass());
            } else
            {
                return false;
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public Map<ISearchCriteria, ISearchCriteria> replace(IOperationContext context, Collection<ISearchCriteria> criteria)
        {
            Set<ID> objectIds = new HashSet<ID>();

            for (ISearchCriteria criterion : criteria)
            {
                IdSearchCriteria<?> idCriterion = (IdSearchCriteria<?>) criterion;
                objectIds.add((ID) idCriterion.getId());
            }

            Map<ISearchCriteria, ISearchCriteria> criterionMap = new HashMap<ISearchCriteria, ISearchCriteria>();
            Map<ID, OBJECT> objectMap = getObjectMap(context, objectIds);

            for (ISearchCriteria criterion : criteria)
            {
                IdSearchCriteria<?> idCriterion = (IdSearchCriteria<?>) criterion;
                OBJECT object = objectMap.get(idCriterion.getId());
                criterionMap.put(criterion, createReplacement(context, idCriterion, object));
            }

            return criterionMap;
        }

        protected abstract Class<ID> getIdClass();

        protected abstract Map<ID, OBJECT> getObjectMap(IOperationContext context, Collection<ID> objectIds);

        protected abstract ISearchCriteria createReplacement(IOperationContext context, ISearchCriteria criteria, OBJECT object);

    }

    private class SpaceIdCriteriaReplacer extends AbstractIdCriteriaReplacer<ISpaceId, SpacePE>
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
        protected ISearchCriteria createReplacement(IOperationContext context, ISearchCriteria criteria, SpacePE space)
        {
            CodeSearchCriteria replacement = new CodeSearchCriteria();
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

    private class ProjectIdCriteriaReplacer extends AbstractIdCriteriaReplacer<IProjectId, ProjectPE>
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
        protected ISearchCriteria createReplacement(IOperationContext context, ISearchCriteria criteria, ProjectPE project)
        {
            PermIdSearchCriteria replacement = new PermIdSearchCriteria();
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

    private class ExperimentIdCriteriaReplacer extends AbstractIdCriteriaReplacer<IExperimentId, ExperimentPE>
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
        protected ISearchCriteria createReplacement(IOperationContext context, ISearchCriteria criteria, ExperimentPE experiment)
        {
            PermIdSearchCriteria replacement = new PermIdSearchCriteria();
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

    private class SampleIdCriteriaReplacer extends AbstractIdCriteriaReplacer<ISampleId, SamplePE>
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
        protected ISearchCriteria createReplacement(IOperationContext context, ISearchCriteria criteria, SamplePE sample)
        {
            PermIdSearchCriteria replacement = new PermIdSearchCriteria();
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

    private class DataSetIdCriteriaReplacer extends AbstractIdCriteriaReplacer<IDataSetId, DataPE>
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
        protected ISearchCriteria createReplacement(IOperationContext context, ISearchCriteria criteria, DataPE dataSet)
        {
            PermIdSearchCriteria replacement = new PermIdSearchCriteria();
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

    private class MaterialIdCriteriaReplacer extends AbstractIdCriteriaReplacer<IMaterialId, MaterialPE>
    {

        @Override
        protected Class<IMaterialId> getIdClass()
        {
            return IMaterialId.class;
        }

        @Override
        protected Map<IMaterialId, MaterialPE> getObjectMap(IOperationContext context, Collection<IMaterialId> materialIds)
        {
            return mapMaterialByIdExecutor.map(context, materialIds);
        }

        @Override
        protected ISearchCriteria createReplacement(IOperationContext context, ISearchCriteria criteria, MaterialPE material)
        {
            TechIdSearchCriteria replacement = new TechIdSearchCriteria();
            if (material == null)
            {
                replacement.thatEquals(-1);
            } else
            {
                replacement.thatEquals(material.getId());
            }
            return replacement;
        }

    }

    private class MaterialPermIdCriteriaReplacer implements ICriteriaReplacer
    {

        @Override
        public boolean canReplace(IOperationContext context, Stack<ISearchCriteria> parentCriteria, ISearchCriteria criteria)
        {
            return criteria instanceof PermIdSearchCriteria && parentCriteria.peek() instanceof MaterialSearchCriteria;
        }

        @Override
        public Map<ISearchCriteria, ISearchCriteria> replace(IOperationContext context, Collection<ISearchCriteria> criteria)
        {
            throw new UnsupportedOperationException("Please use criteria.withId().thatEquals(new MaterialPermId('CODE','TYPE')) instead.");
        }

    }

    private class TagIdCriteriaReplacer extends AbstractIdCriteriaReplacer<ITagId, MetaprojectPE>
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
        protected ISearchCriteria createReplacement(IOperationContext context, ISearchCriteria criteria, MetaprojectPE tag)
        {
            CodeSearchCriteria replacement = new CodeSearchCriteria();
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

    private abstract class AbstractEntityTypeIdCriteriaReplacer extends AbstractIdCriteriaReplacer<IEntityTypeId, EntityTypePE>
    {

        @Override
        protected Class<IEntityTypeId> getIdClass()
        {
            return IEntityTypeId.class;
        }

        @Override
        public boolean canReplace(IOperationContext context, Stack<ISearchCriteria> parentCriteria, ISearchCriteria criteria)
        {
            if (false == super.canReplace(context, parentCriteria, criteria))
            {
                return false;
            }

            Stack<ISearchCriteria> parentCriteriaCopy = new Stack<ISearchCriteria>();
            parentCriteriaCopy.addAll(parentCriteria);

            ISearchCriteria parentCriterion = parentCriteriaCopy.isEmpty() ? null : parentCriteriaCopy.pop();
            ISearchCriteria grandParentCriterion = parentCriteriaCopy.isEmpty() ? null : parentCriteriaCopy.pop();

            return parentCriterion instanceof AbstractEntityTypeSearchCriteria && grandParentCriterion != null
                    && getEntityCriteriaClass().isAssignableFrom(grandParentCriterion.getClass());
        }

        @Override
        protected Map<IEntityTypeId, EntityTypePE> getObjectMap(IOperationContext context, Collection<IEntityTypeId> typeIds)
        {
            return mapEntityTypeByIdExecutor.map(context, getEntityKind(), typeIds);
        }

        @Override
        protected ISearchCriteria createReplacement(IOperationContext context, ISearchCriteria criteria, EntityTypePE type)
        {
            CodeSearchCriteria replacement = new CodeSearchCriteria();
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

        protected abstract Class<?> getEntityCriteriaClass();

    }

    private class ExperimentTypeIdCriteriaReplacer extends AbstractEntityTypeIdCriteriaReplacer
    {

        @Override
        protected EntityKind getEntityKind()
        {
            return EntityKind.EXPERIMENT;
        }

        @Override
        protected Class<?> getEntityCriteriaClass()
        {
            return ExperimentSearchCriteria.class;
        }

    }

    private class SampleTypeIdCriteriaReplacer extends AbstractEntityTypeIdCriteriaReplacer
    {

        @Override
        protected EntityKind getEntityKind()
        {
            return EntityKind.SAMPLE;
        }

        @Override
        protected Class<?> getEntityCriteriaClass()
        {
            return SampleSearchCriteria.class;
        }

    }

    private class DataSetTypeIdCriteriaReplacer extends AbstractEntityTypeIdCriteriaReplacer
    {

        @Override
        protected EntityKind getEntityKind()
        {
            return EntityKind.DATA_SET;
        }

        @Override
        protected Class<?> getEntityCriteriaClass()
        {
            return DataSetSearchCriteria.class;
        }

    }

    private class MaterialTypeIdCriteriaReplacer extends AbstractEntityTypeIdCriteriaReplacer
    {

        @Override
        protected EntityKind getEntityKind()
        {
            return EntityKind.MATERIAL;
        }

        @Override
        protected Class<?> getEntityCriteriaClass()
        {
            return MaterialSearchCriteria.class;
        }

    }

}
