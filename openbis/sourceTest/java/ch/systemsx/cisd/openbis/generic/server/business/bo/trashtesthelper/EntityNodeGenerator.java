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

package ch.systemsx.cisd.openbis.generic.server.business.bo.trashtesthelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.xml.internal.txw2.IllegalAnnotationException;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;

public final class EntityNodeGenerator
{
    private static interface IEntitiesProvider<T extends EntityNode>
    {
        public Collection<? extends EntityNode> getEntities(T entityNode);
    }
    
    private final Map<Long, ExperimentNode> experiments = new LinkedHashMap<Long, ExperimentNode>();

    private final Map<Long, SampleNode> samples = new LinkedHashMap<Long, SampleNode>();

    private final Map<Long, DataSetNode> dataSets = new LinkedHashMap<Long, DataSetNode>();
    
    private interface ILineParser
    {
        public void parse(List<String> parts);
    }
    
    private class LineParser implements ILineParser
    {
        @Override
        public void parse(List<String> parts)
        {
            String entity = parts.get(0);
            List<String> rest = parts.subList(1, parts.size());
            if (entity.startsWith("E"))
            {
                handle(e(Long.parseLong(entity.substring(1))), rest);
            } else if (entity.startsWith("S"))
            {
                handle(s(Long.parseLong(entity.substring(1))), rest);
            } else if (entity.startsWith("DS"))
            {
                handle(ds(Long.parseLong(entity.substring(2))), rest);
            }
        }
        
        protected void handle(ExperimentNode experiment, List<String> parts)
        {
        }
        
        protected void handle(SampleNode sample, List<String> parts)
        {
        }
        
        protected void handle(DataSetNode dataSet, List<String> parts)
        {
        }
    }
    
    private class NodeLinkerParser extends LineParser
    {
        @Override
        protected void handle(ExperimentNode experiment, List<String> parts)
        {
            experiment.has(getSamples("samples", parts));
            experiment.has(getDataSets("data sets", parts));
        }

        @Override
        protected void handle(SampleNode sample, List<String> parts)
        {
            sample.has(getDataSets("data sets", parts));
            sample.hasChildren(getSamples("children", parts));
            sample.hasComponents(getSamples("components", parts));
        }

        @Override
        protected void handle(DataSetNode dataSet, List<String> parts)
        {
            dataSet.hasChildren(getDataSets("children", parts));
            dataSet.hasComponents(getDataSets("components", parts));
        }
        
        protected SampleNode[] getSamples(String name, List<String> parts)
        {
            List<SampleNode> sampleNodes = new ArrayList<SampleNode>();
            List<Long> ids = getIds(name, "S", parts);
            for (Long id : ids)
            {
                SampleNode s = s(id);
                if (s == null)
                {
                    throw new IllegalArgumentException("No sample with id " + id);
                }
                sampleNodes.add(s);
            }
            return sampleNodes.toArray(new SampleNode[ids.size()]);
        }
        
        protected DataSetNode[] getDataSets(String name, List<String> parts)
        {
            List<DataSetNode> dataSetNodes = new ArrayList<DataSetNode>();
            List<Long> ids = getIds(name, "DS", parts);
            for (Long id : ids)
            {
                DataSetNode ds = ds(id);
                if (ds == null)
                {
                    throw new IllegalArgumentException("No data set with id " + id);
                }
                dataSetNodes.add(ds);
            }
            return dataSetNodes.toArray(new DataSetNode[ids.size()]);
        }
        
        protected List<Long> getIds(String name, String prefix, List<String> parts)
        {
            String codesAsString = getValue(name, parts);
            if (codesAsString == null)
            {
                return Collections.emptyList();
            }
            if ((codesAsString.startsWith("[") && codesAsString.endsWith("]")) == false)
            {
                throw new IllegalArgumentException("Missing '[' and ']' for definition '" + name + "'.");
            }
            String[] codes = codesAsString.substring(1, codesAsString.length() - 1).split(",");
            List<Long> result = new ArrayList<Long>();
            for (String code : codes)
            {
                String trimmedCode = code.trim();
                if (trimmedCode.startsWith(prefix) == false)
                {
                    throw new IllegalArgumentException("Entity code doesn't start with '" + prefix + "': " + code);
                }
                result.add(Long.parseLong(trimmedCode.substring(prefix.length())));
            }
            return result;
        }
        
        protected String getValue(String name, List<String> parts)
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
    
    private class CheckNodeLinkParser extends NodeLinkerParser
    {

        @Override
        protected void handle(ExperimentNode experiment, List<String> parts)
        {
        }

        @Override
        protected void handle(SampleNode sample, List<String> parts)
        {
            assertSameExperiment(parts, sample.getExperiment());
            assertSameSample("container", parts, sample.getContainer());
            for (SampleNode parent : getSamples("parents", parts))
            {
                if (parent.getChildren().contains(sample) == false)
                {
                    throw new IllegalAnnotationException("Sample " + sample.getCode() 
                            + " isn't a child of " + parent.getCode() + ".");
                }
            }
        }

        @Override
        protected void handle(DataSetNode dataSet, List<String> parts)
        {
            assertSameExperiment(parts, dataSet.getExperiment());
            assertSameSample("sample", parts, dataSet.getSample());
            for (DataSetNode parent : getDataSets("parents", parts))
            {
                if (parent.getChildren().contains(dataSet) == false)
                {
                    throw new IllegalAnnotationException("Data set " + dataSet.getCode() 
                            + " isn't a child of " + parent.getCode() + ".");
                }
            }
            for (DataSetNode container : getDataSets("containers", parts))
            {
                if (container.getComponents().contains(dataSet) == false)
                {
                    throw new IllegalAnnotationException("Data set " + dataSet.getCode() 
                            + " isn't a component of " + container.getCode() + ".");
                }
            }
        }
        
        private void assertSameExperiment(List<String> parts, ExperimentNode linkExperiment)
        {
            String code = getValue("experiment", parts);
            if (code == null)
            {
                return;
            }
            if (code.startsWith("E") == false)
            {
                throw new IllegalArgumentException("Invalid experiment code: " + code);
            }
            ExperimentNode parsedExperiment = experiments.get(Long.parseLong(code.substring(1)));
            if (parsedExperiment == null)
            {
                throw new IllegalArgumentException("Unknown experiment: " + code);
            }
            if (parsedExperiment != linkExperiment)
            {
                throw new IllegalArgumentException("Experiment should be " + render(linkExperiment) 
                        + " instead of " + render(parsedExperiment));
            }
        }
        
        private void assertSameSample(String name, List<String> parts, SampleNode linkSampe)
        {
            String code = getValue(name, parts);
            if (code == null)
            {
                return;
            }
            if (code.startsWith("S") == false)
            {
                throw new IllegalArgumentException("Invalid sample code: " + code);
            }
            SampleNode parsedSample = samples.get(Long.parseLong(code.substring(1)));
            if (parsedSample == null)
            {
                throw new IllegalArgumentException("Unknown Sample: " + code);
            }
            if (parsedSample != linkSampe)
            {
                throw new IllegalArgumentException("Sample should be " + render(linkSampe) 
                        + " instead of " + render(parsedSample));
            }
        }
        
        private String render(EntityNode entity)
        {
            return entity == null ? "undefined" : entity.getCode();
        }
    }
    
    public void parse(String definition)
    {
        parse(definition, new LineParser());
        parse(definition, new NodeLinkerParser());
        parse(definition, new CheckNodeLinkParser());
    }
    
    private void parse(String definition, ILineParser parser)
    {
        String[] lines = definition.split("\n");
        for (int i = 0; i < lines.length; i++)
        {
            String line = lines[i];
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#"))
            {
                continue;
            }
            try
            {
                parser.parse(getParts(line));
            } catch (Exception ex)
            {
                throw new IllegalArgumentException("Error in line " + (i + 1) + ": " + ex.getMessage(), ex);
            }
        }
    }

    private List<String> getParts(String line)
    {
        List<String> parts = new ArrayList<String>();
        StringBuilder builder = new StringBuilder();
        boolean inSideList = false;
        for (int j = 0; j < line.length(); j++)
        {
            char c = line.charAt(j);
            if (inSideList)
            {
                builder.append(c);
                inSideList = c != ']';
            } else if (c == ',')
            {
                parts.add(builder.toString().trim());
                builder.setLength(0);
            } else
            {
                builder.append(c);
                inSideList = c == '[';
            }
        }
        parts.add(builder.toString().trim());
        return parts;
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

    public Map<Long, DataSetNode> getDataSets()
    {
        return dataSets;
    }
    
    public List<TechId> getSampleIdsByExperimentIds(Collection<TechId> experimentIds)
    {
        return getRelatedIds(experimentIds, experiments, new EntityNodeGenerator.IEntitiesProvider<ExperimentNode>()
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
        return getRelatedIds(containerIds, samples, new EntityNodeGenerator.IEntitiesProvider<SampleNode>()
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
        return getRelatedIds(experimentIds, experiments, new EntityNodeGenerator.IEntitiesProvider<ExperimentNode>()
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
        return getRelatedIds(experimentIds, samples, new EntityNodeGenerator.IEntitiesProvider<SampleNode>()
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
        return getRelatedIds(dataSetIds, dataSets, new EntityNodeGenerator.IEntitiesProvider<DataSetNode>()
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
        return getRelatedIds(dataSetIds, dataSets, new EntityNodeGenerator.IEntitiesProvider<DataSetNode>()
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
        return getRelatedIdsMap(dataSetIds, dataSets,new EntityNodeGenerator.IEntitiesProvider<DataSetNode>()
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
        return getRelatedIdsMap(dataSetIds, dataSets,new EntityNodeGenerator.IEntitiesProvider<DataSetNode>()
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
            EntityNodeGenerator.IEntitiesProvider<T> provider)
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
            EntityNodeGenerator.IEntitiesProvider<T> provider)
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