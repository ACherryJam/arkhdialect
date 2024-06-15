package cherryjam.narfu.arkhdialect.service.card

import cherryjam.narfu.arkhdialect.data.Card
import com.github.javafaker.Faker
import java.util.Locale

class FakerCardService : CardService {
    private var cards: MutableList<Card> = mutableListOf()

    init {
        val faker = Faker.instance(Locale("ru"))

        cards = (1..50).map {
            Card(
                id = it.toLong(),
                word = faker.lorem().word(),
                location = faker.address().cityName()
            )
        }.toMutableList()
    }

    override fun getData(): MutableList<Card> {
        return cards
    }
}