package cherryjam.narfu.arkhdialect.service.attachment

import cherryjam.narfu.arkhdialect.data.TextAttachment
import com.github.javafaker.Faker
import java.util.Locale

class FakerTextAttachmentService : TextAttachmentService {
    private var attachments: MutableList<TextAttachment> = mutableListOf()

    init {
        val faker = Faker.instance(Locale("ru"))

        attachments = (1..50).map {
            TextAttachment(
                id = it.toLong(),
                title = faker.animal().name(),
                data = faker.lorem().paragraph()
            )
        }.toMutableList()
    }

    override fun getData(): MutableList<TextAttachment> {
        return attachments
    }
}