import json
import sys
from datetime import datetime
import time

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


def timestamptonumber(s):
    return time.mktime(datetime.strptime(s,"%Y-%m-%d %H:%M:%S.%f").timetuple())


current_timestamp = content[entity_id][0]['time']
entity = dict()

for entry in content[entity_id]:
    if abs(timestamptonumber(entry['time']) - timestamptonumber(current_timestamp)) > 2:
        report(current_timestamp, entity)
        current_timestamp = entry['time']

    entity[entry['key']] = entry['value']

report(current_timestamp, entity)
print "\n"
