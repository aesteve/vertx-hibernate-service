package mock.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Dog {
	@Id
	private String name;
	private String breed;
	
	public Dog() {
		
	}
	
	public Dog(String name, String breed) {
		this.name = name;
		this.breed = breed;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getBreed() {
		return breed;
	}
	public void setBreed(String breed) {
		this.breed = breed;
	}
}
