package com.carlca
package bitwig

import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.charset.StandardCharsets
import java.nio.ByteBuffer

import scala.util.control.Breaks.{break}
import scala.util.boundary

class ReadResult(pos: Integer, size: Integer, data: Option[Array[Byte]]):
  def this(pos: Integer, size: Integer) = this(pos, size, None)
  def this() = this(0 ,0, None)
  def getPos: Integer = this.pos
  def getSize: Integer = this.size
  def getData: Option[Array[Byte]] = this.data

object Parser: 
  def main(args: Array[String]): Unit = 
    if args.length == 0 then generateDummyOutput()
    else 
      val debug = args.length == 2 && args(1) == "debug"
      processPreset(args(0), debug)
    
  private def generateDummyOutput(): Unit = System.out.println()

  private def processPreset(filename: String, debug: Boolean): Unit =
    val file = new RandomAccessFile(new File(filename), "r")
    var pos = 0x36
    while
      val result = readKeyAndValue(file, pos, debug)
      pos = result.getPos
      result.getSize != 0
    do ()
    if file != null then file.close()

  @throws[IOException]
  private def readKeyAndValue(file: RandomAccessFile, pos: Int, debug: Boolean): ReadResult =
    var pos2 = pos
    var skips = getSkipSize(file, pos2)
    
    if debug then
      getSkipSizeDebug(file, pos2)
      System.out.printf("%d skips\n", skips)
    pos2 += skips
    
    var result = readNextSizeAndChunk(file, pos2)
    pos2 = result.getPos; var size = result.getSize; var data = result.getData.get
    if size == 0 then return new ReadResult()
    printOutput(size, pos2, data)
    
    skips = getSkipSize(file, pos2)
    if debug then
      getSkipSizeDebug(file, pos2)
      System.out.printf("%d skips\n", skips)
    pos2 += skips
    
    result = readNextSizeAndChunk(file, pos2)
    pos2 = result.getPos; size = result.getSize; 
    if result.getData.isEmpty then return new ReadResult()
    data = result.getData.get
    printOutput(size, pos2, data)

    System.out.println()
    new ReadResult(pos2, size)

  @throws[IOException]
  private def getSkipSize(file: RandomAccessFile, pos: Int): Int =
    val bytes = readFromFile(file, pos, 32, false).getData.get
    val check = Array[Int](5, 8, 13)
    bytes.indices.collectFirst:
      case i if bytes(i) >= 0x20 && check.contains(i & 255) => i - 4
    .getOrElse: 
      1

  @throws[IOException]
  private def getSkipSizeDebug(file: RandomAccessFile, pos: Int): Unit =
    val bytes = readFromFile(file, pos, 32, false).getData.get
    for b <- bytes do
      System.out.printf("%02x ", b)
    System.out.println()
    for b <- bytes do
      if b >= 0x41 then System.out.printf(".%c.", b)
      else System.out.print("   ")
    System.out.println()

  private def printOutput(size: Int, pos: Int, data: Array[Byte]): Unit =
    System.out.printf("size: %x\n", size)
    System.out.printf("stringPos: %x\n", pos)
    System.out.println("text: " + new String(data, StandardCharsets.UTF_8))

  @throws[IOException]
  private def readNextSizeAndChunk(file: RandomAccessFile, pos: Int): ReadResult =
    val intChunk = readIntChunk(file, pos)
    var pos2 = intChunk.getPos
    var size = intChunk.getSize
    if size == 0 then return new ReadResult(pos2, 0)
    readFromFile(file, pos2, size, true)

  @throws[IOException]
  private def readIntChunk(file: RandomAccessFile, pos: Int): ReadResult =
    val newRead = readFromFile(file, pos, 4, true)
    new ReadResult(newRead.getPos, ByteBuffer.wrap(newRead.getData.get).getInt)

  @throws[IOException]
  private def readFromFile(file: RandomAccessFile, pos: Int, size: Int, advance: Boolean): ReadResult =
    var pos2 = pos
    var res = new Array[Byte](size)
    file.seek(pos2)
    try file.readFully(res)
    catch case e: IOException => return new ReadResult()
    if advance then pos2 += size
    new ReadResult(pos2, size, Option(res))

end Parser  



