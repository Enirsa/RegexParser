package me.gorky.automata.regex;

import me.gorky.automata.regex.exceptions.FlawedLogicException;
import me.gorky.automata.regex.structures.State;

import java.util.*;

public class DfaHelper {

    // from NFA
    public static State buildDfa(State entry) throws FlawedLogicException {
        State.setCounter(0);

        Set<Character> alphabet = new LinkedHashSet<>(entry.getTransitions().keySet());
        alphabet.remove(null);

        if (alphabet.isEmpty()) {
            State s = new State(alphabet);
            s.makeEntry();
            s.makeExit();
            return s;
        }
        
        State newEntry = mergeStatesViaEpsilon(getEpsilonDestinations(entry), new HashMap<>(), alphabet);
        newEntry.makeEntry();

        return newEntry;
    }

    // from equivalence classes
    public static State buildDfa(Set<Set<State>> equivalenceClasses) throws FlawedLogicException {
        State.setCounter(0);
        State oldEntry = findEntry(equivalenceClasses);
        Map<State, State> oldToNewMap = getBlankOldToNewMap(equivalenceClasses);
        Set<Character> alphabet = oldEntry.getTransitions().keySet();
        State entry = mergeStates(oldEntry, equivalenceClasses, oldToNewMap, alphabet);
        entry.makeEntry();

        return entry;
    }

    public static boolean checkString(String str, State dfaEntry) throws FlawedLogicException {
        if (str.length() == 0) {
            return dfaEntry.isExit();
        }

        TreeSet<State> destinations = dfaEntry.getTransitions().get(str.charAt(0));

        if (destinations != null) {
            if (destinations.size() != 1) {
                throw new FlawedLogicException("Passed argument doesn't seem to be a DFA");
            }

            return checkString(str.substring(1), destinations.first());
        }

        return false;
    }

    public static Set<Set<State>> getEquivalenceClasses(State dfaEntry) throws FlawedLogicException {
        Set<Character> alphabet = dfaEntry.getTransitions().keySet();
        Set<State> reachableStates = getReachableStates(dfaEntry, alphabet);

        if (reachableStates.size() < 2) {
            Set<Set<State>> ss = new HashSet<>();
            ss.add(reachableStates);
            return ss;
        }

        return getEquivalenceClasses(reachableStates, alphabet);
    }

    public static void printEquivalenceClasses(Set<Set<State>> equivalenceClasses) {
        StringBuilder sb = new StringBuilder();
        String delimiter = ", ";
        sb.append("  ");

        for (Set<State> eqClass : equivalenceClasses) {
            sb.append("{");

            for (State state : eqClass) {
                sb.append(state.getId());
                sb.append(delimiter);
            }

            sb.setLength(sb.length() - delimiter.length());
            sb.append("}");
            sb.append(delimiter);
        }

        sb.setLength(sb.length() - delimiter.length());
        sb.append(System.lineSeparator());
        System.out.println(sb.toString());
    }

    private static State mergeStatesViaEpsilon(TreeSet<State> states, Map<String, State> mergedStates, Set<Character> alphabet) throws FlawedLogicException {
        State mergedState = new State(alphabet);
        mergedStates.put(getCombinedId(states), mergedState);

        for (State state : states) {
            if (state.isExit()) {
                mergedState.makeExit();
            }
        }

        for (Character letter : alphabet) {
            TreeSet<State> mergedDestinations = new TreeSet<>();

            for (State state : states) {
                TreeSet<State> destinations = state.getTransitions().get(letter);

                if (destinations != null) {
                    for (State destination : destinations) {
                        mergedDestinations.addAll(getEpsilonDestinations(destination));
                    }
                }
            }

            if (mergedDestinations.size() > 0) {
                String combinedId = getCombinedId(mergedDestinations);

                if (mergedStates.containsKey(combinedId)) {
                    mergedState.addTransition(letter, mergedStates.get(combinedId));
                } else {
                    mergedState.addTransition(letter, mergeStatesViaEpsilon(mergedDestinations, mergedStates, alphabet));
                }
            }
        }

        return mergedState;
    }

    private static TreeSet<State> getEpsilonDestinations(State state) {
        TreeSet<State> epsilonDestinations = new TreeSet<>();
        getEpsilonDestinations(state, epsilonDestinations);

        return epsilonDestinations;
    }

    private static void getEpsilonDestinations(State state, TreeSet<State> epsilonDestinations) {
        TreeSet<State> ed = state.getTransitions().get(null);
        epsilonDestinations.addAll(ed);

        for (State destination : ed) {
            if (destination != state) {
                getEpsilonDestinations(destination, epsilonDestinations);
            }
        }
    }

    private static String getCombinedId(TreeSet<State> states) {
        String combinedId = "";

        for (State s : states) {
            String delimiter = s == states.last() ? "" : ",";
            combinedId += s.getId() + delimiter;
        }

        return combinedId;
    }

    private static Set<State> getReachableStates(State state, Set<Character> alphabet) throws FlawedLogicException {
        Set<State> reachableStates = new TreeSet<>();
        fillWithReachableStates(state, reachableStates, alphabet);

        return reachableStates;
    }

    private static void fillWithReachableStates(State state, Set<State> reachableStates, Set<Character> alphabet) throws FlawedLogicException {
        if (reachableStates.contains(state)) {
            return;
        } else {
            reachableStates.add(state);
        }

        for (Character letter : alphabet) {
            TreeSet<State> destinations = state.getTransitions().get(letter);

            if (destinations != null) {
                if (destinations.size() != 1) {
                    throw new FlawedLogicException("Passed argument doesn't seem to be a DFA");
                }

                fillWithReachableStates(destinations.first(), reachableStates, alphabet);
            }
        }
    }

    private static Set<Set<State>> getEquivalenceClasses(Set<State> reachableStates, Set<Character> alphabet) throws FlawedLogicException {
        Set<Set<State>> ndGroups = getNondistinguishableGroups(reachableStates, alphabet);
        Set<Set<State>> equivalenceClasses = new LinkedHashSet<>(ndGroups);

        for (State state : reachableStates) {
            if (!isInGroup(state, ndGroups)) {
                Set<State> wrapper = new TreeSet<>();
                wrapper.add(state);
                equivalenceClasses.add(wrapper);
            }
        }

        return equivalenceClasses;
    }

    private static Set<Set<State>> getNondistinguishableGroups(Set<State> reachableStates, Set<Character> alphabet) throws FlawedLogicException {
        Set<List<State>> ndPairs = getNondistinguishablePairs(new TreeSet<>(reachableStates), alphabet);

        return groupPairs(ndPairs);
    }

    private static Set<List<State>> getNondistinguishablePairs(Set<State> reachableStates, Set<Character> alphabet) throws FlawedLogicException {
        Set<List<State>> distinguishablePairs = new HashSet<>();
        Set<List<State>> otherPairs = new LinkedHashSet<>();

        for (Iterator<State> iterator = reachableStates.iterator(); iterator.hasNext(); ) {
            State state1 = iterator.next();

            for (State state2 : reachableStates) {
                if (state1 == state2) {
                    continue;
                }

                List<State> pair = new ArrayList<>();
                pair.add(state1);
                pair.add(state2);

                if (state1.isExit() == !state2.isExit()) {
                    distinguishablePairs.add(pair);
                } else {
                    otherPairs.add(pair);
                }
            }

            iterator.remove();
        }

        siftDistinguishablePairs(otherPairs, distinguishablePairs, alphabet);

        return otherPairs; // after sifting, only nondistinguishable pairs are left in the other pairs set
    }

    // sift - просеивать
    private static void siftDistinguishablePairs(Set<List<State>> otherPairs, Set<List<State>> distinguishablePairs, Set<Character> alphabet) throws FlawedLogicException {
        Set<List<State>> newDistinguishablePairs = new HashSet<>();

        for (Iterator<List<State>> iterator = otherPairs.iterator(); iterator.hasNext(); ) {
            List<State> pair = iterator.next();

            for (Character letter : alphabet) {
                TreeSet<State> destinations1 = pair.get(0).getTransitions().get(letter);
                TreeSet<State> destinations2 = pair.get(1).getTransitions().get(letter);

                if ((destinations1 != null && destinations1.size() != 1) || (destinations2 != null && destinations2.size() != 1)) {
                    throw new FlawedLogicException("Passed argument doesn't seem to be a DFA");
                }

                List<State> pairToCheck = new ArrayList<>();
                pairToCheck.add(destinations1 == null ? null : destinations1.first());
                pairToCheck.add(destinations2 == null ? null : destinations2.first());

                if (isDistinguishablePair(pairToCheck, distinguishablePairs)) {
                    newDistinguishablePairs.add(pair);
                    iterator.remove();
                    break;
                }
            }
        }

        if (newDistinguishablePairs.size() > 0) {
            siftDistinguishablePairs(otherPairs, newDistinguishablePairs, alphabet);
        }
    }

    private static boolean isDistinguishablePair(List<State> pair, Set<List<State>> distinguishablePairs) {
        if (pair.get(0) == pair.get(1)) {
            return false;
        } else if (pair.get(0) == null || pair.get(1) == null) {
            return true;
        }

        for (List<State> distPair : distinguishablePairs) {
            if (distPair.contains(pair.get(0)) && distPair.contains(pair.get(1))) {
                return true;
            }
        }

        return false;
    }

    private static Set<Set<State>> groupPairs(Set<List<State>> pairs) {
        Set<Set<State>> groups = new HashSet<>();

        for (List<State> pair : pairs) {
            boolean separateGroup = true;

            for (Set<State> group : groups) {
                if (group.contains(pair.get(0)) || group.contains(pair.get(1))) {
                    group.addAll(pair);
                    separateGroup = false;
                    break;
                }
            }

            if (separateGroup) {
                groups.add(new TreeSet<>(pair));
            }
        }

        return groups;
    }

    private static boolean isInGroup(State state, Set<Set<State>> groups) {
        for (Set<State> group : groups) {
            if (group.contains(state)) {
                return true;
            }
        }

        return false;
    }

    private static Map<State, State> getBlankOldToNewMap(Set<Set<State>> equivalenceClasses) {
        Map<State, State> oldToNewMap = new HashMap<>();

        for (Set<State> eqClass : equivalenceClasses) {
            for (State state : eqClass) {
                oldToNewMap.put(state, null);
            }
        }

        return oldToNewMap;
    }

    private static State findEntry(Set<Set<State>> equivalenceClasses) throws FlawedLogicException {
        for (Set<State> eqClass : equivalenceClasses) {
            for (State state : eqClass) {
                if (state.isEntry()) {
                    return state;
                }
            }
        }

        throw new FlawedLogicException("Couldn't find the entry state in any of the equivalence classes");
    }

    private static State mergeStates(State oldState, Set<Set<State>> equivalenceClasses, Map<State, State> oldToNewMap, Set<Character> alphabet) throws FlawedLogicException {
        State mergedState = new State(alphabet);
        TreeSet<State> eqClass = getCorrespondingEqClass(oldState, equivalenceClasses);

        for (State state : eqClass) {
            oldToNewMap.put(state, mergedState);

            if (state.isExit()) {
                mergedState.makeExit();
            }
        }

        for (Character letter : alphabet) {
            TreeSet<State> destinations = oldState.getTransitions().get(letter);

            if (destinations != null) {
                State destination = destinations.first();

                if (oldToNewMap.containsKey(destination) && oldToNewMap.get(destination) != null) {
                    destination = oldToNewMap.get(destination);
                } else if (oldToNewMap.containsKey(destination)) {
                    destination = mergeStates(destination, equivalenceClasses, oldToNewMap, alphabet);
                }

                mergedState.addTransition(letter, destination);
            }
        }

        return mergedState;
    }

    private static TreeSet<State> getCorrespondingEqClass(State state, Set<Set<State>> equivalenceClasses) throws FlawedLogicException {
        for (Set<State> eqClass : equivalenceClasses) {
            if (eqClass.contains(state)) {
                return new TreeSet<>(eqClass);
            }
        }

        throw new FlawedLogicException("Couldn't find state #" + state.getId() + " in any of the equivalence classes");
    }

}
