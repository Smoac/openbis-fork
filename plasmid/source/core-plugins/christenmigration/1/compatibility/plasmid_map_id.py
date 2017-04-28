import re

def calculate_mapID(): 

	plasmid_map_id=max(entity.propertyValue('MAX_PLASMID_MAP_ID'))+1

	return plasmid_map_id
     
def calculate():
	return calculate_mapID()
