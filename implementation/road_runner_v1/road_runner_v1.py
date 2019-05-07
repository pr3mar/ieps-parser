# -*- coding: utf-8 -*-
import sys, re, BeautifulSoup, time

def roadrunner(htmlFile1, htmlFile2, outputFile):
    wrapper = []
    sample = []
    output = []
    outputIndexes = []

    def optional(list1, list2):
        wrapperTag = list1[0]
        sampleTag = list2[0]
        optionalTag = ''
        if wrapperTag in list2:
            optionalTag = (list2.index(wrapperTag), True)
        elif sampleTag in list1:
            optionalTag = (list1.index(sampleTag), False)
        return optionalTag

    def isTag(tag):
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

    def iterator(tokensList1, tokensList2, i, j, mismatchType):
        terminalTagWrapper = tokensList1[i-1]
        terminalTagSample = tokensList2[j-1]
        initialTagWrapper = tokensList1[i]
        initialTagSample = tokensList2[j]
# Find positions of terminal tags and initial tags on wrapper 
        poswt = findTerminalTagsPosition(tokensList1, terminalTagWrapper, i)
        poswi = findInitialTagsPosition(tokensList1, initialTagWrapper, i)
# Find positions of terminal tags and initial tags on sample
        possi = findInitialTagsPosition(tokensList2, initialTagSample, j)
        posst = findTerminalTagsPosition(tokensList2, terminalTagSample, j)
        
        if poswi != [] and poswt != []:
            return match(i, poswi[-1], poswt[0], tokensList1, False, mismatchType)
        elif possi != [] and posst != []:
            return match(j, possi[-1], posst[0], tokensList2, True, mismatchType)

    def match(currentPosition, initialTag, terminalTag, page, flag, mismatchType):
        largerList = ''
# flag = False -> iterator is on the wrraper page
# flag = True -> iterator is on the samlpe page
        iteratorsList1 = page[currentPosition: terminalTag + 1]
        iteratorsList2 = page[initialTag: currentPosition]

        if len(iteratorsList1) > len(iteratorsList2):
            recursionList = iteratorsList1 
            largerList = False
        elif len(iteratorsList1) < len(iteratorsList2):
            recursionList = iteratorsList1
            largerList = True

        listSize = len(iteratorsList1) if len(iteratorsList1) < len(iteratorsList2) else len(iteratorsList2)
        if mismatchType == 'external':
            for x in range(listSize):
                output.pop()
            mismatchType = 'internal'

        i = 0
        j = 0
        iteratorsMatch = []
        while i < len(iteratorsList1) and j < len(iteratorsList2):
            if iteratorsList1[i] == iteratorsList2[j]:
                iteratorsMatch.append(iteratorsList1[i])
                i += 1
                j += 1
            elif iteratorsList1[i] == '' and iteratorsList2[j] == '':
                i += 1
                j += 1
            elif not isTag(iteratorsList1[i]) and not isTag(iteratorsList2[j]):
                iteratorsMatch.append('#PCDATA')
                iteratorsList1[i] = '#PCDATA'
                iteratorsList2[j] = '#PCDATA'
                i += 1
                j += 1
            elif isTag(iteratorsList1[i]) and not isTag(iteratorsList2[j]):
                iteratorsMatch.append(iteratorsList2[j])
                j += 1
            elif isTag(iteratorsList2[j]) and not isTag(iteratorsList1[i]):
                iteratorsMatch.append(iteratorsList1[i])
                i += 1
            elif isTag(iteratorsList1[i]) and isTag(iteratorsList2[j]):
                iterators = iterator(iteratorsList1, iteratorsList2, i, j, mismatchType)
                if iterators != None:
                    outputIndexes.append([len(output), len(output) + len(iterators[0]) - 1, '+'])
                    for k in iterators[0]:
                        output.append(k)
# flag = False -> iterator is in the wrraper page
# flag = True -> iterator is in the samlpe page
                    if iterators[2]:
                        j += iterators[1]
                    else:
                        i += iterators[1]
                else:
                    mismatch = optional(iteratorsList1[i:], iteratorsList2[j:])
# mismatch[0] = False -> optional tag is on the wrraper
# mismatch[0] = True -> optional tag is on the samlpe
                    if mismatch != '':
                        outputIndexes.append([len(output), len(output) + mismatch[0] - 1, '?'])
                        if mismatch[1]:
                            for x in range(j, j+mismatch[0]):
                                output.append(iteratorsList2[x])
                            j += mismatch[0]
                        else:
                            for x in range(i, i+mismatch[0]):
                                output.append(iteratorsList1[x])
                            i += mismatch[0]
                    else:
                        i += 1
                        j += 1
            else:
                i += 1
                j += 1
# Remove iterator from the output
        if iteratorsMatch != []:
            i = 1
            while output[-1] == iteratorsMatch[-i]:
                output.pop()
                i = +1 if i < len(iteratorsMatch) else 1
            return [iteratorsMatch, len(iteratorsMatch), flag]

    start = time.time()

    pageContent1 = open(htmlFile1, 'r').read().replace('\n', ' ').lower()
    pageContent2 = open(htmlFile2, 'r').read().replace('\n', ' ').lower()
# Take only body of the html
    body1 = re.sub('.*<head>.*?<\/head>', '', pageContent1)
    body2 = re.sub('.*<head>.*?<\/head>', '', pageContent2)
# Remove all script tags
    body1 = re.sub('<script.*?script>', '', body1)
    body2 = re.sub('<script.*?script>', '', body2)
# Remove all comments
    body1 = re.sub('<!\-\-.*?\-\->', '', body1)
    body2 = re.sub('<!\-\-.*?\-\->', '', body2)
# Create xml from the pre-processed input html 
    soup1 = BeautifulSoup.BeautifulSoup(body1)
    soup2 = BeautifulSoup.BeautifulSoup(body2)
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
        mismatchType = 'external'
        if wrapper[i] == sample[j]:
            output.append(wrapper[i])
            i += 1
            j += 1
        elif isTag(wrapper[i]) and not isTag(sample[j]):
            output.append('#PCDATA')
            j += 1
        elif not isTag(wrapper[i]) and isTag(sample[j]):
            output.append('#PCDATA')
            i += 1
        elif wrapper[i] == '' and sample[j] == '':
            i += 1
            j += 1
        elif not isTag(wrapper[i]) and not isTag(sample[j]):
            output.append('#PCDATA')
            wrapper[i] = '#PCDATA'
            sample[j] = '#PCDATA'
            i += 1
            j += 1
        elif isTag(wrapper[i]) and isTag(sample[j]):
            iterators = iterator(wrapper, sample, i, j, mismatchType)
            if iterators != None:
                outputIndexes.append([len(output), len(output) + len(iterators[0]) - 1, '+'])
                for k in iterators[0]:
                    output.append(k)
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
                    outputIndexes.append([len(output), len(output) + mismatch[0] - 1, '?'])
                    if mismatch[1]:
                        for x in range(j, j+mismatch[0]):
                            output.append(sample[x])
                        j += mismatch[0]
                    else:
                        optionalTags = ''
                        for x in range(i, i+mismatch[0]):
                            output.append(wrapper[x])
                        i += mismatch[0]
                else:
                    outputIndexes.append([len(output), len(output), '?'])
                    output.append(wrapper[i])
                    outputIndexes.append([len(output), len(output), '?'])
                    output.append(sample[j])
                    i += 1
                    j += 1
        else:
            i += 1
            j += 1
        
    for k in range(len(outputIndexes) - 1, -1, -1):
        if outputIndexes[k][2] == '+':
            output[outputIndexes[k][1]] = output[outputIndexes[k][1]] + str(')+')
            output[outputIndexes[k][0]] = str('(') + output[outputIndexes[k][0]]
        elif outputIndexes[k][2] == '?':
            output[outputIndexes[k][1]] = output[outputIndexes[k][1]] + str(')?')
            output[outputIndexes[k][0]] = str('(') + output[outputIndexes[k][0]]

    output = ' '.join(output)
    print(output)
    print(end - start)
    end2 = time.time()
    print(end2 - start)
    

    output = BeautifulSoup.BeautifulSoup(output)
    g = open(outputFile, 'w')
    g.write(output.prettify())
    g.close()


if __name__ == "__main__":
    roadrunner(sys.argv[1], sys.argv[2], sys.argv[3])