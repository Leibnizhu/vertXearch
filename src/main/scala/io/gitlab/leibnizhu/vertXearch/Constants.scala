package io.gitlab.leibnizhu.vertXearch

import java.io.File

import com.hankcs.lucene.HanLPAnalyzer
import io.vertx.core.json.JsonObject
import io.vertx.scala.core.{Context, Vertx}

object Constants {
  var vertx: Vertx = _
  var vertxContext: Context = _
  private var config: JsonObject = _
  var indexPath: String = _
  var articlePath: String = _

  def init(ctx:Context):Unit ={
    this.vertxContext = ctx
    this.vertx = ctx.owner
    this.config = vertxContext.config().get
    this.indexPath = config.getString("indexPath", "indexes")
    this.articlePath = config.getString("articlePath", "articles")
    //确保路径以/结尾
    if (!this.indexPath.endsWith(File.separator)) this.indexPath += File.separator
    if (!this.articlePath.endsWith(File.separator)) this.articlePath += File.separator
  }

  def timestampFile: String = indexPath + "index.ts"

  def refreshTimerInterval: Long = config.getInteger("refreshIndexPerSecond", 300) * 1000L

  def keywordPreTag: String = config.getString("keywordPreTag", "<font color='red'>") //返回的关键词前置标签,这里弄成红色字体

  def keywordPostTag: String = config.getString("keywordPostTag", "</font>") //返回的关键词后置标签,要和前置标签闭合

  val ID: String = "id"
  val TITLE: String = "title"
  val AUTHOR:String = "author"
  val CONTENTS: String = "contents"

  val MAX_SEARCH: Int = 30 //单次搜索默认最大返回文章个数
  val FRAGMENT_SIZE:Int = 150 //片段最大长度
  val MAX_HIGHLIGHTER:Int = 10 //最多的高亮次数


  val LINE_SEPARATOR:String = System.getProperty("line.separator", "\n")

  val analyzer: HanLPAnalyzer = new HanLPAnalyzer
}
