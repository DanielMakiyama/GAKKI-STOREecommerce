package com.gakki.store.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Categoria de instrumento. Um instrumento pode ter mais de uma (RN0012).
// Tabela de domínio, populada por migration (RNF0013).

@Entity
@Table(name = "categoria")
@Getter
@Setter
@NoArgsConstructor
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80, unique = true)
    private String nome;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Categoria outra)) {
            return false;
        }
        return id != null && id.equals(outra.getId());
    }

    @Override
    public int hashCode() {
        return Categoria.class.hashCode();
    }
}
