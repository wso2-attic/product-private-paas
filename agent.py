import urllib2,base64,json

base64string = base64.encodestring('%s:%s' % ('admin@udara.com', 'admin')).replace('\n', '')
# Send the GET request
url = 'https://ec2-54-251-18-7.ap-southeast-1.compute.amazonaws.com:9445/stratos/admin/cluster/lb'
request = urllib2.Request(url)
request.add_header("Authorization", "Basic %s" % base64string)  
request.add_header("ContentType", "application/json")  

# Read the response
resp = urllib2.urlopen(request).read()
print resp
data=json.loads(resp)
#print data['cluster']
lb_ip=data['cluster'][0]['member'][0]['memberPublicIp']

hfile = open('/etc/hosts', 'a')
hfile.write("\nlb.privatepass.com " + lb_ip);
hfile.close()
