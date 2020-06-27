package io.github.leibnizhu.vertxearch

import io.github.leibnizhu.vertxearch.verticle.HttpSearchVerticle
import io.vertx.core.json.JsonObject
import io.vertx.scala.core.{DeploymentOptions, Vertx}
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object MainLauncher {
  private val log = LoggerFactory.getLogger(classOf[MainLauncher])

  def main(args: Array[String]): Unit = {
    //Force to use slf4j
    System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory")
    System.setProperty("vertx.disableFileCaching", "true")
    val vertx = Vertx.vertx
    val fs = vertx.fileSystem()
    val configFile = if (args.length > 0) args(0) else "config.json" //配置文件路径,如果没有输入参数则读取当前目录的config.json
    if (fs.existsBlocking(configFile)) { //配置文件若存在则读取,否则使用默认配置
      fs.readFileFuture(configFile).onComplete {
        case Success(result) =>
          log.info("读取配置文件{}成功,准备启动Verticle.", configFile)
          vertx.deployVerticle(s"scala:${classOf[HttpSearchVerticle].getName}",
            DeploymentOptions().setConfig(new JsonObject(result)))
        case Failure(cause) =>
          log.error("读取配置文件失败.", cause)
          System.exit(1)
      }
    } else {
      vertx.deployVerticle(s"scala:${classOf[HttpSearchVerticle].getName}")
    }
  }
}

class MainLauncher //java的main方法要用