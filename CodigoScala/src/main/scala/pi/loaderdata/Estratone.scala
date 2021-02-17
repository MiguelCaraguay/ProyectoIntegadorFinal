package pi.loaderdata
import kantan.csv._
import kantan.csv.generic._
import kantan.csv.ops._


import java.io.File
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.Codec

object Estratone {
  implicit val codec:Codec = Codec.ISO8859
  val path2DataFile = "C:\\2008.csv"

  //Representa a la fila
  case class Estratone(name:String, id:Long=0L)

  //Representa a la tabla
  class EstratoneTable(tag:Tag) extends Table[Estratone](tag,"ESTRATONE") {
    def id= column[Long]("ID",O.PrimaryKey,O.AutoInc)
    def rango = column[String]("RANGO")

    def * = (rango,id).mapTo[Estratone]
  }

  //Medio para la ejecución de consultas
  val qryEstratone = TableQuery[EstratoneTable]

  //Conexión con la base de datos
  val db = Database.forConfig("test01")

  def main(args: Array[String]): Unit = {
    //1. Crear esquema de datos, una única vez
     createDataSchema()
    //2. Cargar datos desde el archivo
    val estratoneList = loadDataFromFile(path2DataFile)
    //3. Importar datos a la base de datos
    importToDataBase(estratoneList)
    //4. Realizar consultas
    // classQueries()

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

    // Traer la data de la servicio

    values.map(_.estratone).distinct.sorted
  }

  def createDataSchema(): Unit = {
    println("Crear el esquema de datos")
    println(qryEstratone.schema.createStatements.mkString)
    exec(qryEstratone.schema.create)
  }

  def importToDataBase(classList: List[String]): Unit = {
    val estratone :List[Estratone] = classList.map(Estratone(_))
    println("Agregando datos")
    exec(qryEstratone++= estratone)
  }

  def classQueries(): Unit = {
    // A. cargar todas las filas
    val provinces = exec(qryEstratone.result)
  }

  def exec[T](program:DBIO[T]): T = Await.result(db.run(program), 2.seconds)
}
