package io.gitlab.leibnizhu.vertXearch

object LuceneTester {

  val indexDir: String = "/Users/leibnizhu/Desktop/Index"
  val dataDir: String = "/Users/leibnizhu/Desktop/Data"
  var indexer: Indexer = _
  var searcher: Searcher = _

  def main(args: Array[String]) {
//    LuceneTester.createIndex()
    LuceneTester.search("编译")
  }

  private def createIndex(): Unit = {
    indexer = new Indexer(indexDir)
    var numIndexed = 0
    val startTime = System.currentTimeMillis
    numIndexed = indexer.createIndex(dataDir)
    val endTime = System.currentTimeMillis
    indexer.close()
    println(numIndexed + " File indexed, time taken: " + (endTime - startTime) + " ms")
  }

  private def search(searchQuery: String): Unit = {
    searcher = new Searcher(indexDir)
    val startTime = System.currentTimeMillis
    val hits = searcher.search(searchQuery)
    val endTime = System.currentTimeMillis
    println(hits.totalHits + " documents found. Time :" + (endTime - startTime))
    for (scoreDoc <- hits.scoreDocs) {
      val doc = searcher.getDocument(scoreDoc)
      println("File: " + doc.get(Constants.ARTICLE_PATH))
    }
    searcher.close()
  }
}
