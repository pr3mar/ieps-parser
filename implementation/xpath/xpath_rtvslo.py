from lxml import html,etree
import json,os,sys

name = sys.argv[1]
f=open(name , 'rb').read()

tree = html.fromstring(f)

authorN = str(tree.xpath('//*[@class="author-name"]//text()')[0])
publishT = str(tree.xpath('//*[@class="publish-meta"]//text()')[0])
title = str(tree.xpath('//*[contains(@class,"news-container")]//header//h1//text()')[0])
subtitle = str(tree.xpath('//*[contains(@class,"news-container")]//header//div[@class="subtitle"]//text()')[0])
lead = str(tree.xpath('//*[contains(@class,"news-container")]//header//p[@class="lead"]//text()')[0])
text = tree.xpath('//div[contains(@class,"article-body")]//p//text()')
item = {"Author":authorN,"Date":publishT.replace("\n\t\t",""),"Title":title,"Subtitle":subtitle,"Lead":lead,"Content":text}

output = sys.argv[2]
out = open(output , 'w').write(json.dumps(item,indent=4,ensure_ascii=False))
