package localhost.sangriademo

object FalsoDB {

  val appContext: AppContext = new AppContext {

    def shopperTransactions(shopper: Shopper,
                            sinceDate: Option[Int],
                            beforeDate: Option[Int],
                            itemIds: Option[Seq[Int]]): Seq[Transaction] = ???

    def itemTransactions(item: Item,
                         sinceDate: Option[Int],
                         beforeDate: Option[Int],
                         shopperIds: Option[Seq[Int]]): Seq[Transaction] = ???

    def transactionItems(transaction: Transaction): Seq[Item] = ???

    def transactionTotal(transaction: Transaction): Int = ???

    def queryShoppers(shopperIds: Option[Seq[Int]]): Seq[Shopper] = ???

    def queryItems(itemIds: Option[Seq[Int]]): Seq[Item] = ???

    def queryTransactions(transactionIds: Option[Seq[Int]],
                          sinceDate: Option[Int],
                          beforeDate: Option[Int],
                          shopperIds: Option[Seq[Int]],
                          itemIds: Option[Seq[Int]]): Seq[Transaction] = ???
  }

  val fakeShoppers: Map[Int, Shopper] = ???

  val fakeItems: Map[Int, Item] = ???

  val fakeTransactions: Map[Int, Transaction] = ???

  val fakeTransactionItems: Map[Int, TransactionItem] = ???
}
