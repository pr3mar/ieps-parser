import re, json, sys

def parse(htmlFile, jsonFile):
    pageContent = open(htmlFile, 'r').read().replace('\n', '')


    # REGEX FOR ITEM
    regexItem = r"<b>([\d\-kKt]{5}.*?)<\/b>|<s>(.*?)<\/s>|<span class=\"bigred\"><b>(\$\d*,*\d+.\d+)</b></span>|<span class=\"littleorange\">(\$\d*,*\d+.\d+)*\s\((\d+\%)\)</span>|<span class=\"normal\">\s*(.*?)\s*<br>"
    item = re.compile(regexItem).findall(pageContent)

    title, listPrice, price, saving, percent, content = [], [], [], [], [], [], 

    for x in item:
        title.append(x[0])
        listPrice.append(x[1])
        price.append(x[2])
        saving.append(x[3])
        percent.append(x[4])
        content.append(x[5])

    title = list(filter(lambda x: x != '', title))
    listPrice = list(filter(lambda x: x != '', listPrice))
    price = list(filter(lambda x: x != '', price))
    saving = list(filter(lambda x: x != '', saving))
    percent = list(filter(lambda x: x != '', percent))
    content = list(filter(lambda x: x != '', content))

    dataItem = [
        {
            "Title": title[i],
            "ListPrice": listPrice[i], 
            "Price": price[i], 
            "Saving": saving[i],
            "SavingPercent": percent[i],
            "Content": content[i]
        }for i in range(0,len(title))
    ]

    with open(jsonFile, 'w') as outfile:
            json.dump(dataItem, outfile, ensure_ascii=False, indent = 4)

if __name__ == "__main__":
    parse(sys.argv[1], sys.argv[2])
