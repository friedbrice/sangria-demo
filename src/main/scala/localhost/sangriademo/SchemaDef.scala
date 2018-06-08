package localhost.sangriademo

import sangria.schema._
import sangria.macros.derive

object SchemaDef {

  lazy val itemIds: Argument[Option[Seq[Int]]] =
    Argument(
      name = "itemIds",
      argumentType = OptionInputType(ListInputType(IntType))
    )

  lazy val shopperIds: Argument[Option[Seq[Int]]] =
    Argument(
      name = "shopperIds",
      argumentType = OptionInputType(ListInputType(IntType))
    )

  lazy val transactionIds: Argument[Option[Seq[Int]]] =
    Argument(
      name = "transactionIds",
      argumentType = OptionInputType(ListInputType(IntType))
    )

  lazy val sinceDate: Argument[Option[Int]] =
    Argument(
      name = "sinceDate",
      argumentType = OptionInputType(IntType)
    )

  lazy val beforeDate: Argument[Option[Int]] =
    Argument(
      name = "beforeDate",
      argumentType = OptionInputType(IntType)
    )

  lazy val shopper: ObjectType[AppContext, Shopper] =
    derive.deriveObjectType[AppContext, Shopper](
      derive.AddFields(
        Field(
          name = "transactions",
          fieldType = ListType(transaction),
          arguments = List(sinceDate, beforeDate, itemIds),
          resolve = cc => cc.ctx.shopperTransactions(
            shopper = cc.value,
            sinceDate = cc.arg(sinceDate),
            beforeDate = cc.arg(beforeDate),
            itemIds = cc.arg(itemIds)
          )
        )
      )
    )

  lazy val item: ObjectType[AppContext, Item] =
    derive.deriveObjectType[AppContext, Item](
      derive.AddFields(
        Field(
          name = "transactions",
          fieldType = ListType(transaction),
          arguments = List(sinceDate, beforeDate, shopperIds),
          resolve = cc => cc.ctx.itemTransactions(
            item = cc.value,
            sinceDate = cc.arg(sinceDate),
            beforeDate = cc.arg(beforeDate),
            shopperIds = cc.arg(shopperIds)
          )
        )
      )
    )

  lazy val transaction: ObjectType[AppContext, Transaction] =
    derive.deriveObjectType[AppContext, Transaction](
      derive.ReplaceField(
        fieldName = "shopperId",
        field = Field(
          name = "shopper",
          fieldType = shopper,
          resolve = cc => cc.ctx.transactionShopper(cc.value)
        )
      ),
      derive.AddFields(
        Field(
          name = "items",
          fieldType = ListType(item),
          resolve = cc => cc.ctx.transactionItems(cc.value)
        ),
        Field(
          name = "total",
          fieldType = IntType,
          resolve = cc => cc.ctx.transactionTotal(cc.value)
        )
      )
    )

  lazy val query: ObjectType[AppContext, Unit] =
    ObjectType(
      name = "Query",
      fields = fields[AppContext, Unit](
        Field(
          name = "shoppers",
          fieldType = ListType(shopper),
          arguments = List(shopperIds),
          resolve = cc => cc.ctx.queryShoppers(cc.arg(shopperIds))
        ),
        Field(
          name = "items",
          fieldType = ListType(item),
          arguments = List(itemIds),
          resolve = cc => cc.ctx.queryItems(cc.arg(itemIds))
        ),
        Field(
          name = "transactions",
          fieldType = ListType(transaction),
          arguments =
            List(transactionIds, sinceDate, beforeDate, shopperIds, itemIds),
          resolve = cc => cc.ctx.queryTransactions(
            transactionIds = cc.arg(transactionIds),
            sinceDate = cc.arg(sinceDate),
            beforeDate = cc.arg(beforeDate),
            shopperIds = cc.arg(shopperIds),
            itemIds = cc.arg(itemIds)
          )
        )
      )
    )

  lazy val schema: Schema[AppContext, Unit] = Schema(query)
}
