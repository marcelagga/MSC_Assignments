def fsm(SISAs, start_state, end_states, input_symbols):
    """Run a finite-state machine.

    Pass in the (state, input), (state, action) pairs. Each pair means
    that if we are in a state and receive an input, we transition to a
    state and execute an action.

    (current_state, input_symbol) -> (next_state, action)

    So, action should be a function we can call.

    Pass in also the start state, and the list of end states (can be
    empty).

    And finally, pass in the input symbols -- can be list, or could
    be a generator in a real application.

    """

    state = start_state
    for input_symbol in input_symbols:
        yield state # tell user what state we're in 
        if state in end_states:
            break
        try:
            # transition to new state according to next input
            state, action = SISAs[state, input_symbol]
            if action is not None and callable(action):
                # run action
                action()
        except KeyError: # this (state, input) doesn't exist
           pass # stay in same state

### Vending machine example

start_state = 0
end_states = []
SISAs = {
    (0, 1): (1, None),
    (0, 2): (2, None),
    (1, 1): (2, None),
    (1, 2): (3, lambda: print("Select drink: ")),
    (2, 1): (3, lambda: print("Select drink: ")),
    (2, 2): (0, lambda: print("Too much, return coins")),
    (3, "7up"): (0, lambda: print("Vending 7up"))
    }

input_symbols = [1, 1, 1, "7up"]
for current_state in fsm(SISAs, start_state, end_states, input_symbols):
    print(current_state)
