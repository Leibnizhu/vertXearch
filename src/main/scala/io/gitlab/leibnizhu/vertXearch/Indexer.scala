package io.gitlab.leibnizhu.vertXearch

import java.io.{File, FileReader}
import java.nio.file.Paths

import org.apache.lucene.document._
import org.apache.lucene.index.{IndexWriter, IndexWriterConfig}
import org.apache.lucene.store.FSDirectory

class Indexer(indexDirectoryPath: String) {
  private val indexDirectory = FSDirectory.open(Paths.get(indexDirectoryPath))
  private val writer: IndexWriter = new IndexWriter(indexDirectory, new IndexWriterConfig(Constants.analyzer))

  def close(): Unit = {
    writer.close()
  }

  private def getDocument(file: File): Document = {
    val document = new Document
    document.add(new TextField(Constants.CONTENTS, new FileReader(file)))
    document.add(new TextField(Constants.TITLE, file.getName, Field.Store.YES))
    document.add(new StringField(Constants.ARTICLE_PATH, file.getCanonicalPath, Field.Store.YES))
    document
  }

  private def indexFile(file: File): Unit = {
    System.out.println("Indexing " + file.getCanonicalPath)
    val document = getDocument(file)
    writer.addDocument(document)
  }

  def createIndex(dataDirPath: String): Int = {
    for (file <- new File(dataDirPath).listFiles)
      if (!file.isDirectory && !file.isHidden && file.exists && file.canRead)
        indexFile(file)
    writer.numDocs
  }
}
