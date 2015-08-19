package ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sort;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.material.Material;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.person.Person;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.project.Project;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.space.Space;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.tag.Tag;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.material.MaterialFetchOptions;

public class SortAndPageTest
{
    @Test
    public void testSortAndPageTopLevel()
    {
        MaterialFetchOptions fo = new MaterialFetchOptions();
        fo.sortBy().code().desc();
        fo.from(1).count(2);

        Material m1 = new Material();
        m1.setCode("S1");
        m1.setFetchOptions(fo);

        Material m2 = new Material();
        m2.setCode("S2");
        m2.setFetchOptions(fo);

        Material m3 = new Material();
        m3.setCode("S3");
        m3.setFetchOptions(fo);

        List<Material> materials = new ArrayList<Material>();
        materials.add(m1);
        materials.add(m2);
        materials.add(m3);

        Collection<Material> results = new SortAndPage().sortAndPage(materials, fo);

        assertMaterials(results, Arrays.asList("S2", "S1"));
    }

    @Test
    public void testSortAndPageSubLevel()
    {
        MaterialFetchOptions fo = new MaterialFetchOptions();
        fo.sortBy().code().desc();
        fo.withTags().from(1).count(1);

        Tag t1 = new Tag();
        t1.setCode("T1");
        Tag t2 = new Tag();
        t2.setCode("T2");
        Tag t3 = new Tag();
        t3.setCode("T3");

        Material m1 = new Material();
        m1.setCode("S1");
        m1.setTags(new LinkedHashSet<Tag>(Arrays.asList(t1, t2)));
        m1.setFetchOptions(fo);

        Material m2 = new Material();
        m2.setCode("S2");
        m2.setTags(new LinkedHashSet<Tag>(Arrays.asList(t2, t3)));
        m2.setFetchOptions(fo);

        List<Material> materials = new ArrayList<Material>();
        materials.add(m1);
        materials.add(m2);

        List<Material> results = new SortAndPage().sortAndPage(materials, fo);

        assertMaterials(results, Arrays.asList("S2", "S1"));
        assertTags(results.get(0).getTags(), Arrays.asList("T3"));
        assertTags(results.get(1).getTags(), Arrays.asList("T2"));
    }

    @Test
    public void testSortAndPageSubLevelThroughSingleRelation()
    {
        MaterialFetchOptions fo = new MaterialFetchOptions();
        fo.sortBy().code().desc();
        fo.withTags().from(1).count(1);
        fo.withRegistrator().withSpace().withProjects().from(1).count(1);
        fo.withMaterialProperties().withTags();

        Project project1 = new Project();
        project1.setCode("P1");
        project1.setFetchOptions(fo.withRegistrator().withSpace().withProjects());

        Project project2 = new Project();
        project2.setCode("P2");
        project2.setFetchOptions(fo.withRegistrator().withSpace().withProjects());

        Space space1 = new Space();
        space1.setProjects(Arrays.asList(project1, project2));
        space1.setFetchOptions(fo.withRegistrator().withSpace());

        Person person1 = new Person();
        person1.setLastName("Foo");
        person1.setSpace(space1);
        person1.setFetchOptions(fo.withRegistrator());

        Tag t1 = new Tag();
        t1.setCode("T1");
        Tag t2 = new Tag();
        t2.setCode("T2");
        Tag t3 = new Tag();
        t3.setCode("T3");

        Material m1 = new Material();
        m1.setCode("M1");
        m1.setTags(new LinkedHashSet<Tag>(Arrays.asList(t1, t2)));
        m1.setRegistrator(person1);
        m1.setFetchOptions(fo);

        Material m2 = new Material();
        m2.setCode("M2");
        m2.setTags(new LinkedHashSet<Tag>(Arrays.asList(t2, t3)));
        m2.setRegistrator(person1);
        m2.setFetchOptions(fo);

        Material m3 = new Material();
        m3.setCode("M3");
        m3.setTags(new LinkedHashSet<Tag>(Arrays.asList(t1, t3)));
        m3.setFetchOptions(fo);

        Map<String, Material> properties1 = new HashMap<String, Material>();
        properties1.put("PROPERTY_1", m1);
        properties1.put("PROPERTY_3", m3);
        m1.setMaterialProperties(properties1);

        Map<String, Material> properties2 = new HashMap<String, Material>();
        properties2.put("PROPERTY_2", m2);
        properties2.put("PROPERTY_3", m3);
        m2.setMaterialProperties(properties2);

        List<Material> materials = new ArrayList<Material>();
        materials.add(m1);
        materials.add(m2);

        List<Material> results = new SortAndPage().sortAndPage(materials, fo);

        assertMaterials(results, Arrays.asList("M2", "M1"));

        Material m1Result = results.get(1);
        Material m2Result = results.get(0);

        assertTags(m1Result.getTags(), Arrays.asList("T2"));
        assertTags(m1Result.getTags(), Arrays.asList("T2"));
        assertTags(m2Result.getTags(), Arrays.asList("T3"));

        assertProjects(m1Result.getRegistrator().getSpace().getProjects(), Arrays.asList("P2"));
        assertProjects(m2Result.getRegistrator().getSpace().getProjects(), Arrays.asList("P2"));

        assertTags(m2Result.getMaterialProperties().get("PROPERTY_3").getTags(), Arrays.asList("T1", "T3"));
    }

    @Test
    public void testSortByMultipleFields()
    {
        MaterialFetchOptions fo = new MaterialFetchOptions();
        fo.sortBy().code().desc();
        fo.sortBy().registrationDate().asc();

        Material m1 = new Material();
        m1.setCode("S1");
        m1.setRegistrationDate(new Date(3));
        m1.setFetchOptions(fo);

        Material m2 = new Material();
        m2.setCode("S1");
        m2.setRegistrationDate(new Date(2));
        m2.setFetchOptions(fo);

        Material m3 = new Material();
        m3.setCode("S3");
        m3.setRegistrationDate(new Date(1));
        m3.setFetchOptions(fo);

        List<Material> materials = new ArrayList<Material>();
        materials.add(m1);
        materials.add(m2);
        materials.add(m3);

        List<Material> results = new SortAndPage().sortAndPage(materials, fo);

        Assert.assertEquals(results.get(0), m3);
        Assert.assertEquals(results.get(1), m2);
        Assert.assertEquals(results.get(2), m1);
    }

    private void assertMaterials(Collection<Material> results, Collection<String> codes)
    {
        Collection<String> actualCodes = new ArrayList<String>();

        for (Material result : results)
        {
            actualCodes.add(result.getCode());
        }

        Assert.assertEquals(actualCodes, codes);
    }

    private void assertTags(Collection<Tag> results, Collection<String> codes)
    {
        Collection<String> actualCodes = new ArrayList<String>();

        for (Tag result : results)
        {
            actualCodes.add(result.getCode());
        }

        Assert.assertEquals(actualCodes, codes);
    }

    private void assertProjects(Collection<Project> results, Collection<String> codes)
    {
        Collection<String> actualCodes = new ArrayList<String>();

        for (Project result : results)
        {
            actualCodes.add(result.getCode());
        }

        Assert.assertEquals(actualCodes, codes);
    }

}
