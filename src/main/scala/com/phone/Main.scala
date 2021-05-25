package com.phone

object Main extends App {

    val pc = new PhoneCompany

    pc
    .runner(args.headOption.getOrElse("calls.log")) match {
        case Bill(content) => content.foreach{ case (customer, cost) =>
            println(s"customer: $customer is being charged ${cost.value / 100.0} pence")
        }
        case Error(err) =>
            println("Invalid call log:")
            err.foreach{ case (row, err) =>
                println(s"For row $row: ${err.msg}")
            }
    }
        

}
