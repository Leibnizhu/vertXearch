package io.gitlab.leibnizhu.vertXearch

import java.nio.file.Paths

import org.apache.lucene.document.Document
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.queryparser.classic.{MultiFieldQueryParser, QueryParser}
import org.apache.lucene.search.{IndexSearcher, Query, ScoreDoc, TopDocs}
import org.apache.lucene.store.FSDirectory
class Searcher(indexDirectoryPath: String) {

  private val indexDirectory = FSDirectory.open(Paths.get(indexDirectoryPath))
  var indexSearcher = new IndexSearcher(DirectoryReader.open(indexDirectory))
  var queryParser = new MultiFieldQueryParser(Array(Constants.TITLE,Constants.CONTENTS, Constants.AUTHOR), Constants.analyzer)

  var reader: DirectoryReader=_

  def search(searchQuery: String, length: Int = Constants.MAX_SEARCH): (Query, List[Document]) = {
    val query = queryParser.parse(searchQuery.toLowerCase)
    (query, indexSearcher.search(query, length).scoreDocs.map(this.getDocument).toList)
  }

  def getDocument(scoreDoc: ScoreDoc): Document = indexSearcher.doc(scoreDoc.doc)

  def close(): Unit = {
  }
}
