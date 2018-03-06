package me.gorky.automata.regex;

import me.gorky.automata.regex.exceptions.BadInputException;
import me.gorky.automata.regex.structures.Node;
import me.gorky.automata.regex.structures.State;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Set;

/**
 * ((дек|велик)аа*н *)*
 */

public class Main {

    public static void main(String[] args) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.print("Enter a regular expression ('e' to exit): ");

            try {
                String input = br.readLine();

                if (input.equals("e")) {
                    break;
                }

                Node root = TreeHelper.buildSyntaxTree(input);
                System.out.println("# syntax tree:");
                TreeHelper.printTree(root);

                String legend = State.ENTRY_STATE_SYMBOL + " — entry state, " + State.EXIT_STATE_SYMBOL + " — exit state";

                State entry = NfaHelper.buildNfa(root);
                System.out.println("# NFA adjacency list (" + legend + "):");
                AutomatonHelper.printAutomaton(entry);

                entry = DfaHelper.buildDfa(entry);
                System.out.println("# DFA adjacency list (" + legend + "):");
                AutomatonHelper.printAutomaton(entry);

                Set<Set<State>> equivalenceClasses = DfaHelper.getEquivalenceClasses(entry);
                System.out.println("# equivalence classes:");
                DfaHelper.printEquivalenceClasses(equivalenceClasses);

                entry = DfaHelper.buildDfa(equivalenceClasses);
                System.out.println("# minimized DFA adjacency list (" + legend + "):");
                AutomatonHelper.printAutomaton(entry);

                while (true) {
                    System.out.print("Enter a string to be checked ('s' to stop): ");
                    input = br.readLine();

                    if (input.equals("s")) {
                        break;
                    }

                    System.out.println("# " + DfaHelper.checkString(input, entry));
                }

                System.out.println("");

            } catch (BadInputException ex) {
                System.out.println("# " + ex.getMessage() + "\r\n");
            } catch (Exception ex) {
                ex.printStackTrace();
                break;
            }
        }
    }

}
