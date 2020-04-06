![GUI](https://github.com/Serfati/Boogle/blob/master/src/main/resources/boogleLogo.png "LOGO")

![Build Status](https://travis-ci.org/lemire/JavaFastPFOR.png)
![Price](https://img.shields.io/badge/price-FREE-0098f7.svg)
[![GitHub](https://img.shields.io/github/license/nevoit/Automated-Messages.svg)](https://github.com/Serfati/The-LionKing-Maze/edit/master/LICENSE)

- Our search engine was implemented with a Model-View-ViewModel (MVVM) Architecture.

The Structure:
-------------

1.	Select the next parts percentage (%), of the documents from the corpus.
2.	Send these documents to the Model.Model.Parser.
3.	Optional: send the output of the Model.Model.Parser to the Stemmer.
4.	Send the output to the Indexer. The Indexer will create a temporary index file for the documents from step one.
5.	Go back to step one until the coverage will be 100 percentage.
6.	Create final posting file from all the temporary files that were created in step 4.
7.	Finish the preprocessing stage.

The Model:
-------------
 - ReadFile – This module reads documents from the corpus.

- Parser – Parses the documents (removes stop-words, converts dates to a unified format etc.).

- Stemmer – Performs stemming on a given document. We used porter's for JAVA 8.

- Indexer – Creates the final posting file separated by alphabet letters.

- Searcher – Runs the query and returns the relevant results.

- Ranker – This model will rank each document given a specific query from the user (from the GUI).  We used Cosine similarity   (With TF, IDF), BM 25, 

- WriteFile – This module writes files to the disk.

- GUI (view) - reach User Interface see below

Screenshots:
-------------
- main index tab

![GUI](https://github.com/Serfati/Boogle/blob/master/src/main/resources/gui1.png "GUI")
- init query parameters can use online/ offline

![GUI](https://github.com/Serfati/Boogle/blob/master/src/main/resources/gui2.png "GUI")
- search tab

![GUI](https://github.com/Serfati/Boogle/blob/master/src/main/resources/gui3.png "GUI")
- query results

![GUI](https://github.com/Serfati/Boogle/blob/master/src/main/resources/gui4.png "GUI")
- about

![GUI](https://github.com/Serfati/Boogle/blob/master/src/main/resources/gui5.png "GUI")
- exit

![GUI](https://github.com/Serfati/Boogle/blob/master/src/main/resources/gui6.png "GUI")


Licence:
-------------

Copyright 2020 Avihai Serfati

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
