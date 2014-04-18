package org.beangle.commons.codec

trait Encoder[S, T] {
  def encode(s: S): T
}

trait Decoder[S, T] {
  def decode(s: S): T
}