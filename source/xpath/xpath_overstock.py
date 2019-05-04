from lxml import html,etree
import json,sys,os

name = sys.argv[1]

f=open(name , 'rb').read()
tree = html.fromstring(f)
pName = tree.xpath('//a/b[contains(text(),"-kt") or contains(text(),"-Kt") ]/text()')
listprice = tree.xpath('//td[b[contains(text(),"List Price:")]]/following-sibling::td//text()')
price = tree.xpath('//td[b[text()="Price:"]]/following-sibling::td//text()')
saving = [i.split(" ") for i in tree.xpath('//td[b[text()="You Save:"]]/following-sibling::td//text()')]
content = tree.xpath('//td[span[@class="normal"]]/span/text()')
allproducts = list(zip(pName,listprice,price,saving,content))

items = {'Item_{}'.format(i+1):{'Title':allproducts[i][0],'ListPrice':allproducts[i][1],'Price':allproducts[i][2],'Savings':(allproducts[i][3])[0],'SavingsPercent':(allproducts[i][3])[1].strip('()'),'Content':allproducts[i][4].replace('\n','')} for i in range(0,len(allproducts))}

output = sys.argv[2]
out = open(output , 'w').write(json.dumps(items,indent=4,ensure_ascii=False))









