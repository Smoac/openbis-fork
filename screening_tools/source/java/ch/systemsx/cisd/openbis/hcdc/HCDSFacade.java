package ch.systemsx.cisd.openbis.hcdc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.IScreeningServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateSingleImageReference;

/**
 * A facade of openBIS API for KNIME-HCDC integration.
 * 
 * @author Tomasz Pylak
 */
public class HCDSFacade
{
    private static final int SERVER_TIMEOUT_MIN = 5;

    private final ICommonServer commonServer;

    private final IScreeningServer screeningServer;

    private final String sessionToken;

    public HCDSFacade(String userId, String userPassword, String serverUrl)
    {
        this.commonServer = getOpenbisServer(serverUrl);
        this.screeningServer = getScreeningServer(serverUrl);
        this.sessionToken = authenticate(commonServer, userId, userPassword);
    }

    public List<PlateSingleImageReference> listImagePathsForPlate(long plateId)
    {
        List<PlateSingleImageReference> images =
                screeningServer.listPlateImages(sessionToken, new TechId(plateId));
        Collections.sort(images, new Comparator<PlateSingleImageReference>()
            {
                public int compare(PlateSingleImageReference o1, PlateSingleImageReference o2)
                {
                    return o1.getImagePath().compareTo(o2.getImagePath());
                }
            });
        return images;
    }

    public TabularData listImageAnalysisResultsForExp(long experimentId)
    {
        TableModel tableModel =
                screeningServer.loadImageAnalysisForExperiment(sessionToken, new TechId(
                        experimentId));
        return createTable(tableModel);
    }

    private TabularData createTable(TableModel tableModel)
    {
        String[] header = convert(tableModel.getHeader());
        String[][] rows = convert(tableModel.getRows());
        boolean[] isNumeric = extractIsNumeric(tableModel.getHeader());
        return new TabularData(header, rows, isNumeric);
    }

    private boolean[] extractIsNumeric(List<TableModelColumnHeader> header)
    {
        boolean[] isNumeric = new boolean[header.size()];
        for (int i = 0; i < isNumeric.length; i++)
        {
            isNumeric[i] = header.get(i).isNumeric();
        }
        return isNumeric;
    }

    private String[][] convert(List<TableModelRow> rows)
    {
        List<String[]> converterRows = new ArrayList<String[]>();
        for (TableModelRow row : rows)
        {
            converterRows.add(convert(row));
        }
        String[][] result = new String[converterRows.size()][];
        for (int i = 0; i < converterRows.size(); i++)
        {
            result[i] = converterRows.get(i);
        }
        return result;
    }

    private String[] convert(TableModelRow row)
    {
        List<ISerializableComparable> values = row.getValues();
        String[] result = new String[values.size()];
        for (int i = 0; i < result.length; i++)
        {
            result[i] = values.get(i).toString();
        }
        return result;
    }

    private String[] convert(List<TableModelColumnHeader> headers)
    {
        String[] result = new String[headers.size()];
        for (int i = 0; i < result.length; i++)
        {
            result[i] = headers.get(i).getTitle();
        }
        return result;
    }

    public TabularData listImageAnalysisResultsForPlate(long plateId)
    {
        TableModel tableModel =
                screeningServer.loadImageAnalysisForPlate(sessionToken, new TechId(plateId));
        return createTable(tableModel);
    }

    public List<EntityReference> listExperimentsIdentifiers()
    {
        List<Project> projects = commonServer.listProjects(sessionToken);
        List<Experiment> allExperiments = new ArrayList<Experiment>();
        for (Project project : projects)
        {
            ProjectIdentifier identifier = createProjectIdentifier(project);
            List<Experiment> experiments =
                    commonServer.listExperiments(sessionToken, getScreeningExperimentType(),
                            identifier);
            allExperiments.addAll(experiments);
        }
        return asReferences(allExperiments);
    }

    private List<EntityReference> asReferences(List<Experiment> experiments)
    {
        List<EntityReference> result = new ArrayList<EntityReference>();
        for (Experiment experiment : experiments)
        {
            result.add(new EntityReference(experiment.getId(), experiment.getIdentifier()));
        }
        return result;
    }

    private static ProjectIdentifier createProjectIdentifier(Project project)
    {
        return new ProjectIdentifier(project.getGroup().getCode(), project.getCode());
    }

    private static ExperimentType getScreeningExperimentType()
    {
        ExperimentType type = new ExperimentType();
        type.setCode("SIRNA_HCS");
        return type;
    }

    public List<EntityReference> listPlateIdentifiers(EntityReference experimentReference)
    {
        List<Sample> samples =
                commonServer.listSamples(sessionToken, ListSampleCriteria
                        .createForExperiment(experimentReference.getId()));
        List<EntityReference> result = new ArrayList<EntityReference>();
        for (Sample sample : samples)
        {
            result.add(new EntityReference(sample.getId(), sample.getIdentifier()));
        }
        return result;
    }

    private static String authenticate(ICommonServer commonServer, String userId,
            String userPassword)
    {
        SessionContextDTO session = commonServer.tryToAuthenticate(userId, userPassword);
        String sessionToken = session.getSessionToken();
        return sessionToken;
    }

    private static ICommonServer getOpenbisServer(String serverUrl)
    {
        return HttpInvokerUtils.createServiceStub(ICommonServer.class, serverUrl + "/rmi-common",
                SERVER_TIMEOUT_MIN);
    }

    private static IScreeningServer getScreeningServer(String serverUrl)
    {
        return HttpInvokerUtils.createServiceStub(IScreeningServer.class, serverUrl
                + "/rmi-screening", SERVER_TIMEOUT_MIN);
    }

}
