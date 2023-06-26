package com.carlca
package bitwig

import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.charset.StandardCharsets
import java.nio.ByteBuffer

import scala.util.control.Breaks.{break}
import scala.util.boundary

case class ReadResult(
    pos: Int,
    size: Int,
    data: Array[Byte]
)

object ReadResult:
  def empty: ReadResult = ReadResult(0, 0, Array.emptyByteArray)
  def apply(pos: Int, size: Int): ReadResult =
    ReadResult(pos, size, Array.emptyByteArray)

object Parser:
  def main(args: Array[String]): Unit =
    if args.length == 0 then generateDummyOutput()
    else
      val debug = args.length == 2 && args(1) == "debug"
      processPreset(args(0), debug)
  end main

  private def generateDummyOutput(): Unit = System.out.println()

  private def processPreset(filename: String, debug: Boolean): Unit =
    val file = RandomAccessFile(File(filename), "r")
    var pos = 0x36
    while
      val result = readKeyAndValue(file, pos, debug)
      pos = result.pos
      result.size != 0
    do ()
    if file != null then file.close()
  end processPreset

  @throws[IOException]
  private def readKeyAndValue(
      file: RandomAccessFile,
      pos: Int,
      debug: Boolean
  ): ReadResult =
    var pos2 = pos
    var skips = getSkipSize(file, pos2)

    if debug then
      getSkipSizeDebug(file, pos2)
      System.out.printf("%d skips\n", skips)
    pos2 += skips

    var result = readNextSizeAndChunk(file, pos2)
    pos2 = result.pos
    var size = result.size
    var data = result.data
    if size == 0 then return ReadResult.empty
    printOutput(size, pos2, data)

    skips = getSkipSize(file, pos2)
    if debug then
      getSkipSizeDebug(file, pos2)
      System.out.printf("%d skips\n", skips)
    pos2 += skips

    result = readNextSizeAndChunk(file, pos2)
    pos2 = result.pos
    size = result.size
    if result.data.isEmpty then ReadResult.empty
    data = result.data
    printOutput(size, pos2, data)

    System.out.println()
    ReadResult(pos2, size)
  end readKeyAndValue

  @throws[IOException]
  private def getSkipSize(file: RandomAccessFile, pos: Int): Int =
    val bytes = readFromFile(file, pos, 32, false).data
    val check = Array[Int](5, 8, 13)
    bytes.indices
      .collectFirst:
        case i if bytes(i) >= 0x20 && check.contains(i & 255) => i - 4
      .getOrElse:
        1
  end getSkipSize

  @throws[IOException]
  private def getSkipSizeDebug(file: RandomAccessFile, pos: Int): Unit =
    val bytes = readFromFile(file, pos, 32, false).data
    for b <- bytes do System.out.printf("%02x ", b)
    System.out.println()
    for b <- bytes do
      if b >= 0x41 then System.out.printf(".%c.", b)
      else System.out.print("   ")
    System.out.println()
  end getSkipSizeDebug

  private def printOutput(size: Int, pos: Int, data: Array[Byte]): Unit =
    System.out.printf("size: %x\n", size)
    System.out.printf("stringPos: %x\n", pos)
    System.out.println("text: " + String(data, StandardCharsets.UTF_8))
  end printOutput

  @throws[IOException]
  private def readNextSizeAndChunk(
      file: RandomAccessFile,
      pos: Int
  ): ReadResult =
    val intChunk = readIntChunk(file, pos)
    val pos2 = intChunk.pos
    val size = intChunk.size
    if size == 0 then return ReadResult(pos2, 0)
    readFromFile(file, pos2, size, true)
  end readNextSizeAndChunk

  @throws[IOException]
  private def readIntChunk(file: RandomAccessFile, pos: Int): ReadResult =
    val newRead = readFromFile(file, pos, 4, true)
    ReadResult(newRead.pos, ByteBuffer.wrap(newRead.data).getInt)
  end readIntChunk

  @throws[IOException]
  private def readFromFile(
      file: RandomAccessFile,
      pos: Int,
      size: Int,
      advance: Boolean
  ): ReadResult =
    var pos2 = pos
    val res = new Array[Byte](size)
    file.seek(pos2)
    try file.readFully(res)
    catch case e: IOException => return ReadResult.empty
    if advance then pos2 += size
    ReadResult(pos2, size, res)
  end readFromFile

end Parser
