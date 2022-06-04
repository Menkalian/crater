package de.menkalian.draco.data.game.event

enum class EventType {
    GAME_STARTED,
    GAME_STATE,
    GAME_RESULTS,

    UPDATE_PLAYER,
    UPDATE_SETTINGS,

    ROUND_RESULTS,

    QUESTION_SELECTED,
    QUESTION_HINT_REVEALED,
    QUESTION_ANSWER_REVEALED,

    GUESS_REQUEST,
    GUESS_RESPONSE,

    ACTION_REQUEST,
    ACTION_RESPONSE
}