package ch.ethz.sis.openbis.generic.server.dss.plugins.test;

import java.io.File;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;

public class EntityDeleter
{
    private static final String URL = "https://localhost:8445/openbis/openbis" + IApplicationServerApi.SERVICE_URL;

    public static void main(String[] args)
    {
        File dir =
                new File(
                        "/Users/gakin/Documents/workspace_openbis_trunk/integration-tests/targets/playground/test_openbis_sync/openbis2/data/store/harvester-tmp",
                        "20161010125005326-5");
        dir.mkdirs();

        for (File f : dir.listFiles())
        {
            System.out.println(f.getAbsolutePath());
        }
        // IApplicationServerApi v3 = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, URL, 10000);
        // String sessionToken = v3.login("admin", "admin");
        //
        //
        // SampleTypeSearchCriteria searchCriteria = new SampleTypeSearchCriteria();
        // searchCriteria.withCode().thatEquals("TST");
        // SampleTypeFetchOptions fetchOptions = new SampleTypeFetchOptions();
        // fetchOptions.withPropertyAssignments();
        // SearchResult<SampleType> searchResult = v3.searchSampleTypes(sessionToken, searchCriteria, fetchOptions);
        // List<SampleType> objects = searchResult.getObjects();
        // for (SampleType sampleType : objects)
        // {
        // List<PropertyAssignment> propertyAssignments = sampleType.getPropertyAssignments();
        // for (PropertyAssignment propertyAssignment : propertyAssignments)
        // {
        // System.out.println("section:" + propertyAssignment.getSection() + "ordinal:" + propertyAssignment.getOrdinal());
        // }
        // }

        // SampleDeletionOptions deletionOptions = new SampleDeletionOptions();
        // deletionOptions.setReason("Delete samples");
        //
        // System.out.println("Delete samples");
        // // logical deletion (move objects to the trash can)
        // IDeletionId smpDeletionId = v3.deleteSamples(sessionToken, Collections.<SamplePermId> emptyList(), deletionOptions);
        // // v3.confirmDeletions(sessionToken, Arrays.asList(smpDeletionId));
        //
        //
        // v3.confirmDeletions(sessionToken, Arrays.asList(smpDeletionId));
        //
        // // delete projects
        // IProjectId prjId = new ProjectIdentifier("DST", "DEFAULT");
        //
        // ProjectDeletionOptions prjDeletionOpts = new ProjectDeletionOptions();
        // prjDeletionOpts.setReason("Delete projects");
        //
        // System.out.println("Delete projects");
        // v3.deleteProjects(sessionToken, Arrays.asList(prjId), prjDeletionOpts);
        //
        // System.out.println("Done and dusted...");
    }
}
