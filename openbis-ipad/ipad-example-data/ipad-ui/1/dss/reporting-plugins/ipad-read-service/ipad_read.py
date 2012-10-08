from ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1 import MaterialIdentifierCollection
from ch.systemsx.cisd.openbis.generic.shared.basic.dto import MaterialIdentifier
from com.fasterxml.jackson.databind import ObjectMapper 

def add_headers(builder):
	"""Configure the headers for the iPad UI -- these are fixed"""
	builder.addHeader("SUMMARY_HEADER")
	builder.addHeader("SUMMARY")
	builder.addHeader("IDENTIFIER")
	builder.addHeader("PERM_ID")
	builder.addHeader("ENTITY_KIND")
	builder.addHeader("ENTITY_TYPE")
	builder.addHeader("IMAGE_URL")
	builder.addHeader("PROPERTIES")


def add_row(builder, entry):
	"""Append a row of data to the table"""
	row = builder.addRow()
	row.setCell("SUMMARY_HEADER", entry.get("SUMMARY_HEADER"))
	row.setCell("SUMMARY", entry.get("SUMMARY"))
	row.setCell("IDENTIFIER", entry.get("IDENTIFIER"))
	row.setCell("PERM_ID", entry.get("PERM_ID"))
	row.setCell("ENTITY_KIND", entry.get("ENTITY_KIND"))
	row.setCell("ENTITY_TYPE", entry.get("ENTITY_TYPE"))
	row.setCell("IMAGE_URL", entry.get("IMAGE_URL"))
	row.setCell("PROPERTIES", str(entry.get("PROPERTIES")))

def material_to_dict(material):
	material_dict = {}
	material_dict['SUMMARY_HEADER'] = material.getCode()
	material_dict['IDENTIFIER'] = material.getMaterialIdentifier()
	material_dict['PERM_ID'] = material.getMaterialIdentifier()
	material_dict['ENTITY_KIND'] = 'MATERIAL'
	material_dict['ENTITY_TYPE'] = material.getMaterialType()
	if material.getMaterialType() == '5HT_COMPOUND':
		chemblId =  material.getCode()
		material_dict['SUMMARY'] = material.getPropertyValue("FORMULA")
		material_dict['IMAGE_URL'] = 'https://www.ebi.ac.uk/chemblws/compounds/%s/image' % chemblId
	else:
		material_dict['SUMMARY'] = material.getPropertyValue("DESC")
		material_dict['IMAGE_URL'] = ""	

	prop_names = ["NAME", "PROT_NAME", "GENE_NAME", "LENGTH", "CHEMBL", "DESC", "FORUMLA", "WEIGHT", "SMILES"]
	properties = dict((name, material.getPropertyValue(name)) for name in prop_names if material.getPropertyValue(name) is not None)
	material_dict['PROPERTIES'] = ObjectMapper().writeValueAsString(properties)
	return material_dict

def sample_to_dict(five_ht_sample):
	sample_dict = {}
	sample_dict['SUMMARY_HEADER'] = five_ht_sample.getCode()
	sample_dict['SUMMARY'] = five_ht_sample.getPropertyValue("DESC")
	sample_dict['IDENTIFIER'] = five_ht_sample.getSampleIdentifier()
	sample_dict['PERM_ID'] = five_ht_sample.getPermId()
	sample_dict['ENTITY_KIND'] = 'SAMPLE'
	sample_dict['ENTITY_TYPE'] = five_ht_sample.getSampleType()
	sample_dict['IMAGE_URL'] = ""
	prop_names = ["DESC"]
	properties = dict((name, five_ht_sample.getPropertyValue(name)) for name in prop_names if five_ht_sample.getPropertyValue(name) is not None)
	sample_dict['PROPERTIES'] = ObjectMapper().writeValueAsString(properties)
	# Need to handle the material links as entity links: "TARGET", "COMPOUND"
	return sample_dict

def add_rows(builder, entities):
	"""Take a collection of dictionaries and add a row for each one"""
	for entry in entities:
		add_row(builder, entry)

def add_material_to_collection(code, collection):
	material_id = MaterialIdentifier.tryParseIdentifier(code)
	collection.addIdentifier(material_id.getTypeCode(), material_id.getCode())

def gather_materials(five_ht_samples):
	material_identifiers = MaterialIdentifierCollection()
	for sample in five_ht_samples:
		add_material_to_collection(sample.getPropertyValue("TARGET"), material_identifiers)
		add_material_to_collection(sample.getPropertyValue("COMPOUND"), material_identifiers)
	return material_identifiers

def materials_to_dict(materials):
	result = [material_to_dict(material) for material in materials]
	return result

def aggregate(parameters, builder):
	add_headers(builder)

	# Get the data and add a row for each data item
	samples = searchService.searchForSamples("DESC", "*", "5HT_PROBE")
	material_identifiers = gather_materials(samples)
	materials = searchService.listMaterials(material_identifiers)
	add_rows(builder, materials_to_dict(materials))

