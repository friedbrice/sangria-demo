package localhost.sangriademo

import sangria.schema.{
  Argument, Field, IntType, ListInputType, ListType,
  ObjectType, OptionInputType, Schema, fields
}
import sangria.macros.derive

object SchemaDef {

  def schema(spec: Schema[_, _]): Schema[Context, Unit] = {

    def docs(path: String*): Option[String] = path match {

      case Seq(typ) =>
        spec.allTypes.get(typ).flatMap(_.description)

      case Seq(typ, fld) => for {
        _typ <- spec.allTypes.get(typ).flatMap(_.cast[ObjectType[_, _]])
        _fld <- _typ.fields.find(f => f.name == fld)
        desc <- _fld.description
      } yield desc

      case Seq(typ, fld, arg) => for {
        _typ <- spec.allTypes.get(typ).flatMap(_.cast[ObjectType[_, _]])
        _fld <- _typ.fields.find(f => f.name == fld)
        _arg <- _fld.arguments.find(f => f.name == arg)
        desc <- _arg.description
      } yield desc
    }

    lazy val shopper: ObjectType[Context, Shopper] = {

      val transactionsField: Field[Context, Shopper] = {

        val itemIds: Argument[Option[Seq[Int]]] =
          Argument(
            name = "itemIds",
            description = docs("Shopper", "transactions", "itemIds").getOrElse(""),
            argumentType = OptionInputType(ListInputType(IntType))
          )

        val sinceDate: Argument[Option[Int]] =
          Argument(
            name = "sinceDate",
            description = docs("Shopper", "transactions", "sinceDate").getOrElse(""),
            argumentType = OptionInputType(IntType)
          )

        val beforeDate: Argument[Option[Int]] =
          Argument(
            name = "beforeDate",
            description = docs("Shopper", "transactions", "beforeDate").getOrElse(""),
            argumentType = OptionInputType(IntType)
          )

        Field(
          name = "transactions",
          description = docs("Shopper", "transactions"),
          fieldType = ListType(transaction),
          arguments = List(sinceDate, beforeDate, itemIds),
          resolve = exec => exec.ctx.shopperTransactions(
            shopper = exec.value,
            sinceDate = exec.arg(sinceDate),
            beforeDate = exec.arg(beforeDate),
            itemIds = exec.arg(itemIds)
          )
        )
      }

      derive.deriveObjectType[Context, Shopper](
        derive.ObjectTypeDescription(docs("Shopper").getOrElse("")),
        derive.DocumentField("id", docs("Shopper", "id").getOrElse("")),
        derive.DocumentField("name", docs("Shopper", "name").getOrElse("")),
        derive.AddFields(transactionsField),
      )
    }

    lazy val item: ObjectType[Context, Item] = {

      val transactionsField: Field[Context, Item] = {

        val shopperIds: Argument[Option[Seq[Int]]] =
          Argument(
            name = "shopperIds",
            description = docs("Item", "transactions", "shopperIds").getOrElse(""),
            argumentType = OptionInputType(ListInputType(IntType))
          )

        val sinceDate: Argument[Option[Int]] =
          Argument(
            name = "sinceDate",
            description = docs("Item", "transactions", "sinceDate").getOrElse(""),
            argumentType = OptionInputType(IntType)
          )

        val beforeDate: Argument[Option[Int]] =
          Argument(
            name = "beforeDate",
            description = docs("Item", "transactions", "beforeDate").getOrElse(""),
            argumentType = OptionInputType(IntType)
          )

        Field(
          name = "transactions",
          description = docs("Item", "transactions"),
          fieldType = ListType(transaction),
          arguments = List(sinceDate, beforeDate, shopperIds),
          resolve = exec => exec.ctx.itemTransactions(
            item = exec.value,
            sinceDate = exec.arg(sinceDate),
            beforeDate = exec.arg(beforeDate),
            shopperIds = exec.arg(shopperIds)
          )
        )
      }

      derive.deriveObjectType[Context, Item](
        derive.ObjectTypeDescription(docs("Item").getOrElse("")),
        derive.DocumentField("id", docs("Item", "id").getOrElse("")),
        derive.DocumentField("name", docs("Item", "name").getOrElse("")),
        derive.DocumentField("price", docs("Item", "price").getOrElse("")),
        derive.DocumentField("description", docs("Item", "description").getOrElse("")),
        derive.AddFields(transactionsField)
      )
    }

    lazy val transaction: ObjectType[Context, Transaction] = {
      derive.deriveObjectType[Context, Transaction](
        derive.ObjectTypeDescription(docs("Transaction").getOrElse("")),
        derive.DocumentField("id", docs("Transaction", "id").getOrElse("")),
        derive.DocumentField("date", docs("Transaction", "date").getOrElse("")),
        derive.ReplaceField(
          fieldName = "shopperId",
          field = Field(
            name = "shopper",
            description = docs("Transaction", "shopper"),
            fieldType = shopper,
            resolve = exec => exec.ctx.transactionShopper(exec.value)
          )
        ),
        derive.AddFields(
          Field(
            name = "items",
            description = docs("Transaction", "items"),
            fieldType = ListType(item),
            resolve = exec => exec.ctx.transactionItems(exec.value)
          ),
          Field(
            name = "total",
            description = docs("Transaction", "total"),
            fieldType = IntType,
            resolve = exec => exec.ctx.transactionTotal(exec.value)
          )
        )
      )
    }

    lazy val query: ObjectType[Context, Unit] = {

      val shoppersField: Field[Context, Unit] = {

        val shopperIds: Argument[Option[Seq[Int]]] =
          Argument(
            name = "shopperIds",
            description = docs("Query", "shoppers", "shopperIds").getOrElse(""),
            argumentType = OptionInputType(ListInputType(IntType))
          )

        Field(
          name = "shoppers",
          description = docs("Query", "shoppers"),
          fieldType = ListType(shopper),
          arguments = List(shopperIds),
          resolve = exec => exec.ctx.queryShoppers(exec.arg(shopperIds))
        )
      }

      val itemsField: Field[Context, Unit] = {

        val itemIds: Argument[Option[Seq[Int]]] =
          Argument(
            name = "itemIds",
            description = docs("Query", "items", "itemIds").getOrElse(""),
            argumentType = OptionInputType(ListInputType(IntType))
          )

        Field(
          name = "items",
          description = docs("Query", "items"),
          fieldType = ListType(item),
          arguments = List(itemIds),
          resolve = exec => exec.ctx.queryItems(exec.arg(itemIds))
        )
      }

      val transactionsField: Field[Context, Unit] = {

        val itemIds: Argument[Option[Seq[Int]]] =
          Argument(
            name = "itemIds",
            description = docs("Query", "transactions", "itemIds").getOrElse(""),
            argumentType = OptionInputType(ListInputType(IntType))
          )

        val shopperIds: Argument[Option[Seq[Int]]] =
          Argument(
            name = "shopperIds",
            description = docs("Query", "transactions", "shopperIds").getOrElse(""),
            argumentType = OptionInputType(ListInputType(IntType))
          )

        val transactionIds: Argument[Option[Seq[Int]]] =
          Argument(
            name = "transactionIds",
            description = docs("Query", "transactions", "transactionIds").getOrElse(""),
            argumentType = OptionInputType(ListInputType(IntType))
          )

        val sinceDate: Argument[Option[Int]] =
          Argument(
            name = "sinceDate",
            description = docs("Query", "transactions", "sinceDate").getOrElse(""),
            argumentType = OptionInputType(IntType)
          )

        val beforeDate: Argument[Option[Int]] =
          Argument(
            name = "beforeDate",
            description = docs("Query", "transactions", "beforeDate").getOrElse(""),
            argumentType = OptionInputType(IntType)
          )

        Field(
          name = "transactions",
          description = docs("Query", "transactions"),
          fieldType = ListType(transaction),
          arguments =
            List(transactionIds, sinceDate, beforeDate, shopperIds, itemIds),
          resolve = exec => exec.ctx.queryTransactions(
            transactionIds = exec.arg(transactionIds),
            sinceDate = exec.arg(sinceDate),
            beforeDate = exec.arg(beforeDate),
            shopperIds = exec.arg(shopperIds),
            itemIds = exec.arg(itemIds)
          )
        )
      }

      ObjectType(
        name = "Query",
        description = docs("Query").getOrElse(""),
        fields = fields[Context, Unit](
          shoppersField,
          itemsField,
          transactionsField
        )
      )
    }

    Schema(query)
  }
}
