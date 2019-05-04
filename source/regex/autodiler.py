# -*- coding: utf-8 -*-
import re, json, sys

def parse(htmlFile, jsonFile):
    page = open(htmlFile, 'r').read().replace('\n', ' ')

    regexImg = r"<div class=\"oglas_thumb.*?\">.*?<img src=\"(.*?.jpg)\""
    img = re.findall(regexImg, page)

    regexName = r"<h1>.*?>(.*?)<"
    name = re.findall(regexName, page)

    regexDate = r"Datum: ([\d\.]+)*\."
    date = re.findall(regexDate, page)

    regexYear = r"Godi.*?te:</strong> <span>(\d{4})"
    year = re.findall(regexYear, page)

    regexKm = r"Kilometra.*?a:</strong> <span>(\d+)"
    km = re.findall(regexKm, page)

    regexCarType = r"Gorivo:</strong> <span>(\w+)<"
    carType = re.findall(regexCarType, page)

    regexCity = r"Grad:</strong> <span>(.*?)<"
    city = re.findall(regexCity, page)

    regexPrice = r"<div class=\"priceWrapper\"><span class=\"currentPrice\">(\d+ \€)(<\/span><span class=\"oldPrice\">(\d+ \€)<\/span><\/div>)*"
    price = re.findall(regexPrice, page)

    currentPrice, oldPrice = [], []
    for i in price:
        currentPrice.append(i[0])
        oldPrice.append(i[2])

    dataItem = {
        i+1: 
        {
            "Name": name[i],
            "Image": img[i], 
            "Date": date[i], 
            "Year": year[i], 
            "KM": km[i], 
            "Fuel type": carType[i],
            "City": city[i],
            "Current Price": currentPrice[i],
            "Old Price": oldPrice[i]
        }for i in range(0,len(img))
    }

    print(json.dumps(dataItem, ensure_ascii=False, indent=4))

    with open(jsonFile, 'w') as outfile:
            json.dump(dataItem, outfile, ensure_ascii=False, indent = 4)

if __name__ == "__main__":
    parse(sys.argv[1], sys.argv[2])