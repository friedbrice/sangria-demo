package localhost.sangriademo

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}

object Eithers {

  implicit class EitherFuture[A](val self: Future[A]) extends AnyVal {
    def either[E](f: Throwable => E): Either[E, A] =
      Try(Await.result(self, Duration.Inf)).either(f)
  }

  implicit class EitherTry[A](val self: Try[A]) extends AnyVal {
    def either[E](f: Throwable => E): Either[E, A] =
      self match {
        case Failure(err) => Left(f(err))
        case Success(a) => Right(a)
      }
  }

  implicit class EitherOption[A](val self: Option[A]) extends AnyVal {
    def either[E](f: => E): Either[E, A] =
      self match {
        case None => Left(f)
        case Some(a) => Right(a)
      }
  }

  implicit class HandleEither[E, A](val self: Either[E, A]) extends AnyVal {
    def handle[E2](f: E => E2): Either[E2, A] = self match {
      case Left(e) => Left(f(e))
      case Right(a) => Right(a)
    }
  }

  implicit class UniteEither[A](val self: Either[A, A]) extends AnyVal {
    def unify: A = self.fold(identity, identity)
  }
}
