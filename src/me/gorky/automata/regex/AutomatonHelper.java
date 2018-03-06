package me.gorky.automata.regex;

import me.gorky.automata.regex.structures.State;

import java.util.*;

public class AutomatonHelper {

    public static void printAutomaton(State entry) {
        if (entry.getTransitions().keySet().isEmpty()) {
            System.out.println(State.ENTRY_STATE_SYMBOL + "" + State.EXIT_STATE_SYMBOL + entry.getId() + ": -\r\n");
            return;
        }

        TreeMap<String, List<String>> adjacencyList = formAdjacencyList(entry);
        int[] columnWidths = determineColumnWidths(adjacencyList);

        printRow("", toListOfStrings(entry.getTransitions().keySet()), columnWidths, true);
        for (String rowName : adjacencyList.keySet()) {
            printRow(rowName, adjacencyList.get(rowName), columnWidths, false);
        }
        System.out.println("");
    }

    private static TreeMap<String, List<String>> formAdjacencyList(State entry) {
        TreeMap<String, List<String>> adjacencyList = new TreeMap<>(new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                int i1 = Integer.parseInt(s1.substring(2, s1.length() - 1));
                int i2 = Integer.parseInt(s2.substring(2, s2.length() - 1));
                return i1 - i2;
            }
        });
        formAdjacencyList(entry, adjacencyList, new HashSet<>());

        return adjacencyList;
    }

    private static void formAdjacencyList(State state, TreeMap<String, List<String>> adjacencyList, Set<State> visited) {
        if (visited.contains(state)) {
            return;
        } else {
            visited.add(state);
        }

        Set<State> toVisit = new HashSet<>();
        Map<Character, TreeSet<State>> transitions = state.getTransitions();
        List<String> row = new ArrayList<>();

        for (Character letter : transitions.keySet()) {
            String destinations = null;

            if (transitions.get(letter) != null && !transitions.get(letter).isEmpty()) { // can it be empty at all?
                List<State> states = new ArrayList<>(transitions.get(letter));
                toVisit.addAll(states);
                destinations = "";

                for (int i = 0; i < states.size(); i++) {
                    String delimiter = (i == states.size() - 1) ? "" : ",";
                    destinations += states.get(i).getId() + delimiter;
                }
            }

            row.add(destinations);
        }

        String prefix;

        if (state.isEntry() && state.isExit()) {
            prefix = State.ENTRY_STATE_SYMBOL + "" + State.EXIT_STATE_SYMBOL;
        } else if (state.isEntry()) {
            prefix = " " + State.ENTRY_STATE_SYMBOL;
        } else if (state.isExit()) {
            prefix = " " + State.EXIT_STATE_SYMBOL;
        } else {
            prefix = "  ";
        }

        String rowName = prefix + state.getId() + ":";
        adjacencyList.put(rowName, row);

        for (State s : toVisit) {
            formAdjacencyList(s, adjacencyList, visited);
        }
    }

    private static int[] determineColumnWidths(TreeMap<String, List<String>> adjacencyList) {
        int rowSize = adjacencyList.firstEntry().getValue().size() + 1;
        int[] columnWidths = new int[rowSize];
        Arrays.fill(columnWidths, 1);

        for (String rowName : adjacencyList.keySet()) {
            columnWidths[0] = rowName.length() > columnWidths[0] ? rowName.length() : columnWidths[0];

            for (int i = 0; i < adjacencyList.get(rowName).size(); i++) {
                String s = adjacencyList.get(rowName).get(i);
                columnWidths[i + 1] = (s != null && s.length() > columnWidths[i + 1]) ? s.length() : columnWidths[i + 1];
            }
        }

        for (int i = 0; i < columnWidths.length - 1; i++) {
            columnWidths[i] += 2; // 2 spaces between columns
        }

        return columnWidths;
    }

    private static void printRow(String firstColumn, List<String> tail, int[] columnWidths, boolean isFirst) {
        while (firstColumn.length() < columnWidths[0] - 2) {
            firstColumn = " " + firstColumn;
        }

        firstColumn += "  ";
        System.out.print(firstColumn);
        String nullReplacement = isFirst ? "ε*" : "-";

        for (int i = 0; i < tail.size(); i++) {
            String s = tail.get(i) == null ? nullReplacement : tail.get(i);

            while (i != tail.size() - 1 && s.length() < columnWidths[i + 1]) {
                s += " ";
            }

            System.out.print(s);
        }

        System.out.println("");
    }

    private static List<String> toListOfStrings(Set<Character> set) {
        List<String> list = new ArrayList<>();

        for (Character ch : set) {
            if (ch == null) {
                list.add(null);
            } else {
                list.add(Character.toString(ch));
            }
        }

        return list;
    }

}
