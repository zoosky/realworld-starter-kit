import play.api.{ApplicationLoader, BuiltInComponentsFromContext, LoggerConfigurator, Mode}
import com.softwaremill.macwire.wire
import controllers.{Assets, ViewController, WebJarAssets}
import modules.SilhouetteModule
import play.api.ApplicationLoader.Context
import play.api.http.{HttpErrorHandler, HttpRequestHandler}
import play.api.mvc.EssentialFilter
import play.api.routing.Router
import play.filters.cors.CORSComponents
import play.filters.csrf.CSRFComponents
import play.filters.gzip.GzipFilterComponents
import play.filters.headers.SecurityHeadersComponents
import router.Routes
import services.silhouetteservices.SilhouetteIdentityService
import utils.web.{Filters, RequestHandler, ServerErrorHandler}
import utils.AppLogger

import scala.concurrent.ExecutionContext

class Loader(context: Context) extends BuiltInComponentsFromContext(context)
    with SilhouetteModule
    with SecurityHeadersComponents
    with CORSComponents
    with CSRFComponents
    with GzipFilterComponents {
  LoggerConfigurator(context.environment.classLoader).foreach {
    _.configure(context.environment)
  }

  lazy val silhouetteIdentityService = wire[SilhouetteIdentityService]

  implicit lazy val executionContext: ExecutionContext = actorSystem.dispatcher
  lazy val routerOption = None
  override lazy val router: Router = {
    // add the prefix string in local scope for the Routes constructor
    val prefix: String = "/"
    wire[Routes]
  }

  // assets
  lazy val assets: Assets = wire[Assets]
  lazy val webjarAssets: WebJarAssets = wire[WebJarAssets]

  // controllers
  lazy val viewController: ViewController = wire[ViewController]

  override lazy val httpRequestHandler: HttpRequestHandler = wire[RequestHandler]
  override lazy val httpErrorHandler: HttpErrorHandler = wire[ServerErrorHandler]
  lazy val filtersWire = wire[Filters]
  override lazy val httpFilters: Seq[EssentialFilter] = filtersWire.filters

  // actors

}

class ServerLoader extends ApplicationLoader with AppLogger {
  log.info(s"Web gateway is loading.")

  override def load(context: Context) = context.environment.mode match {
    case Mode.Dev =>
      new Loader(context).application
    case _ =>
      new Loader(context) {
        // ovveride changes required in production
      }.application
  }
}
