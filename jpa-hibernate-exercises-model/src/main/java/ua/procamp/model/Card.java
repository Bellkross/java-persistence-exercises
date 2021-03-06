package ua.procamp.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@ToString
@Getter
@Setter
@Entity
public class Card {
    @Id
    @GeneratedValue
    Long id;

    String name;

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id")
    private Account holder;
}
