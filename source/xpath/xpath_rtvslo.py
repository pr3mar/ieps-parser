from lxml import html,etree
import json,os,sys

name = sys.argv[1]
f=open(name , 'rb').read()

tree = html.fromstring(f)

imeAutora = str(tree.xpath('//*[@class="author-name"]//text()')[0])
vrijemePublisha = str(tree.xpath('//*[@class="publish-meta"]//text()')[0])
naslov = str(tree.xpath('//*[contains(@class,"news-container")]//header//h1//text()')[0])
podnaslov = str(tree.xpath('//*[contains(@class,"news-container")]//header//div[@class="subtitle"]//text()')[0])
lead = str(tree.xpath('//*[contains(@class,"news-container")]//header//p[@class="lead"]//text()')[0])
tekstKontent = tree.xpath('//div[contains(@class,"article-body")]//p//text()')
slike = tree.xpath('//div[contains(@class,"article-body")]//img/@src')
iframe = tree.xpath('//div[contains(@class,"article-body")]//iframe/@src')
figcaption = tree.xpath('//div[contains(@class,"article-body")]//figcaption[span[@class="icon-photo"]][@itemprop="caption description"]//text()')

item = {"Author":imeAutora,"Date":vrijemePublisha.replace("\n\t\t",""),"Title":naslov,"Subtitle":podnaslov,"Lead":lead,"Content":{"Text":tekstKontent,"Images":slike,"Iframe":iframe,"Figure Captions":figcaption}}

output = sys.argv[2]
out = open(output , 'w').write(json.dumps(item,indent=4,ensure_ascii=False))
