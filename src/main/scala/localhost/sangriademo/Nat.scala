package localhost.sangriademo

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object Nat {

  implicit class TryToFuture[A](val self: Try[A]) extends AnyVal {
    def future: Future[A] = self match {
      case Failure(err) => Future.failed(err)
      case Success(a) => Future.successful(a)
    }
  }

  implicit class OptionToFuture[A](val self: Option[A]) extends AnyVal {
    def future(err: => Throwable): Future[A] = self match {
      case None => Future.failed(err)
      case Some(a) => Future.successful(a)
    }
  }

  implicit class EitherToFuture[E, A](val self: Either[E, A]) extends AnyVal {
    def future(f: E => Throwable): Future[A] = self match {
      case Left(e) => Future.failed(f(e))
      case Right(a) => Future.successful(a)
    }
  }
}
