package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort;

import static org.testng.AssertJUnit.assertEquals;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.Tag;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.SortAndPage;

public class SortAndPageTest
{
	@Test
    public void testFetchedFieldsScore_CodeScore()
    {
		SampleType sampleTypeA = new SampleType();
		sampleTypeA.setCode("DUMMY_CODE_A");
		
		SampleType sampleTypeB = new SampleType();
		sampleTypeB.setCode("DUMMY_CODE_B");
		
		String propertyCode = "DUMMY_PROPERTY";
		
		SampleSearchCriteria c = new SampleSearchCriteria();
		c.withOrOperator();
        c.withCode().thatEquals("S2");
        
		SampleFetchOptions fo = new SampleFetchOptions();
		fo.withType();
		fo.withProperties();
		fo.sortBy().fetchedFieldsScore();
		
        Sample sample1 = new Sample();
        sample1.setType(sampleTypeA);
        sample1.setCode("S1");
        sample1.setProperty(propertyCode, "DUMMY_S1");
        sample1.setFetchOptions(fo);

        Sample sample2 = new Sample();
        sample2.setType(sampleTypeB);
        sample2.setCode("S2");
        sample2.setProperty(propertyCode, "DUMMY_S2");
        sample2.setFetchOptions(fo);

        Sample sample3 = new Sample();
        sample3.setType(sampleTypeA);
        sample3.setCode("S3");
        sample3.setProperty(propertyCode, "DUMMY_S3");
        sample3.setFetchOptions(fo);

        List<Sample> samples = new ArrayList<Sample>();
        samples.add(sample1);
        samples.add(sample2);
        samples.add(sample3);

        Collection<Sample> results = new SortAndPage().sortAndPage(samples, c, fo);

        assertEquals(results, list(sample2, sample1, sample3));
    }
	
	@Test
    public void testFetchedFieldsScore_PropertyScore()
    {
		SampleType sampleTypeA = new SampleType();
		sampleTypeA.setCode("DUMMY_CODE_A");
		
		SampleType sampleTypeB = new SampleType();
		sampleTypeB.setCode("DUMMY_CODE_B");
		
		String propertyCode = "DUMMY_PROPERTY";
		
		SampleSearchCriteria c = new SampleSearchCriteria();
		c.withOrOperator();
        c.withProperty(propertyCode).thatEquals("DUMMY_S3");
        
		SampleFetchOptions fo = new SampleFetchOptions();
		fo.withType();
		fo.withProperties();
		fo.sortBy().fetchedFieldsScore();
		
        Sample sample1 = new Sample();
        sample1.setType(sampleTypeA);
        sample1.setCode("S1");
        sample1.setProperty(propertyCode, "DUMMY_S1");
        sample1.setFetchOptions(fo);

        Sample sample2 = new Sample();
        sample2.setType(sampleTypeB);
        sample2.setCode("S2");
        sample2.setProperty(propertyCode, "DUMMY_S2");
        sample2.setFetchOptions(fo);

        Sample sample3 = new Sample();
        sample3.setType(sampleTypeA);
        sample3.setCode("S3");
        sample3.setProperty(propertyCode, "DUMMY_S3");
        sample3.setFetchOptions(fo);

        List<Sample> samples = new ArrayList<Sample>();
        samples.add(sample1);
        samples.add(sample2);
        samples.add(sample3);

        Collection<Sample> results = new SortAndPage().sortAndPage(samples, c, fo);

        assertEquals(results, list(sample3, sample1, sample2));
    }
	
	@Test
    public void testFetchedFieldsScore_TypeScore()
    {
		SampleType sampleTypeA = new SampleType();
		sampleTypeA.setCode("DUMMY_CODE_A");
		
		SampleType sampleTypeB = new SampleType();
		sampleTypeB.setCode("DUMMY_CODE_B");
		
		String propertyCode = "DUMMY_PROPERTY";
		
		SampleSearchCriteria c = new SampleSearchCriteria();
		c.withOrOperator();
        c.withType().withCode().thatEquals(sampleTypeA.getCode());
        
		SampleFetchOptions fo = new SampleFetchOptions();
		fo.withType();
		fo.withProperties();
		fo.sortBy().fetchedFieldsScore();
		
        Sample sample1 = new Sample();
        sample1.setType(sampleTypeA);
        sample1.setCode("S1");
        sample1.setProperty(propertyCode, "DUMMY_S1");
        sample1.setFetchOptions(fo);

        Sample sample2 = new Sample();
        sample2.setType(sampleTypeB);
        sample2.setCode("S2");
        sample2.setProperty(propertyCode, "DUMMY_S2");
        sample2.setFetchOptions(fo);

        Sample sample3 = new Sample();
        sample3.setType(sampleTypeA);
        sample3.setCode("S3");
        sample3.setProperty(propertyCode, "DUMMY_S3");
        sample3.setFetchOptions(fo);

        List<Sample> samples = new ArrayList<Sample>();
        samples.add(sample1);
        samples.add(sample2);
        samples.add(sample3);

        Collection<Sample> results = new SortAndPage().sortAndPage(samples, c, fo);

        assertEquals(results, list(sample1, sample3, sample2));
    }
	
    @Test
    public void testTopLevel()
    {
    		MaterialSearchCriteria c = new MaterialSearchCriteria();
    		
        MaterialFetchOptions fo = new MaterialFetchOptions();
        fo.sortBy().code().desc();
        fo.from(1).count(2);

        Material material1 = new Material();
        material1.setCode("M1");
        material1.setFetchOptions(fo);

        Material material2 = new Material();
        material2.setCode("M2");
        material2.setFetchOptions(fo);

        Material material3 = new Material();
        material3.setCode("M3");
        material3.setFetchOptions(fo);

        List<Material> materials = new ArrayList<Material>();
        materials.add(material1);
        materials.add(material2);
        materials.add(material3);

        Collection<Material> results = new SortAndPage().sortAndPage(materials, c, fo);

        assertEquals(results, list(material2, material1));
    }

    @Test
    public void testSubLevel()
    {
    		MaterialSearchCriteria c = new MaterialSearchCriteria();
    	
        MaterialFetchOptions fo = new MaterialFetchOptions();
        fo.sortBy().code().desc();
        fo.withTags().from(1).count(1);

        Tag tag1 = new Tag();
        tag1.setCode("T1");
        Tag tag2 = new Tag();
        tag2.setCode("T2");
        Tag tag3 = new Tag();
        tag3.setCode("T3");

        Material material1 = new Material();
        material1.setCode("M1");
        material1.setTags(set(tag1, tag2));
        material1.setFetchOptions(fo);

        Material material2 = new Material();
        material2.setCode("M2");
        material2.setTags(set(tag2, tag3));
        material2.setFetchOptions(fo);

        List<Material> materials = new ArrayList<Material>();
        materials.add(material1);
        materials.add(material2);

        List<Material> results = new SortAndPage().sortAndPage(materials, c, fo);

        assertEquals(results, list(material2, material1));
        assertEquals(results.get(0).getTags(), set(tag3));
        assertEquals(results.get(1).getTags(), set(tag2));
    }

    @Test
    public void testSubLevelThroughSingleRelation()
    {
    		MaterialSearchCriteria c = new MaterialSearchCriteria();
    	
        MaterialFetchOptions fo = new MaterialFetchOptions();
        fo.sortBy().code().desc();
        fo.withTags().from(1).count(1);
        fo.withRegistrator().withSpace().withProjects().from(1).count(1);
        fo.withMaterialProperties().withTags();

        Project project1 = new Project();
        project1.setFetchOptions(fo.withRegistrator().withSpace().withProjects());

        Project project2 = new Project();
        project2.setFetchOptions(fo.withRegistrator().withSpace().withProjects());

        Space space1 = new Space();
        space1.setProjects(Arrays.asList(project1, project2));
        space1.setFetchOptions(fo.withRegistrator().withSpace());

        Person person1 = new Person();
        person1.setSpace(space1);
        person1.setFetchOptions(fo.withRegistrator());

        Tag tag1 = new Tag();
        tag1.setCode("T1");
        Tag tag2 = new Tag();
        tag2.setCode("T2");
        Tag tag3 = new Tag();
        tag3.setCode("T3");

        Material material1 = new Material();
        material1.setCode("M1");
        material1.setTags(set(tag1, tag2));
        material1.setRegistrator(person1);
        material1.setFetchOptions(fo);

        Material material2 = new Material();
        material2.setCode("M2");
        material2.setTags(set(tag2, tag3));
        material2.setRegistrator(person1);
        material2.setFetchOptions(fo);

        Material material3 = new Material();
        material3.setCode("M3");
        material3.setTags(set(tag1, tag3));
        material3.setFetchOptions(fo);

        Map<String, Material> properties1 = new HashMap<String, Material>();
        properties1.put("PROPERTY_1", material1);
        properties1.put("PROPERTY_3", material3);
        material1.setMaterialProperties(properties1);

        Map<String, Material> properties2 = new HashMap<String, Material>();
        properties2.put("PROPERTY_2", material2);
        properties2.put("PROPERTY_3", material3);
        material2.setMaterialProperties(properties2);

        List<Material> materials = new ArrayList<Material>();
        materials.add(material1);
        materials.add(material2);

        List<Material> results = new SortAndPage().sortAndPage(materials, c, fo);

        assertEquals(results, list(material2, material1));

        Material material1Result = results.get(1);
        Material material2Result = results.get(0);

        assertEquals(material1Result.getTags(), set(tag2));
        assertEquals(material2Result.getTags(), set(tag3));

        assertEquals(material1Result.getRegistrator().getSpace().getProjects(), list(project2));
        assertEquals(material2Result.getRegistrator().getSpace().getProjects(), list(project2));

        assertEquals(material2Result.getMaterialProperties().get("PROPERTY_3").getTags(), set(tag1, tag3));
    }

    @Test
    public void testSortByMultipleFields()
    {
    		MaterialSearchCriteria c = new MaterialSearchCriteria();
    	
        MaterialFetchOptions fo = new MaterialFetchOptions();
        fo.sortBy().code().desc();
        fo.sortBy().registrationDate().asc();

        Material material1 = new Material();
        material1.setCode("M1");
        material1.setRegistrationDate(new Date(3));
        material1.setFetchOptions(fo);

        Material material2 = new Material();
        material2.setCode("M2");
        material2.setRegistrationDate(new Date(2));
        material2.setFetchOptions(fo);

        Material material3 = new Material();
        material3.setCode("M3");
        material3.setRegistrationDate(new Date(1));
        material3.setFetchOptions(fo);

        List<Material> materials = new ArrayList<Material>();
        materials.add(material1);
        materials.add(material2);
        materials.add(material3);

        List<Material> results = new SortAndPage().sortAndPage(materials, c, fo);

        assertEquals(results.get(0), material3);
        assertEquals(results.get(1), material2);
        assertEquals(results.get(2), material1);
    }

    @Test
    public void testSamePageMultipleTimes()
    {
    		MaterialSearchCriteria c = new MaterialSearchCriteria();
    	
        Tag tag1 = new Tag();
        tag1.setCode("T1");
        Tag tag2 = new Tag();
        tag2.setCode("T2");
        Tag tag3 = new Tag();
        tag3.setCode("T3");

        MaterialFetchOptions fo = new MaterialFetchOptions();
        fo.from(1).count(1);
        fo.withTags().from(1).count(1);

        Material material1 = new Material();
        material1.setCode("M1");
        material1.setTags(set(tag1, tag3));
        material1.setFetchOptions(fo);

        Material material2 = new Material();
        material2.setCode("M2");
        material2.setTags(set(tag2, tag3));
        material2.setFetchOptions(fo);

        List<Material> materials = new ArrayList<Material>();
        materials.add(material1);
        materials.add(material2);

        List<Material> results = new SortAndPage().sortAndPage(materials, c, fo);

        assertEquals(results, list(material2));
        assertEquals(results.get(0).getTags(), set(tag3));
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> list(T... items)
    {
        return Arrays.asList(items);
    }

    @SuppressWarnings("unchecked")
    private <T> Set<T> set(T... items)
    {
        return new LinkedHashSet<T>(Arrays.asList(items));
    }

}
