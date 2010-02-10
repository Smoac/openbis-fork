/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.server;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericTableResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.client.web.server.AbstractClientService;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.UserFailureExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericTableRow;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.plugin.screening.BuildAndEnvironmentInfo;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientService;
import ch.systemsx.cisd.openbis.plugin.screening.shared.IScreeningServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.ResourceNames;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImages;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellContent;

/**
 * The {@link IScreeningClientService} implementation.
 * 
 * @author Tomasz Pylak
 */
@Component(value = ResourceNames.SCREENING_PLUGIN_SERVICE)
public final class ScreeningClientService extends AbstractClientService implements
        IScreeningClientService
{

    @Resource(name = ResourceNames.SCREENING_PLUGIN_SERVER)
    private IScreeningServer server;

    public ScreeningClientService()
    {
    }

    @Private
    ScreeningClientService(final IScreeningServer server,
            final IRequestContextProvider requestContextProvider)
    {
        super(requestContextProvider);
        this.server = server;
    }

    //
    // AbstractClientService
    //

    @Override
    protected final IServer getServer()
    {
        return server;
    }

    //
    // IScreeningClientService
    //

    @Override
    protected String getVersion()
    {
        return BuildAndEnvironmentInfo.INSTANCE.getFullVersion();
    }

    public final SampleParentWithDerived getSampleGenerationInfo(final TechId sampleId)
            throws UserFailureException
    {
        try
        {
            return server.getSampleInfo(getSessionToken(), sampleId);
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final Material getMaterialInfo(final TechId materialId)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            return server.getMaterialInfo(getSessionToken(), materialId);
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public ExternalData getDataSetInfo(TechId datasetTechId)
    {
        try
        {
            return server.getDataSetInfo(getSessionToken(), datasetTechId);
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public PlateContent getPlateContent(TechId plateId) throws UserFailureException
    {
        try
        {
            return server.getPlateContent(getSessionToken(), plateId);
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public PlateImages getPlateContentForDataset(TechId datasetId)
    {
        try
        {
            return server.getPlateContentForDataset(getSessionToken(), datasetId);
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public List<WellContent> getPlateLocations(TechId geneMaterialId,
            ExperimentIdentifier experimentIdentifier) throws UserFailureException
    {
        try
        {
            return server.getPlateLocations(getSessionToken(), geneMaterialId,
                    parseExperimentIdentifier(experimentIdentifier));
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    private static ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier parseExperimentIdentifier(
            ExperimentIdentifier experimentIdentifier)
    {
        return new ExperimentIdentifierFactory(experimentIdentifier.getIdentifier())
                .createIdentifier();
    }

    public GenericTableResultSet listPlateMetadata(
            IResultSetConfig<String, GenericTableRow> criteria, TechId sampleId)
            throws UserFailureException
    {
        PlateMetadataProvider dataProvider =
                new PlateMetadataProvider(server, getSessionToken(), sampleId);
        ResultSet<GenericTableRow> resultSet = listEntities(criteria, dataProvider);
        return new GenericTableResultSet(resultSet, dataProvider.getHeaders());
    }

    public String prepareExportPlateMetadata(TableExportCriteria<GenericTableRow> criteria)
            throws UserFailureException
    {
        return prepareExportEntities(criteria);
    }
}
