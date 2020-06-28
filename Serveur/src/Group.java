import java.io.Serializable;
import java.util.ArrayList;

public class Group implements Serializable{
	
	private String name;
	private ArrayList<String> participants;
	
	public Group(String name, ArrayList<String> participants) {
		this.name = name;
		this.participants = participants;
	}
	public String getName() {return name;}
	
	public void setName(String name) {this.name = name;}
	
	public ArrayList<String> getParticipants() {return participants;}
	
	public void setParticipants(ArrayList<String> participants) {this.participants = participants;}
	
	@Override
	public String toString() {return "nom : "+name+", participants : \n"+participants.toString();}
	
}
