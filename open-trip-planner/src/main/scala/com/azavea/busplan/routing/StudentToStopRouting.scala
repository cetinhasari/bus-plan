package com.azavea.busplan.routing

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io._
import scala.collection.JavaConverters._

object StudentToStopRouting {

  def createStudentToStopCSV(
    path: String,
    results: Map[String, List[(String, Double)]],
    maxDistance: Double,
    studentToInfo: Map[String, (Int, String)]): Unit = {
    val csv = new FileWriter(path, true)
    val bw = new BufferedWriter(csv)
    for ((k, v) <- results) {
      bw.write(k)
      val newMaxDistance = getMaxDistance(maxDistance, studentToInfo(k)._1)
      val eligibleStops = getStopsBelowThreshold(v, newMaxDistance)
      for (stop <- eligibleStops) {
        bw.write("," + stop)
      }
      bw.newLine()
      bw.flush()
    }
  }

  def getMaxDistance(maxDistance: Double, grade: Int): Double = {
    if (grade < 7) {
      if (maxDistance > 2640) {
        val maxDistance = 2640
      }
    }
    maxDistance
  }

  def getStopsBelowThreshold(
    stopCosts: List[(String, Double)],
    maxDistance: Double): List[String] = {
    stopCosts
      .filter(s => s._2 < maxDistance)
      .map { s => s._1 }
  }

  def routeAllStudents(
    studentToPossibleStops: Map[String, List[String]],
    stopToLocation: Map[String, Node],
    studentToLocation: Map[String, Node],
    walkRouter: RouteGenerator): Map[String, List[(String, Double)]] = {
    studentToPossibleStops
      .map {
        case (key, value) => routeToEachEligibleStop(key, value,
          stopToLocation, studentToLocation, walkRouter)
      }
      .reduce { (map1, map2) => map1 ++ map2 }
  }

  def routeToEachEligibleStop(
    studentId: String,
    possibleStops: List[String],
    stopToLocation: Map[String, Node],
    studentToLocation: Map[String, Node],
    walkRouter: RouteGenerator): Map[String, List[(String, Double)]] = {
    val studentLocation = studentToLocation(studentId)
    val costList = possibleStops
      .map { stop =>
        (stop, walkRouter.getCost(studentLocation, stopToLocation(stop), 1513168200).distance)
      }
      .toList
    Map(studentId -> costList)
  }
}