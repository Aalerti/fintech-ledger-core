package bank.app.springbootbankapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "banks")
public class Bank {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bank_seq_gen")
    @SequenceGenerator(name = "bank_seq_gen", sequenceName = "banks_seq", allocationSize = 50)
    private long id;

    @Column(nullable = false)
    private String name;
}
