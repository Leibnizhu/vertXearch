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

  val CONTENTS: String = "contents"
  val TITLE: String = "title"
  val ARTICLE_PATH: String = "articlePath"
  val MAX_SEARCH: Int = 10

  val analyzer: HanLPAnalyzer = new HanLPAnalyzer
}
