/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;

public final class EntityGraphGenerator
{
    private static final class EntityDescription
    {
        private static final String[] KINDS = {"E", "S", "DS"};
        String kind;
        int id;
        String type;
        EntityDescription(String description)
        {
            int indexOfBracket = description.indexOf('[');
            String code;
            if (indexOfBracket < 0)
            {
                code = description;
            } else
            {
                if (description.endsWith("]") == false)
                {
                    throw new IllegalArgumentException("Missing ']' at the end of " + description);
                }
                code = description.substring(0, indexOfBracket);
                type = description.substring(indexOfBracket + 1,  description.length() - 1);
            }
            for (String k : KINDS)
            {
                if (code.startsWith(k))
                {
                    kind = k;
                    id = Integer.parseInt(code.substring(k.length()));
                }
            }
        }
    }
    
    private static interface IEntitiesProvider<T extends EntityNode>
    {
        public Collection<? extends EntityNode> getEntities(T entityNode);
    }
    
    private class Parser
    {
        private void parse(List<String> parts)
        {
            EntityDescription entity = new EntityDescription(parts.get(0));
            List<String> rest = parts.subList(1, parts.size());
            if (entity.kind == "E")
            {
                handle(e(entity.id), rest);
            } else if (entity.kind == "S")
            {
                handle(s(entity.id), rest);
            } else if (entity.kind == "DS")
            {
                DataSetNode ds = ds(entity.id);
                ds.setType(entity.type);
                handle(ds, rest);
            }
        }
        
        private void handle(ExperimentNode experiment, List<String> parts)
        {
            experiment.has(getSamples("samples", parts));
            experiment.has(getDataSets("data sets", parts));
        }

        private void handle(SampleNode sample, List<String> parts)
        {
            sample.has(getDataSets("data sets", parts));
            sample.hasChildren(getSamples("children", parts));
            sample.hasComponents(getSamples("components", parts));
        }

        private void handle(DataSetNode dataSet, List<String> parts)
        {
            dataSet.hasChildren(getDataSets("children", parts));
            dataSet.hasParents(getDataSets("parents", parts));
            dataSet.hasComponents(getDataSets("components", parts));
        }
        
        private SampleNode[] getSamples(String name, List<String> parts)
        {
            List<SampleNode> sampleNodes = new ArrayList<SampleNode>();
            List<EntityDescription> descriptions = getDescriptions(name, "S", parts);
            for (EntityDescription description : descriptions)
            {
                SampleNode s = s(description.id);
                if (s == null)
                {
                    throw new IllegalArgumentException("No sample with id " + description.id);
                }
                sampleNodes.add(s);
            }
            return sampleNodes.toArray(new SampleNode[descriptions.size()]);
        }
        
        private DataSetNode[] getDataSets(String name, List<String> parts)
        {
            List<DataSetNode> dataSetNodes = new ArrayList<DataSetNode>();
            List<EntityDescription> descriptions = getDescriptions(name, "DS", parts);
            for (EntityDescription description : descriptions)
            {
                DataSetNode ds = ds(description.id);
                if (ds == null)
                {
                    throw new IllegalArgumentException("No data set with id " + description.id);
                }
                ds.setType(description.type);
                dataSetNodes.add(ds);
            }
            return dataSetNodes.toArray(new DataSetNode[descriptions.size()]);
        }
        
        private List<EntityDescription> getDescriptions(String name, String prefix, List<String> parts)
        {
            String descriptionsConcatenated = getValue(name, parts);
            if (descriptionsConcatenated == null)
            {
                return Collections.emptyList();
            }
            String[] descriptions = descriptionsConcatenated.split(" ");
            List<EntityDescription> result = new ArrayList<EntityDescription>();
            for (String description : descriptions)
            {
                result.add(new EntityDescription(description.trim()));
            }
            return result;
        }
        
        private String getValue(String name, List<String> parts)
        {
            for (String part : parts)
            {
                String[] splittedPart = part.split(":");
                if (splittedPart.length == 2 && splittedPart[0].startsWith(name))
                {
                    return splittedPart[1].trim();
                }
            }
            return null;
        }
    }
    
    private final Map<Long, ExperimentNode> experiments = new TreeMap<Long, ExperimentNode>();

    private final Map<Long, SampleNode> samples = new TreeMap<Long, SampleNode>();

    private final Map<Long, DataSetNode> dataSets = new TreeMap<Long, DataSetNode>();
    
    public void parse(String definition)
    {
        Parser parser = new Parser();
        String[] lines = definition.split("\n");
        for (int i = 0; i < lines.length; i++)
        {
            String line = lines[i];
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#"))
            {
                continue;
            }
            String[] splittedLine = line.split(",");
            List<String> parts = new ArrayList<String>();
            for (String part : splittedLine)
            {
                parts.add(part.trim());
            }
            try
            {
                parser.parse(parts);
            } catch (Exception ex)
            {
                throw new IllegalArgumentException("Error in line " + (i + 1) + ": " + ex.getMessage(), ex);
            }
        }
    }

    public void assertConsistency()
    {
        Collection<DataSetNode> values = dataSets.values();
        for (DataSetNode dataSetNode : values)
        {
            ExperimentNode experiment = dataSetNode.getExperiment();
            SampleNode sample = dataSetNode.getSample();
            if (experiment == null && sample == null)
            {
                throw new IllegalStateException("Data set " + dataSetNode.getCode() 
                        + " should belong to an experiment or a sample.");
            }
            if (sample != null && sample.getExperiment() != experiment)
            {
                throw new IllegalStateException("Data set " + dataSetNode.getCode() 
                        + " should belong to the same experiment as the sample.");
            }
        }
    }

    public ExperimentNode e(long id)
    {
        ExperimentNode experimentNode = experiments.get(id);
        if (experimentNode == null)
        {
            experimentNode = new ExperimentNode(id);
            experiments.put(id, experimentNode);
        }
        return experimentNode;
    }

    public SampleNode s(long id)
    {
        SampleNode sampleNode = samples.get(id);
        if (sampleNode == null)
        {
            sampleNode = new SampleNode(id);
            samples.put(id, sampleNode);
        }
        return sampleNode;
    }

    public DataSetNode ds(long id)
    {
        DataSetNode dataSetNode = dataSets.get(id);
        if (dataSetNode == null)
        {
            dataSetNode = new DataSetNode(id);
            dataSets.put(id, dataSetNode);
        }
        return dataSetNode;
    }

    public Map<Long, ExperimentNode> getExperiments()
    {
        return experiments;
    }

    public Map<Long, SampleNode> getSamples()
    {
        return samples;
    }

    public Map<Long, DataSetNode> getDataSets()
    {
        return dataSets;
    }
    
    public List<TechId> getSampleIdsByExperimentIds(Collection<TechId> experimentIds)
    {
        return getRelatedIds(experimentIds, experiments, new EntityGraphGenerator.IEntitiesProvider<ExperimentNode>()
            {
                @Override
                public Collection<? extends EntityNode> getEntities(ExperimentNode experiment)
                {
                    return experiment.getSamples();
                }
            });
    }

    public List<TechId> getSampleIdsByContainerIds(Collection<TechId> containerIds)
    {
        return getRelatedIds(containerIds, samples, new EntityGraphGenerator.IEntitiesProvider<SampleNode>()
            {
                @Override
                public Collection<? extends EntityNode> getEntities(SampleNode sample)
                {
                    return sample.getComponents();
                }
            });
    }

    public List<TechId> getDataSetIdsByExperimentIds(Collection<TechId> experimentIds)
    {
        return getRelatedIds(experimentIds, experiments, new EntityGraphGenerator.IEntitiesProvider<ExperimentNode>()
                {
                    @Override
                    public Collection<? extends EntityNode> getEntities(ExperimentNode experiment)
                    {
                        return experiment.getDataSets();
                    }
                });
    }

    public List<TechId> getDataSetIdsBySampleIds(Collection<TechId> experimentIds)
    {
        return getRelatedIds(experimentIds, samples, new EntityGraphGenerator.IEntitiesProvider<SampleNode>()
            {
                @Override
                public Collection<? extends EntityNode> getEntities(SampleNode sample)
                {
                    return sample.getDataSets();
                }
            });
    }

    public List<TechId> getChildrenDataSetIdsByDataSetIds(Collection<TechId> dataSetIds)
    {
        return getRelatedIds(dataSetIds, dataSets, new EntityGraphGenerator.IEntitiesProvider<DataSetNode>()
            {
                @Override
                public Collection<? extends EntityNode> getEntities(DataSetNode dataSet)
                {
                    return dataSet.getChildren();
                }
            });
    }

    public List<TechId> getComponentDataSetIdsByDataSetIds(Collection<TechId> dataSetIds)
    {
        return getRelatedIds(dataSetIds, dataSets, new EntityGraphGenerator.IEntitiesProvider<DataSetNode>()
            {
                @Override
                public Collection<? extends EntityNode> getEntities(DataSetNode dataSet)
                {
                    return dataSet.getComponents();
                }
            });
    }
    
    public Map<Long, Set<Long>> getContainerDataSetIdsMap(Collection<TechId> dataSetIds)
    {
        return getRelatedIdsMap(dataSetIds, dataSets,new EntityGraphGenerator.IEntitiesProvider<DataSetNode>()
                {
            @Override
            public Collection<? extends EntityNode> getEntities(DataSetNode dataSet)
            {
                return dataSet.getContainers();
            }
        });
    }
    
    public Map<Long, Set<Long>> getParentsDataSetIdsMap(Collection<TechId> dataSetIds)
    {
        return getRelatedIdsMap(dataSetIds, dataSets,new EntityGraphGenerator.IEntitiesProvider<DataSetNode>()
                {
            @Override
            public Collection<? extends EntityNode> getEntities(DataSetNode dataSet)
            {
                return dataSet.getParents();
            }
                });
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        appendTo(builder, "#Experiments", experiments);
        appendTo(builder, "#Samples", samples);
        appendTo(builder, "#Data Sets", dataSets);
        return builder.toString();
    }
    
    private void appendTo(StringBuilder builder, String name, Map<Long, ? extends EntityNode> nodes)
    {
        if (nodes.isEmpty())
        {
            return;
        }
        builder.append(name).append(":\n");
        for (EntityNode entityNode : nodes.values())
        {
            builder.append("  ").append(entityNode).append('\n');
        }
    }

    private <T extends EntityNode> List<TechId> getRelatedIds(Collection<TechId> entityIds, Map<Long, T> entities, 
            EntityGraphGenerator.IEntitiesProvider<T> provider)
    {
        List<TechId> ids = new ArrayList<TechId>();
        for (TechId entityId : entityIds)
        {
            T entity = entities.get(entityId.getId());
            if (entity != null)
            {
                ids.addAll(TechId.createList(provider.getEntities(entity)));
            }
        }
        return ids;
    }
    
    private <T extends EntityNode> Map<Long, Set<Long>> getRelatedIdsMap(Collection<TechId> entityIds, Map<Long, T> entities, 
            EntityGraphGenerator.IEntitiesProvider<T> provider)
    {
        Map<Long, Set<Long>> idsMap = new LinkedHashMap<Long, Set<Long>>();
        for (TechId entityId : entityIds)
        {
            Long id = entityId.getId();
            T entity = entities.get(id);
            if (entity != null)
            {
                List<Long> ids = TechId.asLongs(TechId.createList(provider.getEntities(entity)));
                idsMap.put(id, new LinkedHashSet<Long>(ids));
            }
        }
        return idsMap;
    }

}