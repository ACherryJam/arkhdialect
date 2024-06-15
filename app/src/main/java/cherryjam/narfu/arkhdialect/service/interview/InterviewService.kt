package cherryjam.narfu.arkhdialect.service.interview

import cherryjam.narfu.arkhdialect.data.Interview

interface InterviewService {
    fun getData(): MutableList<Interview>
}