import re

def calculate_maxID(): 

	max_plasmid_map_id=max(entity.propertyValue('PLASMID_MAP_ID'))

	return max_plasmid_map_id
     
def calculate():
	return calculate_maxID()
