package me.gorky.automata.regex.structures;

import me.gorky.automata.regex.exceptions.FlawedLogicException;

import java.util.*;

public class State implements Comparable<State > {

    public static final char ENTRY_STATE_SYMBOL = '>';

    public static final char EXIT_STATE_SYMBOL = 'X';

    private static int counter = 0;

    private int id;

    private boolean isEntry = false;

    private boolean isExit = false;

    private TreeMap<Character, TreeSet<State>> transitions;

    public State(Set<Character> alphabet) throws FlawedLogicException {
        this.id = ++counter;
        transitions = new TreeMap<>(new Comparator<Character>() {
            @Override
            public int compare(Character c1, Character c2) {
                if (c1 == c2)
                    return 0;
                if (c1 == null)
                    return 1;
                if (c2 == null)
                    return -1;
                return c1 - c2;
            }
        });

        for (Character letter : alphabet) {
            transitions.put(letter, null);
        }

        if (alphabet.contains(null)) { // if the alphabet contains null (i.e. ε*), then the state can transition to itself
            addTransition(null, this);
        }
    }

    public static void setCounter(int counter) {
        State.counter = counter;
    }

    public boolean isEntry() {
        return isEntry;
    }

    public void makeEntry() {
        isEntry = true;
    }

    public boolean isExit() {
        return isExit;
    }

    public void makeExit() {
        isExit = true;
    }

    public int getId() {
        return id;
    }

    public TreeMap<Character, TreeSet<State>> getTransitions() {
        return transitions;
    }

    public void addTransition(Character letter, State state) throws FlawedLogicException {
        if (!transitions.containsKey(letter)) {
            throw new FlawedLogicException("The state's alphabet doesn't contain letter '" + letter + "'");
        }

        if (!transitions.keySet().equals(state.getTransitions().keySet())) {
            throw new FlawedLogicException("The states' alphabets do not match");
        }

        if (transitions.get(letter) == null) {
            TreeSet<State> states = new TreeSet<>();
            states.add(state);
            transitions.put(letter, states);
        } else {
            transitions.get(letter).add(state);
        }
    }

    @Override
    public int compareTo(State s) {
        return id - s.getId();
    }
}
