from lxml import html,etree
import json,sys,os

name = sys.argv[1]
f=open(name , 'rb').read()
tree = html.fromstring(f)

imeAuta = tree.xpath('//*[contains(@class,"oglas_thumb")]/div[2]//h1//text()')
imgSource = tree.xpath('//*[contains(@class,"oglas_thumb")]/div[1]//img//@src')
datum = tree.xpath('//*[contains(@class,"oglas_thumb")]/div[3]//span[@class="date"]//text()')
datum = [i.replace("Datum: ","") for i in datum]
godiste = tree.xpath('//*[contains(text(),"Godište")]/following-sibling::span/text()')

kilometraza = tree.xpath('//*[contains(text(),"Kilometr")]/following-sibling::span/text()')
gorivo = tree.xpath('//*[contains(text(),"Gorivo")]/following-sibling::span/text()')
grad = tree.xpath('//*[contains(text(),"Grad")]/following-sibling::span/text()')
trenutnaStara = []
sveCijene = tree.xpath('//div[contains(@class,"priceWrapper")]')
for i in sveCijene:
    trenutnaStara.append((''.join(i.xpath('span[1]/text()')),''.join(i.xpath('span[2]//text()'))))

allproducts = list(zip(imeAuta,imgSource,datum,godiste,kilometraza,gorivo,grad,trenutnaStara))
items = {'Item_{}'.format(i+1):{'Name':allproducts[i][0],'Image':allproducts[i][1],'Date':allproducts[i][2],'Year made':(allproducts[i][3]),'Kilometers':(allproducts[i][4]),'Fuel':allproducts[i][5],'City':allproducts[i][6],"New Price":allproducts[i][7][0],"Old Price":allproducts[i][7][1]} for i in range(0,len(allproducts))}

output = sys.argv[2]
out = open(output , 'w').write(json.dumps(items,indent=4,ensure_ascii=False))












