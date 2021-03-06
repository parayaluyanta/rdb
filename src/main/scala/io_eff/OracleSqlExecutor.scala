package io_eff

import java.sql.{Connection, DriverManager, ResultSet, Statement}

import zio.{App, IO}

import scala.collection.immutable
import scala.collection.mutable.ListBuffer
import reflect.runtime.universe._

object OracleSqlExecutor extends App {

  object Db {
    val url = "jdbc:oracle:thin:@localhost:1521/xe"
    val driver = "oracle.jdbc.driver.OracleDriver"
    val username = "SYSTEM"
    val password = "oracle"
  }

  import zio._

  import Db._

  def getConnection(url: String, username: String, password: String): IO[Throwable, Connection] =
    IO.succeed(DriverManager.getConnection(url, username, password))

  def createStatement(connection: Connection): IO[Throwable, Statement] =
    IO.succeed(connection.createStatement())

  def selectAll(query: String)(statement: Statement): IO[Throwable, ResultSet] =
    IO.succeed(statement.executeQuery(query))

  def run(args: List[String]) =
    program.fold(_ => 1, _ => 0)

  def asResult[a: TypeTag](rs: ResultSet): IO[Throwable, List[List[String]]] =
    IO.succeed {
      val numberOfCols = rs.getMetaData.getColumnCount

      var results = ListBuffer[List[String]]()

      while (rs.next()) {
        val x = typeOf[a]
        val y = typeOf[String]

        val row: Seq[String] = //x match {
//          case y =>
            for (index <- 1 to numberOfCols) yield rs.getString(index)
//          case _ =>
//            for (index <- 1 to numberOfCols) yield rs.getInt(index)
        //}
        //val row: Seq[String] = for (index <- 1 to numberOfCols) yield rs.getString(index)
        results += row.toList
      }

      results.toList
    }

  def program[a: TypeTag]: IO[Throwable, List[List[String]]] =
    getConnection(url, username, password).flatMap { conn =>
      createStatement(conn).flatMap { st =>
        selectAll("select active from CustomerOrder")(st).flatMap { rs =>
          asResult[a](rs).flatMap { r =>
            IO.succeed {
              println(r)
              r
            }
          }
        }
      }
    }

//    def jdbcProgram: IO[Throwable, ResultSet] = for {
//      conn <- getConnection(url, username, password)
//      statement: Statement <- createStatement(conn)
//      result: ResultSet <- selectAll(query = "select active from orders")(statement)
//    } yield result

}
