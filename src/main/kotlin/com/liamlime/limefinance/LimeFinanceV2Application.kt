package com.liamlime.limefinance

import com.liamlime.limefinance.import.ImportIn
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LimeFinanceV2Application

fun main(args: Array<String>) {
	//runApplication<LimeFinanceV2Application>(*args)
	ImportIn().doImport()

}


