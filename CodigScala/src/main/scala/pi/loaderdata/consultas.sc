import java.io.File
import kantan.csv._
import kantan.csv.ops._
// agregamos para el uso del case class
import kantan.csv.generic._

import scala.collection.immutable.ListMap
import scala.io.Codec

implicit val codec:Codec = Codec.ISO8859

case class Matricula(
                      provincia : String,
                      clase : String,
                      combustible : String,
                      marca : String,
                      servicio : String,
                      modelo : String,
                      tonelaje : Double,
                      asientos : Int,
                      Estratone : String,
                      Estrasientos : String
                    )

val path2DataFile = "C:\\2008.csv"
val dataSource = new File(path2DataFile).readCsv[List, Matricula](rfc.withHeader())

val rows = dataSource.filter(row => row.isRight)
// valores

val values = rows.collect({case Right(matricula) => matricula})
// Trabajo Grupal 21/01/2021

// 1. Cual es la distribución (el numero) de los vehiculos clasificados segun
// el tipo de combustible que usan

val  consultaUno = values.map(_.combustible).groupBy(identity)
  .map({case (combustible, lista) => (combustible, lista.length)}).toSeq.sorted
consultaUno.foreach(print)


// 2. Cual es la distribución (el numero) de los vehiculos segun el servicio que presentan.

val  consultaDos = values.map(_.servicio).groupBy(identity)
  .map({case (servicio, lista) => (servicio, lista.length)}).toSeq.sorted
consultaDos.foreach(print)

// 5. Cual es la distribucion general y por provincia (el numero) de los tipos de servicios para
// vehiculos que pueden considerarse de trabajo pesado (Camion, tanquero, trailer y volqueta)

val  consultaTres = values.filter(row => row.clase.endsWith("n") || row.clase == "Tanquero" ||
  row.clase == "Trailer" || row.clase == "Volqueta").groupBy(row => (row.servicio, row.clase,row.provincia))
  .map({case ((servicio, clase, provincia), lista) => (servicio, clase, provincia, lista.length)})




// Creacion de los Archivos

// Creacion del archivo de la primera consulta
new File("C:\\Users\\Usuario iTC\\OneDrive\\Documentos\\consultaUno.csv")
  .writeCsv[(String,Int)](
    consultaUno.map(row => (row._1, row._2)), rfc.withHeader("Combustible", "Total"))

// Creacion del archivo de la segunda consulta
new File("C:\\Users\\Usuario iTC\\OneDrive\\Documentos\\consultaDos.csv")
  .writeCsv[(String,Int)](
    consultaDos.map(row => (row._1, row._2)), rfc.withHeader("Servicio", "Total"))

// Creacion del archivo de la tercera consulta
new File("C:\\Users\\mateo\\OneDrive\\Documentos\\consultaCincoParteDos.csv")
  .writeCsv[(String,String,String,Int)](
    consultaTres.map(row => (row._1, row._2, row._3, row._4)), rfc.withHeader("Servicio", "Clase", "Provincia", "Total"))



