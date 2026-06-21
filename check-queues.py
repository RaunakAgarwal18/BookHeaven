import urllib.request
import base64
import json

req = urllib.request.Request('http://localhost:15672/api/queues')
auth = base64.b64encode(b'guest:guest').decode('utf-8')
req.add_header('Authorization', f'Basic {auth}')
try:
    response = urllib.request.urlopen(req)
    queues = json.loads(response.read().decode('utf-8'))
    for q in queues:
        if 'search' in q['name'] or 'book' in q['name']:
            print(f"Queue: {q['name']}, Messages: {q.get('messages', 0)}")
except Exception as e:
    print(e)
