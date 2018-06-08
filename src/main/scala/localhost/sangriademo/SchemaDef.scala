package localhost.sangriademo

import sangria.schema.{
  Argument => GqlArgument,
  OptionInputType => GqlOptionInputType,
  ListInputType => GqlListInputType,
  IntType => GqlIntType,
  ObjectType => GqlObjectType,
  Field => GqlField,
  ListType => GqlListType,
  fields => gqlFields,
  Schema => GqlSchema
}
import sangria.macros.derive

object SchemaDef {

  lazy val itemIds: GqlArgument[Option[Seq[Int]]] =
    GqlArgument(
      name = "itemIds",
      argumentType = GqlOptionInputType(GqlListInputType(GqlIntType))
    )

  lazy val shopperIds: GqlArgument[Option[Seq[Int]]] =
    GqlArgument(
      name = "shopperIds",
      argumentType = GqlOptionInputType(GqlListInputType(GqlIntType))
    )

  lazy val transactionIds: GqlArgument[Option[Seq[Int]]] =
    GqlArgument(
      name = "transactionIds",
      argumentType = GqlOptionInputType(GqlListInputType(GqlIntType))
    )

  lazy val sinceDate: GqlArgument[Option[Int]] =
    GqlArgument(
      name = "sinceDate",
      argumentType = GqlOptionInputType(GqlIntType)
    )

  lazy val beforeDate: GqlArgument[Option[Int]] =
    GqlArgument(
      name = "beforeDate",
      argumentType = GqlOptionInputType(GqlIntType)
    )

  lazy val shopper: GqlObjectType[AppContext, Shopper] =
    derive.deriveObjectType[AppContext, Shopper](
      derive.AddFields(
        GqlField(
          name = "transactions",
          fieldType = GqlListType(transaction),
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

  lazy val item: GqlObjectType[AppContext, Item] =
    derive.deriveObjectType[AppContext, Item](
      derive.AddFields(
        GqlField(
          name = "transactions",
          fieldType = GqlListType(transaction),
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

  lazy val transaction: GqlObjectType[AppContext, Transaction] =
    derive.deriveObjectType[AppContext, Transaction](
      derive.ReplaceField(
        fieldName = "shopperId",
        field = GqlField(
          name = "shopper",
          fieldType = shopper,
          resolve = cc => cc.ctx.transactionShopper(cc.value)
        )
      ),
      derive.AddFields(
        GqlField(
          name = "items",
          fieldType = GqlListType(item),
          resolve = cc => cc.ctx.transactionItems(cc.value)
        ),
        GqlField(
          name = "total",
          fieldType = GqlIntType,
          resolve = cc => cc.ctx.transactionTotal(cc.value)
        )
      )
    )

  lazy val query: GqlObjectType[AppContext, Unit] =
    GqlObjectType(
      name = "Query",
      fields = gqlFields[AppContext, Unit](
        GqlField(
          name = "shoppers",
          fieldType = GqlListType(shopper),
          arguments = List(shopperIds),
          resolve = cc => cc.ctx.queryShoppers(cc.arg(shopperIds))
        ),
        GqlField(
          name = "items",
          fieldType = GqlListType(item),
          arguments = List(itemIds),
          resolve = cc => cc.ctx.queryItems(cc.arg(itemIds))
        ),
        GqlField(
          name = "transactions",
          fieldType = GqlListType(transaction),
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

  lazy val schema: GqlSchema[AppContext, Unit] = GqlSchema(query)
}
