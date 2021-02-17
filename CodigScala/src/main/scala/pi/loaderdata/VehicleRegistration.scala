package pi.loaderdata

import pi.loaderdata.UtilProvince
import pi.loaderdata.Class.ClassTable
import pi.loaderdata.Combustible.CombustibleTable
import pi.loaderdata.Estrasientos.EstasientosTable
import pi.loaderdata.Estratone.EstratoneTable
import pi.loaderdata.Marca.MarcaTable
import pi.loaderdata.Service.ServiceTable
import kantan.csv._
import kantan.csv.generic._
import kantan.csv.ops._
import pi.loaderdata.UtilProvince.ProvinceTable
import slick.jdbc.MySQLProfile.api._

import java.io.File
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.Codec
object VehicleRegistration extends App {


  lazy val qryProvince = TableQuery[ProvinceTable]
  lazy val qryCombustible = TableQuery[CombustibleTable]
  lazy val qryMarca = TableQuery[MarcaTable]
  lazy val qryService = TableQuery[ServiceTable]
  lazy val qryClass = TableQuery[ClassTable]
  lazy val qryEstratone = TableQuery[EstratoneTable]
  lazy val qryEstrasintos = TableQuery[EstasientosTable]

  // definir las filas de la tabla
  case class VehicleRegistation(modelo: String, tonelaje: Double, asientos: Int, provinceId: Long,
                                classId: Long, combustibleId: Long, marcaId: Long, serviceId: Long,
                                estratoneId: Long, estrasientosId: Long, id: Long = 0L)

  // definir la tabla con sus claves foraneas
  class VehicleRegistrationTable(tag: Tag) extends Table[VehicleRegistation](tag, "VEHICLE_REGISTRATION") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def provinceId = column[Long]("PROVINCE_ID")
    def classId = column[Long]("CLASS_ID")
    def combustibleId = column[Long]("COMBUSTIBLE_ID")
    def marcaId = column[Long]("MARCA_ID")
    def serviceId = column[Long]("SERVICE_ID")
    def estratoneId = column[Long]("ESTRATONE_ID")
    def estrasientosId = column[Long]("ESTRASIENTOS_ID")
    def modelo = column[String]("MODELO")
    def tonelaje = column[Double]("TONELAJE")
    def asientos = column[Int]("ASIENTOS")

    def province = foreignKey("province_fk", provinceId, qryProvince)(_.id)
    def class_ = foreignKey("class_fk", classId, qryClass)(_.id)
    def combustible = foreignKey("combustible_fk", combustibleId, qryCombustible)(_.id)
    def marca = foreignKey("marca_fk", marcaId, qryMarca)(_.id)
    def service = foreignKey("service_fk", serviceId, qryService)(_.id)
    def estratone = foreignKey("estratone_fk", estratoneId, qryEstratone)(_.id)
    def estrasintos = foreignKey("estrasientos_fk", estrasientosId, qryEstrasintos)(_.id)

    def * = (modelo, tonelaje, asientos, provinceId, classId, combustibleId, marcaId, serviceId, estratoneId,
      estrasientosId, id).mapTo[VehicleRegistation]
  }

  lazy val registrationQry = TableQuery[VehicleRegistrationTable]


  val db = Database.forConfig("test01")

  // def exec[T](program:DBIO[T]): T = Await.result(db.run(program), 2.seconds)
  def exec[T](program: DBIO[T]): T = Await.result(db.run(program), Duration.Inf)

  // println((qryProvince.schema ++ registrationQry.schema).createStatements.mkString)

  // exec((registrationQry.schema).create)

  implicit val codec: Codec = Codec.ISO8859
  val path2DataFile = "C:\\2008.csv"

  // obtener datos de las tablas foraneas

  val provinceList = exec(qryProvince.result)

  val classList = exec(qryClass.result)

  val combustibleList = exec(qryCombustible.result)

  val marcaList = exec(qryMarca.result)

  val serviceList = exec(qryService.result)

  val estratoneList = exec(qryEstratone.result)

  val estrasientosList = exec(qryEstrasintos.result)

  // funciones para reemplazar el nombre por el Id

  def getProvinceIdByName(provName: String) =
    provinceList.filter(row => row.name == provName).head.id

  def getClassIdByName(className: String) =
    classList.filter(row => row.name == className).head.id

  def getCombustibleIdByName(combustibleName: String) =
    combustibleList.filter(row => row.name == combustibleName).head.id

  def getMarcaIdByName(marcaName: String) =
    marcaList.filter(row => row.name == marcaName).head.id

  def getServiceIdByName(serviceName: String) =
    serviceList.filter(row => row.name == serviceName).head.id

  def getEstratoneIdByRange(estratoneRange: String) =
    estratoneList.filter(row => row.name == estratoneRange).head.id

  def getEstrasientosIdByRange(estrasientosRange: String) = {
    estrasientosList.filter(row => row.name == estrasientosRange).head.id

  }
  // insetar los datos
  /*
    def getSeqToInsert()=
      registration.map(t => VehicleRegistation(t._1, t._2, t._3, getProvinceIdByName(t._4), getClassIdByName(t._5),
        getCombustibleIdByName(t._6), getMarcaIdByName(t._7), getServiceIdByName(t._8), getEstratoneIdByRange(t._9),
        getEstrasientosIdByRange(t._10)))
     */
  // exec(registrationQry ++= getSeqToInsert())

  // funciones para reemplazar el Id por el nombre
  def getProvinceNameById(provId: Long) =
    provinceList.filter(row => row.id == provId).head.name

  def getServiceNameById(serviceId: Long) =
    serviceList.filter(row => row.id == serviceId).head.name

  def getMarcaNameById(marcaId: Long) =
    marcaList.filter(row => row.id == marcaId).head.name

  // CONSULTAS
  // Cuantas camionetas hay en lOJA con relación a su servicio
  val consultaCuatro = registrationQry.
    filter(k => k.classId === getClassIdByName("Camioneta") && k.provinceId === getProvinceIdByName("LOJA")).
    groupBy(_.serviceId).map({ case (service, list) => (service, list.size) })

  val resultadoCuatro = exec(consultaCuatro.result).map({ case (service, total) => (getServiceNameById(service), total) }).sorted

  new File("C:\\Users\\Usuario iTC\\OneDrive\\Documentos\\ConsultaCuatro.csv") writeCsv[(String, Int)](
    resultadoCuatro.map(row => (row._1, row._2)), rfc.withHeader("Service", "Total"))

  // Cuantos vehiculos son a gasolina y son de marca TOYOTA, con respecto a la provincia
  val consultaCinco = registrationQry.
    filter(k => k.combustibleId === getCombustibleIdByName("Gasolina") && k.marcaId === getMarcaIdByName("TOYOTA")).
    groupBy(_.provinceId).map({ case (province, list) => (province, list.size) })

  val resultadoCinco = exec(consultaCinco.result).map({ case (province, total) => (getProvinceNameById(province), total) }).sorted

  new File("C:\\Users\\Usuario iTC\\OneDrive\\Documentos\\ConsultaCinco.csv") writeCsv[(String, Int)](
    resultadoCinco.map(row => (row._1, row._2)), rfc.withHeader("Province", "Total"))*/

  // Consultar numero de vehiculos segun el tipo de marca que no superen los 4 asientos y sean automóviles a gasolina

  val consultaSeis = registrationQry.filter(k => k.asientos === 4 && k.classId === getClassIdByName("Automóvil")
  && k.combustibleId === getCombustibleIdByName("Gasolina")).groupBy(_.marcaId)
    .map({ case (marca, list) => (marca, list.size)})

  val resultadoSeis = exec(consultaSeis.result).map({ case (marca, total) => (getMarcaNameById(marca), total) }).sortBy(_._2)

  new File("C:\\Users\\Usuario iTC\\OneDrive\\Documentos\\ConsultaSeis.csv") writeCsv[(String, Int)](
    resultadoSeis.map(row => (row._1, row._2)), rfc.withHeader("Marca", "Total"))

}