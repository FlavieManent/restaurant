package com.example

import com.example.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.ReferenceOption.CASCADE
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.TransactionManager

//fun main(args: Array<String>): Unit =
//io.ktor.server.netty.EngineMain.main(args)
//embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
//        .start(wait = true)

import javax.sql.DataSource


fun main() {
    Database.connect("jdbc:postgresql://localhost:5432/dump.sql", driver = "org.postgresql.Driver",
        user = "postgres", password = "")
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)

}

// CUSTOMER

internal object Customers : IntIdTable("Customer") {
    //val ident = integer("id").uniqueIndex()
    val firstName = varchar("firstName", 50)
    val lastName = varchar("lastName", 50)
    val email = varchar("email", 50)
}

class CustomerEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<CustomerEntity>(Customers)
    var firstName by Customers.firstName
    var lastName by Customers.lastName
    var email by Customers.email
}

data class Customer(
    val id:Long = 0,
    val firstName: String,
    val lastName : String,
    val email : String
)
fun CustomerEntity.toCustomer() = Customer (
    id.value.toLong(),
    firstName,
    lastName,
    email
)

// ORDER ITEMS

internal object OrderItems : IntIdTable("OrderItems") {
    val name = varchar("name", 50)
    val price = double("price")
    val amount = integer("amount")
    val order = reference(
        "order_id",
        Orders,
        onDelete = CASCADE,
        onUpdate = CASCADE)
}

class OrderItemEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<OrderItemEntity>(OrderItems)
    var name by OrderItems.name
    var price by OrderItems.price
    var amount by OrderItems.amount
    var order by OrderEntity referencedOn OrderItems.order

}

data class OrderItem(
    val name: String,
    val price: Double,
    val amount: Int
)

fun OrderItemEntity.toOrderItem() = OrderItem (
    name,
    price,
    amount
)

// ORDERS

internal object Orders : IntIdTable("Orders") {
    val number = varchar("number", 50)
    val orderedBy = varchar("orderedBy", 50)
    //val content = List<OrderItem>

}

class OrderEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<OrderEntity>(Orders)
    var number by Orders.number
    var orderedBy by Orders.number
    val content by OrderItemEntity referrersOn OrderItems.order


}

data class Order(
    val number: String,
    val orderedBy : String,
    val content : List<OrderItem>
)



// REQUÊTES

val john = CustomerEntity.new {
    firstName = "John"
    lastName = "Johnson"
    email = "john.johnson@emblock.co"
}

val jane = CustomerEntity.new {
    firstName = "Jane"
    lastName = "Janeson"
    email = "Jane.Janeson@emblock.co"
}

val pizza = OrderItemEntity.new{
    name = "pizza"
    price = 5.5
    amount = 10
}

val soda = OrderItemEntity.new{
    name = "soda"
    price = 3.0
    amount = 10
}

val cake = OrderItemEntity.new{
    name = "cake"
    price = 6.5
    amount = 12
}




val allordersitem = OrderItemEntity.all()

val custo = CustomerEntity.find { Customers.firstName eq "Jane" }

//john.delete() ?

val cust = CustomerEntity.all().sortedBy { it.id }

val prices = OrderItemEntity.all().sortedByDescending{ it.price }

val query = Orders.innerJoin(Customers)
    .slice(Orders.columns)
    .select {
        Customers.firstName eq "Jane" and (Orders.orderedBy eq "Jane")
    }.withDistinct()




@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    //configureSerialization()
    configureRouting()
}