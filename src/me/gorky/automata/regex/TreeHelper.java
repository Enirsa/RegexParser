package me.gorky.automata.regex;

import me.gorky.automata.regex.exceptions.BadInputException;
import me.gorky.automata.regex.exceptions.FlawedLogicException;
import me.gorky.automata.regex.structures.Node;
import me.gorky.automata.regex.structures.Symbol;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;

public class TreeHelper {

    public static Node buildSyntaxTree(String regex) throws BadInputException, FlawedLogicException {
        if (regex.equals("")) {
            return new Node(null);
        }

        LinkedList<Symbol> symbols = makeSymbolList(regex);
        preprocess(symbols);

        return createNode(symbols);
    }

    // recursive method
    private static Node createNode(LinkedList<Symbol> symbols) throws BadInputException, FlawedLogicException {
        int operatorIndex = -1; // will contain the location of an operator with the highest priority

        for (int i = 0; i < symbols.size(); i++) {
            Symbol currentSymbol = symbols.get(i);

            // if we're at the opening bracket, let's skip to the closing one or drop them altogether if the whole expression is enclosed
            if (currentSymbol.isOpeningBracket()) {
                boolean openedAtBeginning = i == 0;
                i = skipToClosingBracket(symbols, i);

                if (i == symbols.size() - 1 && openedAtBeginning) {
                    symbols.removeFirst();
                    symbols.removeLast();
                    i = -1;
                }

                continue;
            }

            if (currentSymbol.isOperator()) {
                if (operatorIndex == -1) {
                    operatorIndex = i;
                } else {
                    boolean higherPriority = currentSymbol.getPriority() > symbols.get(operatorIndex).getPriority();
                    operatorIndex = higherPriority ? i : operatorIndex;
                }
            }
        }

        if (operatorIndex == -1) { // this can either mean an invalid regex or that we're left with a single character i.e. a leaf
            if (symbols.size() != 1) {
                throw new BadInputException("Invalid regex");
            }

            return new Node(symbols.get(0));
        }

        Symbol op = symbols.get(operatorIndex);

        if (op.isUnaryOperator() && operatorIndex != symbols.size() - 1) { // a unary operator can only have symbols to the left
            throw new BadInputException("Invalid regex");
        }

        ArrayList<Node> children = new ArrayList<>();
        LinkedList<Symbol> leftChild = new LinkedList<>(symbols.subList(0, operatorIndex)); // symbols to the left of the operator
        children.add(createNode(leftChild)); // here recursion happens

        if (op.isBinaryOperator()) { // a binary operator (unlike unary) also has symbols to the right
            LinkedList<Symbol> rightChild = new LinkedList<>(symbols.subList(operatorIndex + 1, symbols.size()));
            children.add(createNode(rightChild)); // here recursion happens
        }

        return new Node(op, children);
    }

    // wraps symbols into Symbol objects, determining their type; correctly handles escaped symbols
    private static LinkedList<Symbol> makeSymbolList(String regex) throws BadInputException, FlawedLogicException {
        LinkedList<Symbol> symbols = new LinkedList<>();

        for (int i = 0; i < regex.length(); i++) {
            char currentChar = regex.charAt(i);

            if (currentChar == '\\') { // handles escaping
                // handles unescaped backslash at the end of the regex
                if (i == regex.length() - 1) {
                    throw new BadInputException("Invalid regex");
                }

                char nextChar = regex.charAt(i + 1);

                // the only symbols that can be escaped are operators, brackets and backslash itself
                if (!Symbol.isOperator(nextChar) && nextChar != '(' && nextChar != ')' && nextChar != '\\') {
                    throw new BadInputException("Invalid regex");
                }

                symbols.add(new Symbol(nextChar, "character"));
                i++;

            } else if (Symbol.isOperator(currentChar)) {
                symbols.add(new Symbol(currentChar, "operator"));

            } else if (currentChar == '(' || currentChar == ')') {
                symbols.add(new Symbol(currentChar, "bracket"));

            } else {
                symbols.add(new Symbol(currentChar, "character"));
            }
        }

        return symbols;
    }

    private static void preprocess(LinkedList<Symbol> symbols) throws BadInputException, FlawedLogicException {
        // removes empty brackets
        for (int i = 0; i < symbols.size() - 1; i++) {
            Symbol currentSymbol = symbols.get(i);
            Symbol nextSymbol = symbols.get(i + 1);

            if (currentSymbol.isOpeningBracket() && nextSymbol.isClosingBracket()) {
                symbols.remove(i);
                symbols.remove(i);

                if (i >= symbols.size()) {
                    break;
                }

                i = symbols.get(i).isClosingBracket() ? (i - 2) : (i - 1);

                if (i < 0) {
                    throw new BadInputException("Invalid regex");
                }
            }
        }

        // inserts implicit concatenation operators
        for (int i = 0; i < symbols.size() - 1; i++) {
            Symbol currentSymbol = symbols.get(i);
            Symbol nextSymbol = symbols.get(i + 1);

            if (currentSymbol.isBinaryOperator() || currentSymbol.isOpeningBracket()) {
                continue;
            }

            if (nextSymbol.isOperator() || nextSymbol.isClosingBracket()) {
                continue;
            }

            symbols.add(i + 1, new Symbol(Symbol.CONCATENATION_SYMBOL, "operator"));
            i++;
        }
    }

    private static int skipToClosingBracket(LinkedList<Symbol> symbols, int i) throws BadInputException {
        // every opening bracket pushes an item to the stack, every closing pops it; by the outer closing bracket the stack should become empty
        Stack<Character> brackets = new Stack<>();
        brackets.push('(');
        i++;

        while (brackets.size() > 0 && i < symbols.size()) {
            Symbol currentSymbol = symbols.get(i);

            if (currentSymbol.isOpeningBracket()) {
                brackets.push('(');
            }

            if (currentSymbol.isClosingBracket()) {
                brackets.pop();
            }

            i++;
        }

        if (brackets.size() != 0) {
            throw new BadInputException("Invalid regex");
        } else if (i < symbols.size() && symbols.get(i).isClosingBracket()) {
            throw new BadInputException("Invalid regex");
        }

        return i - 1;
    }

    public static void printTree(Node node) {
        printTree(node, "", true);
        System.out.println("");
    }

    private static void printTree(Node node, String prefix, boolean isTail) {
        String symbol = node.getSymbol() == null ? "" : node.getSymbol().toString();
        ArrayList<Node> children = node.getChildren();
        System.out.println(prefix + (isTail ? "└── " : "├── ") + symbol);

        for (int i = 0; i < children.size() - 1; i++) {
            printTree(children.get(i), prefix + (isTail ? "    " : "│   "), false);
        }

        if (children.size() > 0) {
            printTree(children.get(children.size() - 1), prefix + (isTail ? "    " : "│   "), true);
        }
    }

}
