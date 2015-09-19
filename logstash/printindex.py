from elasticsearch import Elasticsearch,helpers
import os

es = Elasticsearch()
indices_raw = es.cat.indices().splitlines()
max = 0
for index_raw in indices_raw:
    if max > 100:
        break

    index = index_raw.split()[2]

    if 'setting' in index or 'kibana' in index:
        continue

    print "Re-indexing %s" % index
    os.system("curl -XGET http://localhost:9200/"+index+'?pretty')
