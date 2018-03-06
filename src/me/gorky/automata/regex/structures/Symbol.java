package me.gorky.automata.regex.structures;

import me.gorky.automata.regex.exceptions.FlawedLogicException;

import java.util.HashMap;
import java.util.HashSet;

public class Symbol {

    public static final char CONCATENATION_SYMBOL = '·';

    private static final HashSet<String> TYPES;

    // key: operator
    // value: {priority, arity: b/u for binary/unary}
    private static final HashMap<Character, char[]> OPERATORS;

    private String type;

    private char value;

    static {
        TYPES = new HashSet<>();
        TYPES.add("character");
        TYPES.add("operator");
        TYPES.add("bracket");

        OPERATORS = new HashMap<>();
        OPERATORS.put('|', new char[]{2, 'b'});
        OPERATORS.put(CONCATENATION_SYMBOL, new char[]{1, 'b'});
        OPERATORS.put('*', new char[]{0, 'u'});
    }

    public Symbol(char value, String type) throws FlawedLogicException {
        if (!TYPES.contains(type)) {
            throw new FlawedLogicException("There isn't '" + type + "' symbol type");
        }

        if (type.equals("operator") && !OPERATORS.containsKey(value)) {
            throw new FlawedLogicException("'" + value + "' is considered to be an operator whereas it isn't");
        }

        if (type.equals("bracket") && value != '(' && value != ')') {
            throw new FlawedLogicException("'" + value + "' is considered to be a bracket for some reason");
        }

        this.type = type;
        this.value = value;
    }

    public static boolean isOperator(char ch) {
        return OPERATORS.containsKey(ch);
    }

    public boolean isOperator() {
        return type.equals("operator");
    }

    public boolean isUnaryOperator() {
        return isOperator() && OPERATORS.get(value)[1] == 'u';
    }

    public boolean isBinaryOperator() {
        return isOperator() && OPERATORS.get(value)[1] == 'b';
    }

    public int getPriority() throws FlawedLogicException {
        if (!isOperator()) {
            throw new FlawedLogicException("Attempted to get priority of a non-operator symbol '" + value + "'");
        }

        return OPERATORS.get(value)[0];
    }

    public int getOperandsCount() throws FlawedLogicException {
        if (!isOperator()) {
            throw new FlawedLogicException("Attempted to get arity of a non-operator symbol '" + value + "'");
        }

        return isUnaryOperator() ? 1 : 2;
    }

    public boolean isBracket() {
        return type.equals("bracket");
    }

    public boolean isOpeningBracket() {
        return isBracket() && value == '(';
    }

    public boolean isClosingBracket() {
        return isBracket() && value == ')';
    }

    public char toChar() {
        return value;
    }

    @Override
    public String toString() {
        return Character.toString(value);
    }

}
