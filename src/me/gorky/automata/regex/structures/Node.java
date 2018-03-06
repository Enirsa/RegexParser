package me.gorky.automata.regex.structures;

import java.util.ArrayList;

public class Node {

    private Symbol symbol;

    private ArrayList<Node> children;

    public Node(Symbol symbol) {
        this.symbol = symbol;
        this.children = new ArrayList<>();
    }

    public Node(Symbol symbol, ArrayList<Node> children) {
        this.symbol = symbol;
        this.children = children;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public ArrayList<Node> getChildren() {
        return children;
    }

}