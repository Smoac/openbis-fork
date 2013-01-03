from ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1 import MaterialIdentifierCollection
from ch.systemsx.cisd.openbis.generic.shared.basic.dto import MaterialIdentifier
from com.fasterxml.jackson.databind import ObjectMapper 
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchSubCriteria

#
# BEGIN Infrastructure
#

def json_encoded_value(coll):
	"""Utility function for converting a list into a json-encoded list"""
	return ObjectMapper().writeValueAsString(coll)

class RequestHandler:
	"""Abstract superclass for the handlers for concrete requests like ROOT.

	This superclass defines behavior common to all requests.

	Subclasses need to implement the method optional_headers(), which returns
	a list of the optional headers they fill out.

	Subclasses should implement retrieve_data to get the data they provide.

	Subclasses should implement add_data_rows. In this method, they should call add_row.
	The method add_row takes a dictionary as an argument. The keys of the dictionary match the
	headers in the result columns. The dictionary should include data for the required columns
	and optional ones they fill.

	"""

	def __init__(self, parameters, builder):
		self.parameters = parameters
		self.builder = builder
		global searchService
		self.searchService = searchService
		self.headers = ['PERM_ID', 'REFCON'] + self.optional_headers()


	def optional_headers(self):
		"""Return a list of optional headers supported by this request. Sublass responsibility.

		See add_headers() for the list of supported headers
		"""
		return []

	def retrieve_data(self):
		"""Get the data for the request. Subclass responsibility"""
		pass

	def add_data_rows(self):
		"""Take the information from the data and put it into the table.
		Subclass responsibility.
		"""
		pass

	def add_headers(self):
		"""Configure the headers for this request.

		The possible headers come from the following list:
			PERM_ID : A stable identifier for the object. (required)
			REFCON : Data that is passed unchanged back to the server when a row is modified.
				This can be used by the server to encode whatever it needs in order to
				modify the row. (required)
			CATEGORY : A category identifier for grouping entities.
			SUMMARY_HEADER : A short summary of the entity.
			SUMMARY : A potentially longer summary of the entity.
			CHILDREN : The permIds of the children of this entity. Transmitted as JSON.
			IDENTIFIER : An identifier for the object.
			IMAGE_URL : A url for an image associated with this entity. If None or empty, no
				image is shown.
			PROPERTIES : Properties (metadata) that should be displayed for this entity. Transmitted as JSON.
			ROOT_LEVEL : True if the entity should be shown on the root level.

		The relevant headers are determined by the request.
		"""
		for header in self.headers:
			self.builder.addHeader(header)

	def add_row(self, entry):
		"""Append a row of data to the table"""
		row = self.builder.addRow()
		for header in self.headers:
			value = entry.get(header)
			if value is not None:
				row.setCell(header, unicode(entry.get(header)))
			else:
				row.setCell(header, "")

	def add_rows(self, entities):
		"""Take a collection of dictionaries and add a row for each one"""
		for entry in entities:
			self.add_row(entry)

	def process_request(self):
		"""Execute the steps necessary to process the request."""
		self.add_headers()
		self.retrieve_data()
		self.add_data_rows()

class AllDataRequestHandler(RequestHandler):
	"""Abstract Handler for the ALLDATA request."""

	def optional_headers(self):
		return ["CATEGORY", "SUMMARY_HEADER", "SUMMARY", "CHILDREN", "IDENTIFIER", "IMAGE_URL", "PROPERTIES"]

class EmptyDataRequestHandler(RequestHandler):
	"""Return nothing to the caller."""

	def add_data_rows(self):
		pass

class RootRequestHandler(RequestHandler):
	"""Abstract Handler for the ROOT request."""

	def optional_headers(self):
		return ["CATEGORY", "SUMMARY_HEADER", "SUMMARY", "CHILDREN", "ROOT_LEVEL"]

class DrillRequestHandler(RequestHandler):
	"""Abstract Handler for the DRILL request."""

	def optional_headers(self):
		return ["CATEGORY", "SUMMARY_HEADER", "SUMMARY", "CHILDREN"]

class DetailRequestHandler(RequestHandler):
	"""Abstract Handler for the DETAIL request."""

	def optional_headers(self):
		return ["CATEGORY", "SUMMARY_HEADER", "SUMMARY", "IDENTIFIER", "IMAGE_URL", "PROPERTIES"]

#
# END Infrastructure
#

#
# Helper Methods
# 
def image_url_for_compound(material):
	"""Given a material (compound) return the image url"""
	chemblId =  material.getCode()
	return 'https://www.ebi.ac.uk/chemblws/compounds/%s/image' % chemblId

def navigation_layer(oligos, antibodies, chemicals, protocols, medias, pcrs, buffers, plasmids, yeasts):
	oligo_dict = {}
	oligo_dict["SUMMARY_HEADER"] = "oligo"
	oligo_dict["SUMMARY"] = "Oligos in YeastLab"
	oligo_dict['PERM_ID'] = "OLIGO"
	refcon_oligo = {}
	refcon_oligo['code'] =  "OLIGO"
	refcon_oligo['entityKind'] = 'NAVIGATION'
	refcon_oligo['entityType'] = 'NAVIGATION'
	oligo_dict['REFCON'] = json_encoded_value(refcon_oligo)
	oligo_dict['CATEGORY'] = None
	children = [oligo.getPermId() for oligo in oligos]
	oligo_dict['CHILDREN'] = json_encoded_value(children)
	oligo_dict['PROPERTIES'] = json_encoded_value([])
	oligo_dict['ROOT_LEVEL'] = True

    	antibody_dict = {}
	antibody_dict["SUMMARY_HEADER"] = "antibody"
	antibody_dict["SUMMARY"] = "Antibodies in YeastLab"
	antibody_dict['PERM_ID'] = "ANTIBODY"
	refcon_antibody = {}
	refcon_antibody['code'] =  "ANTIBODY"
	refcon_antibody['entityKind'] = 'NAVIGATION'
	refcon_antibody['entityType'] = 'NAVIGATION'
	antibody_dict['REFCON'] = json_encoded_value(refcon_antibody)
	antibody_dict['CATEGORY'] = None
	children = [antibody.getPermId() for antibody in antibodies]
	antibody_dict['CHILDREN'] = json_encoded_value(children)
	antibody_dict['PROPERTIES'] = json_encoded_value([])
	antibody_dict['ROOT_LEVEL'] = True
	chemical_dict = {}

	chemical_dict["SUMMARY_HEADER"] = "chemical"
	chemical_dict["SUMMARY"] = "Chemicals in YeastLab"
	chemical_dict['PERM_ID'] = "CHEMICAL"
	refcon_chemical = {}
	refcon_chemical['code'] =  "CHEMICAL"
	refcon_chemical['entityKind'] = 'NAVIGATION'
	refcon_chemical['entityType'] = 'NAVIGATION'
	chemical_dict['REFCON'] = json_encoded_value(refcon_chemical)
	chemical_dict['CATEGORY'] = None
	children = [chemical.getPermId() for chemical in chemicals]
	chemical_dict['CHILDREN'] = json_encoded_value(children)
	chemical_dict['PROPERTIES'] = json_encoded_value([])
	chemical_dict['ROOT_LEVEL'] = True
	
	protocol_dict = {}
	protocol_dict["SUMMARY_HEADER"] = "protocol"
	protocol_dict["SUMMARY"] = "Protocols in YeastLab"
	protocol_dict['PERM_ID'] = "GENERAL_PROTOCOL"
	refcon_protocol = {}
	refcon_protocol['code'] =  "GENERAL_PROTOCOL"
	refcon_protocol['entityKind'] = 'NAVIGATION'
	refcon_protocol['entityType'] = 'NAVIGATION'
	protocol_dict['REFCON'] = json_encoded_value(refcon_protocol)
	protocol_dict['CATEGORY'] = None
	children = [protocol.getPermId() for protocol in protocols]
	protocol_dict['CHILDREN'] = json_encoded_value(children)
	protocol_dict['PROPERTIES'] = json_encoded_value([])
	protocol_dict['ROOT_LEVEL'] = True	

	media_dict = {}
	media_dict["SUMMARY_HEADER"] = "media"
	media_dict["SUMMARY"] = "Medias in YeastLab"
	media_dict['PERM_ID'] = "MEDIA"
	refcon_media = {}
	refcon_media['code'] =  "MEDIA"
	refcon_media['entityKind'] = 'NAVIGATION'
	refcon_media['entityType'] = 'NAVIGATION'
	media_dict['REFCON'] = json_encoded_value(refcon_media)
	media_dict['CATEGORY'] = None
	children = [media.getPermId() for media in medias]
	media_dict['CHILDREN'] = json_encoded_value(children)
	media_dict['PROPERTIES'] = json_encoded_value([])
	media_dict['ROOT_LEVEL'] = True	

	pcr_dict = {}
	pcr_dict["SUMMARY_HEADER"] = "pcr"
	pcr_dict["SUMMARY"] = "PCR in YeastLab"
	pcr_dict['PERM_ID'] = "PCR"
	refcon_pcr = {}
	refcon_pcr['code'] =  "PCR"
	refcon_pcr['entityKind'] = 'NAVIGATION'
	refcon_pcr['entityType'] = 'NAVIGATION'
	pcr_dict['REFCON'] = json_encoded_value(refcon_pcr)
	pcr_dict['CATEGORY'] = None
	children = [pcr.getPermId() for pcr in pcrs]
	pcr_dict['CHILDREN'] = json_encoded_value(children)
	pcr_dict['PROPERTIES'] = json_encoded_value([])
	pcr_dict['ROOT_LEVEL'] = True	

	buffer_dict = {}
	buffer_dict["SUMMARY_HEADER"] = "buffer"
	buffer_dict["SUMMARY"] = "Solution Buffers in YeastLab"
	buffer_dict['PERM_ID'] = "SOLUTIONS_BUFFERS"
	refcon_buffer = {}
	refcon_buffer['code'] =  "SOLUTIONS_BUFFERS"
	refcon_buffer['entityKind'] = 'NAVIGATION'
	refcon_buffer['entityType'] = 'NAVIGATION'
	buffer_dict['REFCON'] = json_encoded_value(refcon_buffer)
	buffer_dict['CATEGORY'] = None
	children = [buffer.getPermId() for buffer in buffers]
	buffer_dict['CHILDREN'] = json_encoded_value(children)
	buffer_dict['PROPERTIES'] = json_encoded_value([])
	buffer_dict['ROOT_LEVEL'] = True		
	
        plasmid_dict = {}
        plasmid_dict["SUMMARY_HEADER"] = "plasmid"
        plasmid_dict["SUMMARY"] = "Plasmids in YeastLab"
        plasmid_dict['PERM_ID'] = "PLASMID"
        refcon_plasmid = {}
        refcon_plasmid['code'] =  "PLASMID"
        refcon_plasmid['entityKind'] = 'NAVIGATION'
        refcon_plasmid['entityType'] = 'NAVIGATION'
        plasmid_dict['REFCON'] = json_encoded_value(refcon_plasmid)
        plasmid_dict['CATEGORY'] = None
        children = [plasmid.getPermId() for plasmid in plasmids]
        plasmid_dict['CHILDREN'] = json_encoded_value(children)
        plasmid_dict['PROPERTIES'] = json_encoded_value([])
        plasmid_dict['ROOT_LEVEL'] = True	
        
	yeast_dict = {}
        yeast_dict["SUMMARY_HEADER"] = "yeast"
        yeast_dict["SUMMARY"] = "Yeasts in YeastLab"
        yeast_dict['PERM_ID'] = "YEAST"
        refcon_yeast = {}
        refcon_yeast['code'] =  "YEAST"
        refcon_yeast['entityKind'] = 'NAVIGATION'
        refcon_yeast['entityType'] = 'NAVIGATION'
        yeast_dict['REFCON'] = json_encoded_value(refcon_yeast)
        yeast_dict['CATEGORY'] = None
        children = [yeast.getPermId() for yeast in yeasts]
        yeast_dict['CHILDREN'] = json_encoded_value(children)
        yeast_dict['PROPERTIES'] = json_encoded_value([])
        yeast_dict['ROOT_LEVEL'] = True    

	return [oligo_dict, antibody_dict, chemical_dict, protocol_dict, media_dict, pcr_dict, buffer_dict, plasmid_dict, yeast_dict]

def oligo_to_dict(oligo):
        sample_dict = {}
        sample_dict['SUMMARY_HEADER'] = oligo.getCode()
        summary = "Project: " + str(oligo.getPropertyValue("PROJECT"))
        summary = summary + "\n"
        summary = summary + "Target: " + oligo.getPropertyValue("TARGET")
        sample_dict['SUMMARY'] = summary
        sample_dict['IDENTIFIER'] = oligo.getSampleIdentifier()
        sample_dict['PERM_ID'] = oligo.getPermId()
        refcon_oligo = {}
        refcon_oligo['code'] =  oligo.getCode()
        refcon_oligo['entityKind'] = 'SAMPLE'
        refcon_oligo['entityType'] = oligo.getSampleType()
        sample_dict['REFCON'] = json_encoded_value(refcon_oligo)
        sample_dict['CATEGORY'] = oligo.getSampleType()
#       sample_dict['IMAGE_URL'] = image_url_for_compound(compound)

#       children = [oligo.getPropertyValue("TARGET"), oligo.getPropertyValue("COMPOUND")]
        children = []
        sample_dict['CHILDREN'] = json_encoded_value(children)

        prop_names_oligo = ["BOX_NUMBER", "ROW", "COLUMN", "TARGET", "DIRECTION", "RESTRICTION_ENZYME", "MODIFICATIONS", "SEQUENCE"]
        properties_oligo = dict((name, oligo.getPropertyValue(name)) for name in prop_names_oligo if oligo.getPropertyValue(name) is not None)
        sample_dict['PROPERTIES'] = json_encoded_value(properties_oligo)
        sample_dict['ROOT_LEVEL'] = None
        return sample_dict

def antibody_to_dict(antibody):
	sample_antibody_dict = {}
	sample_antibody_dict['SUMMARY_HEADER'] = antibody.getCode()
	summary = "Name: " + str(antibody.getPropertyValue("NAME"))
	summary = summary + "\n"
	summary = summary + "Epitope: " + str(antibody.getPropertyValue("EPITOPE"))
	sample_antibody_dict['SUMMARY'] = summary
	sample_antibody_dict['IDENTIFIER'] = antibody.getSampleIdentifier()
	sample_antibody_dict['PERM_ID'] = antibody.getPermId()
	refcon_antibody = {}
	refcon_antibody['code'] =  antibody.getCode()
	refcon_antibody['entityKind'] = 'SAMPLE'
	refcon_antibody['entityType'] = antibody.getSampleType()
	sample_antibody_dict['REFCON'] = json_encoded_value(refcon_antibody)
	sample_antibody_dict['CATEGORY'] = antibody.getSampleType()
#	sample_antibody_dict['IMAGE_URL'] = image_url_for_compound(compound)

#	children = [oligo.getPropertyValue("TARGET"), oligo.getPropertyValue("COMPOUND")]
	children = []
	sample_antibody_dict['CHILDREN'] = json_encoded_value(children)

	prop_names_antibody = ["STORAGE", "SUPPLIER", "ARTICLE_NUMBER", "CLONALITY", "ISOTYPE", "HOST", "STOCK_CONCENTRATION", "FOR_WHAT", "PUBLICATION", "COMMENTS"]
	properties_antibody = dict((name, antibody.getPropertyValue(name)) for name in prop_names_antibody if antibody.getPropertyValue(name) is not None)
	sample_antibody_dict['PROPERTIES'] = json_encoded_value(properties_antibody)
	sample_antibody_dict['ROOT_LEVEL'] = None
	return sample_antibody_dict

def chemical_to_dict(chemical):
        sample_dict = {}
        sample_dict['SUMMARY_HEADER'] = chemical.getCode()
        summary = "Name: " + str(chemical.getPropertyValue("NAME"))
        sample_dict['SUMMARY'] = summary
        sample_dict['IDENTIFIER'] = chemical.getSampleIdentifier()
        sample_dict['PERM_ID'] = chemical.getPermId()
        refcon_chemical = {}
        refcon_chemical['code'] =  chemical.getCode()
        refcon_chemical['entityKind'] = 'SAMPLE'
        refcon_chemical['entityType'] = chemical.getSampleType()
        sample_dict['REFCON'] = json_encoded_value(refcon_chemical)
        sample_dict['CATEGORY'] = chemical.getSampleType()
        children = []
        sample_dict['CHILDREN'] = json_encoded_value(children)

        prop_names_chemical = ["STORAGE", "LOCAL_ID", "SUPPLIER", "ARTICLE_NUMBER"]
        properties_chemical = dict((name, chemical.getPropertyValue(name)) for name in prop_names_chemical if chemical.getPropertyValue(name) is not None)
        sample_dict['PROPERTIES'] = json_encoded_value(properties_chemical)
        sample_dict['ROOT_LEVEL'] = None
        return sample_dict

def protocol_to_dict(protocol):
        sample_dict = {}
        sample_dict['SUMMARY_HEADER'] = protocol.getCode()
        summary = "Name: " + str(protocol.getPropertyValue("NAME"))
        sample_dict['SUMMARY'] = summary
        sample_dict['IDENTIFIER'] = protocol.getSampleIdentifier()
        sample_dict['PERM_ID'] = protocol.getPermId()
        refcon_protocol = {}
        refcon_protocol['code'] =  protocol.getCode()
        refcon_protocol['entityKind'] = 'SAMPLE'
        refcon_protocol['entityType'] = protocol.getSampleType()
        sample_dict['REFCON'] = json_encoded_value(refcon_protocol)
        sample_dict['CATEGORY'] = protocol.getSampleType()
        children = []
        sample_dict['CHILDREN'] = json_encoded_value(children)

        prop_names_protocol = ["FOR_WHAT", "PROTOCOL_TYPE", "PUBLICATION", "MATERIALS", "PROCEDURE", "PROTOCOL_EVALUATION", "SUGGESTIONS", "PROTOCOL_MODIFICATIONS"]
        properties_protocol = dict((name, protocol.getPropertyValue(name)) for name in prop_names_protocol if protocol.getPropertyValue(name) is not None)
        sample_dict['PROPERTIES'] = json_encoded_value(properties_protocol)
        sample_dict['ROOT_LEVEL'] = None
        return sample_dict

def media_to_dict(media):
        sample_dict = {}
        sample_dict['SUMMARY_HEADER'] = media.getCode()
        summary = "Name: " + str(media.getPropertyValue("NAME"))
        sample_dict['SUMMARY'] = summary
        sample_dict['IDENTIFIER'] = media.getSampleIdentifier()
        sample_dict['PERM_ID'] = media.getPermId()
        refcon_media = {}
        refcon_media['code'] =  media.getCode()
        refcon_media['entityKind'] = 'SAMPLE'
        refcon_media['entityType'] = media.getSampleType()
        sample_dict['REFCON'] = json_encoded_value(refcon_media)
        sample_dict['CATEGORY'] = media.getSampleType()
        children = []
        sample_dict['CHILDREN'] = json_encoded_value(children)

        prop_names_media = ["STOCK_CONCENTRATION", "STORAGE", "FOR_WHAT", "ORGANISM", "STERILIZATION", "DETAILS", "COMMENTS", "PUBLICATION"]
        properties_media = dict((name, media.getPropertyValue(name)) for name in prop_names_media if media.getPropertyValue(name) is not None)
        sample_dict['PROPERTIES'] = json_encoded_value(properties_media)
        sample_dict['ROOT_LEVEL'] = None
        return sample_dict

def pcr_to_dict(pcr):
        sample_dict = {}
        sample_dict['SUMMARY_HEADER'] = pcr.getCode()
        summary = "Name: " + str(pcr.getPropertyValue("NAME"))
        sample_dict['SUMMARY'] = summary
        sample_dict['IDENTIFIER'] = pcr.getSampleIdentifier()
        sample_dict['PERM_ID'] = pcr.getPermId()
        refcon_pcr = {}
        refcon_pcr['code'] =  pcr.getCode()
        refcon_pcr['entityKind'] = 'SAMPLE'
        refcon_pcr['entityType'] = pcr.getSampleType()
        sample_dict['REFCON'] = json_encoded_value(refcon_pcr)
        sample_dict['CATEGORY'] = pcr.getSampleType()
        children = []
        sample_dict['CHILDREN'] = json_encoded_value(children)

        prop_names_pcr = ["SUPPLIER", "ARTICLE_NUMBER", "KIT", "PUBLICATION", "MATERIALS", "TEMPLATE"]
        properties_pcr = dict((name, pcr.getPropertyValue(name)) for name in prop_names_pcr if pcr.getPropertyValue(name) is not None)
        sample_dict['PROPERTIES'] = json_encoded_value(properties_pcr)
        sample_dict['ROOT_LEVEL'] = None
        return sample_dict

def buffer_to_dict(buffer):
        sample_dict = {}
        sample_dict['SUMMARY_HEADER'] = buffer.getCode()
        summary = "Name: " + str(buffer.getPropertyValue("NAME"))
        sample_dict['SUMMARY'] = summary
        sample_dict['IDENTIFIER'] = buffer.getSampleIdentifier()
        sample_dict['PERM_ID'] = buffer.getPermId()
        refcon_buffer = {}
        refcon_buffer['code'] =  buffer.getCode()
        refcon_buffer['entityKind'] = 'SAMPLE'
        refcon_buffer['entityType'] = buffer.getSampleType()
        sample_dict['REFCON'] = json_encoded_value(refcon_buffer)
        sample_dict['CATEGORY'] = buffer.getSampleType()
        children = []
        sample_dict['CHILDREN'] = json_encoded_value(children)

        prop_names_buffer = ["STOCK_CONCENTRATION", "STORAGE", "FOR_WHAT", "STERILIZATION", "DETAILS", "COMMENTS", "PUBLICATION"]
        properties_buffer = dict((name, buffer.getPropertyValue(name)) for name in prop_names_buffer if buffer.getPropertyValue(name) is not None)
        sample_dict['PROPERTIES'] = json_encoded_value(properties_buffer)
        sample_dict['ROOT_LEVEL'] = None
        return sample_dict

def plasmid_to_dict(plasmid, children_map):
        sample_dict = {}
        sample_dict['SUMMARY_HEADER'] = plasmid.getCode()
        summary = "Name: " + str(plasmid.getPropertyValue("PLASMID_NAME"))
        sample_dict['SUMMARY'] = summary
        sample_dict['IDENTIFIER'] = plasmid.getSampleIdentifier()
        sample_dict['PERM_ID'] = plasmid.getPermId()
        refcon_plasmid = {}
        refcon_plasmid['code'] =  plasmid.getCode()
        refcon_plasmid['entityKind'] = 'SAMPLE'
        refcon_plasmid['entityType'] = plasmid.getSampleType()
        sample_dict['REFCON'] = json_encoded_value(refcon_plasmid)
        sample_dict['CATEGORY'] = plasmid.getSampleType()
        children = [child.getPermId() for child in children_map.get(plasmid.getSampleIdentifier(), [])]
        sample_dict['CHILDREN'] = json_encoded_value(children)

        prop_names_plasmid = ["BOX_NUMBER", "ROW", "COLUMN", "OWNER", "OWNER_NUMBER", "BACKBONE", "DERIVATIVE_OF", "BACTERIAL_ANTIBIOTIC_RESISTANCE", "YEAST_MARKER", "OTHER_MARKER", "FLANKING_RESTRICTION_ENZYME", "COMMENTS", "DATE"]
        properties_plasmid = dict((name, plasmid.getPropertyValue(name)) for name in prop_names_plasmid if plasmid.getPropertyValue(name) is not None)
        sample_dict['PROPERTIES'] = json_encoded_value(properties_plasmid)
        sample_dict['ROOT_LEVEL'] = None 
	return sample_dict

def plasmid_to_dict_with_images(plasmid, children_map, data_sets):
	plasmid_dict = plasmid_to_dict(plasmid, children_map)
	plasmid_data_set = None
	for data_set in data_sets:
		if data_set.getSample().getSampleIdentifier() == plasmid.getSampleIdentifier():
			plasmid_data_set = data_set
			break
	if plasmid_data_set is None:
		return plasmid_dict
	image_url = 'https://openbis-csb.ethz.ch/datastore_server/' + plasmid_data_set.getDataSetCode() + '/generated/'
	image_url = image_url + plasmid.getCode() + '.svg'
	plasmid_dict['IMAGE_URL'] = image_url
	return plasmid_dict
	

def yeast_to_dict(yeast, children_map):
        sample_dict = {}
        sample_dict['SUMMARY_HEADER'] = yeast.getCode()
        summary = "Name: " + str(yeast.getPropertyValue("YEAST_STRAIN_NAME"))
        sample_dict['SUMMARY'] = summary
        sample_dict['IDENTIFIER'] =yeast.getSampleIdentifier()
        sample_dict['PERM_ID'] = yeast.getPermId()
        refcon_yeast = {}
        refcon_yeast['code'] =  yeast.getCode()
        refcon_yeast['entityKind'] = 'SAMPLE'
        refcon_yeast['entityType'] = yeast.getSampleType()
        sample_dict['REFCON'] = json_encoded_value(refcon_yeast)
        sample_dict['CATEGORY'] = yeast.getSampleType()
        children = [child.getPermId() for child in children_map.get(yeast.getSampleIdentifier(), [])]
        sample_dict['CHILDREN'] = json_encoded_value(children)

        prop_names_yeast = ["BOX_NUMBER", "ROW", "COLUMN", "OWNER", "OWNER_NUMBER", "GENETIC_BACKGROUND", "MATING_TYPE", "BACKGROUND_SPECIFIC_MARKER", "COMMON_MARKERS", "ENDOGENOUS_PLASMID", "SOURCE", "ORIGIN", "STRAIN_CHECK", "PROJECT", "COMMENTS"]
        properties_yeast = dict((name, yeast.getPropertyValue(name)) for name in prop_names_yeast if yeast.getPropertyValue(name) is not None)
        sample_dict['PROPERTIES'] = json_encoded_value(properties_yeast)
        sample_dict['ROOT_LEVEL'] = None
        return sample_dict

def oligos_to_dict(samples):
	result = [oligo_to_dict(sample) for sample in samples]
	return result

def antibodies_to_dict(samples):
        result = [antibody_to_dict(sample) for sample in samples]
        return result

def chemicals_to_dict(samples):
        result = [chemical_to_dict(sample) for sample in samples]
        return result

def protocols_to_dict(samples):
        result = [protocol_to_dict(sample) for sample in samples]
        return result

def medias_to_dict(samples):
        result = [media_to_dict(sample) for sample in samples]
        return result

def pcrs_to_dict(samples):
        result = [pcr_to_dict(sample) for sample in samples]
        return result

def buffers_to_dict(samples):
        result = [buffer_to_dict(sample) for sample in samples]
        return result

def plasmids_to_dict(samples, children_map):
        result = [plasmid_to_dict(sample, children_map) for sample in samples]
        return result

def plasmids_to_dict_with_images(samples, children_map, data_sets):
        result = [plasmid_to_dict_with_images(sample, children_map, data_sets) for sample in samples]
        return result

def yeasts_to_dict(samples, children_map):
        result = [yeast_to_dict(sample, children_map) for sample in samples]
        return result

def retrieve_samples(sample_perm_ids_and_ref_cons):
	sc = SearchCriteria()
	sc.setOperator(sc.SearchOperator.MATCH_ANY_CLAUSES)
	for sample in sample_perm_ids_and_ref_cons:
		code = sample['REFCON']['code']	
		sc.addMatchClause(sc.MatchClause.createAttributeMatch(sc.MatchClauseAttribute.CODE, code))
	return searchService.searchForSamples(sc)

def retrieve_seq_data_sets(samples):
	sc = SearchCriteria()
	sc.setOperator(sc.SearchOperator.MATCH_ANY_CLAUSES)
	for sample in samples:
		code = sample.getCode()
		sc.addMatchClause(sc.MatchClause.createAttributeMatch(sc.MatchClauseAttribute.CODE, code))
	data_set_sc = SearchCriteria()
	data_set_sc.addMatchClause(data_set_sc.MatchClause.createAttributeMatch(data_set_sc.MatchClauseAttribute.TYPE, "SEQ_FILE"))
	data_set_sc.addSubCriteria(SearchSubCriteria.createSampleCriteria(sc))
	return searchService.searchForDataSets(data_set_sc)


#
# YeastLab iPad Service
#
class YeastLabRootRequestHandler(RootRequestHandler):
	"""Handler for the ROOT request."""

	def retrieve_data(self):
		# Get the data and add a row for each data item
        	sc_oligo = SearchCriteria()
        	sc_oligo.addMatchClause(sc_oligo.MatchClause.createAttributeMatch(sc_oligo.MatchClauseAttribute.TYPE, "OLIGO"))
		self.oligos = self.searchService.searchForSamples(sc_oligo)

		sc_antibody = SearchCriteria()
		sc_antibody.addMatchClause(sc_antibody.MatchClause.createAttributeMatch(sc_antibody.MatchClauseAttribute.TYPE, "ANTIBODY"))
		self.antibodies = self.searchService.searchForSamples(sc_antibody)

		sc_chemical = SearchCriteria()
		sc_chemical.addMatchClause(sc_chemical.MatchClause.createAttributeMatch(sc_chemical.MatchClauseAttribute.TYPE, "CHEMICAL"))
		self.chemicals = self.searchService.searchForSamples(sc_chemical)

		sc_protocol = SearchCriteria()
		sc_protocol.addMatchClause(sc_protocol.MatchClause.createAttributeMatch(sc_protocol.MatchClauseAttribute.TYPE, "GENERAL_PROTOCOL"))
		self.protocols = self.searchService.searchForSamples(sc_protocol)

		sc_media = SearchCriteria()
		sc_media.addMatchClause(sc_media.MatchClause.createAttributeMatch(sc_media.MatchClauseAttribute.TYPE, "MEDIA"))
		self.medias = self.searchService.searchForSamples(sc_media)

		sc_pcr = SearchCriteria()
		sc_pcr.addMatchClause(sc_pcr.MatchClause.createAttributeMatch(sc_pcr.MatchClauseAttribute.TYPE, "PCR"))
		self.pcrs = self.searchService.searchForSamples(sc_pcr)

		sc_buffer = SearchCriteria()
		sc_buffer.addMatchClause(sc_buffer.MatchClause.createAttributeMatch(sc_buffer.MatchClauseAttribute.TYPE, "SOLUTIONS_BUFFERS"))
		self.buffers = self.searchService.searchForSamples(sc_buffer)

                sc_plasmid = SearchCriteria()
                sc_plasmid.addMatchClause(sc_plasmid.MatchClause.createAttributeMatch(sc_plasmid.MatchClauseAttribute.TYPE, "PLASMID"))
                self.plasmids = self.searchService.searchForSamples(sc_plasmid)

                sc_yeast = SearchCriteria()
                sc_yeast.addMatchClause(sc_yeast.MatchClause.createAttributeMatch(sc_yeast.MatchClauseAttribute.TYPE, "YEAST"))
                self.yeasts = self.searchService.searchForSamples(sc_yeast)
		
		self.children_map = dict()
		for plasmid in self.plasmids:
			for parent in plasmid.getParentSampleIdentifiers():
				children = self.children_map.setdefault(parent, [])
				children.append(plasmid)
		for yeast in self.yeasts:
			for parent in yeast.getParentSampleIdentifiers():
				children = self.children_map.setdefault(parent, [])
				children.append(yeast)
			

	def add_data_rows(self):
		self.add_rows(navigation_layer(self.oligos, self.antibodies, self.chemicals, self.protocols, self.medias, self.pcrs, self.buffers, self.plasmids, self.yeasts))
		self.add_rows(oligos_to_dict(self.oligos))
        	self.add_rows(antibodies_to_dict(self.antibodies))
        	self.add_rows(chemicals_to_dict(self.chemicals))
        	self.add_rows(protocols_to_dict(self.protocols))
       	 	self.add_rows(medias_to_dict(self.medias))
        	self.add_rows(pcrs_to_dict(self.pcrs))
        	self.add_rows(buffers_to_dict(self.buffers))
                self.add_rows(plasmids_to_dict(self.plasmids, self.children_map))
                self.add_rows(yeasts_to_dict(self.yeasts, self.children_map))


class YeastLabDrillRequestHandler(DrillRequestHandler):
	"""Handler for the DRILL request."""

	def retrieve_data(self):
		entities = self.parameters['entities']
		drill_navigation = [entity for entity in entities if 'NAVIGATION' == entity['REFCON']['entityType']]
		drill_oligos = [entity for entity in entities if 'OLIGO' == entity['REFCON']['entityType']]
		drill_antibodies = [entity for entity in entities if 'ANTIBODY' == entity['REFCON']['entityType']]
		drill_chemicals = [entity for entity in entities if 'CHEMICAL' == entity['REFCON']['entityType']]	
		drill_protocols = [entity for entity in entities if 'GENERAL_PROTOCOL' == entity['REFCON']['entityType']]	
		drill_medias = [entity for entity in entities if 'MEDIA' == entity['REFCON']['entityType']]	
		drill_pcrs = [entity for entity in entities if 'PCR' == entity['REFCON']['entityType']]	
		drill_buffers = [entity for entity in entities if 'SOLUTIONS_BUFFERS' == entity['REFCON']['entityType']]	
                #drill_plasmids = [entity for entity in entities if 'PLASMID' == entity['REFCON']['entityType']]			
		

		# No information to return for navigation, oligos, or antibodies
		

	def add_data_rows(self):
		pass

class YeastLabDetailRequestHandler(DetailRequestHandler):
	"""Handler for the DETAIL request."""

	def retrieve_data(self):
		# Get the data and add a row for each data item
		entities = self.parameters['entities']
		detail_samples = [entity for entity in entities if 'SAMPLE' == entity['REFCON']['entityKind']]

		self.samples = retrieve_samples(detail_samples)
		self.oligos = [sample for sample in self.samples if 'OLIGO' == sample.getSampleType()]
		self.antibodies = [sample for sample in self.samples if 'ANTIBODY' == sample.getSampleType()]
		self.chemicals = [sample for sample in self.samples if 'CHEMICAL' == sample.getSampleType()]
		self.protocols = [sample for sample in self.samples if 'GENERAL_PROTOCOL' == sample.getSampleType()]
		self.medias = [sample for sample in self.samples if 'MEDIA' == sample.getSampleType()]
		self.pcrs = [sample for sample in self.samples if 'PCR' == sample.getSampleType()]
		self.buffers = [sample for sample in self.samples if 'SOLUTIONS_BUFFERS' == sample.getSampleType()]
                self.plasmids = [sample for sample in self.samples if 'PLASMID' == sample.getSampleType()]
                self.yeasts = [sample for sample in self.samples if 'YEAST' == sample.getSampleType()]
		self.plasmid_data_sets = retrieve_seq_data_sets(self.plasmids)

	def add_data_rows(self):
		self.add_rows(oligos_to_dict(self.oligos))
        	self.add_rows(antibodies_to_dict(self.antibodies))
        	self.add_rows(chemicals_to_dict(self.chemicals))
        	self.add_rows(protocols_to_dict(self.protocols))
        	self.add_rows(medias_to_dict(self.medias))
        	self.add_rows(pcrs_to_dict(self.pcrs))
        	self.add_rows(buffers_to_dict(self.buffers))
                self.add_rows(plasmids_to_dict_with_images(self.plasmids, {}, self.plasmid_data_sets))
                self.add_rows(yeasts_to_dict(self.yeasts, {}))

def aggregate(parameters, builder):
	request_key = parameters.get('requestKey')
	if 'ROOT' == request_key:
		handler = YeastLabRootRequestHandler(parameters, builder)
	elif 'DRILL' == request_key:
		handler = YeastLabDrillRequestHandler(parameters, builder)
	elif 'DETAIL' == request_key:
		handler = YeastLabDetailRequestHandler(parameters, builder)
	else:		
		handler = EmptyDataRequestHandler(parameters, builder)
	handler.process_request()		
