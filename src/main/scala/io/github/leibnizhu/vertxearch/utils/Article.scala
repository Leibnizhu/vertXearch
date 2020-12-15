package io.github.leibnizhu.vertxearch.utils

import java.io.File

import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject
import io.vertx.core.{AsyncResult, Future, Handler, Promise, Vertx}
import org.slf4j.LoggerFactory

import scala.collection.mutable.ArrayBuffer

case class Article(id: String, content: String, path: String = null) extends Serializable {
  /**
    * 将Article对象写入到文件,目录由配置文件指定
    * TODO 按最新路径规则修改
    *
    * @param callback 写入到文件之后的回调,无传入结果
    */
  def writeToFile(vertx: Vertx, callback: Handler[AsyncResult[Void]]): Unit = {
    val fileName = Constants.articlePath + "/" + this.id + ".txt"
    val fileContent = this.content
    vertx.fileSystem().writeFile(fileName, Buffer.buffer(fileContent)).onComplete(callback)
  }

  def toJsonObject: JsonObject = new JsonObject()
    .put("id", this.id).put("content", this.content)

  def toLowerCase: Article = Article(this.id.toLowerCase, this.content.toLowerCase)
}

object Article {
  private val log = LoggerFactory.getLogger(Article.getClass)

  /**
    * 从文件读取文章
    *
    * @param file    文章txt文件
    * @param handler 读取文章之后的回调,传入解析到的Article
    */
  def fromFile(vertx:Vertx, file: File, handler: Handler[AsyncResult[Article]]): Unit =
    vertx.fileSystem().readFile(file.getAbsolutePath).onComplete((ar: AsyncResult[Buffer]) => {
      if (ar.succeeded()) {
        log.debug(s"读取文章文件${file.getAbsolutePath}成功")
        val article = Article(file, ar.result())
        handler.handle(Future.succeededFuture(article))
      } else {
        log.error("读取文章文件失败.", ar.cause())
        handler.handle(Future.failedFuture(ar.cause()))
      }
    })

  /**
    * 解析文章
    * 文件名:[ID].txt
    * 第一行标题，第二行作者，第三行开始正文
    *
    * @param file   文件对象
    * @param buffer 读取到文件内容的Buffer
    * @return
    */
  def apply(file: File, buffer: Buffer): Article = {
    val id = file.getParentFile.getName
    val fileContent = buffer.toString() //2018.07.30 提高通用性,不拆分文件内容了,直接做索引
    //    val fistLineIndex = fileContent.indexOf(LINE_SEPARATOR)
    //    val title = fileContent.substring(0, fistLineIndex)
    //    val secondLineIndex = fileContent.indexOf(LINE_SEPARATOR, fistLineIndex + LINE_SEPARATOR.length)
    //    val author = fileContent.substring(fistLineIndex + LINE_SEPARATOR.length, secondLineIndex)
    //    val content = fileContent.substring(secondLineIndex + LINE_SEPARATOR.length)
    //HanLp区分大小写，所以全转小写
    Article(id, fileContent.toLowerCase, file.getAbsolutePath)
  }

  def getFilesRecursively(root: File): Array[File] = {
    (ArrayBuffer[File]() ++=
      root.listFiles(file => !file.isDirectory && file.exists && file.canRead && "publication.json".equals(file.getName)) ++=
      root.listFiles(_.isDirectory).flatMap(getFilesRecursively))
      .toArray
  }
}