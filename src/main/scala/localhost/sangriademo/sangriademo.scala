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

  case class AuthError(message: String) extends Exception(message)

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

  trait Context {

    def shopperTransactions( shopper: Shopper,
                             sinceDate: Option[Int],
                             beforeDate: Option[Int],
                             itemIds: Option[Seq[Int]]
                           ): Action[Context, Seq[Transaction]]

    def itemTransactions( item: Item,
                          sinceDate: Option[Int],
                          beforeDate: Option[Int],
                          shopperIds: Option[Seq[Int]]
                        ): Action[Context, Seq[Transaction]]

    def transactionShopper( transaction: Transaction
                          ): Action[Context, Shopper]

    def transactionItems( transaction: Transaction
                        ): Action[Context, Seq[Item]]

    def transactionTotal( transaction: Transaction
                        ): Action[Context, Int]

    def queryShoppers( shopperIds: Option[Seq[Int]]
                     ): Action[Context, Seq[Shopper]]

    def queryItems( itemIds: Option[Seq[Int]]
                  ): Action[Context, Seq[Item]]

    def queryTransactions( transactionIds: Option[Seq[Int]],
                           sinceDate: Option[Int],
                           beforeDate: Option[Int],
                           shopperIds: Option[Seq[Int]],
                           itemIds: Option[Seq[Int]]
                         ): Action[Context, Seq[Transaction]]
  }
}
