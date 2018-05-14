package io.gitlab.leibnizhu.vertXearch

import io.vertx.core.{AsyncResult, Handler}

trait Engine {
  /**
    * 对源目录下所有可用文件进行索引构建
    *
    * @return 增加索引的文件数量
    */
  def createIndex(): Unit

  /**
    * 对源目录下所有可用文件进行索引更新
    *
    * @return 更新索引的文件数量
    */
  def refreshIndex(): Int

  /**
    * 启动文章更新定时器
    *
    * @param interval 定时间隔
    * @param callback 回调方法
    */
  def startRefreshTimer(interval: Long, callback: Handler[AsyncResult[Boolean]]):Unit

  /**
    * 按指定关键词进行查找
    *
    * @param searchQuery 查找关键词
    * @param callback 查询成功后的回调方法, 处理内容为 匹配的文档,按相关度降序
    */
  def search(searchQuery: String, length: Int, callback: Handler[AsyncResult[List[Article]]]): Unit

  /**
    * 关闭搜索引擎
    */
  def stop(callback: Handler[AsyncResult[Unit]]): Unit
}
