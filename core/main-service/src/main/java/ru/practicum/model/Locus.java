package ru.practicum.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.dto.locus.LocusType;

import java.util.Objects;

@Entity
@Table(name = "LOCI")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Locus {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "LOC_ID_SEQ")
    @SequenceGenerator(name = "LOC_ID_SEQ", sequenceName = "LOC_ID_SEQ", allocationSize = 1)
    private Long id;
    private String name;
    @Column(name = "locus_type")
    @Enumerated(EnumType.STRING)
    private LocusType locusType;
    private Float lat;
    private Float lon;
    /**
     * radius in kilometers
     */
    private Float rad;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Locus locus = (Locus) o;
        return Objects.equals(id, locus.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Locus{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", locusType=" + locusType +
                ", lat=" + lat +
                ", lon=" + lon +
                ", rad=" + rad +
                '}';
    }
}
