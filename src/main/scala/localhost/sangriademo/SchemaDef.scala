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
          resolve   = exec => exec.ctx.shopperTransactions(
            shopper    = exec.value,
            sinceDate  = exec.arg(sinceDate),
            beforeDate = exec.arg(beforeDate),
            itemIds    = exec.arg(itemIds)
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
          resolve   = exec => exec.ctx.itemTransactions(
            item       = exec.value,
            sinceDate  = exec.arg(sinceDate),
            beforeDate = exec.arg(beforeDate),
            shopperIds = exec.arg(shopperIds)
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
          resolve   = exec => exec.ctx.transactionShopper(exec.value)
        )
      ),
      derive.AddFields(
        GqlField(
          name      = "items",
          fieldType = GqlList(item),
          resolve   = exec => exec.ctx.transactionItems(exec.value)
        ),
        GqlField(
          name      = "total",
          fieldType = GqlInt,
          resolve   = exec => exec.ctx.transactionTotal(exec.value)
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
          resolve   = exec => exec.ctx.queryShoppers(exec.arg(shopperIds))
        ),
        GqlField(
          name      = "items",
          fieldType = GqlList(item),
          arguments = List(itemIds),
          resolve   = exec => exec.ctx.queryItems(exec.arg(itemIds))
        ),
        GqlField(
          name      = "transactions",
          fieldType = GqlList(transaction),
          arguments =
            List(transactionIds, sinceDate, beforeDate, shopperIds, itemIds),
          resolve   = exec => exec.ctx.queryTransactions(
            transactionIds = exec.arg(transactionIds),
            sinceDate      = exec.arg(sinceDate),
            beforeDate     = exec.arg(beforeDate),
            shopperIds     = exec.arg(shopperIds),
            itemIds        = exec.arg(itemIds)
          )
        )
      )
    )

  lazy val schema: GqlSchema[Context, Unit] = GqlSchema(query)
}
