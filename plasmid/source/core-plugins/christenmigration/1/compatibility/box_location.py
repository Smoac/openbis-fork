import re

def calculate_location(): 
	
	plasmid_ID= float(entity.propertyValue('PLASMID_ID'))
	location=(plasmid_ID/81)+0.499999
	round_location=int (round(location))

	return round_location
     
def calculate():
	return calculate_location()
