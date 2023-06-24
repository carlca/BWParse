package com.carlca.bitwig;

class ReadResult {

  private Integer pos;
  private Integer size;
  private byte[] data;

  public ReadResult() {
    this.pos = 0;
    this.size = 0;
    this.data = new byte[0];
  }  
  
  public ReadResult(Integer pos, Integer size) {
    this.pos = pos;
    this.size = size;
    this.data = new byte[0];
  }
  
  public ReadResult(Integer pos, Integer size, byte[] data) {
    this.pos = pos;
    this.size = size;
    this.data = data;
  }
  
  public Integer getPos() {
    return this.pos;
  }

  public Integer getSize() {
    return this.size;
  }

  public byte[] getData() {
    return this.data;
  }

}
