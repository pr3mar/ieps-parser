from lxml import html,etree
import json,sys,os

name = sys.argv[1]
f=open(name , 'rb').read()
tree = html.fromstring(f)

carName = tree.xpath('//*[contains(@class,"oglas_thumb")]/div[2]//h1//text()')
imgSource = tree.xpath('//*[contains(@class,"oglas_thumb")]/div[1]//img//@src')
date = tree.xpath('//*[contains(@class,"oglas_thumb")]/div[3]//span[@class="date"]//text()')
date = [i.replace("Datum: ","") for i in date]
year = tree.xpath('//*[contains(text(),"Godište")]/following-sibling::span/text()')

kilometers = tree.xpath('//*[contains(text(),"Kilometr")]/following-sibling::span/text()')
fuel = tree.xpath('//*[contains(text(),"Gorivo")]/following-sibling::span/text()')
city = tree.xpath('//*[contains(text(),"Grad")]/following-sibling::span/text()')
trenutnaStara = []
sveCijene = tree.xpath('//div[contains(@class,"priceWrapper")]')
for i in sveCijene:
    trenutnaStara.append((''.join(i.xpath('span[1]/text()')),''.join(i.xpath('span[2]//text()'))))

allproducts = list(zip(carName,imgSource,date,year,kilometers,fuel,city,trenutnaStara))
items = [{'Name':allproducts[i][0],'Image':allproducts[i][1],'Date':allproducts[i][2],'Year made':(allproducts[i][3]),'KM':(allproducts[i][4]),'Fuel type':allproducts[i][5],'City':allproducts[i][6],"Current Price":allproducts[i][7][0],"Old Price":allproducts[i][7][1]} for i in range(0,len(allproducts)) ]

output = sys.argv[2]
out = open(output , 'w').write(json.dumps(items,indent=4,ensure_ascii=False))












