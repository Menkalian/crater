package de.menkalian.crater.data.task

import kotlinx.serialization.Serializable

@Serializable
enum class Category {
    TRUTH,
    DARE,
    NEVER_HAVE_I_EVER,
    CHALLENGE,
    MINIGAME,
    RULE,
    WHO_WOULD,
    MAKE_A_CHOICE,
    TEAM_GAME,
    DRINK
}