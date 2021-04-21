package ch.systemsx.cisd.openbis.generic.server.task.events_search;

import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

class ExperimentDeletionProcessor extends DeletionEventProcessor
{
    ExperimentDeletionProcessor(IDataSource dataSource)
    {
        super(dataSource);
    }

    @Override protected EntityType getEntityType()
    {
        return EntityType.EXPERIMENT;
    }

    @Override protected Set<EntityType> getAscendantEntityTypes()
    {
        return EnumSet.of(EntityType.PROJECT);
    }

    @Override protected Set<EntityType> getDescendantEntityTypes()
    {
        return EnumSet.of(EntityType.SAMPLE, EntityType.DATASET);
    }

    @Override protected void processDeletions(LastTimestamps lastTimestamps, Snapshots snapshots, List<NewEvent> newEvents, List<Snapshot> newSnapshots)
    {
        snapshots.loadExistingProjects(Snapshot.getProjectPermIdsOrUnknown(newSnapshots));

        for (Snapshot newSnapshot : newSnapshots)
        {
            if (newSnapshot.unknownPermId == null)
            {
                snapshots.putDeletedExperiment(newSnapshot);
            } else
            {
                Snapshot projectSnapshot = snapshots.getProject(newSnapshot.unknownPermId, newSnapshot.from);
                if (projectSnapshot != null)
                {
                    newSnapshot.projectPermId = newSnapshot.unknownPermId;
                    snapshots.putDeletedExperiment(newSnapshot);
                }
            }
        }

        for (NewEvent newEvent : newEvents)
        {
            try
            {
                snapshots.fillByExperimentPermId(newEvent.identifier, newEvent);
                dataSource.createEventsSearch(newEvent.toNewEventPE());

            } catch (Exception e)
            {
                throw new RuntimeException(String.format("Processing of deletion failed: %s", newEvent), e);
            }
        }
    }
}