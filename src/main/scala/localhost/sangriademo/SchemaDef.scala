package localhost.sangriademo

import sangria.schema.{
  Argument        => GqlArgument,
  Field           => GqlField,
  IntType         => GqlInt,
  ListInputType   => GqlListInput,
  ListType        => GqlList,
  ObjectType      => GqlObject,
  OptionInputType => GqlOptionInput,
  Schema          => GqlSchema,
  fields          => gqlFields
}
import sangria.macros.derive

object SchemaDef {

  lazy val itemIds: GqlArgument[Option[Seq[Int]]] =
    GqlArgument(
      name         = "itemIds",
      argumentType = GqlOptionInput(GqlListInput(GqlInt))
    )

  lazy val shopperIds: GqlArgument[Option[Seq[Int]]] =
    GqlArgument(
      name         = "shopperIds",
      argumentType = GqlOptionInput(GqlListInput(GqlInt))
    )

  lazy val transactionIds: GqlArgument[Option[Seq[Int]]] =
    GqlArgument(
      name         = "transactionIds",
      argumentType = GqlOptionInput(GqlListInput(GqlInt))
    )

  lazy val sinceDate: GqlArgument[Option[Int]] =
    GqlArgument(
      name         = "sinceDate",
      argumentType = GqlOptionInput(GqlInt)
    )

  lazy val beforeDate: GqlArgument[Option[Int]] =
    GqlArgument(
      name         = "beforeDate",
      argumentType = GqlOptionInput(GqlInt)
    )

  lazy val shopper: GqlObject[Context, Shopper] =
    derive.deriveObjectType[Context, Shopper](
      derive.AddFields(
        GqlField(
          name      = "transactions",
          fieldType = GqlList(transaction),
          arguments = List(sinceDate, beforeDate, itemIds),
          resolve   = cc => cc.ctx.shopperTransactions(
            shopper    = cc.value,
            sinceDate  = cc.arg(sinceDate),
            beforeDate = cc.arg(beforeDate),
            itemIds    = cc.arg(itemIds)
          )
        )
      )
    )

  lazy val item: GqlObject[Context, Item] =
    derive.deriveObjectType[Context, Item](
      derive.AddFields(
        GqlField(
          name      = "transactions",
          fieldType = GqlList(transaction),
          arguments = List(sinceDate, beforeDate, shopperIds),
          resolve   = cc => cc.ctx.itemTransactions(
            item       = cc.value,
            sinceDate  = cc.arg(sinceDate),
            beforeDate = cc.arg(beforeDate),
            shopperIds = cc.arg(shopperIds)
          )
        )
      )
    )

  lazy val transaction: GqlObject[Context, Transaction] =
    derive.deriveObjectType[Context, Transaction](
      derive.ReplaceField(
        fieldName = "shopperId",
        field     = GqlField(
          name      = "shopper",
          fieldType = shopper,
          resolve   = cc => cc.ctx.transactionShopper(cc.value)
        )
      ),
      derive.AddFields(
        GqlField(
          name      = "items",
          fieldType = GqlList(item),
          resolve   = cc => cc.ctx.transactionItems(cc.value)
        ),
        GqlField(
          name      = "total",
          fieldType = GqlInt,
          resolve   = cc => cc.ctx.transactionTotal(cc.value)
        )
      )
    )

  lazy val query: GqlObject[Context, Unit] =
    GqlObject(
      name   = "Query",
      fields = gqlFields[Context, Unit](
        GqlField(
          name      = "shoppers",
          fieldType = GqlList(shopper),
          arguments = List(shopperIds),
          resolve   = cc => cc.ctx.queryShoppers(cc.arg(shopperIds))
        ),
        GqlField(
          name      = "items",
          fieldType = GqlList(item),
          arguments = List(itemIds),
          resolve   = cc => cc.ctx.queryItems(cc.arg(itemIds))
        ),
        GqlField(
          name      = "transactions",
          fieldType = GqlList(transaction),
          arguments =
            List(transactionIds, sinceDate, beforeDate, shopperIds, itemIds),
          resolve   = cc => cc.ctx.queryTransactions(
            transactionIds = cc.arg(transactionIds),
            sinceDate      = cc.arg(sinceDate),
            beforeDate     = cc.arg(beforeDate),
            shopperIds     = cc.arg(shopperIds),
            itemIds        = cc.arg(itemIds)
          )
        )
      )
    )

  lazy val schema: GqlSchema[Context, Unit] = GqlSchema(query)
}
