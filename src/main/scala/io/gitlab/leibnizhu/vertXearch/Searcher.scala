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
  var queryParser = new MultiFieldQueryParser(Array(Constants.TITLE,Constants.CONTENTS,Constants.ARTICLE_PATH),Constants.analyzer)

  var reader: DirectoryReader=_

  def search(searchQuery: String): TopDocs = {
    val query = queryParser.parse(searchQuery.toLowerCase)
    indexSearcher.search(query, Constants.MAX_SEARCH)
  }

  def getDocument(scoreDoc: ScoreDoc): Document = indexSearcher.doc(scoreDoc.doc)

  def close(): Unit = {
  }
}
