package it.pixiekevin.innertube.models.bodies

import it.pixiekevin.innertube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class ContinuationBody(
    val context: Context = Context.DefaultWeb,
    val continuation: String,
)
