package io.gitlab.leibnizhu.vertXearch.verticle

import io.gitlab.leibnizhu.vertXearch.utils.EventbusUtil.Method.{ADD_ARTICLE, SEARCH}
import io.gitlab.leibnizhu.vertXearch.utils.EventbusUtil._
import io.vertx.core.json.JsonObject
import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.core.eventbus.Message
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}

class EventbusSearchVerticle extends ScalaVerticle{
  private val log = LoggerFactory.getLogger(getClass)


  override def start(): Unit = {
    super.start()
    vertx.eventBus.consumer[JsonObject](SEARCH_LISTEN_ADDRESS).handler(handleEventbusMessage)
  }

  private def handleEventbusMessage(msg: Message[JsonObject]): Unit = {
    val msgBody = msg.body
    log.debug(s"接收到EventBus请求消息(${msg.address}),消息内容：$msgBody")
    //尝试解析请求的方法名
    val methodStr = msgBody.getString(REQ_METHOD_KEY)
    Try(Method.withName(methodStr)) match {
      case Success(ADD_ARTICLE) => handleAddArticleRequest(msg)
      case Success(SEARCH) => handleSearchRequest(msg)
      case Failure(cause) =>
        log.error(s"错误的请求方法名:$methodStr, 异常信息:${cause.getMessage}")
        msg.fail(404, s"错误的请求方法名,请求字段'$REQ_METHOD_KEY'=$methodStr")
    }
  }

  //TODO 完成新增文章到索引的功能,暂时不处理
  def handleAddArticleRequest(msg: Message[JsonObject]): Unit = {
    msg.reply("Not Finish yet")
  }

  def handleSearchRequest(msg: Message[JsonObject]): Unit = {
    msg.reply("Not Finish yet")
  }
}
