package pi.loaderdata
import kantan.csv._
import kantan.csv.generic._
import kantan.csv.ops._


import java.io.File
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.Codec
object Marca {
  implicit val codec:Codec = Codec.ISO8859
  val path2DataFile = "C:\\2008.csv"

  //Representa a la fila
  case class Marca(name:String, id:Long=0L)

  //Representa a la tabla
  class MarcaTable(tag:Tag) extends Table[Marca](tag,"MARCA") {
    def id= column[Long]("ID",O.PrimaryKey,O.AutoInc)
    def name= column[String]("NAME")

    def * = (name,id).mapTo[Marca]
  }

  //Medio para la ejecución de consultas
  val qryMarca = TableQuery[MarcaTable]

  //Conexión con la base de datos
  val db = Database.forConfig("test01")

  def main(args: Array[String]): Unit = {
    //1. Crear esquema de datos, una única vez
    // createDataSchema()
    //2. Cargar datos desde el archivo
    // val marcaList = loadDataFromFile(path2DataFile)
    //3. Importar datos a la base de datos
    // importToDataBase(marcaList)
    //4. Realizar consultas
    provinceQueries()

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

    // Traer la data de la marca

    values.map(_.marca).distinct.sorted
  }

  def createDataSchema(): Unit = {
    println("Crear el esquema de datos")
    println(qryMarca.schema.createStatements.mkString)
    exec(qryMarca.schema.create)
  }

  def importToDataBase(markList: List[String]): Unit = {
    val marks :List[Marca] = markList.map(Marca(_))
    println("Agregando datos")
    exec(qryMarca++= marks)
  }

  def provinceQueries(): Unit = {
    // A. cargar todas las filas
    val provinces = exec(qryMarca.result)


  }

  def exec[T](program:DBIO[T]): T = Await.result(db.run(program), 2.seconds)

}
