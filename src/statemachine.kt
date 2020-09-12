data class Input(val value: String)
data class State(val value: String)

data class StateMachine(
    val states: Set<State>, val inputs: Set<Input>, val delta: (State, Input) -> State,
    val initialState: State, val isFinalState: (State) -> Boolean
)
