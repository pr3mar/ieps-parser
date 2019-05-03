# -*- coding: utf-8 -*-
import sys, re, BeautifulSoup
import time

def roadrunner(htmlFile1, htmlFile2):
    wrapper = []
    sample = []
    output = []

    def optional(list1, list2):
        wrapperTag = list1[0]
        sampleTag = list2[0]
        optionalTag = ''
        if wrapperTag in list2:
            optionalTag = (list2.index(wrapperTag), True)
        else:
            optionalTag = (1, True)
        if sampleTag in list1:
            optionalTag = (list1.index(sampleTag), False)
            optionalTag = (1, False)
        return optionalTag

    def isTag(tag):
        if tag[0] == '<':
            return True
        else:
            return False

    def isClosingTag(tag):
        if tag[0] == '<':
            return True
        else:
            return False

    def findInitialTagsPosition(page, tag, j):
        tags =[]
        for x in range(0, j):
            if page[x] == tag:
                tags.append(x)
        return tags

    def findTerminalTagsPosition(page, tag, i):
        tags = []
        for x in range(i, len(page)):
            if page[x] == tag:
                tags.append(x)
        return tags

    def iterator(i, j):
        terminalTagWrapper = wrapper[i-1]
        terminalTagSample = sample[j-1]
        initialTagWrapper = wrapper[i]
        initialTagSample = sample[j]
        sqareWrapper = (initialTagWrapper, terminalTagWrapper)
        sqareSample = (initialTagSample, terminalTagSample)
# Find positions of terminal tags and initial tags on wrapper 
        poswt = findTerminalTagsPosition(wrapper, terminalTagWrapper, i)
        poswi = findInitialTagsPosition(wrapper, initialTagWrapper, i)
# Find positions of terminal tags and initial tags on sample
        possi = findInitialTagsPosition(sample, initialTagSample, j)
        posst = findTerminalTagsPosition(sample, terminalTagSample, j)
        # FIX!!!!!!!!!!!!
        if possi != [] and posst != []:
            return match(j, possi[-1], posst[0], True)
        elif poswi != [] and poswt != []:
            return match(i, poswi[-1], poswt[0], False)


    def match(currentPosition, initialTag, terminalTag, flag):
# flag = False -> iterator is on the wrraper page
# flag = True -> iterator is on the samlpe page
        page = sample if flag else wrapper
        iterator1 = []
        iterator2 = []
        for i in range(terminalTag, currentPosition - 1, -1):
            iterator1.append(page[i])
        for i in range(currentPosition - 1, initialTag - 1, -1):
            iterator2.append(page[i])
        i = 0
        j = 0
        iteratorsList = []
        while i < len(iterator1) and j < len(iterator2):
            if iterator1[i] == iterator2[j]:
                iteratorsList.append(iterator1[i])
                i += 1
                j += 1
            elif iterator1[i] == '' or iterator2[j] == '':
                i += 1
                j += 1
            elif (not isTag(iterator1[i]) and not isTag(iterator2[j])) or (iterator1[i] == '#PCDATA' and not isTag(iterator2[j])) or (iterator2[j] == '#PCDATA' and not isTag(iterator1[i])):
                iteratorsList.append('#PCDATA')
                iterator1[i] = '#PCDATA'
                iterator2[j] = '#PCDATA'
                i += 1
                j += 1
            else:
                i += 1
                j += 1
        iteratorTag = ''
        for k in range(len(iteratorsList)-1, -1, -1):
            iteratorTag += iteratorsList[k]
# Remove iterator from the output
        i = 1
        while output[-1] == iteratorsList[i - 1]:
            output.pop()
            if i >= len(iteratorsList):
                i = 1
            else:
                i+= 1
        return [iteratorTag, len(iteratorsList), flag]


    start = time.time()

    pageContent1 = open(htmlFile1, 'r').read().replace('\n', ' ').lower()
    pageContent2 = open(htmlFile2, 'r').read().replace('\n', ' ').lower()
# Take only body of the html
    body1 = re.sub('.*<head>.*<\/head>', '', pageContent1)
    body2 = re.sub('.*<head>.*<\/head>', '', pageContent2)
# Remove all script tags
    bodyNoScripts1 = re.sub('<script.*?script>', '', body1)
    bodyNoScripts2 = re.sub('<script.*?script>', '', body2)
# Create xml from the pre-processed input html 
    soup1 = BeautifulSoup.BeautifulSoup(bodyNoScripts1)
    soup2 = BeautifulSoup.BeautifulSoup(bodyNoScripts2)
    xml1 = soup1.prettify()
    xml2 = soup2.prettify()
# Write xml1 in a file 
    xmlFile1 = htmlFile1 + '.xml'
    g = open(xmlFile1, 'w')
    g.write(xml1)
    g.close()
# Write xml2 in a file
    xmlFile2 = htmlFile2 + '.xml'
    g = open(xmlFile2, 'w')
    g.write(xml2)
    g.close()

    xmlContent1 = open(xmlFile1, 'r')
    xmlContent2 = open(xmlFile2, 'r')
    for line in xmlContent1:
        wrapper.append(line.strip())
    for line in xmlContent2:
        sample.append(line.strip())
    

    end = time.time()


    i = 0
    j = 0
    iterators = None
    while i < len(wrapper) and j < len(sample):
        if wrapper[i] == sample[j]:
            output.append(wrapper[i])
            i += 1
            j += 1
        elif isTag(wrapper[i]) and not isTag(sample[j]):
            print(output)
            output.append('#PCDATA')
            print(output)
            j += 1
        elif not isTag(wrapper[i]) and isTag(sample[j]):
            print(output)
            output.append('#PCDATA')
            print(output)
            i += 1
        elif wrapper[i] == '' or sample[j] == '':
            i += 1
            j += 1
        elif not isTag(wrapper[i]) and not isTag(sample[j]):
            output.append('#PCDATA')
            wrapper[i] = '#PCDATA'
            sample[j] = '#PCDATA'
            i += 1
            j += 1
        elif isTag(wrapper[i]) and isTag(sample[j]):
            iterators = iterator(i, j)
            if iterators != None:
                output.append('(' + iterators[0] + ')+')
# flag = False -> iterator is in the wrraper page
# flag = True -> iterator is in the samlpe page
                if iterators[2]:
                    j += iterators[1]
                else:
                    i += iterators[1]
            else:
                mismatch = optional(wrapper[i:], sample[j:])
# mismatch[0] = False -> optional tag is on the wrraper
# mismatch[0] = True -> optional tag is on the samlpe
                if mismatch != '':
                    if mismatch[1]:
                        optionalTags = ''
                        for x in range(j, j+mismatch[0]):
                            optionalTags += sample[x]
                        output.append('('+ optionalTags +')?')
                        j += mismatch[0]
                    else:
                        optionalTags = ''
                        for x in range(i, i+mismatch[0]):
                            optionalTags += wrapper[x]
                        output.append('('+ optionalTags +')?')
                        i += mismatch[0]
                else:
                    i += 1
                    j += 1
        else:
            i += 1
            j += 1
            
    print(' '.join(output))
    print(end - start)
    end2 = time.time()
    print(end2 - start)



if __name__ == "__main__":
    roadrunner(sys.argv[1], sys.argv[2])