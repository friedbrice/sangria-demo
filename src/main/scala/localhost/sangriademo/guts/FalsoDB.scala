package localhost.sangriademo.guts

import localhost.sangriademo._

/** This file just mocks out a fake database for us. Safe to ignore. */
object FalsoDB {

  val appContext: AppContext = new AppContext {

    def shopperTransactions(shopper: Shopper,
                            sinceDate: Option[Int],
                            beforeDate: Option[Int],
                            itemIds: Option[Seq[Int]]): Seq[Transaction] =
      queryTransactions(None, sinceDate, beforeDate,
        Some(List(shopper.id)), itemIds)

    def itemTransactions(item: Item,
                         sinceDate: Option[Int],
                         beforeDate: Option[Int],
                         shopperIds: Option[Seq[Int]]): Seq[Transaction] =
      queryTransactions(None, sinceDate, beforeDate, None, Some(List(item.id)))

    override def transactionShopper(transaction: Transaction): Shopper =
      fakeShoppers(transaction.shopperId)

    def transactionItems(transaction: Transaction): Seq[Item] =
      fakeTransactionItems.values
        .filter(ti => ti.transactionId == transaction.id)
        .map(ti => fakeItems(ti.itemId))
        .toSeq

    def transactionTotal(transaction: Transaction): Int =
      transactionItems(transaction).map(_.price).sum

    def queryShoppers(shopperIds: Option[Seq[Int]]): Seq[Shopper] =
      fakeShoppers.values
        .filter(shopperIds.pred((ids, s) => ids.contains(s.id)))
        .toSeq

    def queryItems(itemIds: Option[Seq[Int]]): Seq[Item] =
      fakeItems.values
        .filter(itemIds.pred((ids, i) => ids.contains(i.id)))
        .toSeq

    def queryTransactions(transactionIds: Option[Seq[Int]],
                          sinceDate: Option[Int],
                          beforeDate: Option[Int],
                          shopperIds: Option[Seq[Int]],
                          itemIds: Option[Seq[Int]]): Seq[Transaction] =
      fakeTransactions.values
        .filter(transactionIds.pred((ids, t) => ids.contains(t.id)))
        .filter(sinceDate.pred((lower, t) => t.date >= lower))
        .filter(beforeDate.pred((upper, t) => t.date < upper))
        .filter(shopperIds.pred((sids, t) => sids.contains(t.shopperId)))
        .filter(itemIds.pred((iids, t) =>
          transactionItems(t).exists(item => iids.contains(item.id))))
        .toSeq
  }

  val fakeShoppers: Map[Int, Shopper] = Map(
    0 -> Shopper(0, "Alyssa P. Hacker"),
    1 -> Shopper(1, "Ben Bitdiddle"),
    2 -> Shopper(2, "Cy D. Fect"),
    3 -> Shopper(3, "Eva Lu Ator"),
    4 -> Shopper(4, "Lem E. Tweakit"),
    5 -> Shopper(5, "Louis Reasoner")
  )

  val fakeItems: Map[Int, Item] = Map(
    0 -> Item(0, "Scala for the Impatient", 3540, Some("Interest in the Scala programming language continues to grow for many reasons. Scala embraces the functional programming style without abandoning the object-oriented paradigm, and it allows you to write programs more concisely than in Java. Because Scala runs on the JVM, it can access any Java library and is interoperable with familiar Java frameworks. Scala also makes it easier to leverage the full power of concurrency.")),
    1 -> Item(1, "Structure and Interpretation of Computer Programs", 4709, Some("Structure and Interpretation of Computer Programs has had a dramatic impact on computer science curricula over the past decade.")),
    2 -> Item(2, "Design Patterns: Elements of Reusable Object-Oriented Software", 2785, Some("Capturing a wealth of experience about the design of object-oriented software, four top-notch designers present a catalog of simple and succinct solutions to commonly occurring design problems. Previously undocumented, these 23 patterns allow designers to create more flexible, elegant, and ultimately reusable designs without having to rediscover the design solutions themselves.")),
    3 -> Item(3, "Learn You a Haskell for Great Good", 3804, None),
    4 -> Item(4, "Scala with Cats", 0, None),
    5 -> Item(5, "The Cathedral and the Bazaar", 1231, Some("Open source provides the competitive advantage in the Internet Age. According to the August Forrester Report, 56 percent of IT managers interviewed at Global 2,500 companies are already using some type of open source software in their infrastructure and another 6 percent will install it in the next two years. This revolutionary model for collaborative software development is being embraced and studied by many of the biggest players in the high-tech industry, from Sun Microsystems to IBM to Intel.The Cathedral & the Bazaar is a must for anyone who cares about the future of the computer industry or the dynamics of the information economy."))
  )

  val fakeTransactions: Map[Int, Transaction] = {
    val ids = 0 to 20
    val dates = ids.map(_ => math.abs(scala.util.Random.nextInt)).sorted
    val shoppers = ids.map(_ => scala.util.Random.shuffle(0 to 5).head)

    (ids zip dates zip shoppers).map {
      case ((id, d), s) => (id, Transaction(id, d, s))
    }.toMap
  }

  val fakeTransactionItems: Map[Int, TransactionItem] = {
    val ids = 0 to 55
    val transactions = ids.map(_ => scala.util.Random.shuffle(0 to 20).head)
    val items = ids.map(_ => scala.util.Random.shuffle(0 to 5).head)

    (ids zip transactions zip items).map {
      case ((id, t), i) => (id, TransactionItem(id, t, i))
    }.toMap
  }

  implicit class Pred[A](val self: Option[A]) extends AnyVal {
    def pred[B](p: (A, B) => Boolean)(b: B): Boolean = self match {
      case None => true
      case Some(a) => p(a, b)
    }
  }
}
