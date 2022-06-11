package de.menkalian.crater.data.task

import kotlinx.serialization.Serializable

@Serializable
enum class Gimmick {
    ADDITIONAL_PUNISHMENT,
    ADDITIONAL_QUESTION,
    ALTERNATIVE,
    BET,
    BONUS_QUEST,
    CHAMPION_REWARD,
    CONSEQUENCE,
    INVOLVING_SOMEONE,
    REVENGE,
    SECRET,
    SPLITTED,
    STAKE,
    SWAP_PLACES,
    TASK_BUDDY,
    WILDCARD,
}