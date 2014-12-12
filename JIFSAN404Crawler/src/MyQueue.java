import java.net.URL;

/**
 * @author Bob Zimmerman
 *
 * @param <T>
 */
public class MyQueue<T> {
	int size=0;
	Node<T> tail=null;
	Node<T> head=null;

	//Node class containing the data and a reference to the next node
	@SuppressWarnings("hiding")
	class Node<T>{
		public T data;
		public Node<T> next;

		public Node(T t){
			data = t;
		}

		public Node(T t, Node<T> child){
			data = t;
			next = child;
		}
	}

	//Returns the size of the queue
	public synchronized int size(){
		return size;
	}

	//Empties the queue
	public synchronized void clear(){
		head=null;
		tail=null;
	}

	//Adds a node containing data T to the end of the queue
	public synchronized void enqueue(T t){
		//ignore things from outside jifsan
		if(!((URL) t).toString().contains("jifsan.")){
			notifyAll();
			return;
		}
		
		Node<T> newNode = new Node<T>(t);
		if(head==null){
			head=newNode;
			tail=newNode;
			head.next=tail;
			size++;
			notifyAll();
			return;
		}
		if(head==tail){
			tail=newNode;
			head.next=tail;
			size++;
			notifyAll();
			return;
		}
		tail.next=newNode;
		tail=tail.next;
		size++;
		notifyAll();
	}

	//Returns a node that is removed from the front of the queue
	public synchronized T dequeue() throws InterruptedException{
		while(size==0){
			wait();
		}
		//TODO debug
		//System.out.println(head.data.toString());
		if(head==tail){
			T temp = head.data;
			head=null;
			tail=null;
			size--;
			return temp;
		}
		T tempT = head.data;
		head=head.next;
		size--;
		return tempT;
	}

}
