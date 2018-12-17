package localhost.sangriademo

import sangria.schema.Action

import scala.concurrent.ExecutionContext
import scala.util.Random

object FalsoDB {

  def context(authToken: Option[String])
             (implicit ec: ExecutionContext): Context = new Context {

    def shopperTransactions( shopper: Shopper,
                             sinceDate: Option[Int],
                             beforeDate: Option[Int],
                             itemIds: Option[Seq[Int]]
                           ): Action[Context, Seq[Transaction]] =
      queryTransactions( None, sinceDate, beforeDate,
                         Some(List(shopper.id)), itemIds )

    def itemTransactions( item: Item,
                          sinceDate: Option[Int],
                          beforeDate: Option[Int],
                          shopperIds: Option[Seq[Int]]
                        ): Action[Context, Seq[Transaction]] =
      queryTransactions(None, sinceDate, beforeDate, None, Some(List(item.id)))

    def transactionShopper( transaction: Transaction
                          ): Action[Context, Shopper] =
      fakeShoppers(transaction.shopperId)

    def transactionItems( transaction: Transaction
                        ): Action[Context, Seq[Item]] =
      fakeTransactionItems
        .filter { ti => ti.transactionId == transaction.id }
        .map { ti => fakeItems(ti.itemId) }

    def transactionTotal( transaction: Transaction
                        ): Action[Context, Int] =
      transactionItems(transaction).map { items => items.map(_.price).sum }

    def queryShoppers( shopperIds: Option[Seq[Int]]
                     ): Action[Context, Seq[Shopper]] =
      fakeShoppers.filter(shopperIds.pred { (ids, s) => ids.contains(s.id) })

    def queryItems( itemIds: Option[Seq[Int]]
                  ): Action[Context, Seq[Item]] =
      fakeItems.filter(itemIds.pred { (ids, i) => ids.contains(i.id) })

    def queryTransactions( transactionIds: Option[Seq[Int]],
                           sinceDate: Option[Int],
                           beforeDate: Option[Int],
                           shopperIds: Option[Seq[Int]],
                           itemIds: Option[Seq[Int]]
                         ): Action[Context, Seq[Transaction]] =
      fakeTransactions
        .filter(transactionIds.pred { (tIds, t) => tIds.contains(t.id) })
        .filter(sinceDate.pred { (lower, t) => t.date >= lower })
        .filter(beforeDate.pred { (upper, t) => t.date < upper })
        .filter(shopperIds.pred { (sIds, t) => sIds.contains(t.shopperId) })
        .filter(itemIds.pred { (iIds, t) =>
          fakeTransactionItems.exists {
            ti => ti.transactionId == t.id && iIds.contains(ti.itemId)
          }
        })
  }

  val fakeShoppers: IndexedSeq[Shopper] = IndexedSeq(
    Shopper(0, "Alyssa P. Hacker"),
    Shopper(1, "Ben Bitdiddle"),
    Shopper(2, "Cy D. Fect"),
    Shopper(3, "Eva Lu Ator"),
    Shopper(4, "Lem E. Tweakit"),
    Shopper(5, "Louis Reasoner")
  )

  val fakeItems: IndexedSeq[Item] = IndexedSeq(
    Item(0, "Scala for the Impatient", 3540, Some(
      """Interest in the Scala programming language continues to grow for many
        | reasons. Scala embraces the functional programming style without
        | abandoning the object-oriented paradigm, and it allows you to write
        | programs more concisely than in Java. Because Scala runs on the JVM,
        | it can access any Java library and is interoperable with familiar Java
        | frameworks. Scala also makes it easier to leverage the full power of
        | concurrency.
        |""".stripMargin
    )),
    Item(1, "Structure and Interpretation of Computer Programs", 4709, Some(
      """Structure and Interpretation of Computer Programs has had a dramatic
        | impact on computer science curricula over the past decade. This
        | long-awaited revision contains changes throughout the text. There are
        | new implementations of most of the major programming systems in the
        | book, including the interpreters and compilers, and the authors have
        | incorporated many small changes that reflect their experience teaching
        | the course at MIT since the first edition was published.
        |""".stripMargin
    )),
    Item(2,
      "Design Patterns: Elements of Reusable Object-Oriented Software",
      2785,
      Some(
        """Capturing a wealth of experience about the design of object-oriented
          | software, four top-notch designers present a catalog of simple and
          | succinct solutions to commonly occurring design problems. Previously
          | undocumented, these 23 patterns allow designers to create more
          | flexible, elegant, and ultimately reusable designs without having to
          | rediscover the design solutions themselves.
          |""".stripMargin
      )
    ),
    Item(3, "Learn You a Haskell for Great Good", 3804, None),
    Item(4, "Scala with Cats", 0, None),
    Item(5, "The Cathedral and the Bazaar", 1231, Some(
      """Open source provides the competitive advantage in the Internet Age.
        | According to the August Forrester Report, 56 percent of IT managers
        | interviewed at Global 2,500 companies are already using some type of
        | open source software in their infrastructure and another 6 percent
        | will install it in the next two years. This revolutionary model for
        | collaborative software development is being embraced and studied by
        | many of the biggest players in the high-tech industry, from Sun
        | Microsystems to IBM to Intel. The Cathedral & the Bazaar is a must for
        | anyone who cares about the future of the computer industry or the
        | dynamics of the information economy.
        |""".stripMargin
    ))
  )

  val fakeTransactions: IndexedSeq[Transaction] = {
    val ids = 0 to 127
    val dates = ids.map { _ => math.abs(Random.nextInt) }.sorted
    val shoppers = ids.map { _ => Random.shuffle(0 to 5).head }
    (ids, dates, shoppers).zipped.map(Transaction)
  }

  val fakeTransactionItems: IndexedSeq[TransactionItem] = {
    val ids = 0 to 255
    val transactions = ids.map { _ => Random.shuffle(fakeTransactions).head.id }
    val items = ids.map { _ => Random.shuffle(0 to 5).head }
    (ids, transactions, items).zipped.map(TransactionItem)
  }

  implicit class OptionPred[A](val self: Option[A]) extends AnyVal {
    def pred[B](p: (A, B) => Boolean)(b: B): Boolean =
      self.fold(true) { a => p(a, b) }
  }
}
