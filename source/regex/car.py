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

    # take only article-body and work with it for content part
    regexArticle = r"<div class=\"article-body\">(.*?)<\/article>"
    matchArticle = re.compile(regexArticle).search(pageContent)
    article = matchArticle.group(1)

    regexText = r"<p.*?>(.*?)<\/p>"
    text = re.findall(regexText, article)
    regexRemoveIframe = r"(<iframe.*?<\/iframe>)"
    removeIframe = re.search(regexRemoveIframe, article)
    text = [x.replace('<br>', ' ').replace('<strong>', '').replace('</strong>', '') for x in text if x != '' and x != removeIframe.group(1)]


    regexIframe = r"<iframe.*?src=\"(.*?)\".*?><\/iframe>"
    iframe = re.findall(regexIframe, article)

    regexFigure = r"<figcaption .*?<\/span>(.*?)\s*<\/figcaption>"
    figure = re.findall(regexFigure, article)

    # REGEX FOR NOT TO TAKE BANNER IMAG "<a .*? src=\"(.*?[\.jpg])\">"
    regexSRC = r"<a .*? src=\"(.*?[\.jpg])\""
    src = re.findall(regexSRC, article)

    # write data in JSON
    dataItem = {
    "Title": title,
    "Subtitle": subtitle,
    "Lead": lead,
    "Author": author,
    "Date": date,
    "Content": {
        "Text": text,
        "Iframe": iframe,
        "Images": src,
        "Figure caption": figure}
    }

    with open(jsonFile, 'w') as outfile:
            json.dump(dataItem, outfile, ensure_ascii=False, indent = 4, sort_keys=True)

if __name__ == "__main__":
    parse(sys.argv[1], sys.argv[2])