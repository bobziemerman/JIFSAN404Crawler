import java.util.*;

/**
 * 
 * @author Bob Zimmerman
 *
 * @param <T>
 */
public class MySet<T> {
	HashSet<T> set = new HashSet<T>();
	int size;

	//Returns the size of the set
	public synchronized int size(){
		return size;
	}

	//Empties the set
	public synchronized void clear(){
		set = new HashSet<T>();
	}

	//Removes the item T from the set
	public synchronized boolean remove(T t){
		if(set.contains(t)){
			set.remove(t);
			size--;
			return true;
		}
		return false;
	}

	//Adds the item T to the set
	public synchronized boolean add(T t){
		if(set.contains(t)){
			return false;
		}
		set.add(t);
		size++;
		return true;
	}

	//Returns true if the set contains T, false if not
	public synchronized boolean contains(T t){
		if(set.contains(t)){
			return true;
		}
		return false;
	}

}
