package ie.setu.elaine.data.local.entity

/**
 * Data class representing a streak milestone
 *
 * @property days The number of days required to achieve this milestone
 * @property title The title of the milestone
 * @property description A brief description of the milestone's significance
 */
data class Milestone(
    val days: Int,
    val title: String,
    val description: String
)