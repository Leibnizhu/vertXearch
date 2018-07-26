package io.gitlab.leibnizhu.vertXearch

import io.vertx.core.Future
import io.vertx.scala.core.Vertx
import org.apache.lucene.document.Document
import org.scalatest.FunSuite

class LuceneTest extends FunSuite {
  val indexDir: String = "/Users/leibnizhu/workspace/vertx-cn-website/vertXearch/src/test/data/Index"
  val dataDir: String = "/Users/leibnizhu/workspace/vertx-cn-website/vertXearch/src/test/data/Articles"
  var indexer: Indexer = _
  var searcher: Searcher = _

  test("在已经生成索引的情况下,查clojure返回结果非空") {
    Constants.init(Vertx.vertx().getOrCreateContext())
    createIndex(Future.future[Int]().setHandler(_ => assert(search("clojure").nonEmpty)))
  }

  private def createIndex(handler: Future[Int]): Unit = {
    indexer = new Indexer(indexDir)
    indexer.cleanAllIndex()
    var numIndexed = 0
    val startTime = System.currentTimeMillis
    indexer.createIndex(dataDir, Future.future[Int]().setHandler(ar => {
      if (ar.succeeded()) {
        numIndexed = ar.result()
        val endTime = System.currentTimeMillis
        println(s"给${numIndexed}篇文章建立了索引, 耗时:${endTime - startTime} ms.")
        indexer.close()
        handler.complete(numIndexed)
      }
    }))
    Thread.sleep(1000)
  }

  private def search(searchQuery: String): List[Document] = {
    searcher = new Searcher(indexDir)
    val startTime = System.currentTimeMillis
    val (_, hitDocs) = searcher.search(searchQuery)
    val endTime = System.currentTimeMillis
    println(s"找到${hitDocs.size}篇文章, 耗时${endTime - startTime} ms.")
    println(s"查找到的文章ID=${hitDocs.map(_.get(Constants.ID))}")
    searcher.close()
    hitDocs
  }
}
