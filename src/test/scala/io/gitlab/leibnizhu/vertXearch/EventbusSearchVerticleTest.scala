package io.gitlab.leibnizhu.vertXearch

import io.gitlab.leibnizhu.vertXearch.utils.EventbusUtil._
import io.gitlab.leibnizhu.vertXearch.verticle.EventbusSearchVerticle
import io.vertx.core.eventbus.ReplyException
import io.vertx.core.json.JsonObject
import io.vertx.scala.core.{DeploymentOptions, Future, Vertx}
import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global

class EventbusSearchVerticleTest extends FlatSpec with BeforeAndAfterAll {
  private val log = LoggerFactory.getLogger(getClass)
  private val vertx = Vertx.vertx()
  private var config: JsonObject = _
  private val configFile = "/Users/leibnizhu/workspace/vertx-cn-website/vertXearch/src/main/resources/config.json"
  private val futures = Array.fill(4)(Future.future[Unit]())

  override def beforeAll: Unit = {
    this.config = new JsonObject(vertx.fileSystem().readFileBlocking(configFile))
    val future = vertx.deployVerticleFuture(s"scala:${classOf[EventbusSearchVerticle].getName}", DeploymentOptions().setConfig(config))
    while (!future.isCompleted) {}
  }

  "向eventbus发送查询请求,不限长度" should "返回相应查询结果" in {
    vertx.eventBus().sendFuture[JsonObject](SEARCH_LISTEN_ADDRESS, searchRequest("clojure")).onComplete(tried => {
      try {
        assert(tried.isSuccess)
        val respJson = tried.get.body()
        log.info(s"查询clojure不限制长度:接收到EventBus响应消息内容：${respJson.encodePrettily()}")
        assert(respJson.getString("status") == "success")
        assert(!respJson.getJsonArray("results").isEmpty)
        assert(respJson containsKey "cost")
      } finally
        futures(0).complete()
    })
  }

  "向eventbus发送查询请求,限制长度为2" should "返回相应查询结果,长度最大为2" in {
    vertx.eventBus().sendFuture[JsonObject](SEARCH_LISTEN_ADDRESS, searchRequest("clojure", 2)).onComplete(tried => {
      try {
        assert(tried.isSuccess)
        val respJson = tried.get.body()
        log.info(s"查询clojure限制长度为2:接收到EventBus响应消息内容：${respJson.encodePrettily()}")
        assert(respJson.getString("status") == "success")
        assert(!respJson.getJsonArray("results").isEmpty)
        assert(respJson.getJsonArray("results").size() <= 2)
        assert(respJson containsKey "cost")
      } finally
        futures(1).complete()
    })
  }

  "向eventbus发送查询请求,查询不可能存在的词" should "返回结果应为空" in {
    vertx.eventBus().sendFuture[JsonObject](SEARCH_LISTEN_ADDRESS, searchRequest("thisKeywordWillResponseEmptyResult")).onComplete(tried => {
      try {
        assert(tried.isSuccess)
        val respJson = tried.get.body()
        log.info(s"查询thisKeywordWillResponseEmptyResult:接收到EventBus响应消息内容：${respJson.encodePrettily()}")
        assert(respJson.getString("status") == "success")
        assert(respJson.getJsonArray("results").isEmpty)
        assert(respJson containsKey "cost")
      } finally
        futures(2).complete()
    })
  }

  "向eventbus发送错误请求方法" should "返回404错误" in {
    vertx.eventBus().sendFuture[JsonObject](SEARCH_LISTEN_ADDRESS, searchRequest("test").put(REQ_METHOD_KEY, "aaa")).onComplete(tried => {
      try {
        assert(tried.isFailure)
        val exp = tried.failed.get.asInstanceOf[ReplyException]
        assert(exp.failureCode() == 404)
        log.info(s"发送错误的请求方法:抛出异常,出错信息:$exp")
      } finally
        futures(3).complete()
    })
  }

  override def afterAll: Unit = {
    log.info("等待异步任务关闭")
    futures.foreach(f => while (!f.isComplete()) {})
    log.info("关闭Vertx")
    val closeFuture = vertx.closeFuture()
    while (!closeFuture.isCompleted) {}
  }
}
