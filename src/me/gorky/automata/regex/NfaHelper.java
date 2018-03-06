package me.gorky.automata.regex;

import me.gorky.automata.regex.exceptions.FlawedLogicException;
import me.gorky.automata.regex.structures.Node;
import me.gorky.automata.regex.structures.State;
import me.gorky.automata.regex.structures.Symbol;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NfaHelper {

    public static State buildNfa(Node root) throws FlawedLogicException {
        State.setCounter(0);
        Set<Character> alphabet = formAlphabet(root);

        if (root.getSymbol() == null) {
            State s = new State(alphabet);
            s.makeEntry();
            s.makeExit();
            return s;
        }

        State entry = new State(alphabet);
        entry.makeEntry();
        State exit = new State(alphabet);
        exit.makeExit();
        connectStates(entry, exit, root, alphabet);

        return entry;
    }

    private static Set<Character> formAlphabet(Node root) {
        Set<Character> alphabet = new HashSet<>();
        formAlphabet(root, alphabet);
        alphabet.add(null); // null will be considered ε*

        return alphabet;
    }

    private static void formAlphabet(Node node, Set<Character> alphabet) {
        if (node.getChildren().isEmpty()) { // means it's a leaf
            if (node.getSymbol() != null) {
                alphabet.add(node.getSymbol().toChar());
            }
        } else {
            for (Node child : node.getChildren()) {
                formAlphabet(child, alphabet);
            }
        }
    }

    private static void connectStates(State start, State end, Node node, Set<Character> alphabet) throws FlawedLogicException {
        if (node.getChildren().isEmpty()) {
            start.addTransition(node.getSymbol().toChar(), end);
            return;
        }

        Symbol operator = node.getSymbol();
        List<Node> children = node.getChildren();

        if (children.size() != operator.getOperandsCount()) {
            throw new FlawedLogicException("Operator '" + operator + "' has incorrect amount of operands (" + children.size() + ")");
        }

        if (operator.toChar() == '|') {
            connectStates(start, end, children.get(0), alphabet);
            connectStates(start, end, children.get(1), alphabet);

        } else if (operator.toChar() == Symbol.CONCATENATION_SYMBOL) {
            State s = new State(alphabet);
            connectStates(start, s, children.get(0), alphabet);
            connectStates(s, end, children.get(1), alphabet);

        } else if (operator.toChar() == '*') {
            State s1 = new State(alphabet);
            State s2 = new State(alphabet);
            start.addTransition(null, s1);
            start.addTransition(null, end);
            s2.addTransition(null, s1);
            s2.addTransition(null, end);
            connectStates(s1, s2, children.get(0), alphabet);

        } else {
            throw new FlawedLogicException("Intermediary node (symbol '" + operator + "') wasn't recognized as operator");
        }
    }

}
