package mock.model;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.apache.commons.lang3.builder.EqualsBuilder;

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
	
	@Override
	public boolean equals(Object other) {
		return EqualsBuilder.reflectionEquals(this, other, true);
	}
}
