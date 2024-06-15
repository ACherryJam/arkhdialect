package cherryjam.narfu.arkhdialect.service.interview

import cherryjam.narfu.arkhdialect.data.Interview
import com.github.javafaker.Faker
import java.util.Locale

class FakerInterviewService : InterviewService {
    private var interviews: MutableList<Interview> = mutableListOf()

    init {
        val faker = Faker.instance(Locale("ru"))

        interviews = (1..50).map {
            Interview(
                id = it.toLong(),
                name = faker.name().fullName(),
                location = faker.address().cityName()
            )
        }.toMutableList()
    }

    override fun getData(): MutableList<Interview> {
        return interviews
    }
}