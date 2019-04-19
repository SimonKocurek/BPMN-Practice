package receipt.entity;

import java.io.Serializable;
import java.util.Objects;

public class Drug implements Serializable {

	private static final long serialVersionUID = -1551947386706571685L;

	private String name;
	
	private int price;

	public Drug(String name, int price) {
		super();
		this.name = name;
		this.price = price;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Drug drug = (Drug) o;
		return price == drug.price &&
				Objects.equals(name, drug.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, price);
	}

	@Override
	public String toString() {
		return name + " ... " + price + "€";
	}
	
}
