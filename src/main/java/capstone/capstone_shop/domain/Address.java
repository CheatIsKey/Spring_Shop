package capstone.capstone_shop.domain;

import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class Address {

    private String state;  // 시/도
    private String city;   // 도시
    private String street;

    protected Address() {
    }

    public Address(String state, String city, String street) {
        this.state = state;
        this.city = city;
        this.street = street;
    }
}
