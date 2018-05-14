package io.gitlab.leibnizhu.vertXearch


import io.vertx.core.{AsyncResult, Future, Handler}
import io.vertx.lang.scala.AsyncResultWrapper
import org.apache.lucene.search.highlight._

import scala.util.Try



class EngineImpl(indexPath: String, articlePath: String) extends Engine {
  private val indexer: Indexer = new Indexer(indexPath)
  private val searcher: Searcher = new Searcher(indexPath)
  private val formatter: Formatter =new SimpleHTMLFormatter("<font color='red'>", "</font>")
  private val fragmenter: Fragmenter =new SimpleFragmenter(150)

  /**
    * 对源目录下所有可用文件进行索引构建
    *
    * @return 增加索引的文件数量
    */
  override def createIndex(): Unit = {
    indexer.createIndex(articlePath, res => {
      res.succeeded()
    })
  }

  /**
    * 对源目录下所有可用文件进行索引更新
    *
    * @return 更新索引的文件数量
    */
  override def refreshIndex(): Int = {
    0
  }

  /**
    * 启动文章更新定时器
    *
    * @param interval 定时间隔
    * @param callback 回调方法
    */
  override def startRefreshTimer(interval: Long, callback: Handler[AsyncResult[Boolean]]): Unit = {

  }

  /**
    * 按指定关键词进行查找
    *
    * @param searchQuery 查找关键词
    * @return 匹配的文档,按相关度降序
    */
  override def search(searchQuery: String, length: Int, callback: Handler[AsyncResult[List[SearchResult]]]): Unit = {
    val trySearch = Try({
      val (query,docs) = searcher.search(searchQuery, length)
      //设置高亮格式//设置高亮格式
      val highlighter = new Highlighter(formatter, new QueryScorer(query))
      //设置返回字符串长度
      highlighter.setTextFragmenter(fragmenter)
      docs.map(doc => {
        //这里的.replaceAll("\\s*", "")是必须的，\r\n这样的空白字符会导致高亮标签错位
        val content = doc.get(Constants.CONTENTS).replaceAll("\\s*", "")
        //没有高亮字符会返回null
        val highContext = highlighter.getBestFragment(Constants.analyzer, Constants.CONTENTS, content)
        val title = doc.get(Constants.TITLE).replaceAll("\\s*", "")
        val highTitle = highlighter.getBestFragment(Constants.analyzer, Constants.TITLE, title)
        SearchResult(null, //FIXME 要在建立索引的时候定义ID,可能是根据文章本身的格式
          if (highTitle == null) title else highTitle,
          if (highContext == null) subContext(content) else highContext)
      })
    })
    if(trySearch.isSuccess){
      callback.handle(Future.succeededFuture(trySearch.get))
    } else {
      callback.handle(Future.failedFuture(trySearch.failed.get))
    }
  }

  /**
    * 截取片段长度
    *
    * @param content
    * @return
    */
  private def subContext(content: String) =
    if (content.length > Constants.FRAGMENT_SIZE) content.substring(0, Constants.FRAGMENT_SIZE) else content

  /**
    * 关闭搜索引擎
    */
  override def stop(callback: Handler[AsyncResult[Unit]]): Unit = {
    val tryStop = Try({
      indexer.close()
      searcher.close()
    })
    callback.handle(if (tryStop.isSuccess) Future.succeededFuture() else Future.failedFuture(tryStop.failed.get))
  }
}
