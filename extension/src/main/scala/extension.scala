import cats.effect.unsafe.IORuntime
import cats.effect.IO
import cats.syntax.all.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.CORS
import org.http4s.server.middleware.CORSPolicy
import org.http4s.HttpApp
import org.http4s.Response
import typings.vscode.mod.window
import typings.vscode.mod.ExtensionContext
import typings.vscode.mod.SnippetString

import scala.scalajs.js.annotation.JSExportTopLevel
import scala.scalajs.js.JSConverters.*

object extension {

  // I'm lazy, fight me. To reset this, you have to reload the editor window so that the extension reactivates.
  // A better way to do this would be to check if there's already some valid code in the expression being modified.
  var firstNote = true

  @JSExportTopLevel("activate")
  def activate(context: ExtensionContext): Unit =
    EmberServerBuilder
      .default[IO]
      .withHttpApp {

        val base = HttpApp[IO] { req =>
          var text = req.uri.params("snippet")

          if (!firstNote) text = s" + $text"
          else firstNote = false

          IO {
            window.activeTextEditor.toOption.get.insertSnippet(SnippetString(text))
          } *>
            Response[IO]().pure[IO]
        }

        base
      }
      .build
      .useForever
      .unsafeRunAndForget()(IORuntime.global)

}
