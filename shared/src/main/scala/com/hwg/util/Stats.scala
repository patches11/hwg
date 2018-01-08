package com.hwg.util

object Stats {
  def stdDev[T: Fractional](data: Iterable[T])(implicit T: Fractional[T]): Option[Double] = {
    mean(data).flatMap {avg =>
      val squareDiff = data.map { v =>
        val diff = T.minus(v, avg)
        T.times(diff, diff)
      }

      mean(squareDiff).map(s => Math.sqrt(T.toDouble(s)))
    }
  }

  def mean[T: Fractional](data: Iterable[T])(implicit T: Fractional[T]): Option[T] = {
    if (data.isEmpty) {
      None
    } else {
      Some(T.div(data.sum, T.fromInt(data.size)))
    }
  }

  def median[T: Fractional](data: Iterable[T])(implicit T: Fractional[T], ordering: Ordering[T]): Option[T] = {
    data.size match {
      case 0 | 1 =>
        data.headOption
      case n if n % 2 == 0 =>
        Some(data.toArray.sorted(ordering)(data.size / 2))
      case _ =>
        val sorted = data.toArray.sorted(ordering)
        val floor = data.size / 2
        Some(T.div(T.plus(sorted(floor), sorted(floor + 1)), T.fromInt(2)))
    }
  }
}
