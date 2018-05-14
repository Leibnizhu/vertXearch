package io.gitlab.leibnizhu.vertXearch

import java.util

import io.vertx.core.{AsyncResult, Handler}

trait Engine {
  /**
    * 对源目录下所有可用文件进行索引构建
    *
    * @return 索引的文件数量
    */
  def createIndex(): Int

  /**
    * 对源目录下所有可用文件进行索引更新
    *
    * @return 索引的文件数量
    */
  def refreshIndex(): Int

  /**
    * 按指定关键词进行查找
    *
    * @param searchQuery 查找关键词
    * @param callback 查询成功后的回调方法, 处理内容为 匹配的文档,按相关度降序
    */
  def search(searchQuery: String, callback: Handler[AsyncResult[util.List[SearchResult]]]): Unit

  /**
    * 关闭搜索引擎
    */
  def stop(callback: Handler[AsyncResult[Unit]]): Unit
}
