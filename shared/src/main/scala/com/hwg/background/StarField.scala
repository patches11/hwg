package com.hwg.background

import java.awt.Color

import com.hwg.util.{MathExt, Point, RandomQueue}

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

object StarField {

  type Grid = Array[ArrayBuffer[Point]]

  def generateTexture(width: Int, height: Int, newPointsCount: Int, minRadius: Double, maxRadius: Double,
                      minDistFunc: (Double, Double) => Double, random: Random, maxIterations: Int): Array[Color] = {
    val data = Array.fill[Color](width * height)(Color.BLACK)
    val brightness = 150

    generate(width, height, minRadius, maxRadius, minDistFunc, newPointsCount, random, maxIterations).foreach((point) => {
      val index = (Math.floor(point.x) + Math.floor(point.y) * width).toInt
      val c = (random.nextInt(255 - brightness) + brightness).toShort

      data(index) = new Color(255, 255, 255, c)
    })

    data
  }

  def generate(width: Int, height: Int, minRadius: Double, maxRadius: Double,
    minDistFunc: (Double, Double) => Double, newPointsCount: Int, random: Random, maxIterations: Int): Array[Point] = {

    val pointSquare = MathExt.genPointSquare(3, includeOrigin = true)

    //Create the grid
    val cellSize = maxRadius / Math.sqrt(2)

    val cells = (Math.ceil(width / cellSize) * Math.ceil(height / cellSize)).toInt

    val grid: Grid = Array.fill(cells)(ArrayBuffer())

    //RandomQueue works like a queue, except that it
    //pops a random element from the queue instead of
    //the element at the head of the queue
    val processList: RandomQueue[Point] = new RandomQueue(random)
    val samplePoints: ArrayBuffer[Point] = ArrayBuffer()
    samplePoints.sizeHint(cells)

    //generate the first point randomly
    //and updates
    val firstPoint = Point(random.nextDouble * width, random.nextDouble *height)

    //update containers
    processList.push(firstPoint)
    samplePoints.append(firstPoint)
    grid(imageToGrid(firstPoint, cellSize, width)).append(firstPoint)

    var iteration = 0
    //generate other points from points in queue.
    while (!processList.isEmpty) {
      iteration += 1
      if (iteration >= maxIterations) {
        return samplePoints.toArray
      }
      val point = processList.pop()
      (0 until newPointsCount).foreach { _ =>
        val newPoint = generateRandomPointAround(point, minRadius, maxRadius, minDistFunc, random)
        //check that the point is in the image region
        //and no points exists in the point's neighbourhood
        if (!inNeighbourhood(grid, newPoint, minRadius, maxRadius, minDistFunc, cellSize, width, height, pointSquare) && inSquare(newPoint, width, height)) {
          //update containers
          processList.push(newPoint)
          samplePoints.append(newPoint)
          val gridSquare = grid(imageToGrid(newPoint, cellSize, width))
          gridSquare.append(newPoint)
        }
      }
    }

    samplePoints.toArray
  }

  def inSquare(point: Point, width: Int, height: Int): Boolean = {
    point.x >= 0 && point.x < width && point.y >= 0 && point.y < height
  }

  def imageToGridA(point: Point, cellSize: Double): Point = {
    val gridX = Math.floor(point.x / cellSize)
    val gridY = Math.floor(point.y / cellSize)
    Point(gridX, gridY)
  }

  def imageToGrid(point: Point, cellSize: Double, width: Int): Int = {
    val newPoint = imageToGridA(point, cellSize)
    (newPoint.x + newPoint.y * Math.floor(width / cellSize)).toInt
  }

  def generateRandomPointAround(point: Point, minRadius: Double, maxRadius: Double, minDistFunc: (Double, Double) => Double, random: Random): Point = {
    //non-uniform, favours points closer to the inner ring, leads to denser packings
    val r1 = random.nextDouble() //random point between 0 and 1
    val r2 = random.nextDouble()

    val minDist = minRadius + minDistFunc(point.x, point.y) * (maxRadius - minRadius)
    //random radius between mindist and 2 * mindist
    val radius = minDist * (r1 + 1)
    //random angle
    val angle = 2 * Math.PI * r2
    //the new point is generated around the point (x, y)
    val newX = point.x + radius * Math.cos(angle)
    val newY = point.y + radius * Math.sin(angle)

    Point(newX, newY)
  }

  def inNeighbourhood(grid: Grid, point: Point, minRadius: Double, maxRadius: Double,
                      minDistFunc: (Double, Double) => Double, cellSize: Double, width: Int, height: Int, pointSquare: Array[Point]): Boolean = {
    squareAroundPoint(grid, point, width, height, cellSize, pointSquare).exists((cell) => {
      distance(cell, point) < minRadius + minDistFunc(point.x, point.y) * (maxRadius - minRadius)
    })
  }

  def squareAroundPoint(grid: Grid, point: Point, width: Int, height: Int, cellSize: Double, pointSquare: Array[Point]): Array[Point] = {
    val gridPoint = imageToGridA(point, cellSize)

    pointSquare.map((point) => {
      Point(point.x + gridPoint.x, point.y + gridPoint.y)
    }).filter((point) => {
      inSquare(point, (width / cellSize).toInt, (height / cellSize).toInt)
    }).flatMap((gridPoint) => {
      grid((gridPoint.x + gridPoint.y * Math.floor(width / cellSize)).toInt)
    })
  }

  def distance(pointA: Point, pointB: Point): Double = {
    val xDiff = pointA.x - pointB.x
    val yDiff = pointA.y - pointB.y
    Math.sqrt(xDiff * xDiff + yDiff * yDiff)
  }
}
