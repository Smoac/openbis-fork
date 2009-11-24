/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.ethz.bsse.cisd.dsu.tracking.main;

import java.util.List;

import ch.ethz.bsse.cisd.dsu.tracking.Email;
import ch.ethz.bsse.cisd.dsu.tracking.IEntityTrackingEmailGenerator;
import ch.ethz.bsse.cisd.dsu.tracking.TrackedEntities;
import ch.systemsx.cisd.common.mail.From;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.openbis.generic.shared.ITrackingServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifiable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

/**
 * @author Tomasz Pylak
 */
public class TrackingBO
{
    private static final String SEQUENCING_SAMPLE_TYPE = "ILLUMINA_SEQUENCING";

    private static final String FLOW_LANE_SAMPLE_TYPE = "FLOW_LANE";

    private final ITrackingServer trackingServer;

    private final IEntityTrackingEmailGenerator emailGenerator;

    private final IMailClient mailClient;

    public TrackingBO(ITrackingServer trackingServer, IEntityTrackingEmailGenerator emailGenerator,
            IMailClient mailClient)
    {
        this.trackingServer = trackingServer;
        this.emailGenerator = emailGenerator;
        this.mailClient = mailClient;
    }

    public void trackAndNotify(ITrackingDAO trackingDAO, SessionContextDTO session)
    {
        TrackingStateDTO prevTrackingState = trackingDAO.getTrackingState();

        TrackedEntities changedEntities =
                fetchChangedEntities(prevTrackingState, trackingServer, session);
        List<Email> emails = emailGenerator.generateEmails(changedEntities);
        sendEmails(emails, mailClient);
        saveTrackingState(changedEntities, trackingDAO);
    }

    private static void sendEmails(List<Email> emails, IMailClient mailClient)
    {
        for (Email email : emails)
        {
            From from = (email.getFromOrNull() == null) ? null : new From(email.getFromOrNull());
            mailClient.sendMessage(email.getSubject(), email.getContent(),
                    email.getReplyToOrNull(), from, email.getRecipients());
        }
    }

    private static void saveTrackingState(TrackedEntities changedEntities, ITrackingDAO trackingDAO)
    {
        TrackingStateDTO state = new TrackingStateDTO();
        state.setLastSeenSequencingSampleId(calcMaxId(changedEntities.getSequencingSamples()));
        state.setLastSeenFlowLaneSampleId(calcMaxId(changedEntities.getFlowLaneSamples()));
        state.setLastSeenDatasetId(calcMaxId(changedEntities.getDataSets()));
        trackingDAO.saveTrackingState(state);
    }

    private static int calcMaxId(List<? extends IIdentifiable> entities)
    {
        long max = 0;
        for (IIdentifiable entity : entities)
        {
            max = Math.max(max, entity.getId());
        }
        // FIXME 2009--, Tomasz Pylak: refactor ids to long everywhere
        return (int) max;
    }

    private static TrackedEntities fetchChangedEntities(TrackingStateDTO trackingState,
            ITrackingServer trackingServer, SessionContextDTO session)
    {
        List<Sample> sequencingSamples =
                listSamples(SEQUENCING_SAMPLE_TYPE, trackingState.getLastSeenSequencingSampleId(),
                        trackingServer, session);

        List<Sample> flowLaneSamples =
                listSamples(FLOW_LANE_SAMPLE_TYPE, trackingState.getLastSeenFlowLaneSampleId(),
                        trackingServer, session);

        TrackingDataSetCriteria dataSetCriteria =
                new TrackingDataSetCriteria(FLOW_LANE_SAMPLE_TYPE, trackingState
                        .getLastSeenDatasetId());
        List<ExternalData> dataSets =
                trackingServer.listDataSets(session.getSessionToken(), dataSetCriteria);

        return new TrackedEntities(sequencingSamples, flowLaneSamples, dataSets);
    }

    private static List<Sample> listSamples(String sampleType, int lastSeenSampleId,
            ITrackingServer trackingServer, SessionContextDTO session)
    {
        TrackingSampleCriteria criteria = new TrackingSampleCriteria(sampleType, lastSeenSampleId);
        return trackingServer.listSamples(session.getSessionToken(), criteria);
    }
}
