package io.github.leibnizhu.vertxearch

import io.github.leibnizhu.vertxearch.engine.{Indexer, Searcher}
import io.github.leibnizhu.vertxearch.utils.Constants
import io.github.leibnizhu.vertxearch.utils.Constants.{CONTENTS, ID}
import io.vertx.core.json.JsonObject
import io.vertx.core.{AsyncResult, Handler, Vertx}
import org.apache.lucene.document.Document
import org.scalatest.{Assertion, AsyncFlatSpec}
import org.slf4j.LoggerFactory

import scala.concurrent.Promise

class LuceneTest extends AsyncFlatSpec {
  private val log = LoggerFactory.getLogger(getClass)
  private val vertx = Vertx.vertx()
  private val configFile = "src/main/resources/config.json"
  private val config: JsonObject = new JsonObject(vertx.fileSystem().readFileBlocking(configFile))
  val indexDir: String = "src/test/data/Index"
  val dataDir: String = "src/test/data/Articles"
  var indexer: Indexer = _
  var searcher: Searcher = _

  private val keyWord = "clojure"
  s"在已经生成索引的情况下,查$keyWord" should s"返回结果若非空则结果均包含$keyWord" in {
    Constants.init(config)
    val promise = Promise.apply[Assertion]()
    createIndex((_: AsyncResult[Int]) => {
      val documents = search(keyWord)
//      log.info("hits:{}", documents)
      if (documents.nonEmpty) {
        promise.success(assert(documents.forall(_.get(CONTENTS).contains(keyWord))))
      } else {
        promise.failure(new RuntimeException("查不到结果"))
      }
    })
    promise.future
  }

  private def search(searchQuery: String): List[Document] = {
    searcher = new Searcher(indexDir)
    val startTime = System.currentTimeMillis
    val (_, hitDocs) = searcher.search(searchQuery)
    val endTime = System.currentTimeMillis
    log.info(s"找到${hitDocs.size}篇文章, 耗时${endTime - startTime} ms.")
    log.info(s"查找到的文章ID=${hitDocs.map(_.get(ID))}")
    searcher.close()
    hitDocs
  }

  private def createIndex(handler: Handler[AsyncResult[Int]]): Unit = {
    indexer = new Indexer(vertx, indexDir)
    indexer.cleanAllIndex()
    var numIndexed = 0
    val startTime = System.currentTimeMillis
    indexer.createIndex(dataDir, (ar: AsyncResult[Int]) => {
      if (ar.succeeded()) {
        numIndexed = ar.result()
        val endTime = System.currentTimeMillis
        log.info(s"给${numIndexed}篇文章建立了索引, 耗时:${endTime - startTime} ms.")
        indexer.close()
        handler.handle(io.vertx.core.Future.succeededFuture(numIndexed))
      } else {
        handler.handle(io.vertx.core.Future.failedFuture(ar.cause()))
      }
    })
  }
}
