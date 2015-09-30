from elasticsearch import Elasticsearch,helpers

es = Elasticsearch()
indices_raw = es.cat.indices().splitlines()
max = 0
for index_raw in indices_raw:
    if max > 100:
        break

    index = index_raw.split()[2]
    if not '-new' in index:
        continue

    if 'setting' in index or 'kibana' in index:
        continue

    print "Re-indexing %s" % index
    # new_index = index + "-new"
    new_index=index.replace('-new', '')
    print "New index name is %s" % new_index
    # es.indices.create(new_index, body='{ "mappings": { "nginx-access" : { "properties" : { "status_code" : {"type" : "long", "store" : true } } }, "haproxy-access" : { "properties" : { "status_code" : {"type" : "long", "store" : true } } } }}')
    es.indices.create(new_index, body='{ "mappings": { "nginx-access" : { "properties" : { "status_code" : {"type" : "long", "store" : true } } }, "haproxy-access" : { "properties" : { "status_code" : {"type" : "long", "store" : true } } }, "orion-api-access" : { "properties" : { "req_id" : {"type" : "string", "store" : true } } }, "orion-api-log" : { "properties" : { "req_id" : {"type" : "string", "store" : true } } }}}')
    helpers.reindex(es, index, new_index , query=None, target_client=None, chunk_size=500, scroll='5m')
    es.indices.delete(index)
    max = max + 1

