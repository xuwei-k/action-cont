package com.github.hexx.play.cont

import play.api.mvc.AnyContent
import play.api.mvc.Request
import play.api.mvc.Result
import scala.concurrent.ExecutionContext
import scalaz.contrib.std.scalaFuture._

object FlowCont {
  def apply[WholeRequestContext, NormalRequestContext](
    request: Request[AnyContent],
    wholeCont: Request[AnyContent] => ActionCont[WholeRequestContext],
    normalCont: WholeRequestContext => ActionCont[NormalRequestContext],
    handlerCont: NormalRequestContext => ActionCont[Result],
    errorCont: WholeRequestContext => Throwable => ActionCont[Result])
    (implicit executionContext: ExecutionContext): ActionCont[Result] = {

    for {
      wholeRequestContext <- wholeCont(request)
      wholeResult <- ActionCont.recover(
        for {
          normalRequestContext <- normalCont(wholeRequestContext)
          result <- handlerCont(normalRequestContext)
        } yield result) {
          case e => errorCont(wholeRequestContext)(e).run_
        }
    } yield wholeResult
  }
}
