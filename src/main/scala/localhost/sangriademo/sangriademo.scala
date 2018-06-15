package localhost

import sangria.schema.Action

package object sangriademo {

  type Path = String
  type Method = String
  type Body = String
  type AuthToken = String

  type Status = Int
  type ContentType = String
  type Content = String

  type Request = (Path, Method, Body, Option[AuthToken])
  type Response = (Status, ContentType, Content)


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
                             itemIds: Option[Seq[Int]]
                           ): Action[AppContext, Seq[Transaction]]

    def itemTransactions( item: Item,
                          sinceDate: Option[Int],
                          beforeDate: Option[Int],
                          shopperIds: Option[Seq[Int]]
                        ): Action[AppContext, Seq[Transaction]]

    def transactionShopper( transaction: Transaction
                          ): Action[AppContext, Shopper]

    def transactionItems( transaction: Transaction
                        ): Action[AppContext, Seq[Item]]

    def transactionTotal( transaction: Transaction
                        ): Action[AppContext, Int]

    def queryShoppers( shopperIds: Option[Seq[Int]]
                     ): Action[AppContext, Seq[Shopper]]

    def queryItems( itemIds: Option[Seq[Int]]
                  ): Action[AppContext, Seq[Item]]

    def queryTransactions( transactionIds: Option[Seq[Int]],
                           sinceDate: Option[Int],
                           beforeDate: Option[Int],
                           shopperIds: Option[Seq[Int]],
                           itemIds: Option[Seq[Int]]
                         ): Action[AppContext, Seq[Transaction]]
  }
}
