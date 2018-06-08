package localhost.sangriademo

case class Shopper( id: Int,
                    name: String )

case class Item( id: Int,
                 name: String,
                 price: Int,
                 description: Option[String] )

case class Transaction( id: Int,
                        date: Int,
                        shopperId: Int )

case class TransactionItem( id: Int,
                            transactionId: Int,
                            itemId: Int )

trait AppContext {

  def shopperTransactions( shopper: Shopper,
                           sinceDate: Option[Int],
                           beforeDate: Option[Int],
                           itemIds: Option[Seq[Int]] ): Seq[Transaction]

  def itemTransactions( item: Item,
                        sinceDate: Option[Int],
                        beforeDate: Option[Int],
                        shopperIds: Option[Seq[Int]] ): Seq[Transaction]

  def transactionShopper(transaction: Transaction): Shopper

  def transactionItems(transaction: Transaction): Seq[Item]

  def transactionTotal(transaction: Transaction): Int

  def queryShoppers(shopperIds: Option[Seq[Int]]): Seq[Shopper]

  def queryItems(itemIds: Option[Seq[Int]]): Seq[Item]

  def queryTransactions( transactionIds: Option[Seq[Int]],
                         sinceDate: Option[Int],
                         beforeDate: Option[Int],
                         shopperIds: Option[Seq[Int]],
                         itemIds: Option[Seq[Int]] ): Seq[Transaction]
}
