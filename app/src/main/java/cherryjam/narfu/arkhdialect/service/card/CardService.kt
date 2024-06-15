package cherryjam.narfu.arkhdialect.service.card

import cherryjam.narfu.arkhdialect.data.Card

interface CardService {
    fun getData(): MutableList<Card>
}