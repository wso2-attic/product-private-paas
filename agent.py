import urllib2,base64,json

def getLBIp():

url = 'https://ec2-54-251-18-7.ap-southeast-1.compute.amazonaws.com:9445/stratos/admin/cluster/lb'
request = urllib2.Request(url)

base64string = base64.encodestring('%s:%s' % ('admin', 'admin')).replace('\n', '')
request.add_header("Authorization", "Basic %s" % base64string)  
request.add_header("ContentType", "application/json")  

response = urllib2.urlopen(request).read()
cluster=json.loads(response)

#get LB IP
lb_ip=cluster['cluster'][0]['member'][0]['memberPublicIp']

hfile = open('/etc/hosts', 'a')
hfile.write("\n" + lb_ip + "  lb.privatepass.com");
hfile.close()

return lb_ip
