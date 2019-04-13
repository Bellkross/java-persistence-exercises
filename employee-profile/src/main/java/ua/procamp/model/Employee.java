package ua.procamp.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

/**
 * todo:
 * - map unidirectional relation between {@link Employee} and {@link EmployeeProfile} on the child side
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "employee")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private String fistName;
    @Column(nullable = false)
    private String lastName;
}


