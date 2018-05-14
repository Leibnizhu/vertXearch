package io.gitlab.leibnizhu.vertXearch

import java.io.File
import java.nio.file.Paths

import io.gitlab.leibnizhu.vertXearch.Constants._
import io.vertx.core.{AsyncResult, Future, Handler}
import io.vertx.scala.core.CompositeFuture
import org.apache.lucene.document._
import org.apache.lucene.index.{IndexWriter, IndexWriterConfig}
import org.apache.lucene.store.FSDirectory
import org.slf4j.LoggerFactory


class Indexer(indexDirectoryPath: String) {
  private val log = LoggerFactory.getLogger(getClass)
  private val indexDirectory = FSDirectory.open(Paths.get(indexDirectoryPath))
  private val writer: IndexWriter = new IndexWriter(indexDirectory, new IndexWriterConfig(analyzer))

  def close(): Unit = {
    writer.close()
  }

  def createIndex(dataDirPath: String, callback: Handler[AsyncResult[Int]]): Unit = {
    CompositeFuture.all(new File(dataDirPath).listFiles
      .filter(file => !file.isDirectory && file.exists && file.canRead && file.getName.endsWith(".txt"))
      .map(file => {
        val future:io.vertx.scala.core.Future[Boolean] = io.vertx.scala.core.Future.future()
        indexFile(file, future.completer())
        future
      }).toBuffer
    ).setHandler(ar => {
      if(ar.succeeded()){
        callback.handle(Future.succeededFuture(writer.numDocs))
      } else{
        callback.handle(Future.failedFuture(ar.cause()))
      }
    })
  }

  private def indexFile(file: File, callback: Handler[AsyncResult[Boolean]]): Unit = {
    System.out.println("Indexing " + file.getCanonicalPath)
    readDocument(file, doc => {
      writer.addDocument(doc)
      callback.handle(Future.succeededFuture())
    })
  }

  private def readDocument(file: File, callback: Handler[Document]): Unit = {
    Article.fromFile(file, ar => {
      if(ar.succeeded()){
        val article = ar.result()
        log.info(s"读取到文章(ID=${article.id}, 标题=${article.title})")
        val document = new Document
        document.add(new Field(ID, article.id, FieldTypeFactory.storedNotIndexed))//ID不参与索引
        document.add(new TextField(TITLE, article.title, Field.Store.YES))
        document.add(new Field(AUTHOR, article.author, FieldTypeFactory.storedNotAnalyzed))//作者不需要分词
        document.add(new TextField(CONTENTS, article.content.toString, Field.Store.YES))
        callback.handle(document)
      } else {
        log.error("读取文章文件失败.", ar.cause())
      }
    })
  }
}
