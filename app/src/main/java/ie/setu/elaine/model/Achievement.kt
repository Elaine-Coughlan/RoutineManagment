package ie.setu.elaine.model

data class Achievement(
    val title: String,
    val description: String,
    val isUnlocked: Boolean = false
)
