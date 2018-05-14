package io.gitlab.leibnizhu.vertXearch


import io.gitlab.leibnizhu.vertXearch.Constants._
import io.vertx.core.{AsyncResult, Future, Handler}
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
  override def search(searchQuery: String, length: Int, callback: Handler[AsyncResult[List[Article]]]): Unit = {
    val trySearch = Try({
      val (query,docs) = searcher.search(searchQuery, length)
      //设置高亮格式//设置高亮格式
      val highlighter = new Highlighter(formatter, new QueryScorer(query))
      //设置返回字符串长度
      highlighter.setTextFragmenter(fragmenter)
      docs.map(doc => {
        //这里的.replaceAll("\\s*", "")是必须的，\r\n这样的空白字符会导致高亮标签错位
        val id = doc.get(ID)
        val content = doc.get(CONTENTS).replaceAll("\\s*", "")
        val highContext = highlighter.getBestFragment(analyzer, CONTENTS, content)
        val title = doc.get(TITLE).replaceAll("\\s*", "")
        val highTitle = highlighter.getBestFragment(analyzer, TITLE, title)
        val author = doc.get(AUTHOR).replaceAll("\\s*", "")
        val highAuthor = highlighter.getBestFragment(analyzer, AUTHOR, author)
        Article(id,
          Option(highTitle).getOrElse(title),
          Option(highAuthor).getOrElse(author),
          Option(highContext).getOrElse(subContext(content)))
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
    if (content.length > FRAGMENT_SIZE) content.substring(0, FRAGMENT_SIZE) else content

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
