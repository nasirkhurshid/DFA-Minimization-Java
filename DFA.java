import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class DFA {
	private static int noOfSymbols;
	private static String nameOfSymbols[];
	private static ArrayList<State> DFAStates;
	private static ArrayList<State> minDFAStates;
	
	private static void readFile(String filename) {
		try {
	      File myObj = new File(filename);
	      Scanner myReader = new Scanner(myObj);
	      String inputSymbols = myReader.nextLine();
	      String[] arr = inputSymbols.split(", ");
	      noOfSymbols = arr.length - 1;
	      nameOfSymbols = new String[noOfSymbols];
	      for (int i = 1, j = 0; i < arr.length; i++, j++){
	    	  nameOfSymbols[j] = arr[i];
	      }
	      DFAStates = new ArrayList<State>();
	      while (myReader.hasNextLine()) {
	    	  String name;
	    	  String states[] = new String [noOfSymbols];
	    	  boolean initialState = false;
	    	  boolean finalState = false;
	    	  String data = myReader.nextLine();
	          arr = data.split(", ");
	          if(arr[0].charAt(0) == '*') {
	        	  finalState = true;
	        	  if(arr[0].charAt(1) == 'i') {
	        		  initialState = true;
	        		  name = String.valueOf(arr[0].charAt(2));
	        	  }
	        	  else {
	        		  name = String.valueOf(arr[0].charAt(1));
	        	  }
	          }
	          else if(arr[0].charAt(0) == 'i') {
	        	  initialState = true;
	        	  if(arr[0].charAt(1) == '*') {
	        		  finalState = true;
	        		  name = String.valueOf(arr[0].charAt(2));
	        	  }
	        	  else {
	        		  name = String.valueOf(arr[0].charAt(1));
	        	  }
	          }
	          else {
        		  name = String.valueOf(arr[0].charAt(0));
	          }
	          for(int i = 1, j = 0; i<=noOfSymbols;i++, j++) {
	        	  states[j] = arr[i];
	          }
	          DFAStates.add(new State(name, states, initialState, finalState));
	        }
	      myReader.close();
	    } catch (FileNotFoundException e) {
	      System.out.println("An error occurred.");
	      e.printStackTrace();
	    }
	}
	
	public static void removeUnreachableStates() {
		for(int i = 0; i < DFAStates.size(); i++) {
			if(DFAStates.get(i).isInitial()) {
				continue;
			}
			boolean unreachable = true;
			String name = DFAStates.get(i).getName();
			for(int j = 0; j < DFAStates.size(); j++) {
				String[] inputSymbols = DFAStates.get(j).getStates();
				for(String is:inputSymbols) {
					if(name.equals(is)) {
						unreachable = false;
						break;
					}
				}
				if(!unreachable) {
					break;
				}
			}
			if(unreachable) {
				DFAStates.remove(i);
			}
		}
	}
	
	public static boolean moved(State s, ArrayList<ArrayList<State>> curr) {
		for(ArrayList<State> l:curr) {
			for(State i:l) {
				if(i.getName().equals(s.getName())) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean isSame(State rhs, State lhs, ArrayList<ArrayList<State>> prev) {
		String rhsStates[] = rhs.getStates();
		String lhsStates[] = lhs.getStates();
		boolean found[] = new boolean[noOfSymbols];
		for(int i = 0; i<noOfSymbols;i++) {
			found[i] = false;
		}
		for(int i = 0; i<noOfSymbols;i++) {
			for(ArrayList<State> l:prev) {
				for(State s:l) {
					if(s.getName().equals(rhsStates[i])) {
						for(State ss:l) {
							if(ss.getName().equals(lhsStates[i])) {
								found[i] = true;
							}
						}
					}
				}
			}
		}
		for(int i = 0; i<noOfSymbols;i++) {
			if(!found[i]) {
				return false;
			}
		}
		return true;
	}
	
	public static ArrayList<ArrayList<State>> partitioningMethod() {
		ArrayList<ArrayList<State>> prev = new ArrayList<ArrayList<State>>();
		ArrayList<ArrayList<State>> curr = new ArrayList<ArrayList<State>>();
		ArrayList<State> nonAccepting = new ArrayList<State>();
		ArrayList<State> Accepting = new ArrayList<State>();
		for(int i = 0; i < DFAStates.size(); i++) {
			if(DFAStates.get(i).isFinal()) {
				Accepting.add(DFAStates.get(i));
			}
			else {
				nonAccepting.add(DFAStates.get(i));
			}
		}
		curr.add(nonAccepting);
		curr.add(Accepting);
		while(prev.size()!=curr.size()) {
			prev.clear();
			prev.addAll(curr);
			curr.clear();
			for(ArrayList<State> l: prev) {
				for(int i = 0; i<l.size();i++) {
					ArrayList<State> set = new ArrayList<State>();
					if(!moved(l.get(i), curr)) {
						State s = l.get(i);
						set.add(s);
						for(int j = 0;j<l.size();j++) {
							if(!moved(l.get(j), curr) && !set.contains(l.get(j))) {
								if(isSame(s, l.get(j), prev))
									set.add(l.get(j));
							}
						}
						curr.add(set);
					}
				}
			}
		}
		return curr;
	}
	
	public static void minimizeDFA(ArrayList<ArrayList<State>> partitions) {
		minDFAStates = new ArrayList<State>();
		for(ArrayList<State> l: partitions) {
	    	State s = l.get(0);
	    	String name = s.getName();
	    	String states[] = s.getStates();
	    	boolean initialState = s.isInitial();
	    	boolean finalState = s.isFinal();
	    	for(int i = 1; i<l.size();i++) {
	    		name += l.get(i).getName();
	    		if(!initialState) {
	    			initialState = l.get(i).isInitial();
	    		}
	    		if(!finalState) {
	    			finalState = l.get(i).isFinal();
	    		}
	    	}
	    	minDFAStates.add(new State(name, states, initialState, finalState));
		}
		for(int i = 0; i<minDFAStates.size();i++) {
			State s = minDFAStates.get(i);
			String states[] = s.getStates();
			for(int j = 0; j<noOfSymbols;j++) {
				String state = states[j];
				for(State toMatch: minDFAStates) {
					if(toMatch.getName().contains(state)) {
						states[j] = toMatch.getName();
					}
				}
				minDFAStates.get(i).setStates(states);
			}
		}
	}
	
	public static void writeFile(String filename) {
		try {
			FileWriter myWriter = new FileWriter(filename);
			myWriter.write("States/Input Symbols, ");
			for(int i = 0; i< noOfSymbols; i++) {
				myWriter.write(nameOfSymbols[i]);
				if(i!=noOfSymbols-1) {
					myWriter.write(", ");
				}
				else {
					myWriter.write("\n");
				}
			}
			for(State s: minDFAStates) {
				if(s.isInitial()) {
					myWriter.write("i");
				}
				if(s.isFinal()) {
					myWriter.write("*");
				}
				myWriter.write(s.getName()+", ");
				String[] st = s.getStates();
				for(int i = 0; i< noOfSymbols; i++) {
					myWriter.write(st[i]);
					if(i!=noOfSymbols-1) {
						myWriter.write(", ");
					}
					else {
						myWriter.write("\n");
					}
				}
			}
			myWriter.close();
	    } catch (IOException e) {
	    	System.out.println("An error occurred.");
	    	e.printStackTrace();
	    }
	}
	
	public static void main(String[] args) {
		readFile("New Text Document.txt");
		removeUnreachableStates();
		ArrayList<ArrayList<State>> partitions = partitioningMethod();
		minimizeDFA(partitions);
		writeFile("output.txt");
	}
}
