package io.github.leibnizhu.vertxearch.utils

import io.vertx.core.json.JsonObject

import scala.collection.JavaConverters._

object ResponseUtil {
  def successSearch(results: List[Article], costTime: Long): JsonObject =
    new JsonObject().
      put("status", "success")
      .put("results", results.map(_.toJsonObject).asJava)
      .put("cost", costTime)

  def failSearch(cause: Throwable, costTime: Long): JsonObject =
    new JsonObject()
      .put("status", "error")
      .put("message", s"${cause.getClass.getName}:${cause.getMessage}")
      .put("cost", costTime)
}
