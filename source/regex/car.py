# -*- coding: utf-8 -*-
import re, json, sys

def parse(htmlFile, jsonFile):
    pageContent = open(htmlFile, 'r').read().replace('\n', ' ')

    regexTitle = r"<h1>(.*)<\/h1>"
    matchTitle = re.compile(regexTitle).search(pageContent)
    title = matchTitle.group(1)

    regexSubtitle = r"<div class=\"subtitle\">(.*?)<\/div>"
    matchSubtitle = re.compile(regexSubtitle).search(pageContent)
    subtitle = matchSubtitle.group(1)

    regexAuthor = r"<div class=\"author-name\">(.*?)<\/div>"
    matchAuthor = re.compile(regexAuthor).search(pageContent)
    author = matchAuthor.group(1)

    regexDate = r"<div class=\"publish-meta\">[\n\s]*(.*?)<br>"
    matchDate = re.compile(regexDate).search(pageContent)
    date = matchDate.group(1)

    regexLead = r"<p class=\"lead\">(.*?)<\/p>"
    matchLead = re.compile(regexLead).search(pageContent)
    lead = matchLead.group(1)

    regexText = r"<article.*?<p.*?>(.+)<\/p>.*<\/article>"
    text = re.compile(regexText).search(pageContent)
    text = text.group(1).replace('\t', '')
    text = re.sub('<.*?>', '', text)

    # write data in JSON
    dataItem = {
        "Title": title,
        "Subtitle": subtitle,
        "Lead": lead,
        "Author": author,
        "Date": date,
        "Content": text
        }

    with open(jsonFile, 'w') as outfile:
            json.dump(dataItem, outfile, ensure_ascii=False, indent = 4, sort_keys=True)

if __name__ == "__main__":
    parse(sys.argv[1], sys.argv[2])