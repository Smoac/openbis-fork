#! /usr/bin/env python

import urllib2
import json
import re

######

def looks_like_number(x):
    try:
        float(x)
        return True
    except ValueError:
        return False

seen_compounds = {}

########################################################################

def process_uniprot(cols, targets, compounds, samples):
	target_info = { "accession" : cols[0], "name" : cols[1], "protein_names" : cols[3], "gene_names" : cols[4], "length" : cols[6].rstrip() }
	# 1. Use UniProt accession to get target details
	try:
		target_data = json.loads(urllib2.urlopen("https://www.ebi.ac.uk/chemblws/targets/uniprot/%s.json" % target_info['accession']).read())
	except urllib2.HTTPError:
		return

	print "Processing %s" % target_info['accession']
	target_info['chembl'] = target_data['target']['chemblId']
	target_info['desc'] = target_data['target']['description']
	
	# write the target info
	targets.write("%(accession)s\t%(name)s\t%(protein_names)s\t%(gene_names)s\t%(length)s\t%(chembl)s\t%(desc)s\n" % target_info)

	# 2. Get all bioactivties for target CHEMBL_ID
	bioactivity_data = json.loads(urllib2.urlopen("https://www.ebi.ac.uk/chemblws/targets/%s/bioactivities.json" % target_data['target']['chemblId']).read())
	print "\tBioactivity Count (IC50's):  %d" % len([record for record in bioactivity_data['bioactivities'] if record['bioactivity_type'] == 'IC50'])

	# 3. Get compounds with high binding affinity (IC50 < 100)
	index = 0
	for bioactivity in [record for record in bioactivity_data['bioactivities'] if re.search('IC50', record['bioactivity_type']) and looks_like_number(record['value']) and float(record['value']) < 100]:
		compound = bioactivity['ingredient_cmpd_chemblid']
		if compound not in seen_compounds:
			seen_compounds[compound] = True
			compound_data = json.loads(urllib2.urlopen("https://www.ebi.ac.uk/chemblws/compounds/%s.json" % compound).read())
			compound_info = compound_data['compound']
			compounds.write("%(chemblId)s\t%(molecularFormula)s\t%(molecularWeight)s\t%(smiles)s\n" % compound_info)
		samples.write("%s\t%s\t%s\n" % (target_info['accession'], compound, bioactivity['assay_description']))
		index = index + 1
		if index > 10:
			break
		

with open("uniprot-human-serotonin.tab") as f:
	with open('targets.tab', 'w') as targets:
		# The targets header
		targets.write("CODE\tNAME\tPROT_NAME\tGENE_NAME\tLENGTH\tCHEMBL\tDESC\n")
		with open('compounds.tab', 'w') as compounds:
			compounds.write("CODE\tFORMULA\tWEIGHT\tSMILES\n")
			with open('samples.tab', 'w') as samples:
				samples.write("TARGET\tCOMPOUND\tDESC\n")
				index = -1
				for line in f:
					index = index + 1		
					if index == 0:
						continue
					cols = line.split("\t")
					process_uniprot(cols, targets, compounds, samples)
		