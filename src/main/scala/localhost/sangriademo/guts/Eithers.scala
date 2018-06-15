package localhost.sangriademo
package guts

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.Try

object Eithers {

  implicit class EitherFuture[A](val self: Future[A]) extends AnyVal {
    def `catch`[E](await: Duration)(f: Throwable => E): Either[E, A] =
      Try(Await.result(self, await)).`catch`(f)
  }

  implicit class EitherTry[A](val self: Try[A]) extends AnyVal {
    def `catch`[E](f: Throwable => E): Either[E, A] =
      self.toEither.left.map(f)
  }

  implicit class EitherOption[A](val self: Option[A]) extends AnyVal {
    def `catch`[E](f: => E): Either[E, A] = self.toRight(f)
  }

  implicit class UniteEither[A](val self: Either[A, A]) extends AnyVal {
    def converge: A = self.fold(identity, identity)
  }
}
