from ch.systemsx.cisd.openbis.ipad.v2.server import IRequestHandler, AbstractRequestHandler, ClientPreferencesRequestHandler, RootRequestHandler
from ch.systemsx.cisd.openbis.ipad.v2.server import DrillRequestHandler, NavigationRequestHandler, DetailRequestHandler, SearchRequestHandler
from ch.systemsx.cisd.openbis.ipad.v2.server import EmptyDataRequestHandler, IpadServiceUtilities
from ch.systemsx.cisd.openbis.ipad.v2.server import IRequestHandlerFactory, RequestHandlerDispatcher

from ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1 import MaterialIdentifierCollection
from ch.systemsx.cisd.openbis.generic.shared.basic.dto import MaterialIdentifier
from com.fasterxml.jackson.databind import ObjectMapper 
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchSubCriteria
from ch.systemsx.cisd.openbis.generic.shared.managed_property import ManagedPropertyFunctions

import codecs

#
# BEGIN Infrastructure
#

def json_encoded_value(coll):
	"""Utility function for converting a list into a json-encoded list"""
	return IpadServiceUtilities.jsonEncodedValue(coll)

def json_empty_list():
  """Utility function to return an json-encoded empty list"""
  return IpadServiceUtilities.jsonEmptyList()

def json_empty_dict():
  """Utility function to return an json-encoded empty dictionary"""
  return IpadServiceUtilities.jsonEmptyDict()

class RequestHandler(IRequestHandler):
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
			value = entry.get(header)
			if value is not None:
				row.setCell(header, value)
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

	def processRequest(self):
		self.process_request()

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

class SearchRequestHandler(RequestHandler):
	"""Abstract Handler for the SEARCH request."""

	def optional_headers(self):
		return ["CATEGORY", "SUMMARY_HEADER", "SUMMARY", "CHILDREN"]

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

def retrieve_sample_type_properties_definitions(sample_type):
	"""Return the property definitions for each of the referenced entity types.

	The definitions are sorted according to display order.
	"""
	definitions = list(searchService.listPropertiesDefinitionsForSampleType(sample_type))
	definitions.sort(lambda x, y: cmp(x.getPositionInForms(), y.getPositionInForms()))
	return definitions

def properties_for_entity_ignoring(entity, property_definitions, ignored_properties):
	"""Extract the properties, in the correct order, for the entity. 

	If ignored_properties is non-empty, do not include those"""
	properties = []
	check_ignored_properties = len(ignored_properties) > 0
	for propdef in property_definitions:
		propcode = propdef.getPropertyTypeCode()
		# Skip the properties that are designated to be ignored
		if check_ignored_properties and propcode in ignored_properties:
			continue
		value = entity.getPropertyValue(propcode)
		if value == u'\ufffd(undefined)':
			value = None
		prop = {'key' : propcode, 'label' : propdef.getPropertyTypeLabel(), 'value' : value }
		properties.append(prop)
	return properties

def ordered_properties_for_sample_ignoring(sample, props_to_ignore):
	property_definitions = retrieve_sample_type_properties_definitions(sample.getSampleType())
	properties = properties_for_entity_ignoring(sample, property_definitions, props_to_ignore)
	return properties

def json_encoded_children_from_link_props(entity, link_props):
	children = []
	converter = ManagedPropertyFunctions.xmlPropertyConverter()
	for prop in link_props:
		if entity.getPropertyValue(prop) is None: 
			continue	  
		elements = converter.convertStringToElements(entity.getPropertyValue(prop))
		children.extend([element.getAttribute("permId") for element in elements])
	return json_encoded_value(children)

def replace_link_props_with_desc(entity, props_list, link_props):
	converter = ManagedPropertyFunctions.xmlPropertyConverter()
	for prop_key in link_props:
		lines = []
		elements = converter.convertStringToElements(entity.getPropertyValue(prop_key))
		for element in elements:
			line = u"" + element.getAttribute("name")
			line = line + " [" + element.getAttribute("code") + "]"
			quantity = element.getAttribute("quantity", None)
			if quantity:
				line = line + " : " + quantity
			lines.append(line)
		desc = u'\n'.join(lines)
		for prop in props_list:
			if prop['key'] == prop_key:
				prop['value'] = desc
				break

def marquee_image_spec_for_url(image_url):
	return { 'MARQUEE' : { 'URL' : image_url } }

def navigation_layer_simple(summary_header, summary, code, children):
	nav_dict = {}
	nav_dict["SUMMARY_HEADER"] = summary_header
	nav_dict["SUMMARY"] = summary
	nav_dict['PERM_ID'] = code
	refcon_nav = {}
	refcon_nav['code'] = code
	refcon_nav['entityKind'] = 'NAVIGATION'
	refcon_nav['entityType'] = 'NAVIGATION'
	nav_dict['REFCON'] = json_encoded_value(refcon_nav)
	nav_dict['CATEGORY'] = None
	children_permids = [entity.getPermId() for entity in children]
	nav_dict['CHILDREN'] = json_encoded_value(children_permids)
	nav_dict['PROPERTIES'] = json_empty_list()
	nav_dict['ROOT_LEVEL'] = True
	return nav_dict

def oligo_navigation_layer(oligos):
	return navigation_layer_simple("oligo", "Oligos in YeastLab", "OLIGO", oligos)

def antibody_navigation_layer(antibodies):
	return navigation_layer_simple("antibody", "Antibodies in YeastLab", "ANTIBODY", antibodies)

def chemical_navigation_layer(chemicals):
	return navigation_layer_simple("chemical", "Chemicals in YeastLab", "CHEMICAL", chemicals)

def protocol_navigation_layer(protocols):
	return navigation_layer_simple("protocol", "Protocols in YeastLab", "GENERAL_PROTOCOL", protocols)

def media_navigation_layer(medias):
	return navigation_layer_simple("media", "Medias in YeastLab", "MEDIA", medias)

def pcr_navigation_layer(pcrs):
	return navigation_layer_simple("pcr", "PCR in YeastLab", "PCR", pcrs)

def buffer_navigation_layer(buffers):
	return navigation_layer_simple("buffer", "Solution Buffers in YeastLab", "SOLUTIONS_BUFFERS", buffers)

def plasmid_navigation_layer(plasmids):
	return navigation_layer_simple("plasmid", "Plasmids in YeastLab", "PLASMID", plasmids)

def yeast_navigation_layer(yeasts):
	return navigation_layer_simple("yeast", "Yeasts in YeastLab", "YEAST", yeasts)

def bacteria_navigation_layer(bacterias):
	return navigation_layer_simple("bacteria", "Bacteria in YeastLab", "BACTERIA", bacterias)

def enzyme_navigation_layer(enzymes):
	return navigation_layer_simple("enzyme", "Enzymes in YeastLab", "ENZYME", enzymes)

def western_blotting_navigation_layer(westernBlottings):
	return navigation_layer_simple("western blotting", "Western Blotting in YeastLab", "WESTERN_BLOTTING", westernBlottings)

def navigation_layer(oligos, antibodies, chemicals, protocols, medias, pcrs, buffers, plasmids, yeasts, bacterias, enzymes, westernBlottings):
	oligo_dict = oligo_navigation_layer(oligos)
	antibody_dict = antibody_navigation_layer(antibodies)
	chemical_dict = chemical_navigation_layer(chemicals)
	protocol_dict = protocol_navigation_layer(protocols)
	media_dict = media_navigation_layer(medias)
	pcr_dict = pcr_navigation_layer(pcrs)
	buffer_dict = buffer_navigation_layer(buffers)
	plasmid_dict = plasmid_navigation_layer(plasmids)
	yeast_dict = yeast_navigation_layer(yeasts)
	bacteria_dict = bacteria_navigation_layer(bacterias)
	enzyme_dict = enzyme_navigation_layer(enzymes)
	westernBlotting_dict = western_blotting_navigation_layer(westernBlottings)
	return [oligo_dict, antibody_dict, chemical_dict, protocol_dict, media_dict, pcr_dict, buffer_dict, plasmid_dict, yeast_dict, bacteria_dict, enzyme_dict, westernBlotting_dict]

def sample_to_dict_with_props_ignoring(sample, want_props, props_to_ignore):
	"""Convert a sample to a dictionary, ignoring the specified properties.

	Uses the NAME property to construct the summary. 
	Returns empty children. 
	Callers may need to modify the summary and children as well
	"""
	sample_dict = {}
	sample_dict['SUMMARY_HEADER'] = sample.getCode()
	name = sample.getPropertyValue("NAME")
	if name is not None:
		summary = u"Name: " + name
	else:
		summary = u"??"
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
		properties_sample = ordered_properties_for_sample_ignoring(sample, props_to_ignore)
		sample_dict['PROPERTIES'] = json_encoded_value(properties_sample)

	sample_dict['ROOT_LEVEL'] = None
	return sample_dict

def sample_to_dict_with_props(sample, want_props):
	"""Convert a sample to a dictionary. 

	Uses the NAME property to construct the summary. 
	Returns empty children. 
	Callers may need to modify the summary and children as well
	"""
	return sample_to_dict_with_props_ignoring(sample, want_props, [])
	

def oligo_to_dict(oligo, want_props):
	sample_dict = sample_to_dict_with_props(oligo, want_props)
	summary = u"Project: " + unicode(oligo.getPropertyValue("PROJECT"))
	summary = summary + "\n"
	summary = summary + "Target: " + unicode(oligo.getPropertyValue("TARGET"))
	sample_dict['SUMMARY'] = summary
	return sample_dict

def antibody_to_dict(antibody, want_props):
	sample_antibody_dict = sample_to_dict_with_props(antibody, want_props)
	summary = u"Name: " + unicode(antibody.getPropertyValue("NAME"))
	summary = summary + u"\n"
	summary = summary + u"Epitope: " + unicode(antibody.getPropertyValue("EPITOPE"))
	sample_antibody_dict['SUMMARY'] = summary
	return sample_antibody_dict

def chemical_to_dict(chemical, want_props):
	return sample_to_dict_with_props(chemical, want_props)

def protocol_to_dict(protocol, want_props):
	protocol_dict = sample_to_dict_with_props(protocol, False)
	link_props = ["CHEMICALS", "SOLUTIONS_BUFFERS", "MEDIA", "GENERAL_PROTOCOL", "ENZYMES"]
	protocol_dict['CHILDREN'] = json_encoded_children_from_link_props(protocol, link_props)
	if want_props:
		props = ordered_properties_for_sample_ignoring(protocol, [])
		replace_link_props_with_desc(protocol, props, link_props)
		protocol_dict['PROPERTIES'] = json_encoded_value(props)
	return protocol_dict

def media_to_dict(media, want_props):
		return sample_to_dict_with_props(media, want_props)

def pcr_to_dict(pcr, want_props):
		return sample_to_dict_with_props(pcr, want_props)

def buffer_to_dict(buffer, want_props):
		return sample_to_dict_with_props(buffer, want_props)

def plasmid_to_dict(plasmid, children_map, want_props):
	sample_dict = sample_to_dict_with_props(plasmid, want_props)

	summary = u"Name: " + plasmid.getPropertyValue("PLASMID_NAME")
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
		summary = u"Name: " + unicode(yeast.getPropertyValue("YEAST_STRAIN_NAME"))
		sample_dict['SUMMARY'] = summary

		children = [child.getPermId() for child in children_map.get(yeast.getSampleIdentifier(), [])]
		sample_dict['CHILDREN'] = json_encoded_value(children)

		return sample_dict

def bacteria_to_dict(bacteria, with_props):
	sample_dict = sample_to_dict_with_props(bacteria, with_props)
	name = bacteria.getPropertyValue("BACTERIA_STRAIN_NAME")
	summary = u"Name: " + unicode(name)
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
	if len(sample_perm_ids_and_ref_cons) < 1:
		return []
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
		oligo_dict = oligo_navigation_layer([])
		antibody_dict = antibody_navigation_layer([])
		chemical_dict = chemical_navigation_layer([])
		protocol_dict = protocol_navigation_layer([])
		media_dict = media_navigation_layer([])
		pcr_dict = pcr_navigation_layer([])
		buffer_dict = buffer_navigation_layer([])
		plasmid_dict = plasmid_navigation_layer([])
		yeast_dict = yeast_navigation_layer([])
		bacteria_dict = bacteria_navigation_layer([])
		enzyme_dict = enzyme_navigation_layer([])
		westernBlotting_dict = western_blotting_navigation_layer([])
		navigation_rows = [oligo_dict, antibody_dict, chemical_dict, protocol_dict, media_dict, pcr_dict, buffer_dict, plasmid_dict, yeast_dict, bacteria_dict, enzyme_dict, westernBlotting_dict]
		self.add_rows(navigation_rows)

class YeastLabRootRequestHandler(RootRequestHandler):
	"""Handler for the ROOT request."""

	def add_match_clause_for_type(self, all_samples_sc, sample_type):
		all_samples_sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.TYPE, sample_type))	

	def add_match_clause(self, all_samples_sc, sample_type, nav_perm_ids):
		if sample_type in nav_perm_ids:
			self.add_match_clause_for_type(all_samples_sc, sample_type)

	def add_nav_layer_and_data_rows(self, nav_layer, rows):
		self.add_rows([nav_layer])
		self.add_rows(rows)

	def add_requested_data_rows(self, nav_layer, rows, sample_type, nav_perm_ids):
		if sample_type in nav_perm_ids:
			self.add_nav_layer_and_data_rows(nav_layer, rows)

	def sort_samples_by_type(self, allSamples):
		samplesByType = IpadServiceUtilities.groupSamplesByType(allSamples)
		self.oligos = samplesByType.getSamples('OLIGO')
		self.antibodies = samplesByType.getSamples('ANTIBODY')
		self.chemicals = samplesByType.getSamples('CHEMICAL')
		self.protocols = samplesByType.getSamples('GENERAL_PROTOCOL')
		self.medias = samplesByType.getSamples('MEDIA')
		self.pcrs = samplesByType.getSamples('PCR')
		self.buffers = samplesByType.getSamples('SOLUTIONS_BUFFERS')
		self.plasmids = samplesByType.getSamples('PLASMID')
		self.yeasts = samplesByType.getSamples('YEAST')
		self.bacterias = samplesByType.getSamples('BACTERIA')
		self.enzymes = samplesByType.getSamples('ENZYME')
		self.westernBlottings = samplesByType.getSamples('WESTERN_BLOTTING')			

	def retrieve_data(self):
		all_samples_sc = SearchCriteria()
		all_samples_sc.setOperator(SearchCriteria.SearchOperator.MATCH_ANY_CLAUSES)

			# Check which navigational entities are being requested here
		nav_entities = self.entities_parameter()
		print "Root: %s" % nav_entities
		nav_perm_ids = [entity['PERM_ID'] for entity in nav_entities]
		print "Root: %s" % nav_perm_ids
		self.add_match_clause(all_samples_sc, "OLIGO", nav_perm_ids)
		self.add_match_clause(all_samples_sc, "ANTIBODY", nav_perm_ids)
		self.add_match_clause(all_samples_sc, "CHEMICAL", nav_perm_ids)
		self.add_match_clause(all_samples_sc, "GENERAL_PROTOCOL", nav_perm_ids)
		self.add_match_clause(all_samples_sc, "MEDIA", nav_perm_ids)
		self.add_match_clause(all_samples_sc, "PCR", nav_perm_ids)
		self.add_match_clause(all_samples_sc, "SOLUTIONS_BUFFERS", nav_perm_ids)
		if "PLASMID" in nav_perm_ids or "YEAST" in nav_perm_ids:
			self.add_match_clause_for_type(all_samples_sc, "PLASMID")
			self.add_match_clause_for_type(all_samples_sc, "YEAST")
		self.add_match_clause(all_samples_sc, "BACTERIA", nav_perm_ids)
		self.add_match_clause(all_samples_sc, "ENZYME", nav_perm_ids)
		self.add_match_clause(all_samples_sc, "WESTERN_BLOTTING", nav_perm_ids)
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
		nav_entities = self.entities_parameter()
		nav_perm_ids = [entity['PERM_ID'] for entity in nav_entities]

		self.add_requested_data_rows(oligo_navigation_layer(self.oligos), oligos_to_dict(self.oligos, False), "OLIGO", nav_perm_ids)
		self.add_requested_data_rows(antibody_navigation_layer(self.antibodies), antibodies_to_dict(self.antibodies, False), "ANTIBODY", nav_perm_ids)
		self.add_requested_data_rows(chemical_navigation_layer(self.chemicals), chemicals_to_dict(self.chemicals, False), "CHEMICAL", nav_perm_ids)
		self.add_requested_data_rows(protocol_navigation_layer(self.protocols), protocols_to_dict(self.protocols, False), "GENERAL_PROTOCOL", nav_perm_ids)
		self.add_requested_data_rows(media_navigation_layer(self.medias), medias_to_dict(self.medias, False), "MEDIA", nav_perm_ids)
		self.add_requested_data_rows(pcr_navigation_layer(self.pcrs), pcrs_to_dict(self.pcrs, False), "PCR", nav_perm_ids)
		self.add_requested_data_rows(buffer_navigation_layer(self.buffers), buffers_to_dict(self.buffers, False), "SOLUTIONS_BUFFERS", nav_perm_ids)
		if "PLASMID" in nav_perm_ids or "YEAST" in nav_perm_ids:
			self.add_nav_layer_and_data_rows(plasmid_navigation_layer(self.plasmids), plasmids_to_dict(self.plasmids, self.children_map, False))
			self.add_nav_layer_and_data_rows(yeast_navigation_layer(self.yeasts), yeasts_to_dict(self.yeasts, self.children_map, False))
		self.add_requested_data_rows(bacteria_navigation_layer(self.bacterias), bacterias_to_dict(self.bacterias, False), "BACTERIA", nav_perm_ids)
		self.add_requested_data_rows(enzyme_navigation_layer(self.enzymes), enzymes_to_dict(self.enzymes, False), "ENZYME", nav_perm_ids)
		self.add_requested_data_rows(western_blotting_navigation_layer(self.westernBlottings), westernBlottings_to_dict(self.westernBlottings, False), "WESTERN_BLOTTING", nav_perm_ids)		


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
		drill_plasmids = [entity for entity in entities if 'PLASMID' == entity['REFCON']['entityType']]
		drill_bacterias = [entity for entity in entities if 'BACTERIA' == entity['REFCON']['entityType']]	
		drill_enzymes = [entity for entity in entities if 'ENZYME' == entity['REFCON']['entityType']]	
		drill_westernBlottings = [entity for entity in entities if 'WESTERN_BLOTTING' == entity['REFCON']['entityType']]	

		# No information to return for navigation, oligos, or antibodies
		

	def add_data_rows(self):
		pass

class YeastLabSearchRequestHandler(SearchRequestHandler):
	"""Handler for the SEARCH request."""

	def add_match_clause_for_type(self, all_samples_sc, sample_type):
		all_samples_sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.TYPE, sample_type))	

	def add_match_clause(self, all_samples_sc, sample_type, nav_perm_ids):
		if sample_type in nav_perm_ids:
			self.add_match_clause_for_type(all_samples_sc, sample_type)

	def add_nav_layer_and_data_rows(self, nav_layer, rows):
		self.add_rows([nav_layer])
		self.add_rows(rows)

	def add_requested_data_rows(self, nav_layer, rows, sample_type, nav_perm_ids):
		if sample_type in nav_perm_ids:
			self.add_nav_layer_and_data_rows(nav_layer, rows)

	def sort_samples_by_type(self, allSamples):
		samplesByType = IpadServiceUtilities.groupSamplesByType(allSamples)
		self.oligos = samplesByType.getSamples('OLIGO')
		self.antibodies = samplesByType.getSamples('ANTIBODY')
		self.chemicals = samplesByType.getSamples('CHEMICAL')
		self.protocols = samplesByType.getSamples('GENERAL_PROTOCOL')
		self.medias = samplesByType.getSamples('MEDIA')
		self.pcrs = samplesByType.getSamples('PCR')
		self.buffers = samplesByType.getSamples('SOLUTIONS_BUFFERS')
		self.plasmids = samplesByType.getSamples('PLASMID')
		self.yeasts = samplesByType.getSamples('YEAST')
		self.bacterias = samplesByType.getSamples('BACTERIA')
		self.enzymes = samplesByType.getSamples('ENZYME')
		self.westernBlottings = samplesByType.getSamples('WESTERN_BLOTTING')

	def retrieve_data(self):
		print "SearchRequestHandler: retrieve_data"
		all_samples_sc = SearchCriteria()
		all_samples_sc.addMatchClause(SearchCriteria.MatchClause.createAnyFieldMatch(self.parameters['searchtext']))
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
		print "SearchRequestHandler: add_data_rows"
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

class YeastLabDetailRequestHandler(DetailRequestHandler):
	"""Handler for the DETAIL request."""

	def sort_samples_by_type(self, allSamples):
		samplesByType = IpadServiceUtilities.groupSamplesByType(allSamples)
		self.oligos = samplesByType.getSamples('OLIGO')
		self.antibodies = samplesByType.getSamples('ANTIBODY')
		self.chemicals = samplesByType.getSamples('CHEMICAL')
		self.protocols = samplesByType.getSamples('GENERAL_PROTOCOL')
		self.medias = samplesByType.getSamples('MEDIA')
		self.pcrs = samplesByType.getSamples('PCR')
		self.buffers = samplesByType.getSamples('SOLUTIONS_BUFFERS')
		self.plasmids = samplesByType.getSamples('PLASMID')
		self.yeasts = samplesByType.getSamples('YEAST')
		self.bacterias = samplesByType.getSamples('BACTERIA')
		self.enzymes = samplesByType.getSamples('ENZYME')
		self.westernBlottings = samplesByType.getSamples('WESTERN_BLOTTING')	

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

class NavigationRequestHandlerFactory(IRequestHandlerFactory):
	def createRequestHandler(self, parameters, builder, searchService):
		return YeastLabNavigationRequestHandler(parameters, builder)
		
class RootRequestHandlerFactory(IRequestHandlerFactory):
	def createRequestHandler(self, parameters, builder, searchService):
		return YeastLabRootRequestHandler(parameters, builder)
		
class DrillRequestHandlerFactory(IRequestHandlerFactory):
	def createRequestHandler(self, parameters, builder, searchService):
		return YeastLabDrillRequestHandler(parameters, builder)

class DetailRequestHandlerFactory(IRequestHandlerFactory):
	def createRequestHandler(self, parameters, builder, searchService):
		return YeastLabDetailRequestHandler(parameters, builder)
			
class SearchRequestHandlerFactory(IRequestHandlerFactory):
	def createRequestHandler(self, parameters, builder, searchService):
		print "SearchRequestHandlerFactory: createRequestHandler"
		return YeastLabSearchRequestHandler(parameters, builder)
        
def aggregate(parameters, builder):
	dispatcher = RequestHandlerDispatcher()
	dispatcher.navigationRequestHandlerFactory = NavigationRequestHandlerFactory()
	dispatcher.rootRequestHandlerFactory = RootRequestHandlerFactory()
	dispatcher.drillRequestHandlerFactory = DrillRequestHandlerFactory()
	dispatcher.detailRequestHandlerFactory = DetailRequestHandlerFactory()
	dispatcher.searchRequestHandlerFactory = SearchRequestHandlerFactory()
	dispatcher.dispatch(parameters, builder, searchService)
