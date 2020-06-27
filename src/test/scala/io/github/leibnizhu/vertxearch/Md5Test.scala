package io.github.leibnizhu.vertxearch

import java.security.MessageDigest

import org.scalatest.FunSuite

class Md5Test extends FunSuite {
  test("123456") {
    assert(md5("123456") == "E10ADC3949BA59ABBE56E057F20F883E")
    assert(md5("1234562") == "6978915EE7DBC5AFF1BC34B59E8C0FC5")
    assert(md5("12345623455") == "044459010679A7BBEA66F7CE900AA04B")
  }

  test("javaMD5") {
    val javaMd5 = new MD5Java
    assert(javaMd5.md5("123456") == "E10ADC3949BA59ABBE56E057F20F883E")
    assert(javaMd5.md5("1234562") == "6978915EE7DBC5AFF1BC34B59E8C0FC5")
    assert(javaMd5.md5("12345623455") == "044459010679A7BBEA66F7CE900AA04B")
  }

  def md5(s: String): String = {
    val hexStrs = Array("0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F")
    def byteToHex(b: Byte): String = hexStrs((b & 0xf0) >> 4) + hexStrs(b & 0x0f)
    MessageDigest.getInstance("MD5").digest(s.getBytes).map(byteToHex).foldLeft("")(_ + _)
  }
}
