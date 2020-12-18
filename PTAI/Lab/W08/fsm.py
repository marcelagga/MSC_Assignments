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
