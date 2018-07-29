package io.gitlab.leibnizhu.vertXearch.utils

import io.vertx.core.json.JsonObject


object EventbusUtil {
  val SEARCH_LISTEN_ADDRESS = "search"
  val REQ_METHOD_KEY = "method"
  val REQ_KEYWORD_KEY = "keyword"
  val REQ_LENGTH_KEY = "length"

  object Method extends Enumeration {
    type Method = Value
    val ADD_ARTICLE: Method.Value = Value("add")
    val SEARCH: Method.Value = Value("search")
  }

  def searchRequest(keyword: String): JsonObject = new JsonObject()
    .put(REQ_METHOD_KEY, Method.SEARCH.toString)
    .put(REQ_KEYWORD_KEY, keyword)

  def searchRequest(keyword: String, length: Int): JsonObject =
    searchRequest(keyword).put(REQ_LENGTH_KEY, length)

  def addArticleRequest(article: Article): JsonObject = new JsonObject()
    .put(REQ_METHOD_KEY, Method.ADD_ARTICLE.toString)
}
