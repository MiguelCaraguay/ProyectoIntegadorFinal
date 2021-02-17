package pi.loaderdata
import kantan.csv._
import kantan.csv.generic._
import kantan.csv.ops._


import java.io.File
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.Codec

object Combustible {
  implicit val codec:Codec = Codec.ISO8859
  val path2DataFile = "C:\\2008.csv"

  //Representa a la fila
  case class Combustible(name:String, id:Long=0L)

  //Representa a la tabla
  class CombustibleTable(tag:Tag) extends Table[Combustible](tag,"COMBUSTIBLE") {
    def id= column[Long]("ID",O.PrimaryKey,O.AutoInc)
    def name= column[String]("NAME")

    def * = (name,id).mapTo[Combustible]
  }

  //Medio para la ejecución de consultas
  val qryCombustible = TableQuery[CombustibleTable]

  //Conexión con la base de datos
  val db = Database.forConfig("test01")

  def main(args: Array[String]): Unit = {
    //1. Crear esquema de datos, una única vez
    // createDataSchema()
    //2. Cargar datos desde el archivo
    // val provinceList = loadDataFromFile(path2DataFile)
    //3. Importar datos a la base de datos
    // importToDataBase(provinceList)
    //4. Realizar consultas
    combustibleQueries()

  }

  def loadDataFromFile(path2DataFile: String): List[String] = {
    case class Matricula(
                          provincia: String,
                          clase: String,
                          combustible: String,
                          marca: String,
                          servicio: String,
                          modelo: String,
                          tonelaje: Double,
                          asientos: Int,
                          estratone: String,
                          estrasientos: String)

    val dataSource = new File(path2DataFile).readCsv[List, Matricula](rfc.withHeader())
    val rows = dataSource.filter(row => row.isRight)

    val values = rows.collect({case Right(matricula) => matricula})

    // Traer la data de la combustible

    values.map(_.combustible).distinct.sorted
  }

  def createDataSchema(): Unit = {
    println("Crear el esquema de datos")
    println(qryCombustible.schema.createStatements.mkString)
    exec(qryCombustible.schema.create)
  }

  def importToDataBase(combustibleList: List[String]): Unit = {
    val combustibles :List[Combustible] = combustibleList.map(Combustible(_))
    println("Agregando datos")
    exec(qryCombustible++= combustibles)
  }

  def combustibleQueries(): Unit = {
    // A. cargar todas las filas
    val provinces = exec(qryCombustible.result)


  }

  def exec[T](program:DBIO[T]): T = Await.result(db.run(program), 2.seconds)

}
