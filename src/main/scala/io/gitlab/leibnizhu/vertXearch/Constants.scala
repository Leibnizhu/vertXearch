package io.gitlab.leibnizhu.vertXearch

import com.hankcs.lucene.HanLPAnalyzer
import io.vertx.scala.core.{Context, Vertx}

object Constants {
  var vertx: Vertx = _
  var vertxContext: Context = _

  def init(ctx:Context):Unit ={
    this.vertxContext = ctx
    this.vertx = ctx.owner
  }

  def indexPath():String = vertxContext.config().get.getString("indexPath", "indexes")

  def articlePath():String = vertxContext.config().get.getString("articlePath", "articles")

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
