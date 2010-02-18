package ch.systemsx.cisd.openbis.hcdc;

import java.util.List;

import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateSingleImageReference;

/**
 * Demo of the openBIS client which uses simple fasade object to connect to the server.
 * 
 * @author Tomasz Pylak
 */
public class OpenbisConnectionDemo
{
    private static final String USER_ID = "hcdc_observer";

    private static final String USER_PASSWORD = "lmc-openbis";

    private static final String SERVER_URL = "https://onlyweb1.ethz.ch:8443/openbis/openbis";

    public static void main(String[] args)
    {
        HCDSFasade client = new HCDSFasade(USER_ID, USER_PASSWORD, SERVER_URL);

        List<EntityReference> experimentsIdentifiers = client.listExperimentsIdentifiers();
        System.out.println("experiments: " + experimentsIdentifiers);

        for (EntityReference experiment : experimentsIdentifiers)
        {
            if (experiment.getIdentifier().contains("KUTAY"))
            {
                List<EntityReference> plateIdentifiers = client.listPlateIdentifiers(experiment);
                System.out.println("Plates in " + experiment + ": " + plateIdentifiers);

                for (EntityReference plate : plateIdentifiers)
                {
                    if (plate.getIdentifier().contains("H001-1A"))
                    {
                        Long plateId = plate.getId().getId();
                        TsvTable table = client.listImageAnalysisResultsForPlate(plateId);
                        System.out.println("Image analysis results: " + table);
                        System.out.println("Fetching images...");
                        List<PlateSingleImageReference> images =
                                client.listImagePathsForPlate(plateId);
                        System.out.println("Images in " + plate + ": " + toString(images));
                    }
                }
            }
        }
    }

    private static String toString(List<PlateSingleImageReference> images)
    {
        String text = "";
        for (PlateSingleImageReference image : images)
        {
            text += image.getImagePath() + "\n";
        }
        return text;
    }
}
