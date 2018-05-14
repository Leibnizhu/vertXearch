package io.gitlab.leibnizhu.vertXearch

import java.util

import io.vertx.core.{AsyncResult, Handler}
import io.vertx.lang.scala.AsyncResultWrapper


class EngineImpl(indexPath: String, articlePath: String) extends Engine {
  private val indexer: Indexer = new Indexer(indexPath)
  private val searcher: Searcher = new Searcher(indexPath)

  /**
    * 对源目录下所有可用文件进行索引构建
    *
    * @return 索引的文件数量
    */
  override def createIndex(): Int = {
    indexer.createIndex(articlePath)
  }

  /**
    * 对源目录下所有可用文件进行索引更新
    *
    * @return 索引的文件数量
    */
  override def refreshIndex(): Int = {
    0
  }

  /**
    * 按指定关键词进行查找
    *
    * @param searchQuery 查找关键词
    * @return 匹配的文档,按相关度降序
    */
  override def search(searchQuery: String, callback: Handler[AsyncResult[util.List[SearchResult]]]): Unit = {

  }

  /**
    * 关闭搜索引擎
    */
  override def stop(callback: Handler[AsyncResult[Unit]]): Unit = {
    indexer.close()
    searcher.close()
    callback.handle(AsyncResultWrapper[Void, Unit](null, x => x))
  }
}
