package ch.systemsx.cisd.openbis.generic.server.task.events_search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.history.ExperimentRelationType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.HistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.IRelationType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.RelationHistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.id.UnknownRelatedObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.history.ProjectRelationType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.history.SampleRelationType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

class SnapshotsFacade
{

    private final IDataSource dataSource;

    private final Set<String> loadedSpaceCodes = new HashSet<>();

    private final Set<String> loadedProjectPermIds = new HashSet<>();

    private final Set<String> loadedExperimentPermIds = new HashSet<>();

    private final Set<String> loadedSamplePermIds = new HashSet<>();

    private final Snapshots spaceSnapshots;

    private final Snapshots projectSnapshots;

    private final Snapshots experimentSnapshots;

    private final Snapshots sampleSnapshots;

    private final Snapshots dataSetSnapshots;

    public SnapshotsFacade(IDataSource dataSource)
    {
        this.dataSource = dataSource;
        this.spaceSnapshots = new Snapshots();
        this.projectSnapshots = new Snapshots();
        this.experimentSnapshots = new Snapshots();
        this.sampleSnapshots = new Snapshots();
        this.dataSetSnapshots = new Snapshots();
    }

    private static Set<String> addToLoaded(Set<String> loaded, Collection<String> candidatesToLoad)
    {
        Set<String> toLoad = new HashSet<>(candidatesToLoad);
        toLoad.removeAll(loaded);
        loaded.addAll(toLoad);
        return toLoad;
    }

    public void loadExistingSpaces(Collection<String> spaceCodes)
    {
        Set<String> toLoad = addToLoaded(loadedSpaceCodes, spaceCodes);

        if (toLoad.isEmpty())
        {
            return;
        }

        List<SpacePE> spaces = dataSource.loadSpaces(new ArrayList<>(toLoad));

        for (SpacePE space : spaces)
        {
            Snapshot snapshot = new Snapshot();
            snapshot.from = space.getRegistrationDateInternal();
            snapshot.entityCode = space.getCode();
            snapshot.entityPermId = space.getId().toString();

            spaceSnapshots.put(snapshot.entityCode, snapshot);
        }
    }

    public void loadExistingProjects(Collection<String> projectPermIds)
    {
        Set<String> toLoad = addToLoaded(loadedProjectPermIds, projectPermIds);

        if (toLoad.isEmpty())
        {
            return;
        }

        List<Snapshot> snapshots = new LinkedList<>();

        ProjectFetchOptions fo = new ProjectFetchOptions();
        fo.withSpaceHistory();
        fo.withUnknownHistory();

        List<IProjectId> ids = toLoad.stream().map(ProjectPermId::new).collect(Collectors.toList());
        List<Project> projects = dataSource.loadProjects(ids, fo);

        for (Project project : projects)
        {
            List<HistoryEntry> history = new LinkedList<>();
            history.addAll(project.getSpaceHistory() != null ? project.getSpaceHistory() : Collections.emptyList());
            history.addAll(project.getUnknownHistory() != null ? project.getUnknownHistory() : Collections.emptyList());

            for (HistoryEntry historyEntry : history)
            {
                if (historyEntry instanceof RelationHistoryEntry)
                {
                    RelationHistoryEntry relationHistoryEntry = (RelationHistoryEntry) historyEntry;

                    if (ProjectRelationType.SPACE.equals(relationHistoryEntry.getRelationType()) || (relationHistoryEntry.getRelationType() == null
                            && relationHistoryEntry.getRelatedObjectId() != null))
                    {
                        Snapshot snapshot = new Snapshot();
                        snapshot.entityCode = project.getCode();
                        snapshot.entityPermId = project.getPermId().getPermId();
                        snapshot.from = relationHistoryEntry.getValidFrom();
                        snapshot.to = relationHistoryEntry.getValidTo();

                        if (ProjectRelationType.SPACE.equals(relationHistoryEntry.getRelationType()))
                        {
                            snapshot.spaceCode = ((SpacePermId) relationHistoryEntry.getRelatedObjectId()).getPermId();
                        } else
                        {
                            UnknownRelatedObjectId unknownObjectId = (UnknownRelatedObjectId) relationHistoryEntry.getRelatedObjectId();

                            if ("OWNED".equals(unknownObjectId.getRelationType()))
                            {
                                snapshot.unknownPermId = unknownObjectId.getRelatedObjectId();
                            } else
                            {
                                continue;
                            }
                        }

                        snapshots.add(snapshot);
                    }
                }
            }
        }

        putProjects(snapshots);
    }

    public void loadExistingExperiments(Collection<String> experimentPermIds)
    {
        Set<String> toLoad = addToLoaded(loadedExperimentPermIds, experimentPermIds);

        if (toLoad.isEmpty())
        {
            return;
        }

        List<Snapshot> snapshots = new LinkedList<>();

        ExperimentFetchOptions fo = new ExperimentFetchOptions();
        fo.withProjectHistory();
        fo.withUnknownHistory();

        List<IExperimentId> ids = toLoad.stream().map(ExperimentPermId::new).collect(Collectors.toList());
        List<Experiment> experiments = dataSource.loadExperiments(ids, fo);

        for (Experiment experiment : experiments)
        {
            List<HistoryEntry> history = new LinkedList<>();
            history.addAll(experiment.getProjectHistory() != null ? experiment.getProjectHistory() : Collections.emptyList());
            history.addAll(experiment.getUnknownHistory() != null ? experiment.getUnknownHistory() : Collections.emptyList());

            for (HistoryEntry historyEntry : history)
            {
                if (historyEntry instanceof RelationHistoryEntry)
                {
                    RelationHistoryEntry relationHistoryEntry = (RelationHistoryEntry) historyEntry;

                    if (ExperimentRelationType.PROJECT.equals(relationHistoryEntry.getRelationType())
                            || (relationHistoryEntry.getRelationType() == null && relationHistoryEntry.getRelatedObjectId() != null))
                    {
                        Snapshot snapshot = new Snapshot();
                        snapshot.entityCode = experiment.getCode();
                        snapshot.entityPermId = experiment.getPermId().getPermId();
                        snapshot.from = relationHistoryEntry.getValidFrom();
                        snapshot.to = relationHistoryEntry.getValidTo();

                        if (ExperimentRelationType.PROJECT.equals(relationHistoryEntry.getRelationType()))
                        {
                            snapshot.projectPermId = ((ProjectPermId) relationHistoryEntry.getRelatedObjectId()).getPermId();
                        } else
                        {
                            UnknownRelatedObjectId unknownObjectId = (UnknownRelatedObjectId) relationHistoryEntry.getRelatedObjectId();

                            if ("OWNED".equals(unknownObjectId.getRelationType()))
                            {
                                snapshot.unknownPermId = unknownObjectId.getRelatedObjectId();
                            } else
                            {
                                continue;
                            }
                        }

                        snapshots.add(snapshot);
                    }
                }
            }
        }

        putExperiments(snapshots);
    }

    public void loadExistingSamples(Collection<String> samplePermIds)
    {
        Set<String> toLoad = addToLoaded(loadedSamplePermIds, samplePermIds);

        if (toLoad.isEmpty())
        {
            return;
        }

        List<Snapshot> snapshots = new LinkedList<>();

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withSpaceHistory();
        fo.withProjectHistory();
        fo.withExperimentHistory();
        fo.withUnknownHistory();

        List<ISampleId> ids = toLoad.stream().map(SamplePermId::new).collect(Collectors.toList());
        List<Sample> samples = dataSource.loadSamples(ids, fo);

        for (Sample sample : samples)
        {
            List<HistoryEntry> history = new LinkedList<>();
            history.addAll(sample.getSpaceHistory() != null ? sample.getSpaceHistory() : Collections.emptyList());
            history.addAll(sample.getProjectHistory() != null ? sample.getProjectHistory() : Collections.emptyList());
            history.addAll(sample.getExperimentHistory() != null ? sample.getExperimentHistory() : Collections.emptyList());
            history.addAll(sample.getUnknownHistory() != null ? sample.getUnknownHistory() : Collections.emptyList());

            for (HistoryEntry historyEntry : history)
            {
                if (historyEntry instanceof RelationHistoryEntry)
                {
                    RelationHistoryEntry relationHistoryEntry = (RelationHistoryEntry) historyEntry;
                    IRelationType relationType = relationHistoryEntry.getRelationType();
                    IObjectId relatedObjectId = relationHistoryEntry.getRelatedObjectId();

                    if (SampleRelationType.SPACE.equals(relationType) || SampleRelationType.PROJECT.equals(relationType)
                            || SampleRelationType.EXPERIMENT.equals(relationType) || (relationType == null && relatedObjectId != null))
                    {
                        Snapshot snapshot = new Snapshot();
                        snapshot.entityCode = sample.getCode();
                        snapshot.entityPermId = sample.getPermId().getPermId();
                        snapshot.from = relationHistoryEntry.getValidFrom();
                        snapshot.to = relationHistoryEntry.getValidTo();

                        if (SampleRelationType.SPACE.equals(relationType))
                        {
                            snapshot.spaceCode = ((SpacePermId) relationHistoryEntry.getRelatedObjectId()).getPermId();
                        } else if (SampleRelationType.PROJECT.equals(relationType))
                        {
                            snapshot.projectPermId = ((ProjectPermId) relationHistoryEntry.getRelatedObjectId()).getPermId();
                        } else if (SampleRelationType.EXPERIMENT.equals(relationType))
                        {
                            snapshot.experimentPermId = ((ExperimentPermId) relationHistoryEntry.getRelatedObjectId()).getPermId();
                        } else
                        {
                            UnknownRelatedObjectId unknownObjectId = (UnknownRelatedObjectId) relationHistoryEntry.getRelatedObjectId();

                            if ("OWNED".equals(unknownObjectId.getRelationType()))
                            {
                                snapshot.unknownPermId = unknownObjectId.getRelatedObjectId();
                            } else
                            {
                                continue;
                            }
                        }

                        snapshots.add(snapshot);
                    }
                }
            }
        }

        putSamples(snapshots);
    }

    public void putSpaces(List<Snapshot> snapshots)
    {
        for (Snapshot snapshot : snapshots)
        {
            spaceSnapshots.put(snapshot.entityCode, snapshot);
        }
    }

    public void putProjects(List<Snapshot> snapshots)
    {
        loadExistingSpaces(getSpaceCodesOrUnknown(snapshots));

        List<Snapshot> correctSnapshots = new LinkedList<>();

        for (Snapshot snapshot : snapshots)
        {
            if (snapshot.unknownPermId == null)
            {
                correctSnapshots.add(snapshot);
            } else
            {
                Snapshot spaceSnapshot = getSpace(snapshot.unknownPermId, snapshot.from);
                if (spaceSnapshot != null)
                {
                    snapshot.spaceCode = snapshot.unknownPermId;
                    correctSnapshots.add(snapshot);
                }
            }
        }

        for (Snapshot correctSnapshot : correctSnapshots)
        {
            projectSnapshots.put(correctSnapshot.entityPermId, correctSnapshot);
        }
    }

    public void putExperiments(List<Snapshot> snapshots)
    {
        loadExistingProjects(getProjectPermIdsOrUnknown(snapshots));

        List<Snapshot> correctSnapshots = new LinkedList<>();

        for (Snapshot snapshot : snapshots)
        {
            if (snapshot.unknownPermId == null)
            {
                correctSnapshots.add(snapshot);
            } else
            {
                Snapshot projectSnapshot = getProject(snapshot.unknownPermId, snapshot.from);
                if (projectSnapshot != null)
                {
                    snapshot.projectPermId = snapshot.unknownPermId;
                    correctSnapshots.add(snapshot);
                }
            }
        }

        for (Snapshot correctSnapshot : correctSnapshots)
        {
            experimentSnapshots.put(correctSnapshot.entityPermId, correctSnapshot);
        }
    }

    public void putSamples(List<Snapshot> snapshots)
    {
        loadExistingSpaces(getSpaceCodesOrUnknown(snapshots));
        loadExistingProjects(getProjectPermIdsOrUnknown(snapshots));
        loadExistingExperiments(getExperimentPermIdsOrUnknown(snapshots));

        List<Snapshot> correctSnapshots = new LinkedList<>();

        for (Snapshot snapshot : snapshots)
        {
            if (snapshot.unknownPermId == null)
            {
                correctSnapshots.add(snapshot);
            } else
            {
                Snapshot experimentSnapshot = getExperiment(snapshot.unknownPermId, snapshot.from);
                if (experimentSnapshot != null)
                {
                    snapshot.experimentPermId = snapshot.unknownPermId;
                    correctSnapshots.add(snapshot);
                } else
                {
                    Snapshot projectSnapshot = getProject(snapshot.unknownPermId, snapshot.from);
                    if (projectSnapshot != null)
                    {
                        snapshot.projectPermId = snapshot.unknownPermId;
                        correctSnapshots.add(snapshot);
                    } else
                    {
                        Snapshot spaceSnapshot = getSpace(snapshot.unknownPermId, snapshot.from);
                        if (spaceSnapshot != null)
                        {
                            snapshot.spaceCode = snapshot.unknownPermId;
                            correctSnapshots.add(snapshot);
                        }
                    }
                }
            }
        }

        for (Snapshot correctSnapshot : correctSnapshots)
        {
            sampleSnapshots.put(correctSnapshot.entityPermId, correctSnapshot);
        }
    }

    public void putDataSets(List<Snapshot> snapshots)
    {
        loadExistingExperiments(getExperimentPermIdsOrUnknown(snapshots));
        loadExistingSamples(getSamplePermIdsOrUnknown(snapshots));

        List<Snapshot> correctSnapshots = new LinkedList<>();

        for (Snapshot snapshot : snapshots)
        {
            if (snapshot.unknownPermId == null)
            {
                correctSnapshots.add(snapshot);
            } else
            {
                Snapshot experimentSnapshot = getExperiment(snapshot.unknownPermId, snapshot.from);
                if (experimentSnapshot != null)
                {
                    snapshot.experimentPermId = snapshot.unknownPermId;
                    correctSnapshots.add(snapshot);
                } else
                {
                    Snapshot sampleSnapshot = getSample(snapshot.unknownPermId, snapshot.from);
                    if (sampleSnapshot != null)
                    {
                        snapshot.samplePermId = snapshot.unknownPermId;
                        correctSnapshots.add(snapshot);
                    }
                }
            }
        }

        for (Snapshot correctSnapshot : correctSnapshots)
        {
            dataSetSnapshots.put(correctSnapshot.entityPermId, correctSnapshot);
        }
    }

    public Snapshot getSpace(String spaceCode, Date date)
    {
        return spaceSnapshots.get(spaceCode, date);
    }

    public Snapshot getProject(String projectPermId, Date date)
    {
        return projectSnapshots.get(projectPermId, date);
    }

    public Snapshot getExperiment(String experimentPermId, Date date)
    {
        return experimentSnapshots.get(experimentPermId, date);
    }

    public Snapshot getSample(String samplePermId, Date date)
    {
        return sampleSnapshots.get(samplePermId, date);
    }

    public void fillBySpaceCode(String spaceCode, NewEvent newEvent)
    {
        Snapshot spaceSnapshot = spaceSnapshots.get(spaceCode, newEvent.registrationTimestamp);

        if (spaceSnapshot != null)
        {
            newEvent.entitySpaceCode = spaceSnapshot.entityCode;
            newEvent.entitySpacePermId = spaceSnapshot.entityPermId;
        } else
        {
            newEvent.entitySpaceCode = spaceCode;
        }
    }

    public void fillByProjectPermId(String projectPermId, NewEvent newEvent)
    {
        Snapshot projectSnapshot = projectSnapshots.get(projectPermId, newEvent.registrationTimestamp);

        if (projectSnapshot != null)
        {
            newEvent.entityProjectPermId = projectPermId;

            fillBySpaceCode(projectSnapshot.spaceCode, newEvent);

            if (newEvent.entitySpaceCode != null)
            {
                newEvent.entityProject = new ProjectIdentifier(newEvent.entitySpaceCode, projectSnapshot.entityCode).toString();
            }
        } else
        {
            newEvent.entityProjectPermId = projectPermId;
        }
    }

    public void fillByExperimentPermId(String experimentPermId, NewEvent newEvent)
    {
        Snapshot experimentSnapshot = experimentSnapshots.get(experimentPermId, newEvent.registrationTimestamp);

        if (experimentSnapshot != null)
        {
            fillByProjectPermId(experimentSnapshot.projectPermId, newEvent);
        }
    }

    public void fillBySamplePermId(String samplePermId, NewEvent newEvent)
    {
        Snapshot sampleSnapshot = sampleSnapshots.get(samplePermId, newEvent.registrationTimestamp);

        if (sampleSnapshot != null)
        {
            if (sampleSnapshot.experimentPermId != null)
            {
                fillByExperimentPermId(sampleSnapshot.experimentPermId, newEvent);
            } else if (sampleSnapshot.projectPermId != null)
            {
                fillByProjectPermId(sampleSnapshot.projectPermId, newEvent);
            } else if (sampleSnapshot.spaceCode != null)
            {
                fillBySpaceCode(sampleSnapshot.spaceCode, newEvent);
            }
        }
    }

    public void fillByDataSetPermId(String dataSetPermId, NewEvent newEvent)
    {
        Snapshot dataSetSnapshot = dataSetSnapshots.get(dataSetPermId, newEvent.registrationTimestamp);

        if (dataSetSnapshot != null)
        {
            if (dataSetSnapshot.experimentPermId != null)
            {
                fillByExperimentPermId(dataSetSnapshot.experimentPermId, newEvent);
            } else if (dataSetSnapshot.samplePermId != null)
            {
                fillBySamplePermId(dataSetSnapshot.samplePermId, newEvent);
            }
        }
    }

    public static Set<String> getSpaceCodesOrUnknown(Collection<Snapshot> snapshots)
    {
        Set<String> result = new HashSet<>();
        for (Snapshot snapshot : snapshots)
        {
            if (snapshot.spaceCode != null)
            {
                result.add(snapshot.spaceCode);
            } else if (snapshot.unknownPermId != null)
            {
                result.add(snapshot.unknownPermId);
            }
        }
        return result;
    }

    public static Set<String> getProjectPermIdsOrUnknown(Collection<Snapshot> snapshots)
    {
        Set<String> result = new HashSet<>();
        for (Snapshot snapshot : snapshots)
        {
            if (snapshot.projectPermId != null)
            {
                result.add(snapshot.projectPermId);
            } else if (snapshot.unknownPermId != null)
            {
                result.add(snapshot.unknownPermId);
            }
        }
        return result;
    }

    public static Set<String> getExperimentPermIdsOrUnknown(Collection<Snapshot> snapshots)
    {
        Set<String> result = new HashSet<>();
        for (Snapshot snapshot : snapshots)
        {
            if (snapshot.experimentPermId != null)
            {
                result.add(snapshot.experimentPermId);
            } else if (snapshot.unknownPermId != null)
            {
                result.add(snapshot.unknownPermId);
            }
        }
        return result;
    }

    public static Set<String> getSamplePermIdsOrUnknown(Collection<Snapshot> snapshots)
    {
        Set<String> result = new HashSet<>();
        for (Snapshot snapshot : snapshots)
        {
            if (snapshot.samplePermId != null)
            {
                result.add(snapshot.samplePermId);
            } else if (snapshot.unknownPermId != null)
            {
                result.add(snapshot.unknownPermId);
            }
        }
        return result;
    }

}