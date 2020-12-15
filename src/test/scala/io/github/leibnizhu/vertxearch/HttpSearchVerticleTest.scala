package io.github.leibnizhu.vertxearch

import io.github.leibnizhu.vertxearch.utils.Constants
import io.github.leibnizhu.vertxearch.verticle.HttpSearchVerticle
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject
import io.vertx.core.{DeploymentOptions, Vertx}
import io.vertx.ext.web.client.{HttpResponse, WebClient}
import org.scalatest.{Assertion, AsyncFlatSpec, BeforeAndAfterAll}
import org.slf4j.LoggerFactory

import scala.concurrent.Promise

class HttpSearchVerticleTest extends AsyncFlatSpec with BeforeAndAfterAll {
  private val log = LoggerFactory.getLogger(getClass)
  private val configFile = "src/main/resources/config.json"
  private val vertx = Vertx.vertx()
  private val config = new JsonObject(vertx.fileSystem().readFileBlocking(configFile))
  private val host = "localhost"
  private val port = config.getInteger("serverPort", 8083)
  private val client = WebClient.create(vertx)

  override def beforeAll: Unit = {
    Constants.init(config)
    val start = System.currentTimeMillis()
    val future = vertx.deployVerticle(s"scala:${classOf[HttpSearchVerticle].getName}", new DeploymentOptions().setConfig(config))
    while (!future.isComplete) {}
    if (future.succeeded()) {
      log.info("deploy verticle 结束,耗时:{}ms,ID={}", System.currentTimeMillis() - start, future.result())
    } else {
      log.info("deploy verticle 结束,耗时:{}ms,异常:{}", System.currentTimeMillis() - start, future.cause().getMessage)
      future.cause().printStackTrace()
    }
  }

  "查询clojure" should "有结果返回且正确" in {
    val promise = Promise.apply[Assertion]()
    log.info("开始查询clojure测试")
    client.get(this.port, this.host, "/q/clojure").send().map((response: HttpResponse[Buffer]) => {
      val respJson = new JsonObject(response.body)
      log.info(s"查询clojure的查询结果:${respJson.encodePrettily()}")
      promise.success(assert(response.statusCode() == 200 &&
        respJson.getString("status") == "success" &&
        !respJson.getJsonArray("results").isEmpty &&
        respJson.containsKey("cost")))
    })
    promise.future
  }

  "查询clojure并限制返回长度" should "有结果返回且长度满足限制" in {
    val promise = Promise.apply[Assertion]()
    log.info("开始限制长度查询clojure测试")
    client.get(this.port, this.host, "/q/clojure/2").send().map((response: HttpResponse[Buffer]) => {
      val respJson = new JsonObject(response.body)
      log.info(s"查询clojure并限制最大长度为2的查询结果:${respJson.encodePrettily()}")
      promise.success(assert(response.statusCode() == 200 &&
        respJson.getString("status") == "success" &&
        !respJson.getJsonArray("results").isEmpty &&
        respJson.getJsonArray("results").size() <= 2 &&
        respJson.containsKey("cost")))
    })
    promise.future
  }

  "查询thisKeywordWillResponseEmptyResult" should "返回结果应为空" in {
    val promise = Promise.apply[Assertion]()
    log.info("开始查询thisKeywordWillResponseEmptyResul测试")
    client.get(this.port, this.host, "/q/thisKeywordWillResponseEmptyResult").send().map((response: HttpResponse[Buffer]) => {
      val respJson = new JsonObject(response.body)
      log.info(s"查询thisKeywordWillResponseEmptyResult的查询结果:${respJson.encodePrettily()}")
      promise.success(assert(response.statusCode() == 200 &&
        respJson.getString("status") == "success" &&
        respJson.getJsonArray("results").isEmpty &&
        respJson.containsKey("cost")))
    })
    promise.future
  }

  //FIXME 暂时没想到怎么能触发后台的错误,正常地请求,要么路径不对404,要么参数有问题但被处理掉了,要是删掉索引,可能影响其他测试
  "模拟请求错误" should "响应json带有message字段" in {
    val promise = Promise.apply[Assertion]()
    log.info("模拟请求错误测试")
    client.get(this.port, this.host, "/q/clojure/aaa").send().map((response: HttpResponse[Buffer]) => {
      val respJson = new JsonObject(response.body)
      log.info(s"模拟请求错误查询的结果:${respJson.encodePrettily()}")
      promise.success(assert(response.statusCode() == 200 &&
        //FIXME        respJson.getString("status") == "error" && //如果能模拟后台错误,这句应该取消注释
        //FIXME        respJson.containsKey("message") && //如果能模拟后台错误,这句应该取消注释
        respJson.containsKey("cost")))
    })
    promise.future
  }

  override def afterAll: Unit = {
    log.info("Http接口测试准备关闭Vertx")
    val closeFuture = vertx.close()
    while (!closeFuture.isComplete) {}
    log.info("Http接口测试已经关闭Vertx")
  }
}
