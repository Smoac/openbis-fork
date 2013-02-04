from ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1 import MaterialIdentifierCollection
from ch.systemsx.cisd.openbis.generic.shared.basic.dto import MaterialIdentifier
from com.fasterxml.jackson.databind import ObjectMapper 
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchSubCriteria

import codecs

#
# BEGIN Infrastructure
#

def json_encoded_value(coll):
	"""Utility function for converting a list into a json-encoded list"""
	return ObjectMapper().writeValueAsString(coll)

class RequestHandler(object):
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
		
	def entities_parameter(self):
	  """A helper method to get the value of the entities parameter. Returns an empty list if no entities were specified"""
	  entities = self.parameters.get('entities')
	  if entities is None:
		return []
	  return entities


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
			IMAGES : A map with keys coming from the set 'MARQUEE', 'TILED'. The values are image specs or lists of image specs.
				Image specs are maps with the keys: 'URL' (a URL for the iamge) or 'DATA'. The data key contains a map that
				includes the image data and may include some image metadata as well. This format has not yet been specified.
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
			if entry.get(header):
				row.setCell(header, str(entry.get(header)))
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

class ClientPreferencesRequestHandler(object):
	"""Abstract superclass for the handlers for CLIENT_PREFS request.

	This request has a slightly different structure, since it does not return entities.

	Subclasses should override the preferences_dict method to return the preferences dictionary. The superclass
	implements this method with the default values for the standard keys.
	"""

	def __init__(self, parameters, builder):
		self.parameters = parameters
		self.builder = builder
		self.headers = ['KEY', 'VALUE']

	def preferences_dict(self):
		"""The dictionary containing the value for the client preferences. 

		Subclasses may override if they want to change any of the values. The best way to override is to call
		default_preferences_dict then modify/extend the resulting dictionary"""
		return self.default_preferences_dict()

	def default_preferences_dict(self):
		"""The dictionary containing the standard keys and and default values for those keys"""
		prefs = { 
			# The refresh interval is a value in seconds
			'ROOT_SET_REFRESH_INTERVAL' : 60 * 30 
		}
		return prefs

	def add_data_rows(self):
		"""Take the information from the preferences dict and put it into the table."""
		prefs = self.preferences_dict()
		for key in prefs:
			row = self.builder.addRow()
			row.setCell('KEY', key)
			row.setCell('VALUE', prefs[key])

	def add_headers(self):
		"""Configure the headers for this request.

		For preference request, the headers are 
			KEY : The key of the preference.
			VALUE : The value of the preference.
		"""
		for header in self.headers:
			self.builder.addHeader(header)

	def process_request(self):
		"""Execute the steps necessary to process the request."""
		self.add_headers()
		self.add_data_rows()

class AllDataRequestHandler(RequestHandler):
	"""Abstract Handler for the ALLDATA request."""

	def optional_headers(self):
		return ["CATEGORY", "SUMMARY_HEADER", "SUMMARY", "CHILDREN", "IDENTIFIER", "IMAGES", "PROPERTIES"]

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
		return ["CATEGORY", "SUMMARY_HEADER", "SUMMARY", "IDENTIFIER", "IMAGES", "PROPERTIES"]

class NavigationRequestHandler(RequestHandler):
	"""Abstract Handler for the NAVIGATION request."""

	def optional_headers(self):
		return ["CATEGORY", "SUMMARY_HEADER", "SUMMARY", "ROOT_LEVEL"]

#
# END Infrastructure
#

#
# Helper Methods
# 

def sort_samples_by_type(self, allSamples):

	self.oligos = []
	self.antibodies = []
	self.chemicals = []
	self.protocols = []
	self.medias = []
	self.pcrs = []
	self.buffers = []
	self.plasmids = []
	self.yeasts = []
	self.bacterias = []
	self.enzymes = []
	self.westernBlottings = []

	for sample in allSamples:
		if 'OLIGO' == sample.getSampleType():
			self.oligos.append(sample)
		elif 'ANTIBODY' == sample.getSampleType():
			self.antibodies.append(sample)
		elif 'CHEMICAL' == sample.getSampleType():
			self.chemicals.append(sample)
		elif 'GENERAL_PROTOCOL' == sample.getSampleType():
			self.protocols.append(sample)
		elif 'MEDIA' == sample.getSampleType():
			self.medias.append(sample)
		elif 'PCR' == sample.getSampleType():
			self.pcrs.append(sample)
		elif 'SOLUTIONS_BUFFERS' == sample.getSampleType():
			self.buffers.append(sample)
		elif 'PLASMID' == sample.getSampleType():
			self.plasmids.append(sample)
		elif 'YEAST' == sample.getSampleType():
			self.yeasts.append(sample)
		elif 'BACTERIA' == sample.getSampleType():
			self.bacterias.append(sample)
		elif 'ENZYME' == sample.getSampleType():
			self.enzymes.append(sample)
		elif 'WESTERN_BLOTTING' == sample.getSampleType():
			self.westernBlottings.append(sample)

def retrieve_sample_type_properties_definitions(sample_type):
	"""Return the property definitions for each of the referenced entity types.

	The definitions are sorted according to display order.
	"""
	definitions = list(searchService.listPropertiesDefinitionsForSampleType(sample_type))
	definitions.sort(lambda x, y: cmp(x.getPositionInForms(), y.getPositionInForms()))
	return definitions

def properties_for_entity(entity, property_definitions, prop_names_set):
	"""Extract the properties, in the correct order, for the entity. Restricting them to those in the prop_names_set, if it is non-empty"""
	properties = []
	check_prop_names_set = len(prop_names_set) > 0
	for propdef in property_definitions:
		propcode = propdef.getPropertyTypeCode()
		# Only include the properties we explicitly specify
		if check_prop_names_set and propcode not in prop_names_set:
			continue
		value = entity.getPropertyValue(propcode)
		prop = {'key' : propcode, 'label' : propdef.getPropertyTypeLabel(), 'value' : value }
		properties.append(prop)
	return properties

def marquee_image_spec_for_url(image_url):
	return { 'MARQUEE' : { 'URL' : image_url } }

def navigation_layer(oligos, antibodies, chemicals, protocols, medias, pcrs, buffers, plasmids, yeasts, bacterias, enzymes, westernBlottings):
	oligo_dict = {}
	oligo_dict["SUMMARY_HEADER"] = "oligo"
	oligo_dict["SUMMARY"] = "Oligos in YeastLab"
	oligo_dict['PERM_ID'] = "OLIGO"
	refcon_oligo = {}
	refcon_oligo['code'] =	"OLIGO"
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
	refcon_media['code'] =	"MEDIA"
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
	refcon_buffer['code'] =	 "SOLUTIONS_BUFFERS"
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
	refcon_yeast['code'] =	"YEAST"
	refcon_yeast['entityKind'] = 'NAVIGATION'
	refcon_yeast['entityType'] = 'NAVIGATION'
	yeast_dict['REFCON'] = json_encoded_value(refcon_yeast)
	yeast_dict['CATEGORY'] = None
	children = [yeast.getPermId() for yeast in yeasts]
	yeast_dict['CHILDREN'] = json_encoded_value(children)
	yeast_dict['PROPERTIES'] = json_encoded_value([])
	yeast_dict['ROOT_LEVEL'] = True	   
	
	
	bacteria_dict = {}
	bacteria_dict["SUMMARY_HEADER"] = "bacteria"
	bacteria_dict["SUMMARY"] = "Bacteria in YeastLab"
	bacteria_dict['PERM_ID'] = "BACTERIA"
	refcon_bacteria = {}
	refcon_bacteria['code'] =  "BACTERIA"
	refcon_bacteria['entityKind'] = 'NAVIGATION'
	refcon_bacteria['entityType'] = 'NAVIGATION'
	bacteria_dict['REFCON'] = json_encoded_value(refcon_bacteria)
	bacteria_dict['CATEGORY'] = None
	children = [bacteria.getPermId() for bacteria in bacterias]
	bacteria_dict['CHILDREN'] = json_encoded_value(children)
	bacteria_dict['PROPERTIES'] = json_encoded_value([])
	bacteria_dict['ROOT_LEVEL'] = True	  

	enzyme_dict = {}
	enzyme_dict["SUMMARY_HEADER"] = "enzyme"
	enzyme_dict["SUMMARY"] = "Enzymes in YeastLab"
	enzyme_dict['PERM_ID'] = "ENZYME"
	refcon_enzyme = {}
	refcon_enzyme['code'] =	 "ENZYME"
	refcon_enzyme['entityKind'] = 'NAVIGATION'
	refcon_enzyme['entityType'] = 'NAVIGATION'
	enzyme_dict['REFCON'] = json_encoded_value(refcon_enzyme)
	enzyme_dict['CATEGORY'] = None
	children = [enzyme.getPermId() for enzyme in enzymes]
	enzyme_dict['CHILDREN'] = json_encoded_value(children)
	enzyme_dict['PROPERTIES'] = json_encoded_value([])
	enzyme_dict['ROOT_LEVEL'] = True	


	westernBlotting_dict = {}
	westernBlotting_dict["SUMMARY_HEADER"] = "western blotting"
	westernBlotting_dict["SUMMARY"] = "Western Blotting in YeastLab"
	westernBlotting_dict['PERM_ID'] = "WESTERN_BLOTTING"
	refcon_westernBlotting = {}
	refcon_westernBlotting['code'] =  "WESTERN_BLOTTING"
	refcon_westernBlotting['entityKind'] = 'NAVIGATION'
	refcon_westernBlotting['entityType'] = 'NAVIGATION'
	westernBlotting_dict['REFCON'] = json_encoded_value(refcon_westernBlotting)
	westernBlotting_dict['CATEGORY'] = None
	children = [westernBlotting.getPermId() for westernBlotting in westernBlottings]
	westernBlotting_dict['CHILDREN'] = json_encoded_value(children)
	westernBlotting_dict['PROPERTIES'] = json_encoded_value([])
	westernBlotting_dict['ROOT_LEVEL'] = True 

	return [oligo_dict, antibody_dict, chemical_dict, protocol_dict, media_dict, pcr_dict, buffer_dict, plasmid_dict, yeast_dict, bacteria_dict, enzyme_dict, westernBlotting_dict]

def sample_to_dict_with_props(sample, want_props):
	"""Convert a sample to a dictionary. Uses the NAME property to construct the summary. Returns empty children. Callers may need to modify the summary and children as well"""
	sample_dict = {}
	sample_dict['SUMMARY_HEADER'] = sample.getCode()
	summary = "Name: " + unicode(sample.getPropertyValue("NAME"))
	sample_dict['SUMMARY'] = summary
	sample_dict['IDENTIFIER'] = sample.getSampleIdentifier()
	sample_dict['PERM_ID'] = sample.getPermId()
	refcon_sample = {}
	refcon_sample['code'] =	 sample.getCode()
	refcon_sample['entityKind'] = 'SAMPLE'
	refcon_sample['entityType'] = sample.getSampleType()
	sample_dict['REFCON'] = json_encoded_value(refcon_sample)
	sample_dict['CATEGORY'] = sample.getSampleType()
	children = []
	sample_dict['CHILDREN'] = json_encoded_value(children)

	if want_props:
		property_definitions = retrieve_sample_type_properties_definitions(sample.getSampleType())
		properties_sample = properties_for_entity(sample, property_definitions, [])
		sample_dict['PROPERTIES'] = json_encoded_value(properties_sample)

		sample_dict['ROOT_LEVEL'] = None
		return sample_dict
	

def oligo_to_dict(oligo, want_props):
	sample_dict = sample_to_dict_with_props(oligo, want_props)
	summary = "Project: " + str(oligo.getPropertyValue("PROJECT"))
	summary = summary + "\n"
	summary = summary + "Target: " + oligo.getPropertyValue("TARGET")
	sample_dict['SUMMARY'] = summary
	return sample_dict

def antibody_to_dict(antibody, want_props):
	sample_antibody_dict = sample_to_dict_with_props(antibody, want_props)
	summary = "Name: " + str(antibody.getPropertyValue("NAME"))
	summary = summary + "\n"
	summary = summary + "Epitope: " + str(antibody.getPropertyValue("EPITOPE"))
	sample_antibody_dict['SUMMARY'] = summary
	return sample_antibody_dict

def chemical_to_dict(chemical, want_props):
	return sample_to_dict_with_props(chemical, want_props)

def protocol_to_dict(protocol, want_props):
		return sample_to_dict_with_props(protocol, want_props)

def media_to_dict(media, want_props):
		return sample_to_dict_with_props(media, want_props)

def pcr_to_dict(pcr, want_props):
		return sample_to_dict_with_props(pcr, want_props)

def buffer_to_dict(buffer, want_props):
		return sample_to_dict_with_props(buffer, want_props)

def plasmid_to_dict(plasmid, children_map, want_props):
	sample_dict = sample_to_dict_with_props(plasmid, want_props)

	summary = "Name: " + str(plasmid.getPropertyValue("PLASMID_NAME"))
	sample_dict['SUMMARY'] = summary
	children = [child.getPermId() for child in children_map.get(plasmid.getSampleIdentifier(), [])]
	sample_dict['CHILDREN'] = json_encoded_value(children)
	return sample_dict

def plasmid_to_dict_with_images(plasmid, children_map, data_sets):
	plasmid_dict = plasmid_to_dict(plasmid, children_map, True)
	plasmid_data_set = None
	for data_set in data_sets:
		if data_set.getSample().getSampleIdentifier() == plasmid.getSampleIdentifier():
			plasmid_data_set = data_set
			break
	if plasmid_data_set is None:
		return plasmid_dict
	image_url = 'https://openbis-csb.ethz.ch:8444/datastore_server/' + plasmid_data_set.getDataSetCode() + '/generated/'
	image_url = image_url + plasmid.getCode() + '.svg'
	plasmid_dict['IMAGE_URL'] = image_url
	plasmid_dict['IMAGES'] = json_encoded_value(marquee_image_spec_for_url(image_url))
	return plasmid_dict
	
def yeast_to_dict(yeast, children_map, want_props):
		sample_dict = sample_to_dict_with_props(yeast, want_props)
		summary = "Name: " + str(yeast.getPropertyValue("YEAST_STRAIN_NAME"))
		sample_dict['SUMMARY'] = summary

		children = [child.getPermId() for child in children_map.get(yeast.getSampleIdentifier(), [])]
		sample_dict['CHILDREN'] = json_encoded_value(children)

		return sample_dict

def bacteria_to_dict(bacteria, with_props):
	sample_dict = sample_to_dict_with_props(bacteria, with_props)
	name = str(bacteria.getPropertyValue("BACTERIA_STRAIN_NAME").encode('utf-8'))
	summary = "Name: " + name
	sample_dict['SUMMARY'] = summary
	return sample_dict

def enzyme_to_dict(enzyme, with_props):
	sample_dict = sample_to_dict_with_props(enzyme, with_props)
	return sample_dict

def westernBlotting_to_dict(westernBlotting, with_props):
	return sample_to_dict_with_props(westernBlotting, with_props)

def oligos_to_dict(samples, want_props):
	result = [oligo_to_dict(sample, want_props) for sample in samples]
	return result

def antibodies_to_dict(samples, want_props):
	result = [antibody_to_dict(sample, want_props) for sample in samples]
	return result

def chemicals_to_dict(samples, want_props):
	result = [chemical_to_dict(sample, want_props) for sample in samples]
	return result

def protocols_to_dict(samples, want_props):
	result = [protocol_to_dict(sample, want_props) for sample in samples]
	return result

def medias_to_dict(samples, want_props):
	result = [media_to_dict(sample, want_props) for sample in samples]
	return result

def pcrs_to_dict(samples, want_props):
	result = [pcr_to_dict(sample, want_props) for sample in samples]
	return result

def buffers_to_dict(samples, want_props):
	result = [buffer_to_dict(sample, want_props) for sample in samples]
	return result

def plasmids_to_dict(samples, children_map, want_props):
	result = [plasmid_to_dict(sample, children_map, want_props) for sample in samples]
	return result

def plasmids_to_dict_with_images(samples, children_map, data_sets):
	result = [plasmid_to_dict_with_images(sample, children_map, data_sets) for sample in samples]
	return result

def yeasts_to_dict(samples, children_map, want_props):
	result = [yeast_to_dict(sample, children_map, want_props) for sample in samples]
	return result

def bacterias_to_dict(samples, want_props):
	result = [bacteria_to_dict(sample, want_props) for sample in samples]
	return result

def enzymes_to_dict(samples, want_props):
	result = [enzyme_to_dict(sample, want_props) for sample in samples]
	return result

def westernBlottings_to_dict(samples, want_props):
	result = [westernBlotting_to_dict(sample, want_props) for sample in samples]
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

class YeastLabClientPreferencesRequestHandler(ClientPreferencesRequestHandler):
	"""Handler for the CLIENT_PREFS request."""

class YeastLabNavigationRequestHandler(NavigationRequestHandler):
	"""Handler for the NAVIGATION request"""
	def add_data_rows(self):
		self.add_rows(navigation_layer([], [], [], [], [], [], [], [], [], [], [], []))

class YeastLabRootRequestHandler(RootRequestHandler):
	"""Handler for the ROOT request."""
	def retrieve_data(self):
		all_samples_sc = SearchCriteria()
		all_samples_sc.setOperator(SearchCriteria.SearchOperator.MATCH_ANY_CLAUSES)
		all_samples_sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.TYPE, "OLIGO"))
		all_samples_sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.TYPE, "ANTIBODY"))
		all_samples_sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.TYPE, "CHEMICAL"))
		all_samples_sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.TYPE, "GENERAL_PROTOCOL"))
		all_samples_sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.TYPE, "MEDIA"))
		all_samples_sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.TYPE, "PCR"))
		all_samples_sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.TYPE, "SOLUTIONS_BUFFERS"))
		all_samples_sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.TYPE, "PLASMID"))
		all_samples_sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.TYPE, "YEAST"))
		all_samples_sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.TYPE, "BACTERIA"))
		all_samples_sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.TYPE, "ENZYME"))
		all_samples_sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.TYPE, "WESTERN_BLOTTING"))
		self.samples = self.searchService.searchForSamples(all_samples_sc)

		# Sort out the results
		self.sort_samples_by_type(self.samples)
		
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
		self.add_rows(navigation_layer(self.oligos, self.antibodies, self.chemicals, self.protocols, self.medias, self.pcrs, self.buffers, self.plasmids, self.yeasts, self.bacterias, self.enzymes, self.westernBlottings))
		self.add_rows(oligos_to_dict(self.oligos, False))
		self.add_rows(antibodies_to_dict(self.antibodies, False))
		self.add_rows(chemicals_to_dict(self.chemicals, False))
		self.add_rows(protocols_to_dict(self.protocols, False))
		self.add_rows(medias_to_dict(self.medias, False))
		self.add_rows(pcrs_to_dict(self.pcrs, False))
		self.add_rows(buffers_to_dict(self.buffers, False))
		self.add_rows(plasmids_to_dict(self.plasmids, self.children_map, False))            	
		self.add_rows(yeasts_to_dict(self.yeasts, self.children_map, False))
		self.add_rows(bacterias_to_dict(self.bacterias, False))
		self.add_rows(enzymes_to_dict(self.enzymes, False))
		self.add_rows(westernBlottings_to_dict(self.westernBlottings, False))

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
		drill_bacterias = [entity for entity in entities if 'BACTERIA' == entity['REFCON']['entityType']]	
		drill_enzymes = [entity for entity in entities if 'ENZYME' == entity['REFCON']['entityType']]	
		drill_westernBlottings = [entity for entity in entities if 'WESTERN_BLOTTING' == entity['REFCON']['entityType']]	

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
		self.sort_samples_by_type(self.samples)
		self.plasmid_data_sets = retrieve_seq_data_sets(self.plasmids)

	def add_data_rows(self):
		self.add_rows(oligos_to_dict(self.oligos, True))
		self.add_rows(antibodies_to_dict(self.antibodies, True))
		self.add_rows(chemicals_to_dict(self.chemicals, True))
		self.add_rows(protocols_to_dict(self.protocols, True))
		self.add_rows(medias_to_dict(self.medias, True))
		self.add_rows(pcrs_to_dict(self.pcrs, True))
		self.add_rows(buffers_to_dict(self.buffers, True))
		self.add_rows(plasmids_to_dict_with_images(self.plasmids, {}, self.plasmid_data_sets))
		self.add_rows(yeasts_to_dict(self.yeasts, {}, True))
		self.add_rows(bacterias_to_dict(self.bacterias, True))
		self.add_rows(enzymes_to_dict(self.enzymes, True))
		self.add_rows(westernBlottings_to_dict(self.westernBlottings, True))	  
			

def aggregate(parameters, builder):
	request_key = parameters.get('requestKey')
	if 'CLIENT_PREFS' == request_key:
		handler = YeastLabClientPreferencesRequestHandler(parameters, builder)
	elif 'NAVIGATION' == request_key:
		handler = YeastLabNavigationRequestHandler(parameters, builder)
	elif 'ROOT' == request_key:
		handler = YeastLabRootRequestHandler(parameters, builder)
	elif 'DRILL' == request_key:
		handler = YeastLabDrillRequestHandler(parameters, builder)
	elif 'DETAIL' == request_key:
		handler = YeastLabDetailRequestHandler(parameters, builder)
	else:		
		handler = EmptyDataRequestHandler(parameters, builder)
	handler.process_request()		
