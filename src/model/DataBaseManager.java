/*
 * UNIVERSIDAD ICESI
 * TAREA INTEGRADORA 2 - ESTRUCTURAS DE DATOS
 * RODAS / DIAZ / MARTINEZ
 */

package model;

import collections.*;
import javafx.util.converter.LocalDateStringConverter;
import utilities.Pair;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.util.ArrayList;

public class DataBaseManager {

	//------------------------------------------------------------------------------------

	//Constants of the DataBaseManager class

	public static final int MAX_PEOPLE_NUMBER = Integer.MAX_VALUE;
	
	public static final String PICTURE_URL = "https://thispersondoesnotexist.com/";

	public static final String GENERATION_DATA_PATH = "data/generation_data/";
	
	public static final String PEOPLE_DATA_PATH = "data/people_data/people.dat";
	
	//------------------------------------------------------------------------------------

	//Attributes of the DataBaseManager class

	private int savedPeopleNumber;
	
	private BufferedReader br;
	
	private BufferedWriter bw;
	
	private ArrayList<String> countries;
	
	//------------------------------------------------------------------------------------

	//Relations of the DataBaseManager class

	private BinarySearchTreeInterface<String,Person> namesTree;
	
	private BinarySearchTreeInterface<String,Person> surnamesTree;

	private TrieInterface namesTrie;

	private TrieInterface surnamesTrie;

	private TrieInterface fullNamesTrie;

	private BinarySearchTreeInterface<String,Person> fullNamesTree;

	private HashTableInterface<Integer,Person> idsHashTable;

	private Person currentPerson;
	
	private RandomFieldsGenerator randomFieldsGenerator;
	
	
	
	//------------------------------------------------------------------------------------

	// Constructor method of the DataBaseManager class

	public DataBaseManager() {
		surnamesTree = new AVLTree<>();
		namesTree = new AVLTree<>();
		namesTrie = new Trie();
		surnamesTrie = new Trie();
		fullNamesTrie = new Trie();
		idsHashTable = new HashTable<>();
		fullNamesTree = new RedBlackTree<>();
		countries = new ArrayList<>();
	}

	//------------------------------------------------------------------------------------

	// Get's methods of the DataBaseManager class

	public int getSavedPeopleNumber() {
		return savedPeopleNumber;
	}

	public ArrayList<String> getCountries(){
		return countries;
	}

	//------------------------------------------------------------------------------------

	//Operations of class DataBaseManager
	
	//Creates new Person receives all parameters
	public void create(String name, String ln, Sex s, LocalDate ld, Double height, String nat) {
		
		Person p = new Person(name, ln, randomFieldsGenerator.id(), s, ld, height, nat);
		savePerson(p);
		savedPeopleNumber++;
	}
	
	// *****************************************************
	
	private void savePerson(Person newPerson) {
		namesTree.add(newPerson.getName(), newPerson);
		surnamesTree.add(newPerson.getSurname(), newPerson);
		fullNamesTree.add(newPerson.getName() + " " + newPerson.getSurname(), newPerson);
		idsHashTable.insert(newPerson.getId(), newPerson);
		
		namesTrie.add(newPerson.getName());
		surnamesTrie.add(newPerson.getSurname());
		fullNamesTrie.add(newPerson.getName() + " " + newPerson.getSurname());
	}

	// *****************************************************

	public boolean read() {
		return readPeopleData() && readGenerationData();
	}
	
	// *****************************************************
	
	public boolean readPeopleData() {		
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(PEOPLE_DATA_PATH));	
						
			Object[] rawPeople = (Object[]) ois.readObject();
			
			for (Object object : rawPeople) {
				Person person = (Person) object;
				
				String name = person.getName();
				String surname =person.getSurname();
				String fullName = name + " " + surname;
				int id = person.getId();
				
				namesTree.add(name, person);
				surnamesTree.add(surname, person);
				fullNamesTree.add(fullName, person);
				idsHashTable.insert(id, person);
				
				namesTrie.add(name);
				surnamesTrie.add(surname);
				fullNamesTrie.add(fullName);
			}
			
			ois.close();
			
			return true;
		} catch (Exception e) {
			
			return false;
		}
	}
	
	// *****************************************************
	
	public boolean readGenerationData() {
						
		ArrayList<Pair<Integer,Double>> loadedAges = loadAges();
		
		if(loadedAges == null)
			return false;
		
		ArrayList<String> loadedNames = loadNames();
		
		if(loadedNames == null)
			return false;
		
		ArrayList<String> loadedSurnames = loadSurnames();
		
		if(loadedSurnames == null)
			return false;
		
		ArrayList<Pair<String,Double>> loadedCountries = loadCountries();
		
		if(loadedCountries == null)
			return false;
		
		randomFieldsGenerator = new RandomFieldsGenerator(loadedSurnames, loadedSurnames, loadedCountries, loadedAges, (HashTable<Integer,Person>)idsHashTable);
				
		return true;				
	}

	// *****************************************************
	
	private ArrayList<Pair<Integer, Double>> loadAges() {
		ArrayList<Pair<Integer,Double>> ages;
		try {
			
			ages =  new ArrayList<>();
			
			br = new BufferedReader(new FileReader(new File(GENERATION_DATA_PATH + "ages.csv")));
			
			br.readLine();//Ignores first line
			
			double acumulatedProbability = 0;
			String line = br.readLine();
			String[] splitLine = null;
			
			
			while(line != null && !line.isEmpty()) {
									
				splitLine = line.split(",");
						
				int maxAge = Integer.parseInt(splitLine[1]);
					
				acumulatedProbability += Integer.parseInt(splitLine[2]);
					
				ages.add(new Pair<Integer,Double>(maxAge, Double.valueOf(acumulatedProbability)));
				
							
				line = br.readLine();
				
			}
			
			br.close();
			return ages;
		}
		catch(Exception ex) {
			return null;
		}
	}
	
	// *****************************************************
	
	private ArrayList<String> loadNames() {
		ArrayList<String> loadedNames;
		
		try {
			loadedNames = new ArrayList<>();
			
			br = new BufferedReader(new FileReader(new File(GENERATION_DATA_PATH + "names.csv")));
			
			String line = br.readLine();			
			
			while(line != null && !line.isEmpty()) {
													
				loadedNames.add(line);		
							
				line = br.readLine();			
			}
			
			br.close();
			
			return loadedNames;
		}
		catch(Exception ex) {
			return null;
		}
	}
	
	// *****************************************************
	
	private ArrayList<String> loadSurnames() {
		ArrayList<String> loadedSurnames;
		try {
			
			loadedSurnames = new ArrayList<>();
			
			br = new BufferedReader(new FileReader(new File(GENERATION_DATA_PATH + "surnames.csv")));
			
			String line = br.readLine();		
			
			while(line != null && !line.isEmpty()) {
													
				loadedSurnames.add(line);		
							
				line = br.readLine();			
			}
			
			br.close();
			
			return loadedSurnames;
		}
		catch(Exception ex) {
			return null;
		}
	}
	
	// *****************************************************
	
	private ArrayList<Pair<String, Double>> loadCountries() {
		
		ArrayList<Pair<String,Double>> loadedCountries;
		
		try {
			
			loadedCountries = new ArrayList<>();
			
			br = new BufferedReader(new FileReader(new File(GENERATION_DATA_PATH + "countries_population.csv")));
			
			br.readLine();//Ignores first line
			
			double acumulatedSum = 0;
			String line = br.readLine();
			String[] splitLine = null;
			
			
			while(line != null && !line.isEmpty()) {
									
				splitLine = line.split(",");
					
				String country = splitLine[0];
				
				int population = Integer.parseInt(splitLine[1]);
					
				acumulatedSum += population;
					
				loadedCountries.add(new Pair<String,Double>(country, Double.valueOf(population)));
				countries.add(country);
											
				line = br.readLine();				
								
			}
			
			double accumulatedProbability = 0;
			double probability = 0;;
			
			for (int i = 0 ; i < loadedCountries.size() ; i++) {
				Pair<String,Double> pair = loadedCountries.get(i);
				
				probability = pair.getValue()/acumulatedSum;
				accumulatedProbability += probability;
								
				pair.setValue(Double.valueOf(accumulatedProbability));
			}
			
			
			br.close();
			return loadedCountries;
		}
		catch(Exception ex) {
			return null;
		}
	}
	
	// *****************************************************
	
	public void update(String n, String sn, Sex s, LocalDate bd, Double height, String nat) {
		
		if(n != null || sn != null) {
			String prevName = currentPerson.getName();
			String prevSurname = currentPerson.getSurname();
			String prevFullName = prevName + " " + prevSurname;
			
			fullNamesTree.remove(prevFullName);
			fullNamesTrie.remove(prevFullName);
			
			if(n != null) { 			
				namesTree.remove(prevName);
				namesTrie.remove(prevName);
				
				currentPerson.setName(n);
				
				namesTree.add(n, currentPerson);
				namesTrie.add(n);
			}
			
			if(sn != null) {
				surnamesTree.remove(prevSurname);
				surnamesTrie.remove(prevSurname);
				
				currentPerson.setSurname(sn);
				
				surnamesTree.add(sn, currentPerson);
				surnamesTrie.add(sn);		
			}
			
			String newFullName = currentPerson.getName() + " " + currentPerson.getSurname();
			fullNamesTree.add(newFullName, currentPerson);
			fullNamesTrie.add(newFullName);			
		}
		
		if(s!=null) currentPerson.setSex(s);
		
		if(bd != null) currentPerson.setBirthday(bd);
		
		if(height != null) currentPerson.setHeight(height);
		
		if(nat != null) currentPerson.setNationality(nat);		
	}
	
	// *****************************************************
	
	//Serialize data
	public void write() throws FileNotFoundException, IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(PEOPLE_DATA_PATH));
		
		oos.writeObject(idsHashTable.getAll());
		
		oos.close();
	}

	// *****************************************************
	
	//removes current person
	public boolean delete() {
		if(currentPerson != null) {
			String name = currentPerson.getName();
			String surname = currentPerson.getSurname();
			String fullName = name + " " + surname;
			int id = currentPerson.getId();
			
			namesTree.remove(name); // Need to find a way of solving the repeated name problem
			surnamesTree.remove(surname); // Need to find a way of solving the repeated name problem
			fullNamesTree.remove(fullName); // Need to find a way of solving the repeated name problem
			idsHashTable.delete(id);
			
			namesTrie.remove(name);
			surnamesTrie.remove(surname);
			fullNamesTrie.remove(fullName);
			
			clearCurrentPerson();
			
			return true;
		}
		else {
			return false;
		}
	}

	// *****************************************************

	public Person search(SearchCriteria criteria, String data) {
		Person person = null;

		switch(criteria) {

		case NAME:
			person = namesTree.search(data);
			break;		

		case SURNAME:
			person = surnamesTree.search(data);
			break;

		case FULL_NAME:
			person = fullNamesTree.search(data);
			break;

		case ID:
			person = idsHashTable.search(Integer.parseInt(data));
			break;
		}
		
		currentPerson = person;
		
		return person;
	}

	// *****************************************************

	public ArrayList<String> getPredictions(String prefix, SearchCriteria criteria) {
		switch(criteria) {
		case NAME:
			return namesTrie.getPredictions(prefix);
			
		case SURNAME:
			return surnamesTrie.getPredictions(prefix);
			
		case FULL_NAME:
			return fullNamesTrie.getPredictions(prefix);
			
		default:
			return null;
		}		
	}

	// *****************************************************

	public void clearCurrentPerson() {
		currentPerson = null;
	}

	// *****************************************************

	public boolean generatePeople(int n) {
		
		if(MAX_PEOPLE_NUMBER - savedPeopleNumber - n >= 0) {
			for (int i = 0; i < n; i++) {
				Person newPerson = generatePerson();
				
				String name = newPerson.getName();
				String surname = newPerson.getSurname();
				int id = newPerson.getId();
				
				namesTrie.add(name);
				namesTree.add(name, newPerson);
				
				surnamesTrie.add(surname);
				surnamesTree.add(surname, newPerson);
				
				String fullName = name + " " + surname;
				
				fullNamesTrie.add(fullName);
				fullNamesTree.add(fullName,newPerson);
								
				idsHashTable.insert(id,newPerson);				
			}
			savedPeopleNumber += n;
			return true;		
		} 
		else {
			return false;
		}		
	}

	// *****************************************************

	private Person generatePerson() {
		String name = randomFieldsGenerator.name();
		String surname = randomFieldsGenerator.surname();
		int id = randomFieldsGenerator.id();
		Sex sex = randomFieldsGenerator.sex();
		LocalDate birthday = randomFieldsGenerator.birthday();
		int height = randomFieldsGenerator.height();
		String nationality = randomFieldsGenerator.nationality();	
		
		return new Person(name, surname, id, sex, birthday, height, nationality);
	}
	
	//------------------------------------------------------------------------------------

}
