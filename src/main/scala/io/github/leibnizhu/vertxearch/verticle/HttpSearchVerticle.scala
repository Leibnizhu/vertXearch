package io.github.leibnizhu.vertxearch.verticle

import io.github.leibnizhu.vertxearch.engine.{Engine, EngineImpl}
import io.github.leibnizhu.vertxearch.utils.Constants._
import io.github.leibnizhu.vertxearch.utils.HttpRequestUtil.{parseRequestParam, _}
import io.github.leibnizhu.vertxearch.utils.ResponseUtil._
import io.github.leibnizhu.vertxearch.utils.{Article, Constants}
import io.vertx.core._
import io.vertx.core.http.HttpServer
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.ext.web.{Router, RoutingContext}
import org.slf4j.LoggerFactory

class HttpSearchVerticle extends AbstractVerticle {
  private val log = LoggerFactory.getLogger(getClass)
  private var mainRouter: Router = _
  private var server: HttpServer = _
  private var searchEngine: Engine = _

  override def start(startPromise: Promise[Void]): Unit = {
    //初始化工具类/组件
    initComponents((ar: AsyncResult[Unit]) => {
      mountRouters() //挂载所有子路由
      startServer(); //启动服务器
      if (ar.succeeded())
        startPromise.complete()
      else
        startPromise.fail(ar.cause())
    })
  }

  private def initComponents(afterSearchEngineStarted: Handler[AsyncResult[Unit]]): Unit =
    try {
      this.mainRouter = Router.router(vertx)
      this.server = vertx.createHttpServer
      this.searchEngine = new EngineImpl(vertx, indexPath, articlePath).init(afterSearchEngineStarted)
    } catch {
      case e: Throwable => afterSearchEngineStarted.handle(Future.failedFuture(e))
    }

  def mountRouters(): Unit = {
    mainRouter.get("/static/*").handler(StaticHandler.create.setWebRoot("static"))
    mainRouter.get(s"/q/:$REQ_PARAM_KEYWORD").handler(searchByKeyWord)
    mainRouter.get(s"/q/:$REQ_PARAM_KEYWORD/:$REQ_PARAM_LENGTH").handler(searchByKeyWord)
  }

  private def searchByKeyWord: Handler[RoutingContext] = rc => {
    val startTime = System.currentTimeMillis()
    val (request, response) = (rc.request, rc.response)
    val (keyWord, length) = parseRequestParam(request)
    searchEngine.search(keyWord, length, //防止传入的长度值小于等于0
      (ar: AsyncResult[List[Article]]) => {
        val costTime = System.currentTimeMillis() - startTime
        response.putHeader("content-type", "application/json;charset=UTF-8").end(
          if (ar.succeeded()) {
            val results = ar.result()
            log.debug(s"查询关键词'$keyWord'成功, 查询到${results.size}条结果, 耗时${costTime}毫秒")
            successSearch(results, costTime).toString
          } else {
            val cause = ar.cause()
            log.error(s"查询关键词'$keyWord'失败, 耗时${costTime}毫秒", cause)
            failSearch(cause, costTime).toString
          })
      })
  }

  /**
    * 启动服务器
    */
  private def startServer(): Unit = {
    val port = config.getInteger("serverPort", 8083)
    server.requestHandler(mainRouter.handle(_)).listen(port).onComplete(ar => if (ar.succeeded()) {
      log.info("监听{}端口的HTTP服务器启动成功", port)
    } else {
      log.error("监听{}端口的HTTP服务器失败，原因：{}", Array(port, ar.cause().getLocalizedMessage): _*)
    })
  }

  override def stop(): Unit = {
    server.close(res => log.info("HTTP服务器关闭" + (if (res.succeeded) "成功" else "失败")))
    searchEngine.stop((res: AsyncResult[Unit]) => log.info("搜索引擎关闭" + (if (res.succeeded) "成功" else "失败")))
    super.stop()
  }

}
