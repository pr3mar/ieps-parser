import re, json, sys

def parse(htmlFile, jsonFile):
    pageContent = open(htmlFile, 'r').read().replace('\n', '')


    # REGEX FOR ITEM
    regexItem = r"<td valign=\"top\" align=\"center\">.*?src=\"(.*?\.jpg)\"|<b>([\d\-kKt]{5}.*?)<\/b>|<s>(.*?)<\/s>|<span class=\"bigred\"><b>(\$\d*,*\d+.\d+)</b></span>|<span class=\"littleorange\">(\$\d*,*\d+.\d+)*\s\((\d+\%)\)</span>|<span class=\"normal\">\s*(.*?)\s*<br>"
    item = re.compile(regexItem).findall(pageContent)
    # print ('Found imgs | titles | list price | price | savings | percent | content: %s' %len(item))

    title, listPrice, price, saving, percent, content = [], [], [], [], [], [], 

    for x in item:
        title.append(x[1])
        listPrice.append(x[2])
        price.append(x[3])
        saving.append(x[4])
        percent.append(x[5])
        content.append(x[6])

    title = list(filter(lambda x: x != '', title))
    listPrice = list(filter(lambda x: x != '', listPrice))
    price = list(filter(lambda x: x != '', price))
    saving = list(filter(lambda x: x != '', saving))
    percent = list(filter(lambda x: x != '', percent))
    content = list(filter(lambda x: x != '', content))

    dataItem = {
        i+1:
        {
            "Title": title[i],
            "ListPrice": listPrice[i], 
            "Price": price[i], 
            "Saving": saving[i],
            "SavingPercent": percent[i],
            "Content": content[i]
        }for i in range(0,len(title))
    }

    with open(jsonFile, 'w') as outfile:
            json.dump(dataItem, outfile, ensure_ascii=False, indent = 4)

if __name__ == "__main__":
    parse(sys.argv[1], sys.argv[2])
