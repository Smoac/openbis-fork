import json
import sys
from datetime import datetime
import time
import collections
import copy

entity_id = sys.argv[1]

payload = ""
for line in sys.stdin:
    payload = payload + line.strip()

if payload == "":
    print "Entity %s not found " % (entity_id)
    sys.exit(-1)

content = json.loads(payload)

def report(time, entity):
    print "\n-- %s --" % (time)
    for key, value in entity.iteritems():
        print "%s: %s" % (key, value)


epoch = datetime.utcfromtimestamp(0)
def timestamptonumber(s):
    return (datetime.strptime(s,"%Y-%m-%d %H:%M:%S.%f") - epoch).total_seconds() * 1000.0


data = dict()

for entry in content[entity_id]:
    
    key = entry['key']
    value = entry['value']
    validfrom = entry['validFrom']
    validuntil = None
    
    if 'validUntil' in entry:
        validuntil = entry['validUntil']
        
    if validfrom in data:
        data[validfrom].append([validfrom, key, value, "begin"])
    else:
        data[validfrom] = [[validfrom, key, value, "begin"]]

    if validuntil is not None:
        if validuntil in data:
            data[validuntil].append([validfrom, key, value, "end"])
        else:
            data[validuntil] = [[validfrom, key, value, "end"]]
            

sorted_data = collections.OrderedDict(sorted(data.items(), key=lambda t: timestamptonumber(t[0])))

history = []
currenttime = next(iter(sorted_data))
currententity = dict()

for timestamp, events in sorted_data.iteritems():
    if currenttime != timestamp:
        element = copy.deepcopy(currententity)
        element['time'] = currenttime
        currenttime = timestamp
        history.append(element)
    
    
    for event in filter(lambda event: event[3] == "end", events):
        key = event[1]
        value = event[2]
        currententity[key].remove(value)
        if len(currententity[key]) == 0:
            del currententity[key]
            
    for event in filter(lambda event: event[3] == "begin", events):
        key = event[1]
        value = event[2]
        if key not in currententity:
            currententity[key] = set([value])
        else:
            currententity[key].add(value)

if currententity != history[-1]:
    currententity['time'] = currenttime
    history.append(currententity)

for state in history:
    print "-- %s --" % state['time']
    
    for key, value in collections.OrderedDict(reversed(list(state.items()))).iteritems():
        if key != 'time':
            for element in value:
                print "%s: %s" % (key, element)
    print "\n"

