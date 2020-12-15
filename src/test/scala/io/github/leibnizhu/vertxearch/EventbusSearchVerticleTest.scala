package io.github.leibnizhu.vertxearch

import io.github.leibnizhu.vertxearch.utils.Constants
import io.github.leibnizhu.vertxearch.utils.EventbusRequestUtil._
import io.github.leibnizhu.vertxearch.verticle.EventbusSearchVerticle
import io.vertx.core.eventbus.{Message, ReplyException, ReplyFailure}
import io.vertx.core.json.JsonObject
import io.vertx.core.{AsyncResult, DeploymentOptions, Vertx}
import org.scalatest.{Assertion, AsyncFlatSpec, BeforeAndAfterAll}
import org.slf4j.LoggerFactory

import scala.concurrent.Promise

class EventbusSearchVerticleTest extends AsyncFlatSpec with BeforeAndAfterAll {
  private val log = LoggerFactory.getLogger(getClass)
  private val vertx = Vertx.vertx()
  private val configFile = "src/main/resources/config.json"
  private val config = new JsonObject(vertx.fileSystem().readFileBlocking(configFile))
  private val searchEventbusAddress = config.getString("eventbusAddress", "search") //EventBus监听地址

  override def beforeAll: Unit = {
    Constants.init(config)
    val start = System.currentTimeMillis()
    val future = vertx.deployVerticle(s"scala:${classOf[EventbusSearchVerticle].getName}", new DeploymentOptions().setConfig(config))
    while (!future.isComplete) {}
    if (future.succeeded()) {
      log.info("deploy verticle 结束,耗时:{}ms,ID={}", System.currentTimeMillis() - start, future.result())
    } else {
      log.info("deploy verticle 结束,耗时:{}ms,异常:{}", System.currentTimeMillis() - start, future.cause().getMessage)
      future.cause().printStackTrace()
    }
  }

  "向eventbus发送查询请求,不限长度" should "返回相应查询结果" in {
    val promise = Promise.apply[Assertion]()
    vertx.eventBus().request(searchEventbusAddress, searchRequest("clojure"), (ar: AsyncResult[Message[JsonObject]]) => {
      if (ar.succeeded()) {
        val respJson = ar.result().body()
        log.info(s"查询clojure不限制长度:接收到EventBus响应消息内容：${respJson.encodePrettily}")
        promise.success(assert(respJson.getString("status") == "success" &&
          !respJson.getJsonArray("results").isEmpty &&
          respJson.containsKey("cost")))
      } else {
        log.error("抛出异常", ar.cause())
        promise.failure(ar.cause())
      }
    })
    promise.future
  }

  "向eventbus发送查询请求,限制长度为2" should "返回相应查询结果,长度最大为2" in {
    val promise = Promise.apply[Assertion]()
    vertx.eventBus().request(searchEventbusAddress, searchRequest("clojure", 2), (ar: AsyncResult[Message[JsonObject]]) => {
      if (ar.succeeded()) {
        val respJson = ar.result().body()
        log.info(s"查询clojure限制长度为2:接收到EventBus响应消息内容：${respJson.encodePrettily}")
        promise.success(assert(respJson.getString("status") == "success" &&
          !respJson.getJsonArray("results").isEmpty &&
          respJson.getJsonArray("results").size() <= 2 &&
          respJson.containsKey("cost")))
      } else {
        log.error("抛出异常", ar.cause())
        promise.failure(ar.cause())
      }
    })
    promise.future
  }

  "向eventbus发送查询请求,查询不可能存在的词" should "返回结果应为空" in {
    val promise = Promise.apply[Assertion]()
    vertx.eventBus().request(searchEventbusAddress, searchRequest("thisKeywordWillResponseEmptyResult"), (ar: AsyncResult[Message[JsonObject]]) => {
      if (ar.succeeded()) {
        val respJson = ar.result().body()
        log.info(s"查询不可能存在的词:接收到EventBus响应消息内容：${respJson.encodePrettily}")
        promise.success(assert(respJson.getString("status") == "success" &&
          respJson.getJsonArray("results").isEmpty &&
          respJson.containsKey("cost")))
      } else {
        log.error("抛出异常", ar.cause())
        promise.failure(ar.cause())
      }
    })
    promise.future
  }

  "向eventbus发送错误请求方法" should "返回404错误" in {
    val promise = Promise.apply[Assertion]()
    vertx.eventBus().request(searchEventbusAddress, searchRequest("test").put(REQ_METHOD_KEY, "aaa"), (ar: AsyncResult[Message[JsonObject]]) => {
      if (ar.succeeded()) {
        val respJson = ar.result().body()
        log.info(s"发送错误请求方法:接收到EventBus响应消息内容：${respJson.encodePrettily}")
        promise.failure(new IllegalStateException("发送错误请求方法不应该有返回"))
      } else {
        val cause = ar.cause()
        log.error("抛出异常", cause)
        promise.success(assert(cause.isInstanceOf[ReplyException] &&
          cause.asInstanceOf[ReplyException].failureCode() == 404 &&
          cause.asInstanceOf[ReplyException].failureType() == ReplyFailure.RECIPIENT_FAILURE
        ))
      }
    })
    promise.future
  }

    override def afterAll: Unit = {
      log.info("Eventbus测试准备关闭Vertx")
      val closeFuture = vertx.close()
      while (!closeFuture.isComplete) {}
      log.info("Eventbus测试已经关闭Vertx")
    }
}
